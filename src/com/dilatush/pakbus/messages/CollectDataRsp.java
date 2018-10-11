package com.dilatush.pakbus.messages;

import com.dilatush.pakbus.types.ArrayDataType;
import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.CompositeDataType;
import com.dilatush.pakbus.types.DataType;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.values.CompositeDatum;

import static com.dilatush.pakbus.Protocol.BMP5;
import static com.dilatush.pakbus.types.DataTypes.*;

/**
 * Instances of this class represent a collect data request message.  We do some tricky things here to change the type according to the collection
 * mode.  There is one operational difference: if initializing an instance via property setting (rather than decoding), then phase() must be called
 * after setting the "CollectMode" property.  This will change the type to one appropriate for that collect mode.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class CollectDataRsp extends Message {

    // a table record...
    private static CompositeDataType TBLRCDS = new CompositeDataType( "TBLRCDS", null,
            new CP( "TableNbr", UINT2 ),
            new CP( "BegRecNbr", UINT2 )
            );

    // array of table records...
    private static ArrayDataType RECTBL = new ArrayDataType( "RECTBL", null, TBLRCDS );

    // the type after phase processing...
    private static CompositeDataType POST = new CompositeDataType( "POST", null,
            new CP( "MsgType",  BYTE   ),
            new CP( "TranNbr",  BYTE   ),
            new CP( "RespCode", BYTE   ),
            new CP( "Tables",   RECTBL ) );

    // initial type variant, when collect mode is unknown...
    private static CP[] PROPS = {
            new CP( "RespCode",  BYTE  ),
            new CP( "Record",    BYTES )
    };


    final static private int MSGTYPE = 0x89;
    final static private DataType TYPE = getType();


    /**
     * Creates a new instance of this type.  This constructor is intended for instances that will be decoded from a BitBuffer
     * (via {@link CompositeDatum#set(BitBuffer)}).  No fields of this datum are set by this constructor.
     */
    public CollectDataRsp() {
        super( TYPE, BMP5, MSGTYPE );
    }


    /**
     * Creates a new instance of this type with the given transaction number.  This constructor is intended for instances
     * that will be set via individual property setters.  This constructor will set the "MsgType" and "TranNbr" fields of this datum.
     *
     * @param _transactionNumber the transaction number for this datum
     */
    public CollectDataRsp( final int _transactionNumber ) {
        super( TYPE, BMP5, MSGTYPE, _transactionNumber );
    }


    /**
     * Sets this datum's value from the bits in the given buffer.  Upon invocation, the given buffer's position must be at the first bit of this
     * datum's value, and the limit must be at the bit following the last bit that is available.  This method will read as many bits from the buffer
     * as are required to set this datum's value, leaving the position at the first bit following the bits read.  The given buffer's limit is not
     * changed by this method.  After the initial value has been set, we read the collect mode, determine the correct type for this message, change to
     * that type, and then re-set its value.
     *
     * @param _buffer the buffer containing the bits from which this datum's value will be read
     */
    @Override
    public void set( final BitBuffer _buffer ) {
    }


    /**
     * Tells this datum to perform any intermediate phase operations required.  Most commonly this method will change its underlying type to
     * accommodate a dependency on a previously set property.
     */
    @Override
    public void phase() {

    }


    private static DataType getType() {
        return getMessageType( CollectDataRsp.class.getSimpleName(), PROPS );
    }
}
