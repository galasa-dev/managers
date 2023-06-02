/*
 * Copyright contributors to the Galasa project
 */
package dev.galasa.zos3270.common.screens.images;


import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.fail;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.*;

import org.junit.rules.TestName;
import org.junit.Rule;
import org.junit.Test;

import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.zos3270.common.screens.FieldContents;
import dev.galasa.zos3270.common.screens.TerminalField;
import dev.galasa.zos3270.common.screens.TerminalImage;
import dev.galasa.zos3270.common.screens.TerminalSize;

public class TerminalImageTransformTest {

    @Rule
    public TestName testName = new TestName();

    public static class MockConfidentialTextService implements IConfidentialTextService {

        private List<Substitution> substitutions = new ArrayList<Substitution>();

        private class Substitution {
            String confidentialString;
            String comment ;

            Substitution(String confidentialString, String comment) {
                this.confidentialString = confidentialString ;
                this.comment = comment;
            }
        }

        @Override
        public void registerText(String confidentialString, String comment) {
            substitutions.add( new Substitution(confidentialString, comment));
        }

        @Override
        public String removeConfidentialText(String text) {
            String result = text ;
            for( Substitution substitution : substitutions) {
                result = result.replaceAll( substitution.confidentialString , substitution.comment);
            }
            return result;
        }

        @Override
        public void shutdown() {
            throw new UnsupportedOperationException("Unimplemented method 'shutdown'");
        }
    }

    // I'm guessing what the colors are, as we've not implemented colours yet.
    Character GREEN = new Character('g');
    Character BLACK = new Character('k');
    Character WHITE = new Character('w');

    @Test
    public void testEmptyTerminalImageRendersOk() throws Exception {

        MockConfidentialTextService confidentialTextService = new MockConfidentialTextService();

        int columns = 80 ;
        int rows = 26 ;
        TerminalSize size = new TerminalSize(columns,rows);
        
        TerminalImageTransform renderer = new TerminalImageTransform( size, confidentialTextService );

        int sequence = 1 ;
        boolean isInbound = false ;
        TerminalSize imageSize = new TerminalSize( columns,rows);
        TerminalImage image = new TerminalImage(sequence, testName.getMethodName(), isInbound, 
            null, null, imageSize, 0, 0);

        checkTerminalImageAgainstExpected(renderer, image);
    }



    private void checkTerminalImageAgainstExpected(TerminalImageTransform transform, TerminalImage image) throws Exception {

        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        transform.writeImage(image, "png", buff);

        byte[] contentsRendered = buff.toByteArray();

        assertThat(contentsRendered).isNotEmpty();

        String testFileToCompareAgainst = testName.getMethodName()+".png";

        assertFileContentsSame( contentsRendered , testFileToCompareAgainst);
    }

    private void assertFileContentsSame(byte[] contentsRendered, String testFileToCompareAgainst) throws IOException {
        // System.out.println("Test file name to compare against is: "+testFileToCompareAgainst);
        // System.out.println("Comparing rendered image with contents of file "+getClass().getResource(testFileToCompareAgainst));
        // System.out.println("Class loader root is here: "+this.getClass().getResource("."));
        try ( InputStream testImageToCompare = this.getClass().getResourceAsStream("image-data/"+testFileToCompareAgainst)
        ) {

            if (testImageToCompare == null) {
                String tempFilePath = writeTempFile(testFileToCompareAgainst,contentsRendered);
                fail("Testcase logic failure. Tried to open file "+testFileToCompareAgainst+" but it was missing."+
                    "... was hoping to compare it to image\n"+tempFilePath);
            }

            byte[] goodFileContents = testImageToCompare.readAllBytes();

            boolean isSame = true ;

            if (contentsRendered.length != goodFileContents.length ) {
                String tempFilePath = writeTempFile(testFileToCompareAgainst,contentsRendered);
                fail("rendered image is different size to expected."+
                " ... image from the renderer is here:\n"+tempFilePath);
            }
            
            for( int i=0; isSame && i<contentsRendered.length; i++) {
                byte got = contentsRendered[i];
                byte expected = goodFileContents[i];
                if (got != expected) {
                    isSame = false;
                }
            }

            if (!isSame) {
                String tempFilePath = writeTempFile(testFileToCompareAgainst,contentsRendered);
                fail("The rendered image is not the same as the one stored at "+"image-data/"+testFileToCompareAgainst+
                " ... got this image from the renderer:\n"+tempFilePath);
            }
        }
    }

    private String writeTempFile(String name, byte[] bytes) throws IOException {
        java.nio.file.Path tempDir = Files.createTempDirectory("TerminalImageRendererTest");
        java.nio.file.Path filePath = tempDir.resolve(name);

        try ( OutputStream outStream = Files.newOutputStream(filePath, StandardOpenOption.CREATE); 
        ) {
            outStream.write(bytes);
        }
        System.out.println("Image file "+filePath.toAbsolutePath()+" created.");

        return filePath.toAbsolutePath().toString();
    }

    private Character[] stringToCharacterArray(String text ) {
        Character[] result = new Character[text.length()];

        char[] charArray = text.toCharArray();
        for( int i=0; i<charArray.length; i++ ) {
            result[i] = new Character( charArray[i]);
        }
        return result;
    }

    @Test
    public void testTextFieldRendersOk() throws Exception {
        MockConfidentialTextService confidentialTextService = new MockConfidentialTextService();

        int columns = 80 ;
        int rows = 26 ;
        TerminalSize size = new TerminalSize(columns,rows);
        
        TerminalImageTransform renderer = new TerminalImageTransform( size, confidentialTextService );

        int sequence = 1 ;
        boolean isInbound = false ;
        TerminalSize imageSize = new TerminalSize( columns,rows);
        TerminalImage image = new TerminalImage(sequence, testName.getMethodName(), isInbound, 
            null, null, imageSize, 0, 0);
        
        String text = "myTextField";

        int row = 2;
        int column = 3; 
        boolean unformatted = false ;
        boolean fieldProtected = false ;
        boolean fieldNumeric = false;
        boolean fieldDisplay = true;
        boolean fieldIntenseDisplay = false;
        boolean fieldSelectorPen = false;
        boolean fieldModifed = false;
        Character foregroundColour = GREEN ;
        Character backgroundColour = BLACK; 
        Character highlight = WHITE;

        TerminalField field1 = new TerminalField(row,column,unformatted,fieldProtected,fieldNumeric,
            fieldDisplay,fieldIntenseDisplay,fieldSelectorPen,fieldModifed,foregroundColour,backgroundColour,highlight);
        field1.getContents().add( new FieldContents( stringToCharacterArray(text)) );

        image.getFields().add(field1);

        checkTerminalImageAgainstExpected(renderer, image);
    }



    @Test
    public void testConfientialTextIsRedacted() throws Exception {
        MockConfidentialTextService confidentialTextService = new MockConfidentialTextService();
        confidentialTextService.registerText("007", "***");

        int columns = 80 ;
        int rows = 26 ;
        TerminalSize size = new TerminalSize(columns,rows);
        
        TerminalImageTransform renderer = new TerminalImageTransform( size, confidentialTextService );

        int sequence = 1 ;
        boolean isInbound = false ;
        TerminalSize imageSize = new TerminalSize( columns,rows);
        TerminalImage image = new TerminalImage(sequence, testName.getMethodName(), isInbound, 
            null, null, imageSize, 0, 0);
        
        String text = "my password is:'007' which should appear as '***'";

        int row = 2;
        int column = 3; 
        boolean unformatted = false ;
        boolean fieldProtected = false ;
        boolean fieldNumeric = false;
        boolean fieldDisplay = true;
        boolean fieldIntenseDisplay = false;
        boolean fieldSelectorPen = false;
        boolean fieldModifed = false;
        Character foregroundColour = GREEN ;
        Character backgroundColour = BLACK; 
        Character highlight = WHITE;

        TerminalField field1 = new TerminalField(row,column,unformatted,fieldProtected,fieldNumeric,
            fieldDisplay,fieldIntenseDisplay,fieldSelectorPen,fieldModifed,foregroundColour,backgroundColour,highlight);
        field1.getContents().add( new FieldContents( stringToCharacterArray(text)) );

        image.getFields().add(field1);

        checkTerminalImageAgainstExpected(renderer, image);
    }


    @Test
    public void testInboundTrueRendersInboundInStatusArea() throws Exception {
        MockConfidentialTextService confidentialTextService = new MockConfidentialTextService();

        int columns = 80 ;
        int rows = 26 ;
        TerminalSize size = new TerminalSize(columns,rows);
        
        TerminalImageTransform renderer = new TerminalImageTransform( size, confidentialTextService );

        int sequence = 1 ;
        boolean isInbound = true ;
        TerminalSize imageSize = new TerminalSize( columns,rows);
        TerminalImage image = new TerminalImage(sequence, testName.getMethodName(), isInbound, 
            null, null, imageSize, 0, 0);

        checkTerminalImageAgainstExpected(renderer, image);
    }

    @Test
    public void testOutboundRendersAidInStatusArea() throws Exception {
        MockConfidentialTextService confidentialTextService = new MockConfidentialTextService();

        int columns = 80 ;
        int rows = 26 ;
        TerminalSize size = new TerminalSize(columns,rows);
        
        TerminalImageTransform renderer = new TerminalImageTransform( size, confidentialTextService );

        int sequence = 1 ;
        boolean isInbound = false ;
        String aid = "myAid";
        TerminalSize imageSize = new TerminalSize( columns,rows);
        TerminalImage image = new TerminalImage(sequence, testName.getMethodName(), isInbound, 
            null,  aid, imageSize, 0, 0);

        checkTerminalImageAgainstExpected(renderer, image);
    }

    @Test
    public void testDifferentColumnsAndRowsRendersInStatusArea() throws Exception {
        MockConfidentialTextService confidentialTextService = new MockConfidentialTextService();

        int columns = 66 ;
        int rows = 15 ;
        TerminalSize size = new TerminalSize(columns,rows);
        
        TerminalImageTransform renderer = new TerminalImageTransform( size, confidentialTextService );

        int sequence = 1 ;
        boolean isInbound = false ;
        String aid = "myAid";
        TerminalSize imageSize = new TerminalSize( columns,rows);
        TerminalImage image = new TerminalImage(sequence, testName.getMethodName(), isInbound, 
            null,  aid, imageSize, 0, 0);

        checkTerminalImageAgainstExpected(renderer, image);
    }

}
