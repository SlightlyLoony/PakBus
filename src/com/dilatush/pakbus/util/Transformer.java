package com.dilatush.pakbus.util;

import com.dilatush.pakbus.values.Datum;

/**
 * A static method container class for methods that convert between datums and various Java types.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Transformer {


    public static int toInt( final Datum _datum ) {
        validateRead( _datum, 1, 32 );
        return (int) _datum.get().getBits();
    }


    public static void fromInt( final int _int, final Datum _datum ) {
        validateFixedWrite( _datum, 32 - Integer.numberOfLeadingZeros( _int ) );
        _datum.set( new BitBuffer( _int, _datum.size() ) );
    }


    public static String toString( final Datum _datum ) {
        return null;
    }


    public static void fromString( final String _string, final Datum _datum ) {

    }


    public static void validateFixedWrite( final Datum _datum, final int _actBits ) {
        if( _datum == null)
            throw new IllegalArgumentException( "Datum is missing" );
        if( _datum.isSet() )
            throw new IllegalStateException( "Attempting to set datum that is already set" );
        if( _datum.size() == 0 )
            throw new IllegalStateException( "Attempting to set variable length datum with fixed length data" );
        if( _actBits > _datum.size() )
            throw new IllegalArgumentException( "Attempting to set fixed length datum with data that's too long" );
    }


    public static void validateRead( final Datum _datum, int _minBits, int _maxBits ) {
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
