package com.dilatush.pakbus.shims;

import com.dilatush.pakbus.comms.Signature;

import java.util.*;

/**
 * Instances of this class encapsulate a table number and associated field numbers for a data collection query.  Instances of this
 * class are mutable and <i>not</i> threadsafe until marked read only by finishing.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class DataQuery {

    final public int tableIndex;
    final public Signature signature;
    final private List<Integer> fields;

    private boolean readOnly;


    /**
     * Creates a new instance of this class for the given table, ready to be populated with the fields for a query.
     */
    public DataQuery( final int _tableIndex, final Signature _signature) {

        // some setup...
        tableIndex = _tableIndex;
        signature = _signature;
        fields = new ArrayList<>();
    }


    /**
     * Adds the given field number to this query.  Note that fields can be added in any order, as calling {@link #finish()} will sort them.  If the
     * given field number has already been added, this method does nothing.
     *
     * @param _fieldNumber the field to add
     */
    public void addField( final int _fieldNumber ) {

        // if we're read-only, don't allow this...
        if( readOnly )
            throw new IllegalStateException( "Attempted to add field after finishing" );

        // if the field has already been added, ignore this call...
        for( int fn : fields ) {
            if( fn == _fieldNumber )
                return;
        }

        // add the field...
        fields.add( _fieldNumber );
    }


    public int fieldsSize() {
        return fields.size();
    }


    public FieldIterator iterator() {
        return new FieldIterator();
    }


    public class FieldIterator implements Iterator {

        private int fieldIndex;


        private FieldIterator() {
            fieldIndex = -1;
        }

        /**
         * Returns {@code true} if the iteration has more elements. (In other words, returns {@code true} if {@link #next} would return an element
         * rather than throwing an exception.)
         *
         * @return {@code true} if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            return (1 + fieldIndex) < fields.size();
        }


        /**
         * Returns the next element in the iteration.
         *
         * @return the next element in the iteration
         * @throws NoSuchElementException if the iteration has no more elements
         */
        @Override
        public Object next() {
            fieldIndex++;
            return fields.get( fieldIndex );
        }
    }


    /**
     * Finishes the query, making it read-only and preparing it for use in a {@link com.dilatush.pakbus.messages.bmp5.CollectDataReqMsg}.
     */
    public void finish() {

        // mark us read-only to prevent mucking things up...
        readOnly = true;

        // sort the field list...
        fields.sort( new Comparator<Integer>() {
            @Override
            public int compare( final Integer i1, final Integer i2 ) {
                return i1.compareTo( i2 );
            }
        } );
    }
}
