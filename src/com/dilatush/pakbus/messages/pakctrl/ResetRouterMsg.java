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

import static com.dilatush.pakbus.MessageType.OneWay;
import static com.dilatush.pakbus.Protocol.PakCtrl;

/**
 * Represents a PakCtrl "Reset Router" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ResetRouterMsg extends AMsg {


    final static public String FIELD_EXT_CODE = "ExtCode";

    final public int extCode;

    final static public Protocol    PROTOCOL = PakCtrl;
    final static public int         CODE     = 0x0C;
    final static public MessageType TYPE     = OneWay;


    public ResetRouterMsg( final int _extCode, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _context );

        // save our parameters...
        extCode = _extCode;

        // create and initialize our datum...
        initDataType();
        setDatum();
        datum.at( FIELD_EXT_CODE ).setTo( extCode );
    }


    public ResetRouterMsg( final ByteBuffer _bytes, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _bytes, _context );

        // decode our time...
        initDataType();
        datum = new CompositeDatum( getDataType() );
        datum.set( new BitBuffer( _bytes ) );
        extCode = datum.at( FIELD_EXT_CODE ).getAsInt();
    }


    private void initDataType() {
        props.add( new CP( FIELD_EXT_CODE, DataTypes.BYTE ) );
    }
}
