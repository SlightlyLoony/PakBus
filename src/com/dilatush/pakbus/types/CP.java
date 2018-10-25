package com.dilatush.pakbus.types;

import com.dilatush.pakbus.util.Checks;

/**
 * Instances of this class specify a property in a composite data type constructor.  The name (CP) is short for "Composite Property".  Instances of
 * this class are immutable and threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class CP {

    final private String name;
    final private DataType type;
    final private boolean optional;


    /**
     * Constructs a new instance of this class with the given name, type, and required/optional.
     *
     * @param _name the name of the property being specified
     * @param _type the type of the property being specified
     * @param _optional true if the property being specified is optional; false if it is required
     */
    public CP( final String _name, final DataType _type, final boolean _optional ) {

        // sanity checks...
        Checks.required( _type );
        Checks.notEmpty( _name );

        name = _name;
        type = _type;
        optional = _optional;
    }


    /**
     * Constructs a new instance of this class with the given name and type, and required.
     *
     * @param _name the name of the property being specified
     * @param _type the type of the property being specified
     */
    public CP( final String _name, final DataType _type ) {
        this( _name, _type, false);
    }


    public String getName() {
        return name;
    }


    public DataType getType() {
        return type;
    }


    public boolean isOptional() {
        return optional;
    }
}
