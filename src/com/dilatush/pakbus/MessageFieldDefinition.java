package com.dilatush.pakbus;

/**
 * Instances of this class define a single field within a message - it's layout, encoded type, and Java type.  Note that the position of the field
 * is not defined.  That's because the position is inferred from the cumulative length of fields.  This approach is necessary because messages can
 * contain variable length fields.  Instances of this class are immutable and threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MessageFieldDefinition {

    private final String name;                      // the name of the field...
    private final boolean optional;                 // true if this field is optional...
    private final PakBusDataType pakbusType;


    public MessageFieldDefinition( final String _name, final PakBusDataType _pakbusType, final boolean _optional ) {
        name = _name;
        optional = _optional;
        pakbusType = _pakbusType;
    }


    public String getName() {
        return name;
    }


    public int getBits() {
        return pakbusType.getBits();
    }


    public boolean isOptional() {
        return optional;
    }


    public PakBusDataType getPakbusType() {
        return pakbusType;
    }
}
