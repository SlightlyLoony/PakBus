package com.dilatush.pakbus.messages.bmp5;

import com.dilatush.pakbus.types.MessageType;
import com.dilatush.pakbus.values.NSec;
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
 * Represents a BMP5 "Clock Request" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ClockReqMsg extends AMsg {


    final static public String FIELD_ADJ = "Adjustment";
    final static public String FIELD_SECURITY_CODE = "SecurityCode";

    final public NSec adjustment;

    final static public Protocol    PROTOCOL = BMP5;
    final static public int         CODE     = 0x17;
    final static public MessageType TYPE     = Request;


    public ClockReqMsg( final int _securityCode, final NSec _adjustment, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _adjustment, _context );

        // save our parameters...
        adjustment = _adjustment;

        // create and initialize our datum...
        initDataType();
        setDatum();
        datum.at( FIELD_SECURITY_CODE ).setTo( _securityCode );
        datum.at( FIELD_ADJ ).setTo( _adjustment );
    }


    public ClockReqMsg( final ByteBuffer _bytes, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _bytes, _context );

        // decode our time...
        initDataType();
        datum = new CompositeDatum( getDataType() );
        datum.set( new BitBuffer( _bytes ) );
        adjustment = datum.at( FIELD_ADJ ).getAsNSec();
        setBase();
    }


    private void initDataType() {
        props.add( new CP( FIELD_SECURITY_CODE, DataTypes.UINT2 ) );
        props.add( new CP( FIELD_ADJ, DataTypes.NSEC ) );
    }
}
