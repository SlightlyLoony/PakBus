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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static com.dilatush.pakbus.MessageType.Response;
import static com.dilatush.pakbus.Protocol.PakCtrl;

/**
 * Represents a PakCtrl "Get String Settings Response" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class GetStringSettingsRspMsg extends AMsg {


    final static public String FIELD_SETTINGS = "Settings";

    final static public Protocol    PROTOCOL = PakCtrl;
    final static public int         CODE     = 0x87;
    final static public MessageType TYPE     = Response;

    final public Map<String,String> settings;


    /**
     * Creates a new instance of this class with the given colon-separate list of settings names (or empty for all settings), using the given context.
     *
     * @param _settings colon-separated list of setting name=value pairs
     * @param _context the communications context
     */
    public GetStringSettingsRspMsg( final Map<String,String> _settings, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _context, _settings );

        // save our parameters...
        settings = Collections.unmodifiableMap( _settings );

        // create and initialize our datum...
        StringBuilder sb = new StringBuilder();
        settings.forEach( (key, setting) -> {
            if( sb.length() != 0 ) sb.append( ';' );
            sb.append( key );
            sb.append( '=' );
            sb.append( setting );
        } );
        initDataType();
        setDatum();
        datum.at( FIELD_SETTINGS ).setTo( sb.toString() );
    }


    public GetStringSettingsRspMsg( final ByteBuffer _bytes, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _bytes, _context );

        // decode our time...
        initDataType();
        datum = new CompositeDatum( getDataType() );
        datum.set( new BitBuffer( _bytes ) );
        String settingsStr = datum.at( FIELD_SETTINGS ).getAsString();
        Map<String,String> settingsMap = new HashMap<>();
        String[] pairs = settingsStr.split( ";" );
        for( String pair : pairs ) {
            String[] settingParts = pair.split( "=" );
            settingsMap.put( settingParts[0], settingParts[1] );
        }
        settings = Collections.unmodifiableMap( settingsMap );
        setBase();
    }


    private void initDataType() {
        props.add( new CP( FIELD_SETTINGS, DataTypes.ASCIIZ ) );
    }
}
