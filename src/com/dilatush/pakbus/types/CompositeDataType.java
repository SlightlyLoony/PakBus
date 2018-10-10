package com.dilatush.pakbus.types;

import java.util.*;

/**
 * Instances of this class represent PakBus composite data types.  Composite types contain an ordered list of named properties.  Each of these named
 * properties is a data type, and is either required or optional.  Only one property in the list may be optional; usually (but not necessarily) the
 * last property in any given type.  Instances of this class are immutable and threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class CompositeDataType extends ADataType {

    final private Map<String, CP> byName;
    final private List<CP>        byOrder;

    /**
     * Creates a new instance of this class with the given name, code, and properties defined by the given specifications.
     *
     * @param _name the name of this data type
     * @param _pakBusType the PakBus data type equivalent to this type, or null if none
     * @param _specs the ordered list of specifications for this type's properties
     */
    public CompositeDataType( final String _name, final PakBusType _pakBusType, final CP... _specs  ) {
        super( _name, _pakBusType, 0, GeneralDataType.Composite, null );

        // sanity check...
        if( (_name == null) || (_name.length() == 0) || (_specs == null) || (_specs.length == 0) )
            throw new IllegalArgumentException( "Missing required arguments" );

        // initialize our collections...
        byName = new HashMap<>( _specs.length << 1 );
        byOrder = new ArrayList<>( _specs.length );

        // stuff our specs away...
        boolean haveOptional = false;
        int bitTotal = 0;
        for( CP spec : _specs ) {

            if( spec == null )
                throw new IllegalStateException( "Specification is null" );
            if( spec.isOptional() ) {
                if( haveOptional )
                    throw new IllegalStateException( "Attempted to make composite data type with multiple optional properties" );
                haveOptional = true;
            }
            if( byName.containsKey( spec.getName() ) )
                throw new IllegalStateException( "Attempted to make composite data type with duplicate property names: " + spec.getName() );
            if( spec.getType().bits() == 0)
                bitTotal = -1;
            else if( bitTotal >= 0 )
                bitTotal += spec.getType().bits();
            byOrder.add( spec );
            byName.put( spec.getName(), spec );
        }

        // if we have a fixed length, record it...
        if( bitTotal > 0 )
            bits = bitTotal;
    }


    public List<CP> order() {
        return Collections.unmodifiableList( byOrder );
    }


    public Map<String,CP> names() {
        return Collections.unmodifiableMap( byName );
    }
}
