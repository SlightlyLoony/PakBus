package com.dilatush.pakbus.messages.bmp5;

import com.dilatush.pakbus.MessageType;
import com.dilatush.pakbus.Protocol;
import com.dilatush.pakbus.comms.Context;
import com.dilatush.pakbus.messages.AMsg;
import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.DataTypes;
import com.dilatush.pakbus.util.BitBuffer;
import com.dilatush.pakbus.util.Checks;
import com.dilatush.pakbus.values.CompositeDatum;

import java.nio.ByteBuffer;

import static com.dilatush.pakbus.MessageType.Request;
import static com.dilatush.pakbus.Protocol.BMP5;

/**
 * Represents a BMP5 "File Control Request" message.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class FileControlReqMsg extends AMsg {


    final static public String FIELD_SECURITY_CODE = "SecurityCode";
    final static public String FIELD_FILE_NAME1    = "FileName1";
    final static public String FIELD_COMMAND       = "Command";
    final static public String FIELD_FILE_NAME2    = "FileName2";

    final public int securityCode;
    final public String fileName1;
    final public int command;
    final public String fileName2;

    final static public Protocol    PROTOCOL = BMP5;
    final static public int         CODE     = 0x1E;
    final static public MessageType TYPE     = Request;


    /**
     * Creates a new instance of this class ready for transmission.  The command must be one of the following values:
     * <ol>
     * <li>Compile and run the file specified by fileName1 and mark it as run on power up.</li>
     * <li>Set the file specified by fileName1 as the program to run on power up. If fileName1
     * is an empty string, the run on power option will be cleared.</li>
     * <li>Make the file specified by fileName1 hidden so that it will not get listed in directories
     * nor will it be retrievable.</li>
     * <li>Delete the file specified by fileName1.</li>
     * <li>Format the device specified by fileName1.</li>
     * <li>Compile and run the file specified by fileName1 and avoid changing data tables.</li>
     * <li>Stop the running program.</li>
     * <li>Stop the running program and delete any associated data.</li>
     * <li>Perform a full memory reset on the datalogger.</li>
     * <li>Compile and run the file specified by fileName1 but do not change the run on power
     * up file.</li>
     * <li>Pause the execution of the currently running program. This will leave the compiled
     * program image in the datalogger memory and all memory allocated for data tables but it
     * will stop the program scans.</li>
     * <li>Resume the execution of the currently running but paused program.</li>
     * <li>Stop the currently running program, delete any associated data, compile and run the file
     * specified by fileName1, and mark it as run on power up.</li>
     * <li>Stop the currently running program, delete any associated data, and compile and run
     * the program specified by fileName1 without affecting the current run on power up
     * program.</li>
     * <li>Move the file specified by fileName1 to the name and file system specified by
     * fileName2.</li>
     * <li>Stop the currently running program, delete its associated data, move the file specified by
     * fileName1 to the file system and name specified by fileName2, compile and run the
     * file specified by fileName2, and set it to run on power up.</li>
     * <li>Stop the currently running program, delete its associated data, move the file specified by
     * fileName1 to the file system and name specified by fileName2, compile and run the
     * file specified by fileName2 but leave the run on power up alone.</li>
     * <li>>Copy the file specified by fileName1 to the file system and name specified by
     * fileName1.</li
     * <li>Copy the file specified by fileName1 to fileName2, stop the currently running
     * program, delete any associated data, compile and run the file specified by fileName2,
     * and mark it to run on power up.</li>
     * <li>Copy the file specified by fileName1 to fileName2, stop the currently running
     * program, delete any associated data, compile and run the file specified by fileName2
     * but leave the run on power up alone.</li>
     * <li>Compile the program specified by fileName1 but leave it in a paused state and then
     * set the filled flag on all final storage tables. This option is a prelude to an operation that
     * would recover data from the datalogger after the program had been accidentally stopped.</li>
     * <li>Load the file specified by fileName1 as a new operating system image. If possible leave
     * the currently running program and settings untouched.</li>
     * </ol>
     *
     * @param _securityCode
     * @param _fileName1
     * @param _command
     * @param _fileName2
     * @param _context
     */
    public FileControlReqMsg( final int _securityCode, final String _fileName1, final int _command, final String _fileName2, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _fileName1, _context );
        boolean needsFileName2 =  Checks.isOneOf( _command, 15, 16, 17, 18, 19, 20 );
        if( needsFileName2 )
            Checks.required( _fileName2 );

        // save our parameters...
        securityCode = _securityCode;
        fileName1     = _fileName1;
        command       = _command;
        fileName2     = _fileName2;

        // create and initialize our datum...
        initDataType();
        setDatum();
        datum.at( FIELD_SECURITY_CODE ).setTo( securityCode );
        datum.at( FIELD_FILE_NAME1    ).setTo( fileName1    );
        datum.at( FIELD_COMMAND       ).setTo( command      );
        if( needsFileName2 )
            datum.at( FIELD_FILE_NAME2    ).setTo( fileName2    );
    }


    public FileControlReqMsg( final ByteBuffer _bytes, final Context _context ) {
        super( PROTOCOL, CODE, TYPE, _context );

        // sanity check...
        Checks.required( _bytes, _context );

        // decode our time...
        initDataType();
        datum = new CompositeDatum( getDataType() );
        datum.set( new BitBuffer( _bytes ) );
        securityCode = datum.at( FIELD_SECURITY_CODE ).getAsInt();
        fileName1    = datum.at( FIELD_FILE_NAME1    ).getAsString();
        command      = datum.at( FIELD_COMMAND       ).getAsInt();
        if( Checks.isOneOf( command, 15, 16, 17, 18, 19, 20 ) )
            fileName2    = datum.at( FIELD_FILE_NAME2    ).getAsString();
        else
            fileName2    = null;
    }


    private void initDataType() {
        props.add( new CP( FIELD_SECURITY_CODE, DataTypes.UINT2  ) );
        props.add( new CP( FIELD_FILE_NAME1,    DataTypes.ASCIIZ ) );
        props.add( new CP( FIELD_COMMAND,       DataTypes.BYTE   ) );
        props.add( new CP( FIELD_FILE_NAME2,    DataTypes.ASCIIZ ) );
    }
}
