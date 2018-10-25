package com.dilatush.pakbus.types;

/**
 * Represents all possible values for the protocol in a PakBus packet.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum Protocol {


    PakCtrl( 0b00000 ),
    BMP5(    0b00001 ),
    SerPkt(  0b10000 ),
    INVALID( 0b11111 );


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
     * Decodes the given value to the Protocol instance it represents (which may be UNKNOWN if the code is invalid).
     *
     * @param _value the value to decode
     * @return The decoded instance (UNKNOWN if the given value was invalid)
     */
    public static Protocol decode( final int _value ) {

        switch( _value ) {
            case 0b00000: return PakCtrl;
            case 0b00001: return BMP5;
            case 0b10000: return SerPkt;
            default: return INVALID;
        }
    }
}
