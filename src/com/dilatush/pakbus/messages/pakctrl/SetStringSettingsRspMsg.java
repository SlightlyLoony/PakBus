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
 * Represents a PakCtrl "Clock Response" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class SetStringSettingsRspMsg extends AMsg {


    final static public String FIELD_RESPCODE    = "RespCode";
    final static public String FIELD_FAIL_OFFSET = "FailOffset";

    final static public Protocol    PROTOCOL = PakCtrl;
    final static public int         CODE     = 0x88;
    final static public MessageType TYPE     = Response;

    final public int failOffset;
    final public int responseCode;


    /**
     * Creates a new instance of this class with the given parameters.  The response code is 0 for complete, 1 for a read-only setting, 2 for no
     * room to store the settings, 3 for a syntax error in the settings, and 4 for access denied.  The failure offset is present only if there was
     * a failure of some kind, and in that case it's the character offset into the settings string of the first field setting that failed.
     *
     * @param _responseCode the response code (0 = complete, 1 = permission denied)
     * @param _failOffset the offset into the settings string of the first field that failed
     * @param _context the communications context
     */
    public SetStringSettingsRspMsg( final int _responseCode, final int _failOffset, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _context );

        // save our parameters...
        responseCode = _responseCode;
        failOffset = _failOffset;

        // create and initialize our datum...
        initDataType();
        setDatum();
        datum.at( FIELD_RESPCODE ).setTo( responseCode );
        if( (responseCode != 0) && (responseCode != 4))
            datum.at( FIELD_FAIL_OFFSET ).setTo( failOffset );
    }


    public SetStringSettingsRspMsg( final ByteBuffer _bytes, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _bytes, _context );

        // decode our time...
        initDataType();
        datum = new CompositeDatum( getDataType() );
        datum.set( new BitBuffer( _bytes ) );
        responseCode = 0xFF & datum.at( FIELD_RESPCODE ).getAsByte();
        failOffset = datum.at( FIELD_FAIL_OFFSET ).isSet() ? datum.at( FIELD_FAIL_OFFSET ).getAsInt() : 0;
    }


    private void initDataType() {
        props.add( new CP( FIELD_RESPCODE,    DataTypes.BYTE                 ) );
        props.add( new CP( FIELD_FAIL_OFFSET, DataTypes.UINT2, true ) );
    }
}
