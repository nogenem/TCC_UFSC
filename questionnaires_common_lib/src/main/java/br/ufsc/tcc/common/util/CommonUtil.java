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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.Dewey;
import br.ufsc.tcc.common.model.MyNode;

public class CommonUtil {
	
	private static final List<String> singleComps = 
			Arrays.asList("textarea", 
					"select",
					"input[type=text]",
					"input[type=date]",
					"input[type=number]",
					"input[type=email]",
					"input[type=range]",
					"input[type=tel]");
	
	private static final List<String> multiComps =
			Arrays.asList("input[type=radio]",
					"input[type=checkbox]");
	
	private static final List<String> allComps = 
			Arrays.asList("input[type=text]",
					"input[type=date]",
					"input[type=number]",
					"input[type=radio]",
					"input[type=checkbox]",
					"input[type=email]",
					"input[type=range]",
					"input[type=tel]",
					"textarea",
					"select",
					"option");
	
	private final static String REQUIRED_REGEX = 
			"(required|resposta exigida|requirido|\\*)";
	
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
	
	public static String readResource(String resource){
		String content = "";
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			if(cl == null)
				cl = CommonUtil.class.getClassLoader();
			
			InputStream input = cl.getResourceAsStream(resource);
			if(input == null)
				input = cl.getResourceAsStream("/" +resource);
			if(input == null)
				input = cl.getResourceAsStream("resources/" +resource);
			if(input == null)
				input = cl.getResourceAsStream("/resources/" +resource);
			if(input == null)
				throw new IOException("Resource not found ("+resource+").");

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
		return str.replaceAll("\u00a0", " ")
				.replaceAll("&nbsp;", " ").trim();
	}
	
	public static String padNumber(int i){
		return ((i < 0) ? "-" : "") + String.format("%02d", Math.abs(i)); 
	}
	
	public static String getNodeRepresentation(Node node){
		String name = node.nodeName();
		switch(name){
		case "#text":
			name = CommonUtil.trim(node.toString());
			break;
		case "input":
			name += "[type="+ node.attr("type") +"]";
			break;
		case "img":
			name += "[alt="+ node.attr("alt") +"]";
			break;
		default:
			break;
		}
		return name;
	}
	
	public static String removeRequiredAndTrim(String text){
		text = CommonUtil.trim(text);
		return CommonUtil.trim(text.replaceAll(
				String.format("(?i)(^%s|%s$)", 
						REQUIRED_REGEX, REQUIRED_REGEX), ""));
	}
	
	public static boolean isCompImgOrTextNode(Node node){
		String name = removeRequiredAndTrim(getNodeRepresentation(node));
		return (!name.equals("") && node.nodeName().equals("#text")) || 
				((name.startsWith("img") || allComps.contains(name)) && 
						!node.attr("type").equals("hidden"));
	}
	
	public static int getCountOfElems(Element root){
		if(root == null) return -1;
		
		String single = String.join(",", singleComps),
				multi = String.join(",", multiComps);
		
		return root.select(single).size() + 
				root.select(multi).size()/2;
	}
	
	public static int getCountOfElems(Cluster c){
		if(c == null) return -1;
		
		double count = 0;
		//TODO terminar este método
//		for(MyNode node : c.getGroup()){
//			if(singleComps.contains(node.getText()))
//				count++;
//			else if(multiComps.contains(node.getText()))
//				count += 0.5;
//		}
		return (int) count;
	}
	
	public static List<MyNode> findCompsImgsAndTexts(Node root) {
		List<MyNode> ret = new ArrayList<>();
		findCompsImgsAndTexts(root, "01", ret);
//		System.out.println("\n\n");
		return ret;
	}

	private static void findCompsImgsAndTexts(Node root, String dewey, List<MyNode> ret) {
		if(isCompImgOrTextNode(root)){
			MyNode node = new MyNode(root, new Dewey(dewey));
//			System.out.println(node);
			ret.add(node);
		}
		
		int n = 1;
		List<Node> children = root.childNodes();
		for(int i = 0; i < children.size(); i++){
			Node child = children.get(i);
			// Ignora comentarios, tags 'br' e nodos vazios
			if(!child.nodeName().matches("#comment|br") && 
				!trim(child.toString()).isEmpty())
					findCompsImgsAndTexts(children.get(i), 
							dewey +"."+ padNumber(n++), 
							ret);
		}
	}
}
