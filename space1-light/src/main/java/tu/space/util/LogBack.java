package tu.space.util;

import org.slf4j.LoggerFactory;

import ch.qos.logback.classic.BasicConfigurator;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import ch.qos.logback.classic.LoggerContext;

//import org.slf4j.LoggerFactory;
//
//import ch.qos.logback.classic.BasicConfigurator;
//import ch.qos.logback.classic.Level;
//import ch.qos.logback.classic.Logger;
//import ch.qos.logback.classic.LoggerContext;

public class LogBack {
	public static void configure() {
		configure( Level.WARN );
	}
	public static void configure( Level lvl ) {
		BasicConfigurator.configureDefaultContext();
		Logger lc = ((LoggerContext) LoggerFactory.getILoggerFactory()).getLogger( Logger.ROOT_LOGGER_NAME );
		lc.setLevel( lvl );
	}
}
