package com.dilatush.pakbus.messages;

import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.DataType;

import static com.dilatush.pakbus.types.DataTypes.ASCIIZ;
import static com.dilatush.pakbus.types.DataTypes.BYTE;
import static com.dilatush.pakbus.types.DataTypes.UINT2;

/**
 * Instances of this class represent a get values request message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class GetValuesReq extends Message {


    private static CP[] PROPS = {
            new CP( "SecurityCode", UINT2  ),
            new CP( "TableName",    ASCIIZ ),
            new CP( "TypeCode",     BYTE   ),
            new CP( "FieldName",    ASCIIZ ),
            new CP( "Swath",        UINT2  ),

    };


    final static private DataType TYPE = getType();


    public GetValuesReq() {
        super( TYPE );
    }


    private static DataType getType() {
        return getMessageType( GetValuesReq.class.getSimpleName(), PROPS );
    }
}
