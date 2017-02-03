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
	//ERROR esta sempre ativado
	private static String enabledLevels = "ERROR|";
	
	public static void debug(String format, Object ...args){
		if(enabledLevels.contains("DEBUG")){
			if(!logger.isDebugEnabled())
				System.out.println(MessageFormatter.arrayFormat(format, args).getMessage());
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
	
	public static void error(Throwable e){
		if(enabledLevels.contains("ERROR")){
			if(logger.isErrorEnabled())
				logger.error("Stacktrace: ", e);
			else
				e.printStackTrace();
			logToFile(e);
		}
	}
	
	public static void fatalError(Throwable e){
		if(enabledLevels.contains("ERROR")){
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
