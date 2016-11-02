// Logging configuration
// Default log level
def ROOT_LOGGER_LOG_LEVEL = DEBUG

// Definition of loggers and their corresponding log level
def loggers = [
		'com.lhings.java': DEBUG,
	]
// Maximum total size of all log files
def MAX_LOG_SIZE = "20MB"
// Maximum number of days logs will be stored
def MAX_LOG_HISTORY = 10
// Name of the default log file
def LOG_FILENAME = "logback"

// Pattern used for logging
def PATTERN = "%d{HH-mm-ss.SSS} [%10.10thread] %5level in %-30logger{30} - %msg%n"




// Logging definition. 
// WARNING: do not change anything beyond this line, unless you know what you're doing.

import ch.qos.logback.core.util.FileSize

scan("30 seconds")

appender("CONSOLE", ConsoleAppender){
	withJansi = true
	encoder(PatternLayoutEncoder){
		pattern = PATTERN
	}
}

appender("ROLLING-FILE", RollingFileAppender){
	file = "${LOG_FILENAME}.log"
	append = true
	rollingPolicy(TimeBasedRollingPolicy) {
		fileNamePattern = "${LOG_FILENAME}-%d{yyyy-MM-dd}.log"
		maxHistory = MAX_LOG_HISTORY
		totalSizeCap = FileSize.valueOf(MAX_LOG_SIZE)
	}
	encoder(PatternLayoutEncoder){
		pattern = PATTERN
	}
}

root(ROOT_LOGGER_LOG_LEVEL, ["CONSOLE", "ROLLING-FILE"])
loggers.each{ loggerName, loggerLevel -> logger(loggerName, loggerLevel, ["CONSOLE", "ROLLING-FILE"]) }


