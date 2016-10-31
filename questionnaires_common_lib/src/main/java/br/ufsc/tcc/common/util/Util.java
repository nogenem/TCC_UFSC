package br.ufsc.tcc.common.util;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.json.JSONException;
import org.json.JSONObject;

public final class Util {
	
	/**
	 * Le todo o conteúdo de um arquivo e o retorna em 
	 * forma de uma String.
	 * 
	 * @param file				Arquivo que se quer o conteúdo.
	 * @return					Uma String que contém todo o conteúdo do arquivo
	 * 							passado.
	 */	
	public static String readFile(String file){
		String content = "";
		try {
			Path path = Paths.get(file);
			
			content = new String(Files.readAllBytes(
					path),
					Charset.forName("UTF-8"));
		} catch (IOException e) {
			System.err.println("Util:readFile()> "+ e.toString());
			System.exit(-1);
		}
		return content;
	}
	
	/**
	 * Le todo o conteúdo de um recurso e o retorna em 
	 * forma de uma String.
	 * 
	 * @param resource			Recurso que se quer o conteúdo.
	 * @return					Uma String que contém todo o conteúdo do recurso
	 * 							passado.
	 */	
	public static String readResource(String resource){
		String content = "";
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			if(cl == null)
				cl = Util.class.getClassLoader();
			
			InputStream input = cl.getResourceAsStream(resource);
			if(input == null)
				input = cl.getResourceAsStream("/" +resource);
			if(input == null)
				input = cl.getResourceAsStream("resources/" +resource);
			if(input == null)
				input = cl.getResourceAsStream("/resources/" +resource);

			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = input.read(buffer)) != -1) {
			    result.write(buffer, 0, length);
			}
			
			content = result.toString("UTF-8");
		} catch (IOException e) {
			System.err.println("Util:readFile()> "+ e.toString());
			System.exit(-1);
		}
		return content;
	}
	
	/**
	 * Escreve o conteúdo passado em um arquivo.
	 * 
	 * @param path			Caminho para o arquivo que se quer salvar.
	 * @param content		Conteúdo que se quer salvar.
	 * @return				<b>TRUE</b> caso seja possivel escrever o arquivo, ou</br>
	 * 						<b>FALSE</b> caso contrario.
	 */
	public static boolean writeFile(String path, String content){
		try {
			Files.write(Paths.get(path), content.getBytes(StandardCharsets.UTF_8));
			return true;
		} catch (IOException e) {
			System.err.println("Util:writeFile()> "+ e.toString());
			return false;
		}
	}
	
	/**
	 * Abre um arquivo utilizando o programa padrão do sistema operacional.
	 * 
	 * @param path		Caminho para o arquivo que se quer abrir.
	 * @return			<b>TRUE</b> caso seja possivel abrir o arquivo, ou</br>
	 * 					<b>FALSE</b> caso contrario.
	 */
	public static boolean openFile(String path) {
		return openFile(new File(path));
	}
	
	/**
	 * Abre um arquivo utilizando o programa padrão do sistema operacional.
	 * 
	 * @param file		Arquivo que se quer abrir.
	 * @return			<b>TRUE</b> caso seja possivel abrir o arquivo, ou</br>
	 * 					<b>FALSE</b> caso contrario.
	 */
	public static boolean openFile(final File file) {
		//TODO testar no linux
		if (!Desktop.isDesktopSupported())
			return false;

		Desktop desktop = Desktop.getDesktop();
		if(!desktop.isSupported(Desktop.Action.OPEN))
			return false;
		
		try {
			desktop.open(file);
		} catch (IOException e) {
			System.err.println("Util:editFile()> "+ e.toString());
		    return false;
		}
		return true;
	}
	
	/**
	 * Retorna um JSONObject da String content passada.
	 * 
	 * @param content	Conteudo que se quer gerar um JSONObject.
	 * @return			Um JSONObject do conteudo passado.
	 */
	public static JSONObject parseJson(String content){
		JSONObject obj = null;
		try{
			obj = new JSONObject(content);
		}catch(JSONException e){
			System.err.println("Util:getJson()> " +e.toString());
			System.err.println("Util:getJson()> Content provided:\n" +content);
			System.exit(-1);
		}
		return obj;
	}
	
	/**
	 * Remove espaços antes e depois da String str, incluindo o caracter
	 * '\u00a0'.
	 * 
	 * @param str	String que se quer remover os espaços em volta.
	 * @return		A String str sem espaços em volta.
	 */
	public static String trim(String str){
		return str.replaceAll("\u00a0", "").trim();
	}
	
}
