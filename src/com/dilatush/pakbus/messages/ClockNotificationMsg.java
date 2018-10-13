package com.dilatush.pakbus.messages;

import com.dilatush.pakbus.NSec;
import com.dilatush.pakbus.comms.Context;
import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.DataTypes;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.values.CompositeDatum;

import java.nio.ByteBuffer;

import static com.dilatush.pakbus.MessageType.Broadcast;
import static com.dilatush.pakbus.Protocol.PakCtrl;

/**
 * Represents a PakCtrl "ClockNotification" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ClockNotificationMsg extends AMsg {


    final public NSec time;

    public ClockNotificationMsg( final NSec _time, final Context _context ) {
        super( PakCtrl, 0x02, Broadcast, _context );

        // sanity check...
        if( (_time == null) || (_context == null) )
            throw new IllegalArgumentException( "Required argument is missing" );

        // save our parameters...
        time = _time;

        // create and initialize our datum...
        initDataType();
        setDatum();
        datum.at( "time" ).setTo( _time );
    }


    public ClockNotificationMsg( final ByteBuffer _bytes, final Context _context ) {
        super( PakCtrl, 0x02, Broadcast, _context );

        // sanity check...
        if( (_bytes == null) || (_context == null) )
            throw new IllegalArgumentException( "Required argument is missing" );

        // decode our time...
        initDataType();
        datum = new CompositeDatum( getDataType() );
        datum.set( new BitBuffer( _bytes ) );
        time = datum.at( "time" ).getAsNSec();
    }


    private void initDataType() {
        props.add( new CP( "time", DataTypes.NSEC ) );
    }
}
