package com.dilatush.pakbus.messages;

import com.dilatush.pakbus.Protocol;
import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.CompositeDataType;
import com.dilatush.pakbus.types.DataType;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.values.CompositeDatum;

import static com.dilatush.pakbus.types.DataTypes.BYTE;

/**
 * Abstract base class for all PakCtl and BMP5 message classes.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
abstract public class Message extends CompositeDatum {


    static private CP[] BASE = {
            new CP( "MsgType",  BYTE   ),
            new CP( "TranNbr",  BYTE   )
    };


    final Protocol protocol;
    final int msgType;


    /**
     * Creates a new instance of this type using the given data type, protocol, and message type.  This constructor is intended for instances that
     * will be decoded from a BitBuffer (via {@link CompositeDatum#set(BitBuffer)}).  No fields of this datum are set by this constructor.
     *
     * @param _type the underlying data type of this datum
     * @param _protocol the protocol this message belongs to (PakCtl or BMP5)
     * @param _msgType the Campbell Scientific message type number for this datum
     */
    public Message( final DataType _type, final Protocol _protocol, final int _msgType ) {
        super( _type );
        protocol = _protocol;
        msgType = _msgType;
    }


    /**
     * Creates a new instance of this type using the given data type, protocol, message type, and transaction number.  This constructor is intended
     * for instances that will be set via individual property setters.  This constructor will set the "MsgType" and "TranNbr" fields of this datum.
     *
     * @param _type the underlying data type of this datum
     * @param _protocol the protocol this message belongs to (PakCtl or BMP5)
     * @param _msgType the Campbell Scientific message type number for this datum
     * @param _transactionNumber the transaction number for this datum
     */
    public Message( final DataType _type, final Protocol _protocol, final int _msgType, final int _transactionNumber ) {
        super( _type );
        protocol = _protocol;
        msgType = _msgType;
        at( "MsgType" ).setTo( _msgType );
        at( "TranNbr" ).setTo( _transactionNumber );
    }


    public Protocol getProtocol() {
        return protocol;
    }


    public int getMsgType() {
        return msgType;
    }


    static protected DataType getMessageType( final String _name, final CP... _cps ) {

        CP[] args = new CP[ _cps.length + BASE.length ];
        System.arraycopy( BASE, 0, args, 0, BASE.length );
        System.arraycopy( _cps, 0, args, BASE.length, _cps.length );
        return new CompositeDataType( _name, null, args );
    }
}
