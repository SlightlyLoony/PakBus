package com.dilatush.pakbus.messages.bmp5;

import com.dilatush.pakbus.MessageType;
import com.dilatush.pakbus.Protocol;
import com.dilatush.pakbus.comms.Context;
import com.dilatush.pakbus.messages.AMsg;
import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.util.Checks;
import com.dilatush.pakbus.values.CompositeDatum;

import java.nio.ByteBuffer;

import static com.dilatush.pakbus.MessageType.Response;
import static com.dilatush.pakbus.Protocol.BMP5;
import static com.dilatush.pakbus.types.DataTypes.*;

/**
 * Represents a BMP5 "Collect Data Response" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class CollectDataRspMsg extends AMsg {


    final static public String FIELD_RESPONSE_CODE = "RespCode";
    final static public String FIELD_TABLE_NUMBER  = "TableNumber";
    final static public String FIELD_RECORD_NUMBER = "RecordNumber";
    final static public String FIELD_COUNT_OFF     = "CountOff";
    final static public String FIELD_BYTES         = "Bytes";
    final static public String FIELD_MORE_RECORDS  = "MoreRecords";

    final public ResponseCode          responseCode;
    final public int tableNumber;
    final public boolean isFragment;
    final public int firstRecord;
    final public int countOff;
    final public ByteBuffer bytes;

    final static public Protocol    PROTOCOL = BMP5;
    final static public int         CODE     = 0x89;
    final static public MessageType TYPE     = Response;


    public CollectDataRspMsg( final ByteBuffer _bytes, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _bytes, _context );

        // a little analysis to see what we've got here...
        tableNumber = 0xFFFF & _bytes.getShort( 3 );
        isFragment = ((0x80 & _bytes.get( 9 )) != 0);
        firstRecord = _bytes.getInt( 5 );
        countOff = isFragment ? (0x7FFFFFFF & _bytes.getInt( 9 )) : _bytes.getShort( 9 );

        // decode our response...
        props.add( new CP( FIELD_RESPONSE_CODE, BYTE ) );
        props.add( new CP( FIELD_TABLE_NUMBER, UINT2 ) );
        props.add( new CP( FIELD_RECORD_NUMBER, UINT4 ) );
        if( isFragment )
            props.add( new CP( FIELD_COUNT_OFF, UINT4 ) );
        else
            props.add( new CP( FIELD_COUNT_OFF, UINT2 ) );
        props.add( new CP( FIELD_BYTES, BYTES, true ) );
        props.add( new CP( FIELD_MORE_RECORDS, BOOL ) );
        datum = new CompositeDatum( getDataType() );
        datum.set( new BitBuffer( _bytes ) );
        responseCode = ResponseCode.decode( datum.at( FIELD_RESPONSE_CODE ).getAsInt() );
        bytes = datum.at( FIELD_BYTES ).getAsByteBuffer();
        setBase();
    }
}
