package com.dilatush.pakbus.values;

import com.dilatush.pakbus.types.DataType;
import com.dilatush.pakbus.types.SimpleDataType;
import com.dilatush.pakbus.util.BitBuffer;

/**
 * Instances of this class represent a simple datum (that is, neither an array nor a composite type).  Instances of this class are mutable and NOT
 * threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class SimpleDatum extends ADatum {


    public SimpleDatum( final DataType _type ) {
        super( _type );

        // sanity check...
        if( !(_type instanceof SimpleDataType ) )
            throw new IllegalArgumentException( "Attempted to create a SimpleDatum from a different data type: " + _type );
    }


    /**
     * Sets this datum's value from the bits in the given buffer.  Upon invocation, the given buffer's position must be at the first bit of this
     * datum's value, and the limit must be at the bit following the last bit that is available.  This method will read as many bits from the buffer
     * as are required to set this datum's value, leaving the position at the first bit following the bits read.  The given buffer's limit is not
     * changed by this method.
     *
     * @param _buffer the buffer containing the bits from which this datum's value will be read
     */
    @Override
    public void set( final BitBuffer _buffer ) {

        // sanity checks...
        if( _buffer == null )
            throw new IllegalArgumentException( "Required bits argument missing" );

        // this is a simple type, so the bit length is known by definition; all we need to do is check for enough bits...
        if( type.bits() > _buffer.remaining() )
            throw new IllegalStateException( "Source has insufficient data: need " + type.bits() + " bits, but the buffer has only "
                    + _buffer.remaining() + " bits left" );

        // get the value...
        buffer = new BitBuffer( type.bits() );
        buffer.put( _buffer, type.bits() );
        buffer.flip();
    }
}
