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

import static com.dilatush.pakbus.MessageType.Request;
import static com.dilatush.pakbus.Protocol.BMP5;

/**
 * Represents a BMP5 "Get Programming Statistics Request" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class GetProgrammingStatisticsReqMsg extends AMsg {


    final static public String FIELD_SECURITY_CODE = "SecurityCode";

    final public int securityCode;

    final static public Protocol    PROTOCOL = BMP5;
    final static public int         CODE     = 0x18;
    final static public MessageType TYPE     = Request;


    public GetProgrammingStatisticsReqMsg( final int _securityCode, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _context );

        // save our parameters...
        securityCode = _securityCode;

        // create and initialize our datum...
        initDataType();
        setDatum();
        datum.at( FIELD_SECURITY_CODE ).setTo( securityCode );
    }


    public GetProgrammingStatisticsReqMsg( final ByteBuffer _bytes, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _bytes, _context );

        // decode our time...
        initDataType();
        datum = new CompositeDatum( getDataType() );
        datum.set( new BitBuffer( _bytes ) );
        securityCode = datum.at( FIELD_SECURITY_CODE ).getAsInt();
    }


    private void initDataType() {
        props.add( new CP( FIELD_SECURITY_CODE, DataTypes.UINT2 ) );
    }
}
