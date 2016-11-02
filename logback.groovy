// Logging configuration
def ROOT_LOGGER_LOG_LEVEL = DEBUG
def loggers = [
		'com.lhings.java': DEBUG,
	]

def MAX_LOG_SIZE = "20MB"
def MAX_LOG_HISTORY = 10
def LOG_FILENAME = "logback"

def PATTERN = "%d{HH-mm-ss.SSS} [%10.10thread] %5level in %-30logger{30} - %msg%n"






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


