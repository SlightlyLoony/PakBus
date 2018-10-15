package com.dilatush.pakbus.messages.bmp5;

import com.dilatush.pakbus.MessageType;
import com.dilatush.pakbus.NSec;
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
 * Represents a BMP5 "Clock Response" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ClockRspMsg extends AMsg {


    final static public String FIELD_RESPCODE = "RespCode";
    final static public String FIELD_OLDTIME = "OldTime";

    final static public Protocol    PROTOCOL = BMP5;
    final static public int         CODE     = 0x97;
    final static public MessageType TYPE     = Response;

    final public NSec oldTime;
    final public byte responseCode;


    /**
     * Creates a new instance of this class with the given parameters.  The old time is only required if the response code is zero.
     *
     * @param _responseCode the response code (0 = complete, 1 = permission denied)
     * @param _oldTime if response code was complete, the time before any adjustment
     * @param _context the communications context
     */
    public ClockRspMsg( final byte _responseCode, final NSec _oldTime, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _context );
        if( _responseCode == 0 )
            Checks.required( _oldTime );

        // save our parameters...
        responseCode = _responseCode;
        oldTime = _oldTime;

        // create and initialize our datum...
        initDataType();
        setDatum();
        datum.at( FIELD_RESPCODE ).setTo( responseCode );
        if( responseCode == 0)
            datum.at( FIELD_OLDTIME ).setTo( oldTime );
    }


    public ClockRspMsg( final ByteBuffer _bytes, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _bytes, _context );

        // decode our time...
        initDataType();
        datum = new CompositeDatum( getDataType() );
        datum.set( new BitBuffer( _bytes ) );
        responseCode = datum.at( FIELD_RESPCODE ).getAsByte();
        oldTime = (responseCode == 0) ? datum.at( FIELD_OLDTIME ).getAsNSec() : null;
    }


    private void initDataType() {
        props.add( new CP( FIELD_RESPCODE, DataTypes.BYTE               ) );
        props.add( new CP( FIELD_OLDTIME, DataTypes.NSEC, true ) );
    }
}
