package com.dilatush.pakbus.messages.bmp5;

import com.dilatush.pakbus.types.MessageType;
import com.dilatush.pakbus.types.Protocol;
import com.dilatush.pakbus.comms.Context;
import com.dilatush.pakbus.messages.AMsg;
import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.CompositeDataType;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.util.Checks;
import com.dilatush.pakbus.values.CompositeDatum;
import com.dilatush.pakbus.values.Datum;

import java.nio.ByteBuffer;

import static com.dilatush.pakbus.types.MessageType.Response;
import static com.dilatush.pakbus.types.Protocol.BMP5;
import static com.dilatush.pakbus.types.DataTypes.*;

/**
 * Represents a BMP5 "Collect Data Response" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class CollectDataRspMsg extends AMsg {


    final static public String FIELD_RESPONSE_CODE = "RespCode";
    final static public String FIELD_BYTES         = "Bytes";
    final static public String FIELD_MORE_RECORDS  = "MoreRecords";
    final static public String FIELD_PAYLOAD       = "Payload";

    final public ResponseCode responseCode;
    final public ByteBuffer   bytes;
    final public boolean      moreRecords;

    final static public Protocol    PROTOCOL = BMP5;
    final static public int         CODE     = 0x89;
    final static public MessageType TYPE     = Response;


    public CollectDataRspMsg( final ByteBuffer _bytes, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _bytes, _context );

        // decode our response...
        props.add( new CP( FIELD_RESPONSE_CODE, BYTE ) );
        props.add( new CP( FIELD_PAYLOAD, new CompositeDataType( FIELD_PAYLOAD, null,
                        new CP( FIELD_BYTES, BYTES, true ),
                        new CP( FIELD_MORE_RECORDS, BOOL          ) ), true )
                 );
        datum = new CompositeDatum( getDataType() );
        datum.set( new BitBuffer( _bytes ) );
        responseCode = ResponseCode.decode( datum.at( FIELD_RESPONSE_CODE ).getAsInt() );
        Datum payload = datum.at( FIELD_PAYLOAD );
        if( payload.at( FIELD_BYTES ).isSet() ) {
            bytes = payload.at( FIELD_BYTES ).getAsByteBuffer();
            moreRecords = payload.at( FIELD_MORE_RECORDS ).getAsBoolean();
        }
        else {
            bytes = null;
            moreRecords = false;
        }
        setBase();
    }
}
