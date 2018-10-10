package com.dilatush.pakbus.messages;

import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.DataType;

import static com.dilatush.pakbus.types.DataTypes.BYTE;
import static com.dilatush.pakbus.types.DataTypes.NSEC;

/**
 * Instances of this class represent a clock response message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ClockRsp extends Message {

    private static CP[] PROPS = {
            new CP( "RespCode", BYTE ),
            new CP( "OldTime",  NSEC, true  )
    };


    final static private DataType TYPE = getType();


    public ClockRsp() {
        super( TYPE );
    }


    private static DataType getType() {
        return getMessageType( ClockRsp.class.getSimpleName(), PROPS );
    }
}
