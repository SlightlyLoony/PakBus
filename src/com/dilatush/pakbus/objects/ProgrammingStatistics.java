package com.dilatush.pakbus.objects;

import com.dilatush.pakbus.NSec;
import com.dilatush.pakbus.types.CP;
import com.dilatush.pakbus.types.CompositeDataType;
import com.dilatush.pakbus.types.DataTypes;
import com.dilatush.pakbus.util.Checks;
import com.dilatush.pakbus.values.CompositeDatum;
import com.dilatush.pakbus.values.Datum;

/**
 * Encapsulates the programming statistics returned by a {@link com.dilatush.pakbus.messages.bmp5.GetProgrammingStatisticsRspMsg}.
 *
 * @author Tom Dilatush  tom@dilatush.com
 */
public class ProgrammingStatistics {

    static final public String FIELD_TYPE0         = "TYPE0";
    static final public String FIELD_TYPE1         = "TYPE1";
    static final public String FIELD_MODEL_NAME    = "ModelName";
    static final public String FIELD_STATION_NAME  = "StationName";
    static final public String FIELD_OS_VER        = "OsVer";
    static final public String FIELD_OS_SIG        = "OsSig";
    static final public String FIELD_SERIAL_NO     = "SerialNo";
    static final public String FIELD_POWER_UP_PROG = "PowerUpProg";
    static final public String FIELD_COMP_STATE    = "CompState";
    static final public String FIELD_PROG_NAME     = "ProgName";
    static final public String FIELD_PROG_SIG      = "ProgSig";
    static final public String FIELD_COMP_TIME     = "CompTime";
    static final public String FIELD_COMP_RESULT   = "CompResult";


    static final public CompositeDataType TYPE1 = new CompositeDataType( FIELD_TYPE1, null,
            new CP( FIELD_MODEL_NAME,    DataTypes.ASCIIZ ),
            new CP( FIELD_STATION_NAME,  DataTypes.ASCIIZ, true ) );

    static final public CompositeDataType TYPE0 = new CompositeDataType( FIELD_TYPE0, null,
            new CP( FIELD_OS_VER,        DataTypes.ASCIIZ ),
            new CP( FIELD_OS_SIG,        DataTypes.UINT2  ),
            new CP( FIELD_SERIAL_NO,     DataTypes.ASCIIZ ),
            new CP( FIELD_POWER_UP_PROG, DataTypes.ASCIIZ ),
            new CP( FIELD_COMP_STATE,    DataTypes.BYTE   ),
            new CP( FIELD_PROG_NAME,     DataTypes.ASCIIZ ),
            new CP( FIELD_PROG_SIG,      DataTypes.UINT2  ),
            new CP( FIELD_COMP_TIME,     DataTypes.NSEC   ),
            new CP( FIELD_COMP_RESULT,   DataTypes.ASCIIZ ),
            new CP( FIELD_TYPE1,       TYPE1, true ) );

    // the public properties...
    final public String osVersion;
    final public int    osSignature;
    final public String serialNumber;
    final public String powerUpProgram;
    final public int    programStatus;
    final public String currentProgramFileName;
    final public int    currentProgramSignature;
    final public NSec   currentProgramCompileTime;
    final public String currentProgramCompileResult;
    final public String dataloggerModel;
    final public String stationName;


    public ProgrammingStatistics( final CompositeDatum _datum ) {

        Checks.required( _datum );
        if( _datum.at( FIELD_TYPE0 ).isSet() ) {
            Datum datum                 = _datum.at( FIELD_TYPE0         );
            osVersion                   = datum.at(  FIELD_OS_VER        ).getAsString();
            osSignature                 = datum.at(  FIELD_OS_SIG        ).getAsInt();
            serialNumber                = datum.at(  FIELD_SERIAL_NO     ).getAsString();
            powerUpProgram              = datum.at(  FIELD_POWER_UP_PROG ).getAsString();
            programStatus               = datum.at(  FIELD_COMP_STATE    ).getAsInt();
            currentProgramFileName      = datum.at(  FIELD_PROG_NAME     ).getAsString();
            currentProgramSignature     = datum.at(  FIELD_PROG_SIG      ).getAsInt();
            currentProgramCompileTime   = datum.at(  FIELD_COMP_TIME     ).getAsNSec();
            currentProgramCompileResult = datum.at(  FIELD_COMP_RESULT   ).getAsString();

            if( datum.at( FIELD_TYPE1 ).isSet() ) {
                datum           = datum.at( FIELD_TYPE1 );
                dataloggerModel = datum.at( FIELD_MODEL_NAME ).getAsString();
                if( datum.at( FIELD_STATION_NAME ).isSet() )
                    stationName     = datum.at( FIELD_STATION_NAME ).getAsString();
                else
                    stationName     = null;
            }
            else {
                dataloggerModel = null;
                stationName     = null;
            }
        }

        else {
            osVersion                   = null;
            osSignature                 = 0;
            serialNumber                = null;
            powerUpProgram              = null;
            programStatus               = 0;
            currentProgramFileName      = null;
            currentProgramSignature     = 0;
            currentProgramCompileTime   = null;
            currentProgramCompileResult = null;
            dataloggerModel             = null;
            stationName                 = null;
        }
    }


    public ProgrammingStatistics( final String _osVersion, final int _osSignature, final String _serialNumber, final String _powerUpProgram,
                                  final int _programStatus, final String _currentProgramFileName, final int _currentProgramSignature,
                                  final NSec _currentProgramCompileTime, final String _currentProgramCompileResult, final String _dataloggerModel,
                                  final String _stationName ) {

        Checks.required( _serialNumber, _powerUpProgram, _currentProgramFileName, _currentProgramCompileTime, _currentProgramCompileResult,
                _dataloggerModel, _stationName, _osVersion );

        osVersion = _osVersion;
        osSignature = _osSignature;
        serialNumber = _serialNumber;
        powerUpProgram = _powerUpProgram;
        programStatus = _programStatus;
        currentProgramFileName = _currentProgramFileName;
        currentProgramSignature = _currentProgramSignature;
        currentProgramCompileTime = _currentProgramCompileTime;
        currentProgramCompileResult = _currentProgramCompileResult;
        dataloggerModel = _dataloggerModel;
        stationName = _stationName;
    }


    public void set( final CompositeDatum _datum ) {

        Checks.required( _datum );

        Datum datum = _datum.at( FIELD_TYPE0 );

        datum.at( FIELD_OS_VER        ).setTo( osVersion                   );
        datum.at( FIELD_OS_SIG        ).setTo( osSignature                 );
        datum.at( FIELD_SERIAL_NO     ).setTo( serialNumber                );
        datum.at( FIELD_POWER_UP_PROG ).setTo( powerUpProgram              );
        datum.at( FIELD_COMP_STATE    ).setTo( programStatus               );
        datum.at( FIELD_PROG_NAME     ).setTo( currentProgramFileName      );
        datum.at( FIELD_PROG_SIG      ).setTo( currentProgramSignature     );
        datum.at( FIELD_COMP_TIME     ).setTo( currentProgramCompileTime   );
        datum.at( FIELD_COMP_RESULT   ).setTo( currentProgramCompileResult );

        datum = datum.at( FIELD_TYPE1 );

        datum.at( FIELD_MODEL_NAME    ).setTo( dataloggerModel             );
        datum.at( FIELD_STATION_NAME  ).setTo( stationName                 );
    }
}
