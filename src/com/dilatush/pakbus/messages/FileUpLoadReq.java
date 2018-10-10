package com.dilatush.pakbus.messages;

import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.DataType;

import static com.dilatush.pakbus.types.DataTypes.*;

/**
 * Instances of this class represent a file upload (datalogger to application) request message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class FileUpLoadReq extends Message {


    private static CP[] PROPS = {
            new CP( "SecurityCode", UINT2  ),
            new CP( "FileName",     ASCIIZ ),
            new CP( "CloseFlag",    BYTE   ),
            new CP( "FileOffset",   UINT4  ),
            new CP( "Swath",        UINT2  )
    };


    final static private DataType TYPE = getType();


    public FileUpLoadReq() {
        super( TYPE );
    }


    private static DataType getType() {
        return getMessageType( FileUpLoadReq.class.getSimpleName(), PROPS );
    }
}
