package com.dilatush.pakbus.values;

import com.dilatush.pakbus.NSec;
import com.dilatush.pakbus.types.*;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.util.Checks;

import java.nio.ByteBuffer;

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


    /**
     * Tells this datum to perform any intermediate phase operations required.  Most commonly this method will change its underlying type to
     * accommodate a dependency on a previously set property.
     */
    default void phase() {}


    /**
     * Perform any operations required in preparation for encoding this datum.
     */
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


    /**
     * Returns the datum at the given path and given indices to array datum(s) within that path.  The path consists of dot-separated names of
     * composite datum properties.  If at("x") were called on a composite property with a property named "x", the datum representing that property
     * would be returned.  If "x" was itself a composite datum, then at("x.y") would return the datum representing the property "y" on its parent
     * property "x".  If "y" was an array datum, then at("x.y",3) would return the fourth element of it.  If that element was a composite property,
     * then at("x.y.z",3) would return the "z" property of it.
     *
     * @param _path the path to the datum to be returned
     * @param _indices the indices into any array datum(s) within the path
     * @return the datum at the path
     */
    Datum at( final String _path, int... _indices );


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
    ArrayDatum arrayAt( final String _path, int... _indices );


    /**
     * Sets the value of this datum to the given byte value.  This setter works as expected on any integer value up to 8 bits, and it also works
     * on a datum of any fixed-length type that is no longer than 8 bits.
     *
     * @param _value the byte value to set this datum to
     */
    void setTo( final byte _value );


    /**
     * Returns the value of this datum as a byte value.  This getter works as expected on any integer value up to 8 bits long, and it also works
     * on a datum of any fixed-length type that is no longer than 8 bits.
     *
     * @return the value of this datum as a byte
     */
    byte getAsByte();


    /**
     * Sets the value of this datum to the given short value.  This setter works as expected on any integer value up to 16 bits, and it also works
     * on a datum of any fixed-length type that is no longer than 16 bits.
     *
     * @param _value the short value to set this datum to
     */
    void setTo( final short _value );


    /**
     * Returns the value of this datum as a short value.  This getter works as expected on any integer value up to 16 bits long, and it also works
     * on a datum of any fixed-length type that is no longer than 16 bits.
     *
     * @return the value of this datum as a short
     */
    short getAsShort();


    /**
     * Sets the value of this datum to the given integer value.  This setter works as expected on any integer value up to 32 bits, and it also works
     * on a datum of any fixed-length type that is no longer than 32 bits.
     *
     * @param _value the integer value to set this datum to
     */
    void setTo( final int _value );


    /**
     * Returns the value of this datum as an integer value.  This getter works as expected on any integer value up to 32 bits long, and it also works
     * on a datum of any fixed-length type that is no longer than 32 bits.
     *
     * @return the value of this datum as an integer
     */
    int getAsInt();


    /**
     * Sets the value of this datum to the given long value.  This setter works as expected on any integer value up to 64 bits, and it also works
     * on a datum of any fixed-length type that is no longer than 64 bits.
     *
     * @param _value the integer value to set this datum to
     */
    void setTo( final long _value );


    /**
     * Returns the value of this datum as a long value.  This getter works as expected on any integer value up to 64 bits long, and it also works
     * on a datum of any fixed-length type that is no longer than 64 bits.
     *
     * @return the value of this datum as a long
     */
    long getAsLong();


    /**
     * Sets the value of this datum to the given string.  This setter works on a datum of any character or string type (i.e., ASCII or arrays of
     * ASCII). Invoking this method on a datum of any other type will throw an exception.  If this datum is a fixed-length type, then the given
     * string must exactly match the length, or an exception will be thrown.  Note that the string will be encoded as UTF-8, which is compatible
     * with US-ASCII but allows Unicode characters.
     *
     * @param _value the string value to set this datum to
     */
    void setTo( final String _value );


    /**
     * Returns the value of this datum as a string value.  This getter works on a datum of any character or string type (i.e., ASCII or arrays of
     * ASCII). Invoking this method on a datum of any other type will throw an exception.  Note that this datum will be decoded as UTF-8, which is
     * compatible with US-ASCII but allows Unicode characters.
     *
     * @return the value of this datum as a string
     */
    String getAsString();


    /**
     * Sets the value of this datum to the given boolean.  This setter works on a simple datum of any boolean or integer type.  Invoking this method
     * on any array datum, composite datum, or simple datum that is not a boolean or integer type will throw an exception.  For integer types, a value
     * of false will be set as a zero, and a value of true will be set as a one.
     *
     * @param _value the boolean value to set this datum to.
     */
    void setTo( final boolean _value );


    /**
     * Returns the value of this datum as a boolean value.  This getter works on a simple datum of any boolean or integer type.  Invoking this method
     * on any array datum, composite datum, or simple datum that is not a boolean or integer type will throw an exception.  For integer types, a value
     * of zero will be returned as false, and any other value will be returned as true.
     *
     * @return the value of this datum as a boolean
     */
    boolean getAsBoolean();


    /**
     * Sets the value of this datum to the given double.  This setter works on any simple datum of any integer or float type.  Invoking this method on
     * a datum of any other type will throw an exception.  Depending on this datum's type, the resulting value may be out of range (which will throw
     * an exception) or may lose precision (because the PakBus type is less precise than a double).  When setting integer values with this method, the
     * double value will first be rounded to the nearest integer.
     *
     * @param _value the double value to set this datum to
     */
    void setTo( final double _value );


    /**
     * Returns the value of this datum as a double.  This getter works on any simple datum of any integer or float type.  Invoking this method on
     * a datum of any other type will throw an exception.  Depending on this datum's type, the returned value may be out of range (which will throw an
     * exception).
     *
     * @return the value of this datum as a double
     */
    double getAsDouble();


    /**
     * Sets the value of this datum to the given ByteBuffer.  This setter works on any datum with a variable length, or with a fixed length that is
     * an even number of bytes.
     *
     * @param _buffer the bytes to set this datum to
     */
    void setTo( final ByteBuffer _buffer );


    /**
     * Returns the value of this datum as a ByteBuffer.  This getter works on any datum with a length that is an even number of bytes.
     *
     * @return the value of this datum as a ByteBuffer
     */
    ByteBuffer getAsByteBuffer();


    /**
     * Sets the value of this datum to the given time.  The datum must be a time type (Sec, USec, or NSec).
     *
     * @param _time the bytes to set this datum to
     */
    void setTo( final NSec _time );


    /**
     * Returns the value of this datum as a ByteBuffer.  This getter works on any datum with a length that is an even number of bytes.
     *
     * @return the value of this datum as NSec time
     */
    NSec getAsNSec();


    /**
     * Returns a newly constructed datum appropriate for the given data type.
     *
     * @param _type the type of datum to construct
     * @return the newly constructed datum
     */
    static Datum from( final DataType _type ) {

        Checks.required( _type );

        if( _type instanceof SimpleDataType )
            return new SimpleDatum( _type );
        else if( _type instanceof ArrayDataType )
            return new ArrayDatum( _type );
        return new CompositeDatum( _type );
    }


    /**
     * Returns a newly constructed datum appropriate for the given PakBus data type.
     *
     * @param _type the type of datum to construct
     * @return the newly constructed datum
     */
    static Datum from( final PakBusType _type ) {

        Checks.required( _type );

        return from( DataTypes.fromPakBusType( _type ) );
    }


    /**
     * Returns a newly constructed datum appropriate for the given data type name.
     *
     * @param _typeName the name of the data type
     * @return the newly constructed datum
     */
    static Datum from( final String _typeName ) {

        Checks.required( _typeName );

        DataType dataType = DataTypes.fromName( _typeName );
        Checks.required( dataType );

        return from( dataType );
    }
}
