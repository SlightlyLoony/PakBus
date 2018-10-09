package com.dilatush.pakbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.dilatush.pakbus.types.PakBusType.*;
import static com.dilatush.pakbus.types.PakBusType.Byte;

/**
 * A singleton that constructs and caches message definitions for all PakCtrl and BMP5 message types.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MessageDefinitions {

    public final static MessageDefinitions INSTANCE = new MessageDefinitions();

    private final Map<MessageType, MessageDefinition > fromType;


    /**
     * Creates a new instance of this class to populate the singleton instance.
     */
    private MessageDefinitions() {

        // initialize our map...
        fromType = new HashMap<>( 100 );

        // build all the message definitions...
        makeDeliveryFailureDef();
        makeHelloTrnsReq();
        makeHelloTrnsRsp();
        makeHelloReq();
        makeGetSettingsReqDef();

    }


    /**
     * Return the message definition for the given message type, or null if no such type exists.
     *
     * @param _type the message type to get a definition for
     * @return the message definition for the given message type, or null if no such type exists
     */
    public MessageDefinition getDefinition( final MessageType _type ) {
        return fromType.get( _type );
    }


    private void makeDeliveryFailureDef() {
        MessageType type = MessageType.DeliveryFailure;
        List<MessageFieldDefinition> fields = new ArrayList<>( 6 );
        fields.add( new MessageFieldDefinition( "ErrCode",     Byte,  0 ) );
        fields.add( new MessageFieldDefinition( "HiProtoCode", Bits4, 0 ) );
        fields.add( new MessageFieldDefinition( "DstPBAddr",   Bits12,0 ) );
        fields.add( new MessageFieldDefinition( "HopCnt",      Bits4, 0 ) );
        fields.add( new MessageFieldDefinition( "SrcPBAddr",   Bits12,0 ) );
        fields.add( new MessageFieldDefinition( "MsgData",     Bytes, 0 ) );
        MessageDefinition def = new MessageDefinition( type, fields );
        fromType.put( type, def );
    }


    private void makeHelloTrnsReq() {
        MessageType type = MessageType.HelloTrnsReq;
        List<MessageFieldDefinition> fields = new ArrayList<>( 6 );
        fields.add( new MessageFieldDefinition( "IsRouter",    Bool, 0 ) );
        fields.add( new MessageFieldDefinition( "HopMetric",   Byte, 0 ) );
        fields.add( new MessageFieldDefinition( "VerifyIntv",  UInt2,0 ) );
        MessageDefinition def = new MessageDefinition( type, fields );
        fromType.put( type, def );
    }


    private void makeHelloTrnsRsp() {
        MessageType type = MessageType.HelloTrnsRsp;
        List<MessageFieldDefinition> fields = new ArrayList<>( 6 );
        fields.add( new MessageFieldDefinition( "IsRouter",    Bool, 0 ) );
        fields.add( new MessageFieldDefinition( "HopMetric",   Byte, 0 ) );
        fields.add( new MessageFieldDefinition( "VerifyIntv",  UInt2,0 ) );
        MessageDefinition def = new MessageDefinition( type, fields );
        fromType.put( type, def );
    }


    private void makeHelloReq() {
        MessageType type = MessageType.HelloReq;
        List<MessageFieldDefinition> fields = new ArrayList<>( 6 );
        MessageDefinition def = new MessageDefinition( type, fields );
        fromType.put( type, def );
    }


    private void makeGetSettingsReqDef() {
        MessageType type = MessageType.GetSettingsReq;
        List<MessageFieldDefinition> fields = new ArrayList<>( 1 );
        fields.add( new MessageFieldDefinition( "NameList",    ASCIIZ,  0 ) );
        MessageDefinition def = new MessageDefinition( type, fields );
        fromType.put( type, def );
    }
}
