package com.dilatush.pakbus.messages.serpkt;

import com.dilatush.pakbus.comms.Context;
import com.dilatush.pakbus.comms.Packet;
import com.dilatush.pakbus.messages.AMsg;
import com.dilatush.pakbus.types.ExpectMore;
import com.dilatush.pakbus.types.LinkState;
import com.dilatush.pakbus.types.Priority;
import com.dilatush.pakbus.values.PacketOptions;

import static com.dilatush.pakbus.types.LinkState.Ring;
import static com.dilatush.pakbus.types.MessageType.Request;
import static com.dilatush.pakbus.types.Protocol.SerPkt;

/**
 * Represents a SerPkt "Ring" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class RingMsg extends AMsg {

    /**
     * Creates a new instance of this message (for sending) with the given context.
     *
     * @param _context the communications context to use when creating this message
     */
    public RingMsg( final Context _context ) {
        super( SerPkt, Ring.getCode(), Request, _context );
    }


    /**
     * Encodes this message into a new instance of {@link Packet} that is returned, using the given packet options.  This method is overridden to
     * force the packet options to those appropriate for a ring message.
     *
     * @param _options the packet options to use in the encoded packet
     * @return the packet encoded from this message
     */
    @Override
    public Packet encode( final PacketOptions _options ) {
        return super.encode( new PacketOptions( LinkState.Ring, ExpectMore.Neutral, Priority.Normal ));
    }
}
