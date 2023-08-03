/*
 * Copyright contributors to the Galasa project
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package dev.galasa.zos3270.common.screens.images;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.io.OutputStream;

import javax.imageio.ImageIO;

import dev.galasa.framework.spi.IConfidentialTextService;
import dev.galasa.zos3270.common.screens.FieldContents;
import dev.galasa.zos3270.common.screens.TerminalField;
import dev.galasa.zos3270.common.screens.TerminalImage;
import dev.galasa.zos3270.common.screens.TerminalSize;

public class TerminalImageTransform {
    
    private TerminalSize terminalSize ;
    private int targetRowCount ;
    private int targetColumnCount;

    private int imageWidthPixels ;
    private int imageHeightPixels ;

    private BufferedImage image ;
    private Graphics2D graphics ;

    private int fontHeight ;
    private int fontWidth ;

    IConfidentialTextService confidentialTextService ;

    public TerminalImageTransform(TerminalSize terminalSize , IConfidentialTextService confidentialTextService ) throws TerminalImageException {
        
        this.confidentialTextService = confidentialTextService;

        this.terminalSize = terminalSize;
        this.targetRowCount = terminalSize.getRows() + 2;
        this.targetColumnCount = terminalSize.getColumns();

         // 7 and 13 represent the dimensions of the default monospaced font on MacOS
        // Ideally, these values would be retrieved from the font metrics but that requires a
        // Graphics object to be created, which in turn requires the image to be created -
        // We plan to improve this in the future
        this.imageWidthPixels = targetColumnCount * 7;
        this.imageHeightPixels = targetRowCount * 13; 

        this.image = new BufferedImage(imageWidthPixels, imageHeightPixels, BufferedImage.TYPE_INT_ARGB);
        this.graphics = image.createGraphics();

        // Ensures the font family is monospaced so that the images appear as expected
        // If the font family is not monospaced, the font defaults to "Dialog", which skews images
        Font font = new Font(Font.MONOSPACED, Font.PLAIN, 10);
        if (!font.getFamily().equals(Font.MONOSPACED)) {
            throw new TerminalImageException("Unable to set Monospaced font");
        }
        graphics.setFont(font);

        // Collect the font dimensions
        FontMetrics fontMetrics = graphics.getFontMetrics();
        this.fontHeight = fontMetrics.getHeight();
        this.fontWidth = fontMetrics.getMaxAdvance();

        graphics.setPaint(Color.black);
        graphics.fillRect(0, 0, imageWidthPixels, imageHeightPixels);
        graphics.setPaint(Color.green);
    }

    private void clearImage() {
        graphics.clearRect(0, 0, imageWidthPixels, imageHeightPixels);
    }

    public void writeImage( TerminalImage sourceTerminalImage , String outputFormat , OutputStream outStream) throws IOException {
        clearImage();
        renderTerminalImage(sourceTerminalImage);
        ImageIO.write(image, outputFormat, outStream);
        outStream.flush();
    }

    private void renderTerminalImage(TerminalImage sourceTerminalImage) {

        for (TerminalField field : sourceTerminalImage.getFields()) {
            StringBuilder sb = new StringBuilder();
            for (FieldContents contents : field.getContents()) {
                // Origin of each character glyph is bottom right of the character.
                int col = (field.getColumn()  );
                // Add one to the rows, so that the row so that row 0 of the input
                // displays at row1...
                int row = (field.getRow() + 1); 
                
                // Converting FieldContents to Strings
                for (Character c : contents.getChars()) {
                    if (c == null) {
                        sb.append(" ");
                    } else {
                        sb.append(c);
                    }
                }

                // Apply the filter to confidential text if it matches, and such filtering is enabled.
                String fieldText = sb.toString();

                if (confidentialTextService != null) {
                    fieldText = confidentialTextService.removeConfidentialText(fieldText);
                }

                for (Character c : fieldText.toCharArray()) {
                    if (col > targetColumnCount) {
                        col = 1;
                        row++;
                        if (row > targetRowCount) {
                            row = 1;
                        }
                    }
                    graphics.drawString(Character.toString(c), col * fontWidth, row * fontHeight);
                    col++;
                }
            }
        }

        String terminalStatusRow = writeTerminalStatusRow(sourceTerminalImage, terminalSize.getColumns(), terminalSize.getRows());
        graphics.drawString(terminalStatusRow, 0 * fontWidth, (terminalSize.getRows() + 1) * fontHeight);

    }

    private String writeTerminalStatusRow(TerminalImage terminalImage, int cols, int rows) {
    
        StringBuilder sb = new StringBuilder();

        if (terminalImage.getId() != null) {
            sb.append(terminalImage.getId());
            sb.append(" - ");
        }

        sb.append(Integer.toString(cols));
        sb.append("x");
        sb.append(Integer.toString(rows));
        sb.append(" - ");

        if (terminalImage.isInbound()) {
            sb.append("Inbound ");
        } else {
            sb.append("Outbound - ");
            sb.append(terminalImage.getAid());
        }

        return sb.toString();
    }
}