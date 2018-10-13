package com.dilatush.pakbus;

/**
 * Represents all possible values of the "expect more" field in a PakBus packet.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum ExpectMore {


    Last(       0b000 ),
    ExpectMore( 0b001 ),
    Neutral(    0b010 ),
    Reverse(    0b011 ),
    INVALID(    0b100 );


    private final int code;

    ExpectMore( final int _code ) {
        code = _code;
    }


    public int getCode() {
        return code;
    }


    public boolean isValid() {
        return this != INVALID;
    }


    /**
     * Decodes the given value to the ExpectMore instance it represents (which may be INVALID if the code is invalid).
     *
     * @param _value the value to decode
     * @return The decoded instance (INVALID if the given value was invalid)
     */
    public static ExpectMore decode( final int _value ) {

        switch( _value ) {
            case 0b000: return Last;
            case 0b001: return ExpectMore;
            case 0b010: return Neutral;
            case 0b011: return Reverse;
            default: return INVALID;
        }
    }
}
