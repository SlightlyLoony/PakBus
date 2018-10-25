package com.dilatush.pakbus.values;

import com.dilatush.pakbus.types.DataType;
import com.dilatush.pakbus.types.DataTypes;
import com.dilatush.pakbus.types.GeneralDataType;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.util.Checks;

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
    protected boolean childSet;  // true if any child property has been set...
    protected Datum parent;  // a link to the parent datum (array or composite), or null if none...


    protected ADatum( final DataType _type ) {

        Checks.required( _type );

        type = _type;
        buffer = null;
        size = _type.bits();

        childSet = false;
        parent = null;
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


    /**
     * Sets the childSet property in all parents of this datum.
     */
    protected void informParents() {
        ADatum current = (ADatum) parent;
        while( current != null ) {
            current.childSet = true;
            current = (ADatum) current.parent;
        }
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

        // if we just got an array datum, use an index if there's one available...
        if( (current instanceof ArrayDatum) && (indiceIndex < _indices.length) )
            current = ((ArrayDatum)current).get( _indices[indiceIndex++] );

        // traverse our path...
        for( String part : parts ) {

            // if the current datum is not composite, we've got an error...
            Checks.isTrue( current instanceof CompositeDatum, "Path component, parent of '" + part + "', is not a composite datum" );

            // if it doesn't have that property, we've got an error...
            current = ((CompositeDatum)current).getDatum( part );
            Checks.isNonNull( current, "Path component '" + part + "' does not exist" );

            // if we just got an array datum, use an index if there's one available...
            if( (current instanceof ArrayDatum) && (indiceIndex < _indices.length) )
                current = ((ArrayDatum)current).get( _indices[indiceIndex++] );
        }

        // and we're done...
        return current;
    }


    /**
     * Returns the array datum at the given path and given indices to array datum(s) within that path.  The path consists of dot-separated names of
     * composite datum properties.  If at("x") were called on a composite property with a property named "x", the datum representing that property
     * would be returned.  If "x" was itself a composite datum, then at("x.y") would return the datum representing the property "y" on its parent
     * property "x".  If "y" was an array datum, then at("x.y",3) would return the fourth element of it.  If that element was a composite property,
     * then at("x.y.z",3) would return the "z" property of it.
     *
     * @param _path the path to the datum to be returned
     * @param _indices the indices into any array datum(s) within the path
     * @return the array datum at the path
     */
    @Override
    public ArrayDatum arrayAt( final String _path, final int... _indices ) {

        // get the datum at the path...
        Datum datum = at( _path, _indices );

        // if it's not an array datum, we've got a problem...
        Checks.isTrue( datum instanceof ArrayDatum, "Given path does not resolve to an array datum: " + _path );

        // we're good, so get outta here...
        return (ArrayDatum) datum;
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
        Checks.required( _buffer );
        Checks.isTrue( !isSet(), "Attempting to set datum that is already set" );
        Checks.isTrue( (type.bits() & 7) == 0, "Attempting to set a fixed-length datum that is not an even number of bytes long" );

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
        Checks.isTrue( isSet(), "Attempting to read a datum that has not been set" );
        Checks.isTrue( (size & 7) == 0, "Attempting to read a datum that is not an even number of bytes long" );

        return get().getByteBuffer();
    }


    /**
     * Sets the value of this datum to the given time.  The datum must be a time type (Sec, USec, or NSec).
     *
     * @param _time the bytes to set this datum to
     */
    @Override
    public void setTo( final NSec _time ) {

        // sanity checks...
        Checks.required( _time );
        Checks.isTrue( (type == DataTypes.NSEC) || (type == DataTypes.USEC) || (type == DataTypes.SEC),
                "Attempted to set time on a non-time datum" );

        if( type == DataTypes.NSEC ) {
            at( "Seconds" ).setTo( _time.seconds );
            at( "Nanoseconds" ).setTo( _time.nanoseconds );
        }

        else if( type == DataTypes.SEC ) {
            setTo( _time.add( new NSec( 0, 500000000 ) ).seconds );
        }

        else { // it must be a USEC...
            long usecs = (long)_time.seconds;
            usecs &= 0xFFFFFFFFL;
            usecs *= 1000000;
            usecs += (_time.nanoseconds + 500) / 1000;
            setTo( usecs );
        }
    }


    /**
     * Returns the value of this datum as a ByteBuffer.  This getter works on any datum with a length that is an even number of bytes.
     *
     * @return the value of this datum as NSec time
     */
    @Override
    public NSec getAsNSec() {

        // sanity checks...
        Checks.isTrue( isSet(), "Attempting to read a datum that has not been set" );
        Checks.isTrue( (type == DataTypes.NSEC) || (type == DataTypes.USEC) || (type == DataTypes.SEC),
                "Attempted to get time a non-time datum" );

        if( type == DataTypes.NSEC ) {
            return new NSec( at( "Seconds" ).getAsInt(), at( "Nanoseconds" ).getAsInt() );
        }

        else if( type == DataTypes.SEC ) {
            return new NSec( getAsInt(), 0 );
        }

        else { // it must be a USEC...
            long usecs = 0xFFFFFFFFFFFFL & getAsLong();
            int ss = (int)(usecs / 1000000);
            int ns = (int)((usecs % 1000000) * 1000);
            return new NSec( ss, ns );
        }
    }


    protected void processIntegerSet( final long _value, final int _size, final int _actBits ) {

        // sanity checks...
        Checks.isTrue( !isSet(), "Attempting to set a datum that has already been set" );
        Checks.isTrue( size() > 0, "Attempting to set variable-length datum with fixed length data" );
        Checks.isTrue( _actBits <= size(), "Attempting to set fixed-length datum with data that's too long" );

        // sign-extend our value if necessary, then set it...
        long value = _value;
        if( type.generalType() == GeneralDataType.SignedInteger )
            value = ((value << size) >> size);  // sign extend...
        set( new BitBuffer( value, size() ) );
    }


    protected long processIntegerGet( int _maxBits ) {

        // sanity checks...
        Checks.isTrue( isSet(), "Attempting to read a datum that has not been set" );
        Checks.inBounds( buffer.capacity(), 0, _maxBits,
                "Attempted to read from datum with " + buffer.capacity() + " bits; valid range is [0.." + _maxBits + "]" );

        // get our value and sign-extend it if necessary...
        long value = buffer.getBits();
        if( type.generalType() == GeneralDataType.SignedInteger )
            value = ((value << size) >> size);  // sign extend...
        return value;
    }
}
