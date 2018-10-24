package com.dilatush.pakbus.messages.bmp5;

/**
 * Represents all possible response codes for PakBus response messages.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum ResponseCode {


    OK(                     0 ),
    PermissionDenied(       1 ),
    OutOfResources(         2 ),   // insufficient resources or out of memory
    InvalidTable(           7 ),   // table number or signature is invalid
    InvalidFragment(        9 ),   // fragment not in sequential order
    InvalidFileName(       13 ),
    FileNotAccessible(     14 ),
    InvalidControlOption(  15 ),   // the control option code is invalid
    InvalidName(           16 ),   // invalid table or field name
    UnsupportedConversion( 17 ),   // the requested data type conversion is not supported
    MemoryViolation(       18 ),   // specified field name and swath would report more values than are available
    UnsupportedValue(      19 ),   // in fileCmd parameter of file control
    DirectoryFull(         20 ),
    SuccessWithReboot(     21 ),   // variable change needs reboot to be effective
    CannotDelete(          22 ),   // datalogger has file open
    CannotFormat(          23 ),   // datalogger has one or more files open
    IncompatibleFormat(    24 ),   // more information in "explanation" parameter
    UNKNOWN(               -1 );


    private final int code;

    ResponseCode( final int _code ) {
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
    public static ResponseCode decode( final int _value ) {

        switch( _value ) {
            case  0: return OK;
            case  1: return PermissionDenied;
            case  2: return OutOfResources;
            case  7: return InvalidTable;
            case  9: return InvalidFragment;
            case 13: return InvalidFileName;
            case 14: return FileNotAccessible;
            case 15: return InvalidControlOption;
            case 16: return InvalidName;
            case 17: return UnsupportedConversion;
            case 18: return MemoryViolation;
            case 19: return UnsupportedValue;
            case 20: return DirectoryFull;
            case 21: return SuccessWithReboot;
            case 22: return CannotDelete;
            case 23: return CannotFormat;
            case 24: return IncompatibleFormat;
            default: return UNKNOWN;
        }
    }
}
