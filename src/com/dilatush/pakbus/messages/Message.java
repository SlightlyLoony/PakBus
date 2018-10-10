package com.dilatush.pakbus.messages;

import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.CompositeDataType;
import com.dilatush.pakbus.types.DataType;
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


    public Message( final DataType _type ) {
        super( _type );
    }


    static protected DataType getMessageType( final String _name, final CP... _cps ) {

        CP[] args = new CP[ _cps.length + BASE.length ];
        System.arraycopy( BASE, 0, args, 0, BASE.length );
        System.arraycopy( _cps, 0, args, BASE.length, _cps.length );
        return new CompositeDataType( _name, null, args );
    }
}
