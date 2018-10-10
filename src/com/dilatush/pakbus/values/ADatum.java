package com.dilatush.pakbus.values;

import com.dilatush.pakbus.types.DataType;
import com.dilatush.pakbus.types.GeneralDataType;
import com.dilatush.pakbus.util.BitBuffer;

import java.nio.ByteBuffer;
import java.util.Objects;

/**
 * Abstract base class for all classes that implement Datum.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public abstract class ADatum implements Datum {

    protected DataType type;   // the type of this datum...

    protected BitBuffer buffer;
    protected int size;  // the actual size of this datum, in bits, or zero if it is not yet known...


    public ADatum( final DataType _type ) {

        if( _type == null )
            throw new IllegalArgumentException( "Required type argument is missing" );

        type = _type;
        buffer = null;
        size = _type.bits();
    }


    public void finish() {

        if( !isSet() )
            throw new IllegalStateException( "Attempted to finish a datum whose value has not been set" );
    }


    @Override
    public boolean equals( final Object _o ) {
        if( this == _o ) return true;
        if( _o == null || getClass() != _o.getClass() ) return false;
        ADatum aDatum = (ADatum) _o;
        return size == aDatum.size &&
                Objects.equals( type, aDatum.type ) &&
                Objects.equals( buffer, aDatum.buffer );
    }


    @Override
    public int hashCode() {
        return Objects.hash( type, buffer, size );
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
    public DataType type() {
        return type;
    }


    @Override
    public int size() {
        return size;
    }


    /**
     * Returns the datum at the given path and given indices to array datum(s) within that path.  The path consists of dot-separated names of
     * composite datum properties.  If at("x") were called on a composite property with a property named "x", the datum representing that property
     * would be returned.  If "x" was itself a composite datum, then at("x.y") would return the datum representing the property "y" on its parent
     * property "x".  If "y" was an array datum, then at("x.y",3) would return the fourth element of it.  If that element was a composite property,
     * then at("x.y.z",3) would return the "z" property of it.
     *
     * @param _path    the path to the datum to be returned
     * @param _indices the indices into any array datum(s) within the path
     * @return the datum at the path
     */
    @Override
    public Datum at( final String _path, final int... _indices ) {

        // if there's no path, we just return ourself...
        if( (_path == null) || (_path.length() == 0) )
            return this;

        // some setup...
        int indiceIndex = 0;
        String[] parts = _path.split( "\\." );
        Datum current = this;

        // traverse our path...
        for( String part : parts ) {

            // if the current datum is not composite, we've got an error...
            if( !(current instanceof CompositeDatum) )
                throw new IllegalArgumentException( "Path component, parent of '" + part + "', is not a composite datum" );

            // if it doesn't have that property, we've got an error...
            current = ((CompositeDatum)current).getDatum( part );
            if( current == null )
                throw new IllegalArgumentException( "Path component '" + part + "' does not exist" );

            // if we just got an array datum, use an index if there's one available...
            if( (current instanceof ArrayDatum) && (indiceIndex < _indices.length) )
                current = ((ArrayDatum)current).get( indiceIndex++ );
        }

        // and we're done...
        return current;
    }


    /**
     * Sets the value of this datum to the given byte value.  This setter works as expected on any integer value up to 8 bits, and it also works on a
     * datum of any fixed-length type that is no longer than 8 bits.
     *
     * @param _value the byte value to set this datum to
     */
    @Override
    public void setTo( final byte _value ) {
        processIntegerSet( _value, 8, 32 - Integer.numberOfLeadingZeros( _value & 0xFF ) );
    }


    /**
     * Returns the value of this datum as a byte value.  This getter works as expected on any integer value up to 8 bits long, and it also works on a
     * datum of any fixed-length type that is no longer than 8 bits.
     *
     * @return the value of this datum as a byte
     */
    @Override
    public byte getAsByte() {
        return (byte) processIntegerGet( 8 );
    }


    /**
     * Sets the value of this datum to the given short value.  This setter works as expected on any integer value up to 16 bits, and it also works on
     * a datum of any fixed-length type that is no longer than 16 bits.
     *
     * @param _value the short value to set this datum to
     */
    @Override
    public void setTo( final short _value ) {
        processIntegerSet( _value, 16, 32 - Integer.numberOfLeadingZeros( _value & 0xFFFF ) );
    }


    /**
     * Returns the value of this datum as a short value.  This getter works as expected on any integer value up to 16 bits long, and it also works on
     * a datum of any fixed-length type that is no longer than 16 bits.
     *
     * @return the value of this datum as a short
     */
    @Override
    public short getAsShort() {
        return (short) processIntegerGet( 16 );
    }


    /**
     * Sets the value of this datum to the given integer value.  This setter works as expected on any integer value up to 32 bits, and it also works
     * on a datum of any fixed-length type that is no longer than 32 bits.
     *
     * @param _value the integer value to set this datum to
     */
    @Override
    public void setTo( final int _value ) {
        processIntegerSet( _value, 32, 32 - Integer.numberOfLeadingZeros( _value ) );
    }


    /**
     * Returns the value of this datum as an integer value.  This getter works as expected on any integer value up to 32 bits long, and it also works
     * on a datum of any fixed-length type that is no longer than 32 bits.
     *
     * @return the value of this datum as an integer
     */
    @Override
    public int getAsInt() {
        return (int) processIntegerGet( 32 );
    }


    /**
     * Sets the value of this datum to the given long value.  This setter works as expected on any integer value up to 64 bits, and it also works on a
     * datum of any fixed-length type that is no longer than 64 bits.
     *
     * @param _value the integer value to set this datum to
     */
    @Override
    public void setTo( final long _value ) {
        processIntegerSet( _value, 64,64 - Long.numberOfLeadingZeros( _value ) );
    }


    /**
     * Returns the value of this datum as an integer value.  This getter works as expected on any integer value up to 64 bits long, and it also works
     * on a datum of any fixed-length type that is no longer than 64 bits.
     *
     * @return the value of this datum as a long
     */
    @Override
    public long getAsLong() {
        return processIntegerGet( 64 );
    }


    /**
     * Sets the value of this datum to the given ByteBuffer.  This setter works on any datum with a variable length, or with a fixed length that is an
     * even number of bytes.
     *
     * @param _buffer the bytes to set this datum to
     */
    @Override
    public void setTo( final ByteBuffer _buffer ) {

        // sanity checks...
        if( _buffer == null )
            throw new IllegalArgumentException( "Required buffer is missing" );
        if( isSet() )
            throw new IllegalStateException( "Attempting to set datum that is already set" );
        if( (type.bits() & 7) != 0 )
            throw new IllegalStateException( "Attempting to set a fixed-length datum that is not an even number of bytes long" );

        set( new BitBuffer( _buffer ) );
    }


    /**
     * Returns the value of this datum as a ByteBuffer.  This getter works on any datum with a length that is an even number of bytes.
     *
     * @return the value of this datum as a ByteBuffer
     */
    @Override
    public ByteBuffer getAsByteBuffer() {

        // sanity checks...
        if( !isSet() )
            throw new IllegalStateException( "Attempting to read a datum that has not been set" );
        if( (size & 7) != 0 )
            throw new IllegalStateException( "Attempting to read a datum that is not an even number of bytes long" );

        return get().getByteBuffer();
    }


    protected void processIntegerSet( final long _value, final int _size, final int _actBits ) {

        // sanity checks...
        if( isSet() )
            throw new IllegalStateException( "Attempting to set datum that is already set" );
        if( size() == 0 )
            throw new IllegalStateException( "Attempting to set variable-length datum with fixed length data" );
        if( _actBits > size() )
            throw new IllegalArgumentException( "Attempting to set fixed-length datum with data that's too long" );

        // sign-extend our value if necessary, then set it...
        long value = _value;
        if( type.generalType() == GeneralDataType.SignedInteger )
            value = ((value << size) >> size);  // sign extend...
        set( new BitBuffer( value, size() ) );
    }


    protected long processIntegerGet( int _maxBits ) {

        // sanity checks...
        if( buffer == null)
            throw new IllegalStateException( "Attempted to read unset datum" );
        if( (buffer.capacity() < 0) || (buffer.capacity() > _maxBits) )
            throw new IllegalStateException( "Attempted to read from datum with " + buffer.capacity()
                    + " bits; valid range is [0.." + _maxBits + "]" );

        // get our value and sign-extend it if necessary...
        long value = buffer.getBits();
        if( type.generalType() == GeneralDataType.SignedInteger )
            value = ((value << size) >> size);  // sign extend...
        return value;
    }
}
