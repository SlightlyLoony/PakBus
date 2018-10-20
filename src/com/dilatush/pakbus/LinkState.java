package com.dilatush.pakbus;

/**
 * Represents all possible states of a PakBus link.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum LinkState {


    OffLine(  0b01000 ),
    Ring(     0b01001 ),
    Ready(    0b01010 ),
    Finished( 0b01011 ),
    Pause(    0b01100 ),
    INVALID(  0b10000 );


    private final int code;

    LinkState( final int _code ) {
        code = _code;
    }


    public int getCode() {
        return code;
    }


    public boolean isValid() {
        return this != INVALID;
    }


    /**
     * Decodes the given value to the LinkState instance it represents (which may be UNKNOWN if the code is invalid).
     *
     * @param _value the value to decode
     * @return The decoded instance (UNKNOWN if the given value was invalid)
     */
    public static LinkState decode( final int _value ) {

        switch( _value ) {
            case 0b01000: return OffLine;
            case 0b01001: return Ring;
            case 0b01010: return Ready;
            case 0b01011: return Finished;
            case 0b01100: return Pause;
            default: return INVALID;
        }
    }
}
