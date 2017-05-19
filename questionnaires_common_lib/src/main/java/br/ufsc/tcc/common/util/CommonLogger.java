package br.ufsc.tcc.common.util;

import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.helpers.MessageFormatter;

import br.ufsc.tcc.common.config.ProjectConfigs;


public class CommonLogger {

	protected static final String path = "./log.txt";
	protected static final Logger logger = LoggerFactory.getLogger(CommonLogger.class);
	//FATAL_ERROR esta sempre ativado
	private static String enabledLevels = "FATAL_ERROR|";
	
	// Debug level
	public static boolean isDebugEnabled(){
		return enabledLevels.contains("|DEBUG");
	}
	
	public static void setDebugEnabled(boolean enabled){
		if(enabled && !isDebugEnabled())
			enabledLevels += "|DEBUG";
		else if(!enabled && isDebugEnabled())
			enabledLevels = enabledLevels.replace("|DEBUG", "");
	}
	
	public static void debug(String format, Object ...args){
		if(enabledLevels.contains("DEBUG")){
			System.out.println(MessageFormatter.arrayFormat(format, args).getMessage());
		}
	}
	
	public static void debug(List<? extends Object> arr){
		if(enabledLevels.contains("DEBUG")){
			arr.forEach(System.out::println);
			System.out.println();
		}
	}
	
	// INFO level
	public static boolean isInfoEnabled(){
		return enabledLevels.contains("|INFO");
	}
	
	public static void setInfoEnabled(boolean enabled){
		if(enabled && !isInfoEnabled())
			enabledLevels += "|INFO";
		else if(!enabled && isInfoEnabled())
			enabledLevels = enabledLevels.replace("|INFO", "");
	}
	
	public static void info(String format, Object ...args){
		if(enabledLevels.contains("INFO")){
			if(logger.isInfoEnabled())
				logger.info(format, args);
			else
				System.err.println(MessageFormatter.arrayFormat(format, args).getMessage());
		}
	}
	
	public static void info(List<? extends Object> arr){
		if(enabledLevels.contains("INFO")){
			if(logger.isInfoEnabled()){
				final StringBuilder builder = new StringBuilder();
				builder.append("\n");
				arr.forEach(e -> builder.append(e.toString()+"\n"));
				logger.info(builder.toString());
			}else{
				arr.forEach(System.err::println);
				System.out.println();
			}
		}
	}
	
	// ERROR level
	public static boolean isErrorEnabled(){
		return enabledLevels.contains("|ERROR");
	}
	
	public static void setErrorEnabled(boolean enabled){
		if(enabled && !isErrorEnabled())
			enabledLevels += "|ERROR";
		else if(!enabled && isErrorEnabled())
			enabledLevels = enabledLevels.replace("|ERROR", "");
	}
	
	public static void error(Throwable e){
		if(enabledLevels.contains("ERROR")){
			if(logger.isErrorEnabled())
				logger.error("Stacktrace: ", e);
			else
				e.printStackTrace();
			logToFile(e);
		}
	}
	
	// FATAL_ERROR level
	public static boolean isFatalErrorEnabled(){
		return enabledLevels.contains("FATAL_ERROR");
	}
		
	public static void fatalError(Throwable e){
		if(enabledLevels.contains("FATAL_ERROR")){
			if(!logger.isErrorEnabled())
				logger.error("Stacktrace: ", e);
			else
				e.printStackTrace();

			logToFile(e);
			JOptionPane.showMessageDialog(null, "Erro: "+e.getMessage()+"\n\n"
					+ "Verifique o arquivo de log para mais detalhes.", "Ocorreu um erro!", 
					JOptionPane.ERROR_MESSAGE);
			
			System.exit(-1);
		}
	}

	private static boolean logToFile(Throwable e) {
		String content = CommonUtil.getCurrentTime() +" | Stacktrace: ";
		content += "\n"+CommonUtil.exceptionStacktraceToString(e)+"\n";
		return CommonUtil.appendToFile(path, content);
	}
	
	// Bloco estático
	static {
		enabledLevels += ProjectConfigs.getLogLevels().toUpperCase();
		// Pequena gambiarra para setar o nivel de log do Crawler4J
		ch.qos.logback.classic.Level level = enabledLevels.contains("|INFO") ? ch.qos.logback.classic.Level.INFO : 
			ch.qos.logback.classic.Level.WARN;
		ch.qos.logback.classic.Logger root = (ch.qos.logback.classic.Logger)
				LoggerFactory.getLogger(ch.qos.logback.classic.Logger.ROOT_LOGGER_NAME);
		root.setLevel(level);
	}
}
