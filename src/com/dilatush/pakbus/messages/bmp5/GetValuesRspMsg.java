package com.dilatush.pakbus.messages.bmp5;

import com.dilatush.pakbus.MessageType;
import com.dilatush.pakbus.Protocol;
import com.dilatush.pakbus.comms.Context;
import com.dilatush.pakbus.messages.AMsg;
import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.DataTypes;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.util.Checks;
import com.dilatush.pakbus.values.CompositeDatum;

import java.nio.ByteBuffer;

import static com.dilatush.pakbus.MessageType.Response;
import static com.dilatush.pakbus.Protocol.BMP5;

/**
 * Represents a BMP5 "Get Values Response" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class GetValuesRspMsg extends AMsg {


    final static public String FIELD_RESPONSE_CODE = "RespCode";
    final static public String FIELD_BYTES         = "Bytes";

    final static public Protocol    PROTOCOL = BMP5;
    final static public int         CODE     = 0x9A;
    final static public MessageType TYPE     = Response;

    final public ResponseCode responseCode;
    final public ByteBuffer   bytes;


    public GetValuesRspMsg( final ByteBuffer _bytes, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _bytes, _context );

        // decode our response...
        props.add( new CP( FIELD_RESPONSE_CODE, DataTypes.BYTE  ) );
        props.add( new CP( FIELD_BYTES,         DataTypes.BYTES ) );
        datum = new CompositeDatum( getDataType() );
        datum.set( new BitBuffer( _bytes ) );
        responseCode = ResponseCode.decode( datum.at( FIELD_RESPONSE_CODE ).getAsInt() );
        if( responseCode == ResponseCode.OK ) {
            bytes = datum.at( FIELD_BYTES ).getAsByteBuffer();
        }
        else {
            bytes = null;
        }
        setBase();
    }
}
