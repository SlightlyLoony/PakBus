package com.dilatush.pakbus.util;

import com.dilatush.pakbus.types.ArrayDataType;
import com.dilatush.pakbus.types.DataTypes;
import com.dilatush.pakbus.values.ArrayDatum;
import com.dilatush.pakbus.values.Datum;

import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;

/**
 * A static method container class for methods that convert between datums and various Java types.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Transformer {


    /**
     * Returns the value of the given datum as a byte.  The datum must have its value set, and must be between 1 and 8 bits long.
     *
     * @param _datum the value of the given datum as a byte
     * @return the byte value of the datum
     */
    public static byte toByte( final Datum _datum ) {
        validateRead( _datum, 1, 8 );
        return (byte) _datum.get().getBits();
    }


    /**
     * Sets the value of the given datum from the given byte. The datum's value must not be already set, and it must be a fixed-length datum
     * with the capacity to hold all the bits in the given byte, ignoring any leading (MSBs) zeros.
     *
     * @param _byte the integer to set the value of the datum from
     * @param _datum the datum to set the value of
     */
    public static void fromByte( final int _byte, final Datum _datum ) {
        validateFixedWrite( _datum, 8 - Integer.numberOfLeadingZeros( _byte ) );
        _datum.set( new BitBuffer( _byte, _datum.size() ) );
    }


    /**
     * Returns the value of the given datum as an integer.  The datum must have its value set, and must be between 1 and 32 bits long.
     *
     * @param _datum the value of the given datum as an integer
     * @return the integer value of the datum
     */
    public static int toInt( final Datum _datum ) {
        validateRead( _datum, 1, 32 );
        return (int) _datum.get().getBits();
    }


    /**
     * Sets the value of the given datum from the given integer. The datum's value must not be already set, and it must be a fixed-length datum
     * with the capacity to hold all the bits in the given integer, ignoring any leading (MSBs) zeros.
     *
     * @param _int the integer to set the value of the datum from
     * @param _datum the datum to set the value of
     */
    public static void fromInt( final int _int, final Datum _datum ) {
        validateFixedWrite( _datum, 32 - Integer.numberOfLeadingZeros( _int ) );
        _datum.set( new BitBuffer( _int, _datum.size() ) );
    }


    /**
     * Returns the value of the given datum as a string.  The datum must be a character or string type, and must have its value set.
     *
     * @param _datum the character or string datum to get a string from
     * @return the string value of the datum
     */
    public static String toStr( final Datum _datum ) {

        // sanity checks...
        if( _datum == null )
            throw new IllegalArgumentException( "Required datum argument is missing" );
        if( !_datum.isSet() )
            throw new IllegalStateException( "Attempted to read string from unset datum" );

        // if we have a datum of type ASCII, just extract the character and we're done...
        if( _datum.type() == DataTypes.fromName( "ASCII" ) ) {

            // read the character code, then make a string and we're done...
            return Character.toString( (char) toInt( _datum ) );
        }

        // if we don't have an array of ASCII characters, we've got a problem...
        boolean isString = (_datum instanceof ArrayDatum);
        isString = isString & (((ArrayDataType)_datum.type()).getItemType() == DataTypes.fromName( "ASCII" ) );
        if( !isString )
            throw new IllegalStateException( "Attempted to read string from a datum that is neither character nor string" );

        // we're good, so handle them...
        ArrayDatum ad = (ArrayDatum)_datum;
        StringBuilder sb = new StringBuilder();
        for( int i = 0; i < ad.elements(); i++ ) {
            Datum id = ad.get( i );
            char c = (char) toInt( id );
            if( c != 0)
                sb.append( c );
        }
        return sb.toString();
    }


    /**
     * Sets the value of the given datum from the given string.  The datum's value must not already be set, and it must be a character or string type
     * that is long enough to accept the given string.
     *
     * @param _string the string to set the value of the datum from
     * @param _datum the datum to set the value of
     */
    public static void fromStr( final String _string, final Datum _datum ) {

        // sanity checks...
        if( _datum == null )
            throw new IllegalArgumentException( "Required datum argument is missing" );
        if( _datum.isSet() )
            throw new IllegalStateException( "Attempted to write string to a datum that's already set" );

        // if we have a datum of type ASCII, just write the character and we're done...
        if( _datum.type() == DataTypes.fromName( "ASCII" ) ) {

            // if our string isn't exactly one character long, we've got a problem...
            if( _string.length() != 1 )
                throw new IllegalArgumentException( "String must be one character long, is " + _string.length() );

            // write the character code...
            fromByte( (byte) _string.charAt( 0 ), _datum );

            return;
        }

        // if we don't have an array of ASCII characters, we've got a problem...
        boolean isString = (_datum instanceof ArrayDatum);
        isString = isString & (((ArrayDataType)_datum.type()).getItemType() == DataTypes.fromName( "ASCII" ) );
        if( !isString )
            throw new IllegalStateException( "Attempted to write string to a datum that is neither character nor string" );

        // some setup...
        ArrayDatum ad = (ArrayDatum)_datum;
        byte[] strBytes = _string.getBytes( StandardCharsets.UTF_8 );
        ArrayDataType arrayType = (ArrayDataType)(ad.type());

        // sanity check...
        if( arrayType.isFixedLength() && (ad.elements() != strBytes.length) )
            throw new IllegalArgumentException( "Attempted to write " + strBytes.length + " byte string to "
                    + ad.elements() + " character long fixed ASCII array" );

        // get the bits we need to set the array...
        ByteBuffer bb = ByteBuffer.allocate( strBytes.length + (arrayType.isZeroTerminated() ? 1 : 0) );
        bb.put( strBytes );
        bb.flip();
        bb.limit( bb.capacity() );
        BitBuffer bits = new BitBuffer( bb );

        // then just set our array and we're done...
        _datum.set( bits );
    }


    private static void validateFixedWrite( final Datum _datum, final int _actBits ) {
        if( _datum == null)
            throw new IllegalArgumentException( "Datum is missing" );
        if( _datum.isSet() )
            throw new IllegalStateException( "Attempting to set datum that is already set" );
        if( _datum.size() == 0 )
            throw new IllegalStateException( "Attempting to set variable length datum with fixed length data" );
        if( _actBits > _datum.size() )
            throw new IllegalArgumentException( "Attempting to set fixed length datum with data that's too long" );
    }


    private static void validateRead( final Datum _datum, int _minBits, int _maxBits ) {
        if( _datum == null)
            throw new IllegalArgumentException( "Datum is missing" );
        BitBuffer bb = _datum.get();
        if( bb == null)
            throw new IllegalStateException( "Attempted to read unset datum" );
        if( (bb.capacity() < _minBits) || (bb.capacity() > _maxBits) )
            throw new IllegalStateException( "Attempted to read from datum with " + bb.capacity()
                    + " bits; valid range is [" + _minBits + ".." + _maxBits + "]" );
    }
}
