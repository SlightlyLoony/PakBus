package com.dilatush.pakbus.shims;

/**
 * Represents all possible program status codes.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum ProgramStatus {


    NoProgramRunning(          0 ),
    DataloggerProgramRunning(  1 ),
    CompileError(              2 ),
    DataloggerProgramPaused(   3 ),
    UNKNOWN(                  -1 );


    private final int code;

    ProgramStatus( final int _code ) {
        code = _code;
    }


    public int getCode() {
        return code;
    }


    public boolean isValid() {
        return this != UNKNOWN;
    }


    /**
     * Decodes the given value to the instance of this class that it represents (which may be UNKNOWN if the code is invalid).
     *
     * @param _value the value to decode
     * @return The decoded instance (UNKNOWN if the given value was invalid)
     */
    public static ProgramStatus decode( final int _value ) {

        switch( _value ) {
            case 0:  return NoProgramRunning;
            case 1:  return DataloggerProgramRunning;
            case 2:  return CompileError;
            case 3:  return DataloggerProgramPaused;
            default: return UNKNOWN;
        }
    }
}
