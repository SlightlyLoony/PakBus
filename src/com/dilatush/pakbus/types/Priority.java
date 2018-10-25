package com.dilatush.pakbus.types;

/**
 * Represents all possible values of the message priority in a PakBus packet.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum Priority {


    Low(     0b000 ),
    Normal(  0b001 ),
    High(    0b010 ),
    Urgent(  0b011 ),
    INVALID( 0b100 );


    private final int code;

    Priority( final int _code ) {
        code = _code;
    }


    public int getCode() {
        return code;
    }


    public boolean isValid() {
        return this != INVALID;
    }


    /**
     * Decodes the given value to the Priority instance it represents (which may be UNKNOWN if the code is invalid).
     *
     * @param _value the value to decode
     * @return The decoded instance (UNKNOWN if the given value was invalid)
     */
    public static Priority decode( final int _value ) {

        switch( _value ) {
            case 0b000: return Low;
            case 0b001: return Normal;
            case 0b010: return High;
            case 0b011: return Urgent;
            default: return INVALID;
        }
    }
}
