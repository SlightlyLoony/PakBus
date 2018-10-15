package com.dilatush.pakbus.messages.pakctrl;

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
import static com.dilatush.pakbus.Protocol.PakCtrl;

/**
 * Represents a PakCtrl "Hello Response" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class HelloRspMsg extends AMsg {


    final static public String FIELD_IS_ROUTER       = "IsRouter";
    final static public String FIELD_HOP_METRIC      = "HopMetric";
    final static public String FIELD_VERIFY_INTERVAL = "VerifyInterval";

    final static public Protocol    PROTOCOL = PakCtrl;
    final static public int         CODE     = 0x89;
    final static public MessageType TYPE     = Response;

    final public byte isRouter;
    final public byte hopMetric;
    final public int verifyInterval;


    public HelloRspMsg( final int _isRouter, final int _hopMetric, final int _verifyInterval, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _context );

        // save our parameters...
        isRouter = (byte) _isRouter;
        hopMetric = (byte) _hopMetric;
        verifyInterval = _verifyInterval;

        // create and initialize our datum...
        initDataType();
        setDatum();
        datum.at( FIELD_IS_ROUTER ).setTo( isRouter );
        datum.at( FIELD_HOP_METRIC ).setTo( hopMetric );
        datum.at( FIELD_VERIFY_INTERVAL ).setTo( verifyInterval );
    }


    public HelloRspMsg( final ByteBuffer _bytes, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _bytes, _context );

        // decode our time...
        initDataType();
        datum = new CompositeDatum( getDataType() );
        datum.set( new BitBuffer( _bytes ) );
        isRouter       = datum.at( FIELD_IS_ROUTER       ).getAsByte();
        hopMetric      = datum.at( FIELD_HOP_METRIC      ).getAsByte();
        verifyInterval = datum.at( FIELD_VERIFY_INTERVAL ).getAsInt();
    }


    private void initDataType() {
        props.add( new CP( FIELD_IS_ROUTER,       DataTypes.BYTE  ) );
        props.add( new CP( FIELD_HOP_METRIC,      DataTypes.BYTE  ) );
        props.add( new CP( FIELD_VERIFY_INTERVAL, DataTypes.UINT2 ) );
    }
}
