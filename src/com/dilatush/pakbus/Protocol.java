package com.dilatush.pakbus;

/**
 * Represents all possible values of the higher level protocol in a PakBus packet.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum Protocol {


    PakCtrl( 0b00000 ),
    BMP5(    0b00001 ),
    INVALID( 0b10000 );


    private final int code;

    Protocol( final int _code ) {
        code = _code;
    }


    public int getCode() {
        return code;
    }


    public boolean isValid() {
        return this != INVALID;
    }


    /**
     * Decodes the given value to the Protocol instance it represents (which may be INVALID if the code is invalid).
     *
     * @param _value the value to decode
     * @return The decoded instance (INVALID if the given value was invalid)
     */
    public static Protocol decode( final int _value ) {

        switch( _value ) {
            case 0b00000: return PakCtrl;
            case 0b00001: return BMP5;
            default: return INVALID;
        }
    }
}
