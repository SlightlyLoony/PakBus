package com.dilatush.pakbus.messages.pakctrl;

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

import static com.dilatush.pakbus.types.MessageType.Broadcast;
import static com.dilatush.pakbus.types.Protocol.PakCtrl;

/**
 * Represents a PakCtrl "Clock Notification" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ClockNotificationMsg extends AMsg {


    final static public String FIELD_TIME = "Time";

    final static public Protocol    PROTOCOL = PakCtrl;
    final static public int         CODE     = 0x02;
    final static public MessageType TYPE     = Broadcast;

    final public NSec time;

    public ClockNotificationMsg( final NSec _time, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _context, _time );

        // save our parameters...
        time = _time;

        // create and initialize our datum...
        initDataType();
        setDatum();
        datum.at( FIELD_TIME ).setTo( _time );
    }


    public ClockNotificationMsg( final ByteBuffer _bytes, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _bytes, _context );

        // decode our time...
        initDataType();
        datum = new CompositeDatum( getDataType() );
        datum.set( new BitBuffer( _bytes ) );
        time = datum.at( FIELD_TIME ).getAsNSec();
    }


    private void initDataType() {
        props.add( new CP( FIELD_TIME, DataTypes.NSEC ) );
    }
}
