package com.dilatush.pakbus.util;

import java.nio.ByteBuffer;

/**
 * Static container class for functions related to checking parameters.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Checks {


    /**
     * If any of the given arguments are null, throws an {@link IllegalArgumentException} with a message saying that a required argument is missing.
     *
     * @param _args the arguments to check
     */
    static public void required( final Object... _args ) {
        for( Object arg : _args ) {
            if( arg == null )
                throw new IllegalArgumentException( "At least one required argument is missing" );
        }
    }


    /**
     * If the given argument is non-null, does nothing.  Otherwise, throws an {@link IllegalStateException} with the given message.
     *
     * @param _arg the argument to test
     * @param _msg the message if it is null
     */
    static public void isNonNull( final Object _arg, final String _msg ) {
        if( _arg == null )
            throw new IllegalStateException( _msg );
    }


    /**
     * Checks to see if the given argument to test is within the given (inclusive) low to high bounds.  If it is not, then this method throws an
     * {@link IllegalArgumentException} with the given message.
     *
     * @param _arg the argument to test
     * @param _low the low bound, inclusive
     * @param _high the high bound, inclusive
     * @param _msg the message upon failure
     */
    static public void inBounds( final int _arg, final int _low, final int _high, final String _msg ) {
        if( (_arg < _low) || (_arg > _high) )
            throw new IllegalArgumentException( _msg );
    }


    /**
     * Checks to see if the given argument to test is within the given (inclusive) low to high bounds.  If it is not, then this method throws an
     * {@link IllegalArgumentException} with the given message.
     *
     * @param _arg the argument to test
     * @param _low the low bound, inclusive
     * @param _high the high bound, inclusive
     * @param _msg the message upon failure
     */
    static public void inBounds( final long _arg, final long _low, final long _high, final String _msg ) {
        if( (_arg < _low) || (_arg > _high) )
            throw new IllegalArgumentException( _msg );
    }


    /**
     * If any of the given arguments are null or empty (length == 0), throws an {@link IllegalArgumentException} describing the problem.
     *
     * @param _args the strings to check
     */
    static public void notEmpty( final String... _args ) {
        if( _args == null )
            throw new IllegalArgumentException( "Required string argument is missing or empty" );
        for( String string : _args ) {
            if( (string == null) || (string.length() == 0) )
                throw new IllegalArgumentException( "Required string argument is missing or empty" );
        }
    }


    /**
     * If any of the given arguments are null or empty (length == 0), throws an {@link IllegalArgumentException} describing the problem.
     *
     * @param _args the buffers to check
     */
    static public void notEmpty( final ByteBuffer... _args ) {
        if( _args == null )
            throw new IllegalArgumentException( "Required ByteBuffer argument is missing or empty" );
        for( ByteBuffer buffer : _args ) {
            if( (buffer == null) || (buffer.limit() == 0) )
                throw new IllegalArgumentException( "Required ByteBuffer argument is missing or empty" );
        }
    }


    /**
     * If the given buffer is null, or has no bytes remaining, throws an {@link IllegalArgumentException} describing the problem.
     *
     * @param _arg the buffer to check
     */
    static public void hasRemaining( final ByteBuffer _arg ) {
        if( (_arg == null) || (_arg.remaining() == 0) )
            throw new IllegalArgumentException( "Required ByteBuffer argument is missing or has no bytes remaining" );
    }


    /**
     * If the given argument is null or empty (length == 0), or if any element is null, throws an {@link IllegalArgumentException} describing the
     * problem.
     *
     * @param _arg the objects to check
     */
    static public void notEmpty( final Object[] _arg ) {
        if( (_arg == null) || (_arg.length == 0) )
            throw new IllegalArgumentException( "Required argument is missing or empty" );
        for( Object obj : _arg ) {
            if( obj == null )
                throw new IllegalArgumentException( "Required argument is missing or empty" );
        }
    }


    /**
     * Returns true if the given operand is equal to one of the give test numbers.
     *
     * @param _op the operand to test
     * @param _test the values to test the operand against
     * @return true if the given operand is equal to one of the give test numbers
     */
    static public boolean isOneOf( final int _op, final int... _test ) {
        for( int test : _test ) {
            if( _op == test )
                return true;
        }
        return false;
    }


    /**
     * If the given argument is true, does nothing.  Otherwise, throws an {@link IllegalStateException} with the given message.
     *
     * @param _test the test condition, which should be true
     * @param _failMessage the message to include with the thrown exception
     */
    static public void isTrue( final boolean _test, final String _failMessage ) {
        if( !_test )
            throw new IllegalStateException( _failMessage );
    }
}
