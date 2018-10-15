package com.dilatush.pakbus.messages;

import com.dilatush.pakbus.MessageType;
import com.dilatush.pakbus.Packet;
import com.dilatush.pakbus.PacketOptions;
import com.dilatush.pakbus.Protocol;
import com.dilatush.pakbus.comms.Context;
import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.CompositeDataType;
import com.dilatush.pakbus.values.CompositeDatum;

import java.nio.ByteBuffer;
import java.util.ArrayList;
import java.util.List;

import static com.dilatush.pakbus.Protocol.BMP5;
import static com.dilatush.pakbus.Protocol.PakCtrl;
import static com.dilatush.pakbus.types.DataTypes.BYTE;

/**
 * Abstract base class for all classes that represent PakBus messages.  Note that all instances of classes implementing {@link Msg} are mutable and
 * <i>not</i> threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
abstract public class AMsg implements Msg {

    final protected Protocol    protocol;   // the protocol used for this message
    final protected MessageType type;       // the type of this message
    final protected int         code;       // the PakBus message type code (for BMP5 and PakCtrl protocols) or the LinkState code (for SerPkt)
    final protected Context     context;    // the communications context associated with this message

    protected CompositeDatum    datum;      // the datum for the message contents, if any
    protected List<CP>          props;      // the ordered list of datum properties for PakCtrl and BMP5 messages
    protected Packet            packet;     // the packet this message was decoded from (for received messages)


    /**
     * Creates a new instance of this class using the given parameters.
     *
     * @param _protocol the protocol for this message
     * @param _code the PakBus message type code
     * @param _type the message type for this message
     * @param _context the communications context for this message
     */
    public AMsg( final Protocol _protocol, final int _code, final MessageType _type, final Context _context ) {

        protocol = _protocol;
        type     = _type;
        code     = _code;
        context  = _context;
        datum    = null;

        // set up our property list if this is a PakCtrl or BMP5 message...
        if( (protocol == BMP5) || (protocol == PakCtrl) ) {
            props = new ArrayList<CP>();
            props.add( new CP( "MsgType", BYTE ) );
            props.add( new CP( "TranNbr", BYTE ) );
        }
    }


    /**
     * Creates the datum for this message.
     */
    protected void setDatum() {
        datum = new CompositeDatum( getDataType() );
        datum.at( "MsgType" ).setTo( code );
        datum.at( "TranNbr" ).setTo( context.transactionNumber() );
    }


    /**
     * Return the data type for this message.
     *
     * @return the data type for this message
     */
    protected CompositeDataType getDataType() {
        return new CompositeDataType( getClass().getSimpleName(), null, props );
    }


    /**
     * Encodes this message into a new instance of {@link Packet} that is returned.  The default packet options ("Ready" link state, "ExpectMore"
     * expect more code, and "Normal" priority will be used.
     *
     * @return the packet encoded from this message
     */
    @Override
    public Packet encode() {
        return encode( new PacketOptions() );
    }


    /**
     * Encodes this message into a new instance of {@link Packet} that is returned, using the given packet options.
     *
     * @param _options the packet options to use in the encoded packet
     * @return the packet encoded from this message
     */
    @Override
    public Packet encode( final PacketOptions _options ) {
        return new Packet( this, _options );
    }


    /**
     * Returns the encoded bytes of this message, if it is a PakCtrl or BMP5 message.
     *
     * @return the encoded bytes of this message
     */
    @Override
    public ByteBuffer bytes() {

        // sanity check...
        if( (protocol != BMP5) && (protocol != PakCtrl) )
            throw new IllegalStateException( "Tried to get bytes for a message that wasn't BMP5 or PakCtrl" );

        // if we haven't finished our datum, do so now...
        if( !datum.isSet() )
            datum.finish();

        // now get our bytes...
        return datum.getAsByteBuffer();
    }


    /**
     * Returns the communications context associated with this message.
     *
     * @return the communications context associated with this message
     */
    @Override
    public Context context() {
        return context;
    }


    /**
     * Returns the protocol of this message.
     *
     * @return the protocol of this message
     */
    @Override
    public Protocol protocol() {
        return protocol;
    }


    /**
     * Returns the type of this message.
     *
     * @return the type of this message
     */
    @Override
    public MessageType type() {
        return type;
    }


    public Packet getPacket() {
        return packet;
    }


    public void setPacket( final Packet _packet ) {
        packet = _packet;
    }
}
