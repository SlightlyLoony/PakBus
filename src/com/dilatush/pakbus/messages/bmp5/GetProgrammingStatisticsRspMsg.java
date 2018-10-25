package com.dilatush.pakbus.messages.bmp5;

import com.dilatush.pakbus.types.MessageType;
import com.dilatush.pakbus.types.Protocol;
import com.dilatush.pakbus.comms.Context;
import com.dilatush.pakbus.messages.AMsg;
import com.dilatush.pakbus.shims.ProgrammingStatistics;
import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.DataTypes;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.util.Checks;
import com.dilatush.pakbus.values.CompositeDatum;

import java.nio.ByteBuffer;

import static com.dilatush.pakbus.types.MessageType.Response;
import static com.dilatush.pakbus.types.Protocol.BMP5;

/**
 * Represents a BMP5 "Collect Data Response" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class GetProgrammingStatisticsRspMsg extends AMsg {


    final static public String FIELD_RESPONSE_CODE = "RespCode";

    final public ResponseCode          responseCode;
    final public ProgrammingStatistics programmingStatistics;

    final static public Protocol    PROTOCOL = BMP5;
    final static public int         CODE     = 0x89;
    final static public MessageType TYPE     = Response;


    public GetProgrammingStatisticsRspMsg( final ByteBuffer _bytes, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _bytes, _context );

        // decode our time...
        initDataType();
        datum = new CompositeDatum( getDataType() );
        datum.set( new BitBuffer( _bytes ) );
        responseCode = ResponseCode.decode( datum.at( FIELD_RESPONSE_CODE ).getAsInt() );
        programmingStatistics = new ProgrammingStatistics( datum );
        setBase();
    }


    private void initDataType() {
        props.add( new CP( FIELD_RESPONSE_CODE, DataTypes.BYTE ) );
        props.add( new CP( ProgrammingStatistics.FIELD_TYPE0, ProgrammingStatistics.TYPE0, true ) );
    }
}
