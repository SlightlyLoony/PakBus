package com.dilatush.pakbus;

/**
 * @author Tom Dilatush  tom@dilatush.com
 */
public class Log {


    static public synchronized void log( final String _msg ) {
        System.out.print( _msg );
    }


    static public synchronized void logLn( final String _msg ) {
        System.out.println( _msg );
    }


    static public String toHex2( final int _byte ) {
        return Integer.toHexString( 0x100 + (0xFF & _byte) ).substring( 1 );
    }
}
