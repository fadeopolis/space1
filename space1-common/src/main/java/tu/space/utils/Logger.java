package tu.space.utils;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.PatternLayout;

public class Logger {
	public static void configure() {
		org.apache.log4j.Logger root = org.apache.log4j.Logger.getRootLogger();
	    root.addAppender( 
	    	new ConsoleAppender( 
	    		new PatternLayout( "%d{dd.MM.yyyy-HH:mm:ss.SSS} %p [%t] %x - %m%n" ),
	    		ConsoleAppender.SYSTEM_ERR
	    	)
	    );
	    root.setLevel( Level.INFO );
	}

	public static Logger make( Class<?> c ) {
		return new Logger( org.apache.log4j.Logger.getLogger( c ) );
	}
	public static Logger make( String name ) {
		return new Logger( org.apache.log4j.Logger.getLogger( name ) );
	}
	
	public void info( String msg, Object... args ) {
		log( Level.INFO, msg, args );
	}
	public void debug( String msg, Object... args ) {
		log( Level.DEBUG, msg, args );
	}
	public void warn( String msg, Object... args ) {
		log( Level.WARN, msg, args );
	}
	public void error( String msg, Object... args ) {
		log( Level.ERROR, msg, args );
	}
	public void log( Level lvl, String msg, Object... args ) {
		log.log( lvl, String.format( msg, args ) );
	}

	private final org.apache.log4j.Logger log;
	
	private Logger( org.apache.log4j.Logger log ) { this.log = log; }
}
