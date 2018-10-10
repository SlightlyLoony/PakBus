package com.dilatush.pakbus.messages;

import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.DataType;

import static com.dilatush.pakbus.types.DataTypes.*;

/**
 * Instances of this class represent a file upload (datalogger to application) response message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class FileUpLoadRsp extends Message {


    private static CP[] PROPS = {
            new CP( "RespCode",   BYTE  ),
            new CP( "FileOffset", UINT4 ),
            new CP( "FileData",   BYTES, true  )
    };


    final static private DataType TYPE = getType();


    public FileUpLoadRsp() {
        super( TYPE );
    }


    private static DataType getType() {
        return getMessageType( FileUpLoadRsp.class.getSimpleName(), PROPS );
    }
}
