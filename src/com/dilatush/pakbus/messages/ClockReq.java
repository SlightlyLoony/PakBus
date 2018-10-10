package com.dilatush.pakbus.messages;

import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.DataType;

import static com.dilatush.pakbus.types.DataTypes.NSEC;
import static com.dilatush.pakbus.types.DataTypes.UINT2;

/**
 * Instances of this class represent the clock request message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ClockReq extends Message {


    private static CP[] PROPS = {
            new CP( "SecurityCode", UINT2 ),
            new CP( "Adjustment",   NSEC  )
    };


    final static private DataType TYPE = getType();


    public ClockReq() {
        super( TYPE );
    }


    private static DataType getType() {
        return getMessageType( ClockReq.class.getSimpleName(), PROPS );
    }
}
