/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
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



    //----------------------------------------------------
    // Utility functions
    //----------------------------------------------------
    private void checkTerminalImageAgainstExpected(TerminalImageTransform transform, TerminalImage image) throws Exception {

        ByteArrayOutputStream buff = new ByteArrayOutputStream();
        transform.writeImage(image, "png", buff);

        byte[] contentsRendered = buff.toByteArray();

        assertThat(contentsRendered).isNotEmpty();

        String testFileToCompareAgainst = testName.getMethodName()+".png";

        String imageFolderName = getImageFolderName();
        if ( imageFolderName == null ) {
            System.out.println("The operating system you are running on doesn't have the same fonts installed"+
                " as when the test data images were recorded and checked for correctness."+
                " As a result, this test cannot check the expected vs actual images rendered."
            );
        } else {
            assertFileContentsSame( contentsRendered , testFileToCompareAgainst , imageFolderName );
        }
    }

    private String getImageFolderName() { 
        String folderName = null;

        String osName = System.getProperty("os.name");

        // We have mac images captured and stored waiting to be compared against.
        if (osName == "Mac OS X") {
            folderName = "mac";
        }

        // Other operating systems have different fonts, so the rendered images look different.

        return folderName ;
    }

    private void assertFileContentsSame(
        byte[] contentsRendered, 
        String testFileToCompareAgainst,
        String imageFolderName
    ) throws IOException {

        boolean isCheckedAgainstTestFile = false ;
        try ( InputStream testImageToCompare = this.getClass().getClassLoader().getResourceAsStream(imageFolderName+"/"+testFileToCompareAgainst)
        ) {
            if (testImageToCompare == null) {
                String tempFilePath = writeTempFile(testFileToCompareAgainst,contentsRendered);
                System.out.println("Testcase logic info: Tried to open file "+testFileToCompareAgainst+
                    " using this.getClass().getClassLoader().getResourceAsStream(...) but it was missing. "+
                    "... was hoping to compare it to image\n"+tempFilePath);
            } else {
                System.out.println("Testcase logic info: Opened file "+testFileToCompareAgainst+
                    " using this.getClass().getClassLoader().getResourceAsStream(...)");
                isCheckedAgainstTestFile = true ;
                assertInputStreamContentsSame(testImageToCompare, contentsRendered, testFileToCompareAgainst);
            }
        }


        if (!isCheckedAgainstTestFile) {
            fail("Testcase logic failure. Could not open test data file "+testFileToCompareAgainst+" so we could use it to compare with what was generated");
        }
    }

    private void assertInputStreamContentsSame( InputStream testImageToCompare , byte[] contentsRendered, String testFileToCompareAgainst) throws IOException {
        
        byte[] goodFileContents = testImageToCompare.readAllBytes();

        System.out.println("rendered image is "+Integer.toString(contentsRendered.length)+" bytes long");
        System.out.println("expected image is "+Integer.toString(goodFileContents.length)+" bytes long");

        boolean isSame = true ;

        if (contentsRendered.length != goodFileContents.length ) {
            String tempFilePath = writeTempFile(testFileToCompareAgainst,contentsRendered);
            fail("rendered image is different size to expected."+
                " ... image from the renderer is here:\n"+tempFilePath+
                " rendered image is "+Integer.toString(contentsRendered.length)+" bytes long"+
                " whereas expected image is "+Integer.toString(goodFileContents.length)+" bytes long"
            );
        }
        
        for( int i=0; isSame && i<contentsRendered.length; i++) {
            byte got = contentsRendered[i];
            byte expected = goodFileContents[i];
            if (got != expected) {
                System.out.println("Byte "+Integer.toString(i)+" of images being compared don't match.");
                System.out.println("Rendered image byte value:"+Byte.toString(got));
                System.out.println("Expected image byte value:"+Byte.toString(expected));
                isSame = false;
            }
        }

        if (!isSame) {
            String tempFilePath = writeTempFile(testFileToCompareAgainst,contentsRendered);
            fail("The rendered image is not the same as the one stored at "+"image-data/"+testFileToCompareAgainst+
            " ... got this image from the renderer:\n"+tempFilePath);
        }
    }

    static java.nio.file.Path tempDir = null;

    private String writeTempFile(String name, byte[] bytes) throws IOException {
        // We want all image files we write out to appear in the same folder for ease of looking at them all.
        if (tempDir == null) {
            tempDir = Files.createTempDirectory("TerminalImageRendererTest");
        }
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



    //----------------------------------------------------
    // Test functions
    //----------------------------------------------------

    // Simplest path test.
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
        
        String text = "single text field in middle";

        int row = 10;
        int column = 13; 
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
    public void testConfidentialTextIsRedacted() throws Exception {
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

        int row = 10;
        int column = 10; 
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
        
        int sequence = 1 ;
        boolean isInbound = true ;
        TerminalSize imageSize = new TerminalSize( columns,rows);
        TerminalImage image = new TerminalImage(sequence, testName.getMethodName(), isInbound, 
            null, null, imageSize, 0, 0);

        TerminalImageTransform renderer = new TerminalImageTransform( size, confidentialTextService );

        {
            String text = "\"Inbound\" should appear on the status line.";

            int row = 10;
            int column = 10; 
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

            TerminalField field = new TerminalField(row,column,unformatted,fieldProtected,fieldNumeric,
                fieldDisplay,fieldIntenseDisplay,fieldSelectorPen,fieldModifed,foregroundColour,backgroundColour,highlight);
            field.getContents().add( new FieldContents( stringToCharacterArray(text)) );

            image.getFields().add(field);
        }

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

        {
            String text = "'Outbound' and 'myAid' should appear on the status line.";

            int row = 10;
            int column = 10; 
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

            TerminalField field = new TerminalField(row,column,unformatted,fieldProtected,fieldNumeric,
                fieldDisplay,fieldIntenseDisplay,fieldSelectorPen,fieldModifed,foregroundColour,backgroundColour,highlight);
            field.getContents().add( new FieldContents( stringToCharacterArray(text)) );

            image.getFields().add(field);
        }

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


        {
            String text = "small image '66x15' on status line.";

            int row = 6;
            int column = 0; 
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

            TerminalField field = new TerminalField(row,column,unformatted,fieldProtected,fieldNumeric,
                fieldDisplay,fieldIntenseDisplay,fieldSelectorPen,fieldModifed,foregroundColour,backgroundColour,highlight);
            field.getContents().add( new FieldContents( stringToCharacterArray(text)) );

            image.getFields().add(field);
        }
        checkTerminalImageAgainstExpected(renderer, image);
    }


    @Test
    public void testTextAtOriginFieldRendersOk() throws Exception {
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
        
        String text = "^ this is the origin. Should be in top left.";

        int row = 0;
        int column = 0 ; 
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
    public void testTextAtBottomRightFieldRendersOk() throws Exception {
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
        
        {
            String text = "The 'v' should be in bottom right";

            int row = 16;
            int column = 0; 
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

            TerminalField field = new TerminalField(row,column,unformatted,fieldProtected,fieldNumeric,
                fieldDisplay,fieldIntenseDisplay,fieldSelectorPen,fieldModifed,foregroundColour,backgroundColour,highlight);
            field.getContents().add( new FieldContents( stringToCharacterArray(text)) );

            image.getFields().add(field);
        }

        {
            String text = "v";

            int row = 25;
            int column = 79; 
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

            TerminalField field = new TerminalField(row,column,unformatted,fieldProtected,fieldNumeric,
                fieldDisplay,fieldIntenseDisplay,fieldSelectorPen,fieldModifed,foregroundColour,backgroundColour,highlight);
            field.getContents().add( new FieldContents( stringToCharacterArray(text)) );

            image.getFields().add(field);
        }

        checkTerminalImageAgainstExpected(renderer, image);
    }


    @Test
    public void testTextFullRowFieldRendersOk() throws Exception {
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
        
        {
            String text = "The line should have 80 characters visible";

            int row = 16;
            int column = 0; 
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

            TerminalField field = new TerminalField(row,column,unformatted,fieldProtected,fieldNumeric,
                fieldDisplay,fieldIntenseDisplay,fieldSelectorPen,fieldModifed,foregroundColour,backgroundColour,highlight);
            field.getContents().add( new FieldContents( stringToCharacterArray(text)) );

            image.getFields().add(field);
        }


        {
            String text = "0         1         2         3         4         5         6         7";

            int row = 19;
            int column = 0; 
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

            TerminalField field = new TerminalField(row,column,unformatted,fieldProtected,fieldNumeric,
                fieldDisplay,fieldIntenseDisplay,fieldSelectorPen,fieldModifed,foregroundColour,backgroundColour,highlight);
            field.getContents().add( new FieldContents( stringToCharacterArray(text)) );

            image.getFields().add(field);
        }

        {
            String text = "01234567890123456789012345678901234567890123456789012345678901234567890123456789";

            int row = 20;
            int column = 0; 
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

            TerminalField field = new TerminalField(row,column,unformatted,fieldProtected,fieldNumeric,
                fieldDisplay,fieldIntenseDisplay,fieldSelectorPen,fieldModifed,foregroundColour,backgroundColour,highlight);
            field.getContents().add( new FieldContents( stringToCharacterArray(text)) );

            image.getFields().add(field);
        }

        checkTerminalImageAgainstExpected(renderer, image);
    }



    @Test
    public void testTextAOnAllRowsRendersOk() throws Exception {
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

        {
            String text = "Each of the 26 rows should have a number in";

            int row = 16;
            int column = 10; 
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

            TerminalField field = new TerminalField(row,column,unformatted,fieldProtected,fieldNumeric,
                fieldDisplay,fieldIntenseDisplay,fieldSelectorPen,fieldModifed,foregroundColour,backgroundColour,highlight);
            field.getContents().add( new FieldContents( stringToCharacterArray(text)) );

            image.getFields().add(field);
        }

        for ( int i=0;i<26;i++) {
            String text = Integer.toString(i);

            int row = i;
            int column = 0; 
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

            TerminalField field = new TerminalField(row,column,unformatted,fieldProtected,fieldNumeric,
                fieldDisplay,fieldIntenseDisplay,fieldSelectorPen,fieldModifed,foregroundColour,backgroundColour,highlight);
            field.getContents().add( new FieldContents( stringToCharacterArray(text)) );

            image.getFields().add(field);
        }

        checkTerminalImageAgainstExpected(renderer, image);
    }
}
