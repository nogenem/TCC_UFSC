package br.ufsc.tcc.common.util;

import java.util.List;

import javax.swing.JOptionPane;

import org.slf4j.helpers.MessageFormatter;

import br.ufsc.tcc.common.config.ProjectConfigs;
import uk.org.lidalia.slf4jext.Logger;
import uk.org.lidalia.slf4jext.LoggerFactory;

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
			format += "\n";
			if(!logger.isDebugEnabled())
				System.out.print(MessageFormatter.arrayFormat(format, args).getMessage());
			else
				logger.debug(format, args);
		}
	}
	
	public static void debug(List<? extends Object> arr){
		if(enabledLevels.contains("DEBUG")){
			if(!logger.isDebugEnabled()){
				arr.forEach(System.out::println);
				System.out.println();
			}else{
				final StringBuilder builder = new StringBuilder();
				builder.append("\n");
				arr.forEach(e -> builder.append(e.toString()+"\n"));
				logger.debug(builder.toString());
			}
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
			format += "\n";
			if(logger.isInfoEnabled())
				logger.info(format, args);
			else
				System.err.print(MessageFormatter.arrayFormat(format, args).getMessage());
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
	
	//PS: FATAL_ERROR esta sempre ativo e não pode ser desabilitado
	public static boolean isFatalErrorEnabled(){
		return true;
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
	}
}
