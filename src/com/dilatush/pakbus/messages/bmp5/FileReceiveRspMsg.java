package com.dilatush.pakbus.messages.bmp5;

import com.dilatush.pakbus.types.MessageType;
import com.dilatush.pakbus.types.Protocol;
import com.dilatush.pakbus.comms.Context;
import com.dilatush.pakbus.messages.AMsg;
import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.CompositeDataType;
import com.dilatush.pakbus.types.DataTypes;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.util.Checks;
import com.dilatush.pakbus.values.CompositeDatum;
import com.dilatush.pakbus.values.Datum;

import java.nio.ByteBuffer;

import static com.dilatush.pakbus.types.MessageType.Response;
import static com.dilatush.pakbus.types.Protocol.BMP5;

/**
 * Represents a BMP5 "File Receive Response" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class FileReceiveRspMsg extends AMsg {


    final static public String FIELD_RESPONSE_CODE = "RespCode";
    final static public String FIELD_FILE_OFFSET   = "FileOffset";
    final static public String FIELD_FILE_DATA     = "FileData";
    final static public String FIELD_VALID_DATA    = "ValidData";
    final static public String FIELD_TYPE1         = "TYPE1";

    final static private CompositeDataType TYPE1 = new CompositeDataType( FIELD_TYPE1, null,
            new CP( FIELD_FILE_OFFSET,   DataTypes.UINT4 ),
            new CP( FIELD_FILE_DATA,     DataTypes.BYTES ) );

    final static public Protocol    PROTOCOL = BMP5;
    final static public int         CODE     = 0x9D;
    final static public MessageType TYPE     = Response;

    final public ResponseCode responseCode;
    final public int          fileOffset;
    final public ByteBuffer   fileData;


    public FileReceiveRspMsg( final ResponseCode _responseCode, final int _fileOffset, final ByteBuffer _fileData, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _fileData, _context );

        // save our parameters...
        responseCode = _responseCode;
        fileOffset   = _fileOffset;
        fileData     = _fileData.asReadOnlyBuffer();

        // create and initialize our datum...
        initDataType();
        setDatum();
        datum.at( FIELD_RESPONSE_CODE ).setTo( responseCode.getCode() );
        datum.at( FIELD_FILE_OFFSET   ).setTo( fileOffset             );
        datum.at( FIELD_FILE_DATA     ).setTo( fileData               );
    }


    public FileReceiveRspMsg( final ByteBuffer _bytes, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _bytes, _context );

        // decode our response...
        initDataType();
        datum = new CompositeDatum( getDataType() );
        datum.set( new BitBuffer( _bytes ) );
        responseCode = ResponseCode.decode( datum.at( FIELD_RESPONSE_CODE ).getAsInt() );
        if( responseCode == ResponseCode.OK ) {
            Datum dataDatum = datum.at( FIELD_VALID_DATA );
            fileOffset = dataDatum.at( FIELD_FILE_OFFSET ).getAsInt();
            fileData = dataDatum.at( FIELD_FILE_DATA ).getAsByteBuffer();
        }
        else {
            fileOffset = -1;
            fileData = null;
        }
        setBase();
    }


    private void initDataType() {
        props.add( new CP( FIELD_RESPONSE_CODE, DataTypes.BYTE       ) );
        props.add( new CP( FIELD_VALID_DATA,    TYPE1, true ) );
    }
}
