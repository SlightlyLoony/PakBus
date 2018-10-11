package com.dilatush.pakbus.values;

import com.dilatush.pakbus.types.DataType;
import com.dilatush.pakbus.types.DataTypes;
import com.dilatush.pakbus.types.GeneralDataType;
import com.dilatush.pakbus.types.SimpleDataType;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.util.PakBusFloat;

import java.nio.charset.StandardCharsets;

import static com.dilatush.pakbus.types.GeneralDataType.*;

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


    /**
     * Sets the value of this datum to the given string.  This setter works on a datum of any character or string type (i.e., ASCII or arrays of
     * ASCII). Invoking this method on a datum of any other type will throw an exception.  If this datum is a fixed-length type, then the given string
     * must exactly match the length, or an exception will be thrown.  Note that the string will be encoded as UTF-8, which is compatible with
     * US-ASCII but allows Unicode characters.
     *
     * @param _value the string value to set this datum to
     */
    @Override
    public void setTo( final String _value ) {

        // sanity checks...
        if( isSet() )
            throw new IllegalStateException( "Attempted to set string to a datum that's already set" );
        if( type != DataTypes.ASCII )
            throw new IllegalStateException( "Attempted to set string to a simple datum that's not ASCII" );
        if( _value == null )
            throw new IllegalArgumentException( "Value is missing" );
        byte[] strBytes = _value.getBytes( StandardCharsets.UTF_8 );
        if( strBytes.length != 1 )
            throw new IllegalArgumentException( "Value is not a single character long" );

        // write the character code...
        setTo( strBytes[0] );
    }


    /**
     * Returns the value of this datum as a string value.  This getter works on a datum of any character or string type (i.e., ASCII or arrays of
     * ASCII). Invoking this method on a datum of any other type will throw an exception.  Note that this datum will be decoded as UTF-8, which is
     * compatible with US-ASCII but allows Unicode characters.
     *
     * @return the value of this datum as a string
     */
    @Override
    public String getAsString() {

        // sanity checks...
        if( !isSet() )
            throw new IllegalStateException( "Attempted to read character from unset datum" );
        if( type != DataTypes.ASCII )
            throw new IllegalStateException( "Attempted to read character from a simple datum that's not ASCII" );

        // read the character code, then make a string and we're done...
        return Character.toString( (char) getAsShort() );
    }


    /**
     * Verifies that this datum has been set, in preparation for encoding.
     */
    public void finish() {

        if( !isSet() )
            throw new IllegalStateException( "Attempted to finish a datum whose value has not been set" );
    }


    /**
     * Sets the value of this datum to the given boolean.  This setter works on a simple datum of any boolean or integer type.  Invoking this method
     * on any array datum, composite datum, or simple datum that is not a boolean or integer type will throw an exception.  For integer types, a value
     * of false will be set as a zero, and a value of true will be set as a one.
     *
     * @param _value the boolean value to set this datum to.
     */
    @Override
    public void setTo( final boolean _value ) {

        // if this datum isn't a boolean or integer type, we've got an error...
        GeneralDataType gt = type.generalType();
        if( !((gt == SignedInteger) || (gt == UnsignedInteger) || (gt == Boolean)) )
            throw new IllegalStateException( "Attempted to set boolean to simple datum that is neither boolean nor integer" );

        // set our value...
        setTo( _value ? 1L : 0L );
    }


    /**
     * Returns the value of this datum as a boolean value.  This getter works on a simple datum of any boolean or integer type.  Invoking this method
     * on any array datum, composite datum, or simple datum that is not a boolean or integer type will throw an exception.  For integer types, a value
     * of zero will be returned as false, and any other value will be returned as true.
     *
     * @return the value of this datum as a boolean
     */
    @Override
    public boolean getAsBoolean() {

        // if this datum isn't a boolean or integer type, we've got an error...
        GeneralDataType gt = type.generalType();
        if( !((gt == SignedInteger) || (gt == UnsignedInteger) || (gt == Boolean)) )
            throw new IllegalStateException( "Attempted to get boolean from simple datum that is neither boolean nor integer" );

        // get our velue...
        return (getAsLong() != 0);
    }


    /**
     * Sets the value of this datum to the given double.  This setter works on any simple datum of any integer or float type.  Invoking this method on
     * a datum of any other type will throw an exception.  Depending on this datum's type, the resulting value may be out of range (which will throw
     * an exception) or may lose precision (because the PakBus type is less precise than a double).  When setting integer values with this method, the
     * double value will first be rounded to the nearest integer.
     *
     * @param _value the double value to set this datum to
     */
    @Override
    public void setTo( final double _value ) {

        // if this datum isn't a float or integer type, we've got an error...
        GeneralDataType gt = type.generalType();
        if( !((gt == SignedInteger) || (gt == UnsignedInteger) || (gt == Float)) )
            throw new IllegalStateException( "Attempted to set double on simple datum that is neither float nor integer" );

        // handle floats...
        if( gt == Float )
            set( PakBusFloat.toPakBusFloat( _value, type.pakBusType() ) );

        // and integers...
        else
            setTo( Math.round( _value ) );
    }


    /**
     * Returns the value of this datum as a double.  This getter works on any simple datum of any integer or float type.  Invoking this method on a
     * datum of any other type will throw an exception.  Depending on this datum's type, the returned value may be out of range (which will throw an
     * exception).
     */
    @Override
    public double getAsDouble() {

        // if this datum isn't a float or integer type, we've got an error...
        GeneralDataType gt = type.generalType();
        if( !((gt == SignedInteger) || (gt == UnsignedInteger) || (gt == Float)) )
            throw new IllegalStateException( "Attempted to get double from simple datum that is neither float nor integer" );

        // handle floats...
        if( gt == Float )
            return PakBusFloat.fromPakBusFloat( buffer.getByteBuffer(), type.pakBusType() );

        // and integers...
        else
            return (double) getAsLong();
    }
}
