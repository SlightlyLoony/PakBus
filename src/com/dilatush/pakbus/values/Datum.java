package com.dilatush.pakbus.values;

import com.dilatush.pakbus.types.DataType;
import com.dilatush.pakbus.util.BitBuffer;

/**
 * Implemented by all classes that represent a PakBus datum.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface Datum {

    /**
     * Returns true if this datum's value has been set.  There are two mechanisms that can set a value: individual setters on each datum, or direct
     * setting of the binary representation (typically during packet decoding).
     *
     * @return true if this datum's value has been set
     */
    boolean isSet();

    /**
     * Returns a bit buffer containing the bits that represent the value of this datum.  The buffer has exactly the necessary capacity, its position
     * is initially zero, and it's limit is the same as its capacity.  If this datum's value has not yet been set, a null is returned.  Note that the
     * bit buffer is mutable, so any code calling this method can modify it.
     *
     * @return the bit buffer containing the bits that represent the value of this datum
     */
    BitBuffer get();


    /**
     * Sets this datum's value from the bits in the given buffer.  Upon invocation, the given buffer's position must be at the first bit of this
     * datum's value, and the limit must be at the bit following the last bit that is available.  This method will read as many bits from the buffer
     * as are required to set this datum's value, leaving the position at the first bit following the bits read.  The given buffer's limit is not
     * changed by this method.
     *
     * @param _buffer the buffer containing the bits from which this datum's value will be read
     */
    void set( final BitBuffer _buffer );


    void finish();

    /**
     * Returns the type of this datum.
     *
     * @return the type of this datum
     */
    DataType type();


    /**
     * Returns the size of this datum's binary representation, in bits.  If the size is not known (because it's a variable-length datum that has not
     * yet been set), then zero is returned.
     *
     * @return the size of this datum's binary representation, in bits
     */
    int size();
}
