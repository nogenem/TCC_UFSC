package br.ufsc.tcc.common.util;

import java.util.ArrayList;

import br.ufsc.tcc.common.config.ProjectConfigs;
import uk.org.lidalia.slf4jext.Level;
import uk.org.lidalia.slf4jext.Logger;
import uk.org.lidalia.slf4jext.LoggerFactory;

public class CommonLogger {

	protected static final Logger logger = LoggerFactory.getLogger(CommonLogger.class);
	protected static final String path = "log.txt";
	private static final ArrayList<Level> enabledLevels = new ArrayList<>();
	
	public static void debug(String msg){
		if(enabledLevels.contains(Level.DEBUG)){
			if(!logger.isDebugEnabled())
				System.out.println(msg);
			else
				logger.debug(msg);
		}
	}
	
	public static void error(Exception e){
		if(enabledLevels.contains(Level.ERROR)){
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
		String levels = ProjectConfigs.getLogLevels().toUpperCase();	
		if(levels.contains("TRACE"))
			enabledLevels.add(Level.TRACE);
		if(levels.contains("DEBUG"))
			enabledLevels.add(Level.DEBUG);
		if(levels.contains("INFO"))
			enabledLevels.add(Level.INFO);
		if(levels.contains("WARN"))
			enabledLevels.add(Level.WARN);
		if(levels.contains("ERROR"))
			enabledLevels.add(Level.ERROR);
	}
}
