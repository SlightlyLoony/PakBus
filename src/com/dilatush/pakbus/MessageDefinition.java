package com.dilatush.pakbus;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Instances of this class define a particular PakCtrl or BMP5 message.  This definition includes the layout of the binary packet, field locations,
 * encoded type, and mapped Java type.  Instances of this class are immutable and threadsafe.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class MessageDefinition {

    private final MessageType                         type;      // type of message defined in this instance...
    private final List<MessageFieldDefinition>        fields;    // ordered list of message field definitions for this message type...
    private final Map<String, MessageFieldDefinition> fromName;  // map from field name to field definition...


    /**
     * Create a new instance of this class with the given message type and ordered field list.  Note that two fields (MsgType and TranNbr) are
     * automatically prepended to the list, as they exist in every message type.
     *
     * @param _type the message type of the definition to create
     * @param _fields the fields (other than MsgType and TranNbr) contained in the message
     */
    public MessageDefinition( final MessageType _type, final List<MessageFieldDefinition> _fields ) {

        // sanity checks...
        if( (_type == null) || (_fields == null) )
            throw new IllegalArgumentException( "Required arguments missing" );

        type = _type;

        // if our type is INVALID or EMPTY, there are no fields, so just make an empty list and map and skedaddle...
        if( _type.isEmpty() || !_type.isValid() ) {
            fields = new ArrayList<>( 0 );
            fromName = new HashMap<>( 0 );
            return;
        }

        // otherwise, make our field list...
        // note that it is NOT in the list passed as an argument, so it's safe from external manipulation...
        fields = new ArrayList<>( _fields.size() + 2 );
        fromName = new HashMap<>( 2 * fields.size() );

        // add our two common fields (MsgType and TranNbr)...
//        addField( new MessageFieldDefinition( "MsgType", Byte, false ) );
//        addField( new MessageFieldDefinition( "TranNbr", Byte, false ) );

        // now add the given fields...
        _fields.forEach( this::addField );
    }


    public int size() {
        return fields.size();
    }


    public MessageFieldDefinition get( final int _index ) {
        return fields.get( _index );
    }


    public MessageFieldDefinition get( final String _name ) {
        return fromName.get( _name );
    }


    /**
     * Adds the given field definition to the list of field definitions and to the map of field names to field definitions.
     *
     * @param _def the definition of the field
     */
    private void addField( final MessageFieldDefinition _def ) {
        fields.add( _def );
        fromName.put( _def.getName(), _def );
    }


    public MessageType getType() {
        return type;
    }
}
