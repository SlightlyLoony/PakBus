package com.dilatush.pakbus.messages;

import static com.dilatush.pakbus.MessageType.Request;
import static com.dilatush.pakbus.Protocol.INVALID;

/**
 * Represents an "Invalid" message.  Invalid messages are created when decoding a message fails.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class InvalidMsg extends AMsg {

    /**
     * Creates a new instance of this message (for sending) with the given context.
     *
     * @param _msg an explanatory message
     * @param _exception the exception that caused a problem, if there was one
     */
    public InvalidMsg( final String _msg, final Exception _exception ) {
        super( INVALID, -1, Request, null );
    }
}
