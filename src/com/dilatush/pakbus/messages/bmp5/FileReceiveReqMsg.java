package com.dilatush.pakbus.messages.bmp5;

import com.dilatush.pakbus.types.MessageType;
import com.dilatush.pakbus.types.Protocol;
import com.dilatush.pakbus.comms.Context;
import com.dilatush.pakbus.messages.AMsg;
import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.DataTypes;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.util.Checks;
import com.dilatush.pakbus.values.CompositeDatum;

import java.nio.ByteBuffer;

import static com.dilatush.pakbus.types.MessageType.Request;
import static com.dilatush.pakbus.types.Protocol.BMP5;

/**
 * Represents a BMP5 "File Receive Request" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class FileReceiveReqMsg extends AMsg {


    final static public String FIELD_SECURITY_CODE = "SecurityCode";
    final static public String FIELD_FILE_NAME     = "FileName";
    final static public String FIELD_CLOSE_FLAG    = "CloseFlag";
    final static public String FIELD_FILE_OFFSET   = "FileOffset";
    final static public String FIELD_SWATH         = "Swath";

    final public int securityCode;
    final public String fileName;
    final public int closeFlag;
    final public int fileOffset;
    final public int swath;

    final static public Protocol    PROTOCOL = BMP5;
    final static public int         CODE     = 0x1D;
    final static public MessageType TYPE     = Request;


    public FileReceiveReqMsg( final int _securityCode, final String _fileName, final int _closeFlag, final int _fileOffset, final int _swath,
                              final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _fileName, _context );

        // save our parameters...
        securityCode = _securityCode;
        fileName     = _fileName;
        closeFlag    = _closeFlag;
        fileOffset   = _fileOffset;
        swath        = _swath;

        // create and initialize our datum...
        initDataType();
        setDatum();
        datum.at( FIELD_SECURITY_CODE ).setTo( securityCode );
        datum.at( FIELD_FILE_NAME     ).setTo( fileName     );
        datum.at( FIELD_CLOSE_FLAG    ).setTo( closeFlag    );
        datum.at( FIELD_FILE_OFFSET   ).setTo( fileOffset   );
        datum.at( FIELD_SWATH         ).setTo( swath        );
    }


    public FileReceiveReqMsg( final ByteBuffer _bytes, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _bytes, _context );

        // decode our time...
        initDataType();
        datum = new CompositeDatum( getDataType() );
        datum.set( new BitBuffer( _bytes ) );
        securityCode = datum.at( FIELD_SECURITY_CODE ).getAsInt();
        fileName     = datum.at( FIELD_FILE_NAME     ).getAsString();
        closeFlag    = datum.at( FIELD_CLOSE_FLAG    ).getAsInt();
        fileOffset   = datum.at( FIELD_FILE_OFFSET   ).getAsInt();
        swath        = datum.at( FIELD_SWATH         ).getAsInt();
        setBase();
    }


    private void initDataType() {
        props.add( new CP( FIELD_SECURITY_CODE, DataTypes.UINT2  ) );
        props.add( new CP( FIELD_FILE_NAME,     DataTypes.ASCIIZ ) );
        props.add( new CP( FIELD_CLOSE_FLAG,    DataTypes.BYTE   ) );
        props.add( new CP( FIELD_FILE_OFFSET,   DataTypes.UINT4  ) );
        props.add( new CP( FIELD_SWATH,         DataTypes.UINT2  ) );
    }
}
