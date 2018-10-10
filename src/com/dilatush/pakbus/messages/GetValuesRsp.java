package com.dilatush.pakbus.messages;

import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.DataType;

import static com.dilatush.pakbus.types.DataTypes.*;

/**
 * Instances of this class represent a get values response message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class GetValuesRsp extends Message {


    private static CP[] PROPS = {
            new CP( "RespCode", BYTE                ),
            new CP( "Values",   BYTES, true )
    };


    final static private DataType TYPE = getType();


    public GetValuesRsp() {
        super( TYPE );
    }


    private static DataType getType() {
        return getMessageType( GetValuesRsp.class.getSimpleName(), PROPS );
    }
}
