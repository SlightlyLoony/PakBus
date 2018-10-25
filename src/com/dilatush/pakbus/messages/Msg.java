package com.dilatush.pakbus.messages;

import com.dilatush.pakbus.comms.Context;
import com.dilatush.pakbus.comms.Packet;
import com.dilatush.pakbus.types.MessageType;
import com.dilatush.pakbus.types.Protocol;
import com.dilatush.pakbus.values.PacketOptions;

import java.nio.ByteBuffer;

/**
 * Implemented by all classes that represent a PakBus message.  Note that all instances of classes implementing {@link Msg} are mutable and <i>not</i>
 * threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public interface Msg {


    /**
     * Returns the communications context associated with this message.
     *
     * @return the communications context associated with this message
     */
    Context context();


    /**
     * Returns the protocol of this message.
     *
     * @return the protocol of this message
     */
    Protocol protocol();


    /**
     * Returns the type of this message.
     *
     * @return the type of this message
     */
    MessageType type();


    /**
     * Encodes this message into a new instance of {@link Packet} that is returned.  The default packet options ("Ready" link state, "ExpectMore"
     * expect more code, and "Normal" priority will be used.
     *
     * @return the packet encoded from this message
     */
    Packet encode();


    /**
     * Encodes this message into a new instance of {@link Packet} that is returned, using the given packet options.
     *
     * @param _options the packet options to use in the encoded packet
     * @return the packet encoded from this message
     */
    Packet encode( final PacketOptions _options );


    /**
     * Returns the encoded bytes of this message, if it is a PakCtrl or BMP5 message.
     *
     * @return the encoded bytes of this message
     */
    ByteBuffer bytes();


    /**
     * Return the packet this message was decoded from.
     *
     * @return the packet this message was decoded from
     */
    Packet getPacket();


    /**
     * Returns the transaction number for this message.
     *
     * @return the transaction number for this message
     */
    int transactionNumber();
}
