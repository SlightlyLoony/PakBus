package com.dilatush.pakbus.messages;

import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.DataType;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.values.CompositeDatum;

import static com.dilatush.pakbus.Protocol.PakCtrl;
import static com.dilatush.pakbus.types.DataTypes.ASCIIZ;

/**
 * Instances of this class represent a get string settings request message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class GetStringSettingsReq extends Message {


    private static CP[] PROPS = {
            new CP( "NameList", ASCIIZ )
    };


    final static private int MSGTYPE = 0x07;
    final static private DataType TYPE = getType();


    /**
     * Creates a new instance of this type.  This constructor is intended for instances that will be decoded from a BitBuffer
     * (via {@link CompositeDatum#set(BitBuffer)}).  No fields of this datum are set by this constructor.
     */
    public GetStringSettingsReq() {
        super( TYPE, PakCtrl, MSGTYPE );
    }


    /**
     * Creates a new instance of this type with the given transaction number.  This constructor is intended for instances
     * that will be set via individual property setters.  This constructor will set the "MsgType" and "TranNbr" fields of this datum.
     *
     * @param _transactionNumber the transaction number for this datum
     */
    public GetStringSettingsReq( final int _transactionNumber ) {
        super( TYPE, PakCtrl, MSGTYPE, _transactionNumber );
    }


    private static DataType getType() {
        return getMessageType( GetStringSettingsReq.class.getSimpleName(), PROPS );
    }
}
