package com.dilatush.pakbus;

import com.dilatush.pakbus.types.PakBusType;

/**
 * Instances of this class define a single field within a message - it's layout, encoded type, and Java type.  Note that the position of the field
 * is not defined.  That's because the position is inferred from the cumulative length of fields.  This approach is necessary because messages can
 * contain variable length fields.  Instances of this class are immutable and threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MessageFieldDefinition {

    private final String name;                  // the name of the field...
    private final int optionalLevel;            // zero if this field is required, > zero to indicate optional section nesting level...
    private final PakBusType pakbusType;


    public MessageFieldDefinition( final String _name, final PakBusType _pakbusType, final int _optionalLevel ) {
        name = _name;
        optionalLevel = _optionalLevel;
        pakbusType = _pakbusType;
    }


    public String getName() {
        return name;
    }


    public int getBits() {
        return pakbusType.getBits();
    }


    public int getOptionalLevel() {
        return optionalLevel;
    }


    public PakBusType getPakbusType() {
        return pakbusType;
    }
}
