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

import static com.dilatush.pakbus.types.MessageType.Response;
import static com.dilatush.pakbus.types.Protocol.BMP5;

/**
 * Represents a BMP5 "File Control Response" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class FileControlRspMsg extends AMsg {


    final static public String FIELD_RESPONSE_CODE = "RespCode";
    final static public String FIELD_HOLDOFF       = "HoldOff";

    final static public Protocol    PROTOCOL = BMP5;
    final static public int         CODE     = 0x9E;
    final static public MessageType TYPE     = Response;

    final public ResponseCode responseCode;
    final public int          holdoff;


    public FileControlRspMsg( final ResponseCode _responseCode, final int _holdoff, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _context );

        // save our parameters...
        responseCode = _responseCode;
        holdoff = _holdoff;

        // create and initialize our datum...
        initDataType();
        setDatum();
        datum.at( FIELD_RESPONSE_CODE ).setTo( responseCode.getCode() );
        datum.at( FIELD_HOLDOFF  ).setTo( holdoff      );
    }


    public FileControlRspMsg( final ByteBuffer _bytes, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _bytes, _context );

        // decode our time...
        initDataType();
        datum = new CompositeDatum( getDataType() );
        datum.set( new BitBuffer( _bytes ) );
        responseCode = ResponseCode.decode( datum.at( FIELD_RESPONSE_CODE ).getAsInt() );
        holdoff      = datum.at( FIELD_HOLDOFF ).getAsInt();
        setBase();
    }


    private void initDataType() {
        props.add( new CP( FIELD_RESPONSE_CODE, DataTypes.BYTE ) );
        props.add( new CP( FIELD_HOLDOFF, DataTypes.UINT2 ) );
    }
}
