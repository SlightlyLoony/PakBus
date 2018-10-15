package com.dilatush.pakbus.util;

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
    static public void required( Object... _args ) {
        for( Object arg : _args ) {
            if( arg == null )
                throw new IllegalArgumentException( "At least one required argument is missing" );
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
}
