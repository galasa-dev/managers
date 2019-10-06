/*
 * Copyright (c) 2019 IBM Corporation.
 */
package dev.galasa.zos3270.internal.datastream;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import dev.galasa.zos3270.spi.DatastreamException;

public class OrderStartFieldExtended extends AbstractOrder {

    public static final byte            ID         = 0x29;

    private final ArrayList<IAttribute> attributes = new ArrayList<>();

    public OrderStartFieldExtended(ByteBuffer buffer) throws DatastreamException {
        byte[] rep = new byte[4];
        rep[0] = 0;
        rep[1] = 0;
        rep[2] = 0;
        rep[3] = buffer.get();

        ByteBuffer countBuffer = ByteBuffer.wrap(rep);
        int fieldAttributeCount = countBuffer.getInt();

        for (int i = 0; i < fieldAttributeCount; i++) {
            byte attributeId = buffer.get();
            switch (attributeId) {
                case OrderStartField.ATTRIBUTE_ID:
                    attributes.add(new OrderStartField(buffer));
                    break;
                case AttributeFieldValidation.ATTRIBUTE_ID:
                    attributes.add(new AttributeFieldValidation(buffer));
                    break;
                case AttributeFieldOutlining.ATTRIBUTE_ID:
                    attributes.add(new AttributeFieldOutlining(buffer));
                    break;
                case AttributeExtendedHighlighting.ATTRIBUTE_ID:
                    attributes.add(new AttributeExtendedHighlighting(buffer));
                    break;
                case AttributeForegroundColour.ATTRIBUTE_ID:
                    attributes.add(new AttributeForegroundColour(buffer));
                    break;
                case AttributeCharacterSet.ATTRIBUTE_ID:
                    attributes.add(new AttributeCharacterSet(buffer));
                    break;
                case AttributeBackgroundColour.ATTRIBUTE_ID:
                    attributes.add(new AttributeBackgroundColour(buffer));
                    break;
                case AttributeTransparency.ATTRIBUTE_ID:
                    attributes.add(new AttributeTransparency(buffer));
                    break;
                default:
                    throw new DatastreamException("Unrecognised attribute in SFE, '" + attributeId + "'");
            }
        }
    }

    @Override
    public byte[] getBytes() {
        throw new UnsupportedOperationException("Not available yet");
    }

    public List<IAttribute> getAttributes() {
        return attributes;
    }

}
