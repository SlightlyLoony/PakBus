package com.dilatush.pakbus;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import static com.dilatush.pakbus.Protocol.BMP5;
import static com.dilatush.pakbus.Protocol.PakCtrl;

/**
 * Represents all possible message types within the PakCtrl and BMP5 protocols.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public enum MessageType {


    DeliveryFailure(       new Key( PakCtrl, 0x81 ) ),
    HelloTrnsReq(          new Key( PakCtrl, 0x09 ) ),
    HelloTrnsRsp(          new Key( PakCtrl, 0x89 ) ),
    HelloReq(              new Key( PakCtrl, 0x0E ) ),
    Bye(                   new Key( PakCtrl, 0x0D ) ),
    GetSettingsReq(        new Key( PakCtrl, 0x07 ) ),
    GetSettingsRsp(        new Key( PakCtrl, 0x87 ) ),
    SetSettingsReq(        new Key( PakCtrl, 0x08 ) ),
    SetSettingsRsp(        new Key( PakCtrl, 0x88 ) ),
    CfgGetSettingsReq(     new Key( PakCtrl, 0x0F ) ),
    CfgGetSettingsRsp(     new Key( PakCtrl, 0x8F ) ),
    CfgSetSettingsReq(     new Key( PakCtrl, 0x10 ) ),
    CfgSetSettingsRsp(     new Key( PakCtrl, 0x90 ) ),
    CfgGetSettingsFragReq( new Key( PakCtrl, 0x11 ) ),
    CfgGetSettingsFragRsp( new Key( PakCtrl, 0x91 ) ),
    CfgSetSettingsFragReq( new Key( PakCtrl, 0x12 ) ),
    CfgSetSettingsFragRsp( new Key( PakCtrl, 0x92 ) ),
    CfgCtrlCmdReq(         new Key( PakCtrl, 0x13 ) ),
    CfgCtrlCmdRsp(         new Key( PakCtrl, 0x93 ) ),
    PleaseWait(            new Key(    BMP5, 0xA1 ) ),
    ClockTransReq(         new Key(    BMP5, 0x17 ) ),
    ClockTransRsp(         new Key(    BMP5, 0x97 ) ),
    FileDownloadReq(       new Key(    BMP5, 0x1c ) ),
    FileDownloadRsp(       new Key(    BMP5, 0x9c ) ),
    FileUploadReq(         new Key(    BMP5, 0x1d ) ),
    FileUploadRsp(         new Key(    BMP5, 0x9d ) ),
    FileCtrlReq(           new Key(    BMP5, 0x1e ) ),
    FileCtrlRsp(           new Key(    BMP5, 0x9e ) ),
    GetProgStatsReq(       new Key(    BMP5, 0x18 ) ),
    GetProgStatsRsp(       new Key(    BMP5, 0x98 ) ),
    CollectDataReq(        new Key(    BMP5, 0x09 ) ),
    CollectDataRsp(        new Key(    BMP5, 0x89 ) ),
    TableDefinitionOneWay( new Key(    BMP5, 0x20 ) ),
    DataMsgBodyOneWay(     new Key(    BMP5, 0x14 ) ),
    TableCtrlReq(          new Key(    BMP5, 0x19 ) ),
    TableCtrlRsp(          new Key(    BMP5, 0x99 ) ),
    GetValuesReq(          new Key(    BMP5, 0x1a ) ),
    GetValuesRsp(          new Key(    BMP5, 0x9a ) ),
    SetValuesReq(          new Key(    BMP5, 0x1b ) ),
    SetValuesRsp(          new Key(    BMP5, 0x9b ) ),
    EMPTY(      new Key( Protocol.INVALID, 0xFFFE ) ),
    INVALID(    new Key( Protocol.INVALID, 0xFFFF ) );


    private static Map<Key, MessageType> fromKey;
    private final Key key;


    MessageType( final Key _key ) {
        key = _key;
        init( _key );
    }


    private void init( final Key _key ) {
        if( fromKey == null )
            fromKey = new HashMap<>( 100 );
        fromKey.put( _key, this );
    }


    /**
     * Get an instance of this class representing the given protocol and message type code, or null if there is no such instance.
     *
     * @param _protocol the protocol for the desired instance
     * @param _code the message type code for the desired instance
     * @return the matching instance of this class, or null if no such instance exists
     */
    public MessageType get( final Protocol _protocol, final int _code ) {
        return fromKey.get( new Key( _protocol, _code ) );
    }


    public int getCode() {
        return key.code;
    }


    public Protocol getProtocol() {
        return key.protocol;
    }


    public boolean isValid() {
        return this != INVALID;
    }


    public boolean isEmpty() {
        return this == EMPTY;
    }


    private static class Key {
        private final Protocol protocol;
        private final int code;


        private Key( final Protocol _protocol, final int _code ) {
            protocol = _protocol;
            code = _code;
        }


        @Override
        public boolean equals( final Object _o ) {
            if( this == _o ) return true;
            if( _o == null || getClass() != _o.getClass() ) return false;
            Key key = (Key) _o;
            return code == key.code &&
                    protocol == key.protocol;
        }


        @Override
        public int hashCode() {
            return Objects.hash( protocol, code );
        }
    }
}
