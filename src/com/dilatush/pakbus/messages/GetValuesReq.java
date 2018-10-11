package com.dilatush.pakbus.messages;

import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.DataType;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.values.CompositeDatum;

import static com.dilatush.pakbus.Protocol.BMP5;
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


    final static private int MSGTYPE = 0x1A;
    final static private DataType TYPE = getType();


    /**
     * Creates a new instance of this type.  This constructor is intended for instances that will be decoded from a BitBuffer
     * (via {@link CompositeDatum#set(BitBuffer)}).  No fields of this datum are set by this constructor.
     */
    public GetValuesReq() {
        super( TYPE, BMP5, MSGTYPE );
    }


    /**
     * Creates a new instance of this type with the given transaction number.  This constructor is intended for instances
     * that will be set via individual property setters.  This constructor will set the "MsgType" and "TranNbr" fields of this datum.
     *
     * @param _transactionNumber the transaction number for this datum
     */
    public GetValuesReq( final int _transactionNumber ) {
        super( TYPE, BMP5, MSGTYPE, _transactionNumber );
    }


    private static DataType getType() {
        return getMessageType( GetValuesReq.class.getSimpleName(), PROPS );
    }
}
