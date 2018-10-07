package com.dilatush.pakbus.values;

import com.dilatush.pakbus.types.DataType;
import com.dilatush.pakbus.util.BitBuffer;

/**
 * Abstract base class for all classes that implement Datum.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public abstract class ADatum implements Datum {

    final private DataType type;   // the type of this datum...

    private BitBuffer buffer;
    private int size;  // the actual size of this datum, in bits, or zero if it is not yet known...


    public ADatum( final DataType _type ) {

        if( _type == null )
            throw new IllegalArgumentException( "Required type argument is missing" );

        type = _type;
        buffer = null;
        size = _type.bits();
    }


    public ADatum( final DataType _type, final BitBuffer _buffer ) {
        type = _type;
        buffer = _buffer;
    }


    @Override
    public boolean isSet() {
        return buffer != null;
    }


    @Override
    public BitBuffer get() {
        return buffer;
    }


    @Override
    public void set( final BitBuffer _bits ) {

        if( _bits == null )
            throw new IllegalArgumentException( "Required bits argument missing" );
        if( (type.bits() != 0) && (type.bits() != _bits.capacity()) )
            throw new IllegalStateException( "Attempted to set " + _bits.capacity() + " bits to datum with fixed length of " + size + " bits" );

        buffer = _bits;
        if( size == 0 )
            size = _bits.capacity();
    }


    @Override
    public DataType type() {
        return type;
    }


    @Override
    public int size() {
        return size;
    }
}
