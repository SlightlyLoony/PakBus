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

import static com.dilatush.pakbus.MessageType.Request;
import static com.dilatush.pakbus.Protocol.PakCtrl;

/**
 * Represents a PakCtrl "Get String Settings Request" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class GetStringSettingsReqMsg extends AMsg {


    final static public String FIELD_NAMELIST = "NameList";

    final static public Protocol    PROTOCOL = PakCtrl;
    final static public int         CODE     = 0x07;
    final static public MessageType TYPE     = Request;

    final public String nameList;


    /**
     * Creates a new instance of this class with the given colon-separate list of settings names (or empty for all settings), using the given context.
     *
     * @param _nameList colon-separated list of settings names, or empty for all settings
     * @param _context the communications context
     */
    public GetStringSettingsReqMsg( final String _nameList, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _context, _nameList );

        // save our parameters...
        nameList = _nameList;

        // create and initialize our datum...
        initDataType();
        setDatum();
        datum.at( FIELD_NAMELIST ).setTo( nameList );
    }


    public GetStringSettingsReqMsg( final ByteBuffer _bytes, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _bytes, _context );

        // decode our time...
        initDataType();
        datum = new CompositeDatum( getDataType() );
        datum.set( new BitBuffer( _bytes ) );
        nameList = datum.at( FIELD_NAMELIST ).getAsString();
    }


    private void initDataType() {
        props.add( new CP( FIELD_NAMELIST, DataTypes.ASCIIZ ) );
    }
}
