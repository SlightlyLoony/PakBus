package com.dilatush.pakbus.messages.bmp5;

import com.dilatush.pakbus.types.MessageType;
import com.dilatush.pakbus.types.Protocol;
import com.dilatush.pakbus.comms.Context;
import com.dilatush.pakbus.messages.AMsg;
import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.DataTypes;
import com.dilatush.pakbus.types.PakBusType;
import com.dilatush.pakbus.util.Checks;

import static com.dilatush.pakbus.types.MessageType.Request;
import static com.dilatush.pakbus.types.Protocol.BMP5;

/**
 * Represents a BMP5 "Get Values Request" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class GetValuesReqMsg extends AMsg {


    final static public String FIELD_SECURITY_CODE = "SecurityCode";
    final static public String FIELD_TABLE_NAME    = "TableName";
    final static public String FIELD_FIELD_NAME    = "FieldName";
    final static public String FIELD_FIELD_TYPE    = "FieldType";
    final static public String FIELD_SWATH         = "Swath";

    final public int securityCode;
    final public String tableName;
    final public String fieldName;
    final public PakBusType fieldType;
    final public int swath;

    final static public Protocol    PROTOCOL = BMP5;
    final static public int         CODE     = 0x1A;
    final static public MessageType TYPE     = Request;


    public GetValuesReqMsg( final int _securityCode, final String _tableName, final String _fieldName, final PakBusType _fieldType, final int _swath,
                            final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _tableName, _fieldName, _fieldType, _context );

        // save our parameters...
        securityCode = _securityCode;
        tableName    = _tableName;
        fieldName    = _fieldName;
        fieldType    = _fieldType;
        swath        = _swath;

        // create and initialize our datum...
        props.add( new CP( FIELD_SECURITY_CODE, DataTypes.UINT2  ) );
        props.add( new CP( FIELD_TABLE_NAME,    DataTypes.ASCIIZ ) );
        props.add( new CP( FIELD_FIELD_TYPE,    DataTypes.BYTE   ) );
        props.add( new CP( FIELD_FIELD_NAME,    DataTypes.ASCIIZ ) );
        props.add( new CP( FIELD_SWATH,         DataTypes.UINT2  ) );
        setDatum();
        datum.at( FIELD_SECURITY_CODE ).setTo( securityCode        );
        datum.at( FIELD_TABLE_NAME    ).setTo( tableName           );
        datum.at( FIELD_FIELD_TYPE    ).setTo( fieldType.getCode() );
        datum.at( FIELD_FIELD_NAME    ).setTo( fieldName           );
        datum.at( FIELD_SWATH         ).setTo( swath               );
    }
}
