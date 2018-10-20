package com.dilatush.pakbus.shims;

import com.dilatush.pakbus.comms.Signature;

import java.util.*;

/**
 * Instances of this class encapsulate any number of table numbers and associated field numbers for a data collection query.  Instances of this
 * class are mutable and <i>not</i> threadsafe until marked read only by finishing.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class DataQuery {

    private boolean readOnly;
    final private List<Table> tables;


    /**
     * Creates a new instance of this class, ready to be populated with the tables and fields for a query.
     */
    public DataQuery() {

        // some setup...
        tables = new ArrayList<>();
    }


    /**
     * Adds a new table to this query.  If the table has already been added, this does nothing.  Otherwise, the new table is added.  Note that tables
     * can be added in any order, as calling {@link #finish()} will sort them.
     *
     * @param _tableNumber the index number of the table to add
     * @param _signature the signature of the table being added
     */
    public void addTable( final int _tableNumber, final Signature _signature ) {

        // if we're read-only, don't allow this...
        if( readOnly )
            throw new IllegalStateException( "Attempted to add table after finishing" );

        // if the table has already been added, ignore this call...
        if( getTable( _tableNumber ) != null )
            return;

        // ok, it really needs to be added...
        tables.add( new Table( _tableNumber, _signature ) );
    }


    /**
     * Adds the given field number to the query for the given table number.  The table number must have already been added to this instance.  Note
     * that fields can be added in any order, as calling {@link #finish()} will sort them.  If the given field number has already been added to the
     * given table, this method does nothing.
     *
     * @param _tableNumber the table to add a field to
     * @param _fieldNumber the field to add
     */
    public void addField( final int _tableNumber, final int _fieldNumber ) {

        // if we're read-only, don't allow this...
        if( readOnly )
            throw new IllegalStateException( "Attempted to add field after finishing" );

        // get our table record...
        Table table = getTable( _tableNumber );
        if( table == null )
            throw new IllegalArgumentException( "Table has not been added to this instance yet: " + _tableNumber );

        // if the field has already been added, ignore this call...
        for( int fn : table.fields ) {
            if( fn == _fieldNumber )
                return;
        }

        // add the field...
        table.fields.add( _fieldNumber );
    }


    public TableIterator iterator() {
        return new TableIterator();
    }


    public class TableIterator implements Iterator {

        private int tableIndex;


        public TableIterator() {
            tableIndex = -1;
        }

        /**
         * Returns {@code true} if the iteration has more elements. (In other words, returns {@code true} if {@link #next} would return an element
         * rather than throwing an exception.)
         *
         * @return {@code true} if the iteration has more elements
         */
        @Override
        public boolean hasNext() {
            return (1 + tableIndex) < tables.size();
        }


        /**
         * Advances to the next table in the iteration, and returns null.  Use {@link #getTableNumber()}, {@link #getTableSignature()} and
         * {@link #iterator()} to get information about a table and its fields.
         *
         * @return the next element in the iteration
         * @throws NoSuchElementException if the iteration has no more elements
         */
        @Override
        public Object next() {
            tableIndex++;
            return null;
        }


        public int getTableNumber() {
            return tables.get( tableIndex ).tableNumber;
        }


        public Signature getTableSignature() {
            return tables.get( tableIndex ).signature;
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
                return (1 + fieldIndex) < tables.get( tableIndex ).fields.size();
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
                return tables.get( tableIndex ).fields.get( fieldIndex );
            }
        }
    }


    /**
     * Finishes the query, making it read-only and preparing it for use in a {@link com.dilatush.pakbus.messages.bmp5.CollectDataReqMsg}.
     */
    public void finish() {

        // mark us read-only to prevent mucking things up...
        readOnly = true;

        // sort our tables...
        tables.sort( new Comparator<Table>() {
            @Override
            public int compare( final Table t1, final Table t2 ) {
                return t1.tableNumber - t2.tableNumber;
            }
        } );

        // then sort the field list within each table...
        tables.forEach( (table) -> {
            table.fields.sort( new Comparator<Integer>() {
                @Override
                public int compare( final Integer i1, final Integer i2 ) {
                    return i1.compareTo( i2 );
                }
            } );
        } );
    }


    /**
     * Return the table record with the given table number, or null if none.
     *
     * @param _tableNumber the table number to get
     * @return the table instance, or null if none
     */
    private Table getTable( final int _tableNumber ) {
        for( Table table : tables ) {
            if( table.tableNumber == _tableNumber ) return table;
        }
        return null;
    }


    final static class Table {
        final private int tableNumber;
        final private Signature signature;
        final List<Integer> fields;


        private Table( final int _tableNumber, final Signature _signature ) {
            tableNumber = _tableNumber;
            signature = _signature;
            fields = new ArrayList<>();
        }
    }
}
