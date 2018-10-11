package com.dilatush.pakbus.messages;

import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.DataType;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.values.CompositeDatum;

import static com.dilatush.pakbus.Protocol.BMP5;
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


    final static private int MSGTYPE = 0x9A;
    final static private DataType TYPE = getType();


    /**
     * Creates a new instance of this type.  This constructor is intended for instances that will be decoded from a BitBuffer
     * (via {@link CompositeDatum#set(BitBuffer)}).  No fields of this datum are set by this constructor.
     */
    public GetValuesRsp() {
        super( TYPE, BMP5, MSGTYPE );
    }


    /**
     * Creates a new instance of this type with the given transaction number.  This constructor is intended for instances
     * that will be set via individual property setters.  This constructor will set the "MsgType" and "TranNbr" fields of this datum.
     *
     * @param _transactionNumber the transaction number for this datum
     */
    public GetValuesRsp( final int _transactionNumber ) {
        super( TYPE, BMP5, MSGTYPE, _transactionNumber );
    }


    private static DataType getType() {
        return getMessageType( GetValuesRsp.class.getSimpleName(), PROPS );
    }
}
