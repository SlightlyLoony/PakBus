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
public class CollectDataReq extends Message {

    // the various types we need...
    private static ArrayDataType FIELDS = new ArrayDataType( "FIELDS", null, UINT2, UINT2 );

    // type variant A, for collect mode 3
    private static CompositeDataType REC_A = new CompositeDataType( "REC_A", null,
            new CP( "TableNbr", UINT2 ),
            new CP( "TableDefSig", UINT2 ),
            new CP( "Fields", FIELDS ) );
    private static ArrayDataType SPEC_A = new ArrayDataType( "SPEC_A", null, REC_A );
    private static CompositeDataType VAR_A = new CompositeDataType( "VAR_A", null,
            new CP( "MsgType",  BYTE   ),
            new CP( "TranNbr",  BYTE   ),
            new CP( "SecurityCode", UINT2 ),
            new CP( "CollectMode",  BYTE  ),
            new CP( "Specs", SPEC_A ) );

    // type variant B, for collect modes 4 and 5
    private static CompositeDataType REC_B = new CompositeDataType( "REC_B", null,
            new CP( "TableNbr", UINT2 ),
            new CP( "TableDefSig", UINT2 ),
            new CP( "P1", UINT4 ),
            new CP( "Fields", FIELDS ) );
    private static ArrayDataType SPEC_B = new ArrayDataType( "SPEC_B", null, REC_B );
    private static CompositeDataType VAR_B = new CompositeDataType( "VAR_B", null,
            new CP( "MsgType",  BYTE   ),
            new CP( "TranNbr",  BYTE   ),
            new CP( "SecurityCode", UINT2 ),
            new CP( "CollectMode",  BYTE  ),
            new CP( "Specs", SPEC_B ) );

    // type variant C, for collect modes 6 and 8
    private static CompositeDataType REC_C = new CompositeDataType( "REC_C", null,
            new CP( "TableNbr", UINT2 ),
            new CP( "TableDefSig", UINT2 ),
            new CP( "P1", UINT4 ),
            new CP( "P2", UINT4 ),
            new CP( "Fields", FIELDS ) );
    private static ArrayDataType SPEC_C = new ArrayDataType( "SPEC_C", null, REC_C );
    private static CompositeDataType VAR_C = new CompositeDataType( "VAR_C", null,
            new CP( "MsgType",  BYTE   ),
            new CP( "TranNbr",  BYTE   ),
            new CP( "SecurityCode", UINT2 ),
            new CP( "CollectMode",  BYTE  ),
            new CP( "Specs", SPEC_C ) );

    // type variant D, for collect mode 7
    private static CompositeDataType REC_D = new CompositeDataType( "REC_D", null,
            new CP( "TableNbr", UINT2 ),
            new CP( "TableDefSig", UINT2 ),
            new CP( "P1", NSEC ),
            new CP( "P2", NSEC ),
            new CP( "Fields", FIELDS ) );
    private static ArrayDataType SPEC_D = new ArrayDataType( "SPEC_D", null, REC_D );
    private static CompositeDataType VAR_D = new CompositeDataType( "VAR_D", null,
            new CP( "MsgType",  BYTE   ),
            new CP( "TranNbr",  BYTE   ),
            new CP( "SecurityCode", UINT2 ),
            new CP( "CollectMode",  BYTE  ),
            new CP( "Specs", SPEC_D ) );

    // initial type variant, when collect mode is unknown...
    private static CP[] PROPS = {
            new CP( "SecurityCode", UINT2 ),
            new CP( "CollectMode",  BYTE  ),
            new CP( "Spec",         BYTES )
    };


    final static private int MSGTYPE = 0x09;
    final static private DataType TYPE = getType();


    /**
     * Creates a new instance of this type.  This constructor is intended for instances that will be decoded from a BitBuffer
     * (via {@link CompositeDatum#set(BitBuffer)}).  No fields of this datum are set by this constructor.
     */
    public CollectDataReq() {
        super( TYPE, BMP5, MSGTYPE );
    }


    /**
     * Creates a new instance of this type with the given transaction number.  This constructor is intended for instances
     * that will be set via individual property setters.  This constructor will set the "MsgType" and "TranNbr" fields of this datum.
     *
     * @param _transactionNumber the transaction number for this datum
     */
    public CollectDataReq( final int _transactionNumber ) {
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

        // sanity check...
        if( _buffer == null )
            throw new IllegalArgumentException( "Required buffer argument is missing" );

        // remember our position so we can re-run this in a moment...
        int oldPosition = _buffer.position();

        // do the initial decoding...
        super.set( _buffer );

        // get our collect mode...
        int collectMode = at( "CollectMode" ).getAsInt();

        // change our underlying type...
        changeTypeTo( getTypeFromCollectMode( collectMode ) );

        // rewind our buffer and decode again, with the new type...
        _buffer.position( oldPosition );
        super.set( _buffer );
    }


    /**
     * Tells this datum to perform any intermediate phase operations required.  Most commonly this method will change its underlying type to
     * accommodate a dependency on a previously set property.
     */
    @Override
    public void phase() {
        changeTypeTo( getTypeFromCollectMode( at( "CollectMode" ).getAsInt() ) );
    }


    private CompositeDataType getTypeFromCollectMode( final int _collectMode ) {
        switch( _collectMode ) {
            case 3: return VAR_A;
            case 4: return VAR_B;
            case 5: return VAR_B;
            case 6: return VAR_C;
            case 7: return VAR_D;
            case 8: return VAR_C;
            default: throw new IllegalStateException( "Collect mode is an invalid value: " + _collectMode );
        }
    }


    private static DataType getType() {
        return getMessageType( CollectDataReq.class.getSimpleName(), PROPS );
    }
}
