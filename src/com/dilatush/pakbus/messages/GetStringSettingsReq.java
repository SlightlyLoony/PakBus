package com.dilatush.pakbus.messages;

import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.DataType;

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


    final static private DataType TYPE = getType();


    public GetStringSettingsReq() {
        super( TYPE );
    }


    private static DataType getType() {
        return getMessageType( GetStringSettingsReq.class.getSimpleName(), PROPS );
    }
}
