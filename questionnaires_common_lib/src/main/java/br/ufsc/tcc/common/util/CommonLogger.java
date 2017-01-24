package br.ufsc.tcc.common.util;

import org.slf4j.helpers.MessageFormatter;

import br.ufsc.tcc.common.config.ProjectConfigs;
import uk.org.lidalia.slf4jext.Logger;
import uk.org.lidalia.slf4jext.LoggerFactory;

public class CommonLogger {

	protected static final String path = "./log.txt";
	protected static final Logger logger = LoggerFactory.getLogger(CommonLogger.class);
	//Começa como ERROR para caso de erro na leitura do arquivo de configurações
	private static String enabledLevels = "ERROR";
	
	public static void debug(String format, Object ...args){
		if(enabledLevels.contains("DEBUG")){
			if(!logger.isDebugEnabled())
				System.out.println(MessageFormatter.arrayFormat(format, args).getMessage());
			else
				logger.debug(format, args);
		}
	}
	
	public static void info(String format, Object ...args){
		if(enabledLevels.contains("INFO")){
			if(logger.isInfoEnabled())
				logger.info(format, args);
			else
				System.err.println(MessageFormatter.arrayFormat(format, args).getMessage());
		}
	}
	
	public static void error(Exception e){
		if(enabledLevels.contains("ERROR")){
			if(logger.isErrorEnabled())
				logger.error("Stacktrace: ", e);
			else
				e.printStackTrace();
			logToFile(e);
		}
	}

	private static boolean logToFile(Exception e) {
		String content = CommonUtil.getCurrentTime() +" | Stacktrace: ";
		content += "\n"+CommonUtil.exceptionStacktraceToString(e)+"\n";
		return CommonUtil.appendToFile(path, content);
	}
	
	// Bloco estático
	static {
		enabledLevels = ProjectConfigs.getLogLevels().toUpperCase();
	}
}
