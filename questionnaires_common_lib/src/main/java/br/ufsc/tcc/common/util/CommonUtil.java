package br.ufsc.tcc.common.util;

import java.awt.Desktop;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.OpenOption;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.nodes.Node;

import com.google.common.base.CharMatcher;

import br.ufsc.tcc.common.model.DeweyExt;
import br.ufsc.tcc.common.model.MyNode;

public class CommonUtil {
	
	private static final List<String> singleComps = 
			Arrays.asList("textarea", 
					"select",
					"input[type=text]",
					"input[type=number]",
					"input[type=date]",
					"input[type=email]",
					"input[type=tel]",
					"input[type=time]",
					"input[type=url]",
					"input[type=range]");
	
	private static final List<String> multiComps =
			Arrays.asList("input[type=radio]",
					"input[type=checkbox]");
	
	private static final List<String> allComps = 
			Arrays.asList("input[type=text]",
					"input[type=number]",
					"input[type=date]",
					"input[type=email]",
					"input[type=tel]",
					"input[type=time]",
					"input[type=url]",
					"input[type=range]",
					"input[type=radio]",
					"input[type=checkbox]",
					"textarea",
					"select",
					"option");
	
	public final static String REQUIRED_REGEX = 
			"\\(?(required|resposta exigida|requerido|\\*obrigat(ó|o)rio|\\*)\\)?";
	
	// Getters e Setters
	public static List<String> getSingleComps(){
		return singleComps;
	}
	
	public static List<String> getMultiComps(){
		return multiComps;
	}
	
	public static List<String> getAllComps(){
		return allComps;
	}
	
	// Demais métodos
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
			
			content = new String(Files.readAllBytes(path),
					Charset.forName("UTF-8"));
		} catch (IOException e) {
			CommonLogger.fatalError(e);
		}
		return content;
	}
	
	/**
	 * Le todo o conteúdo de um recurso e o retorna em 
	 * forma de uma String.
	 * 
	 * @param resource				Recurso que se quer o conteúdo.
	 * @return						Uma String que contém todo o conteúdo do recurso
	 * 								passado.
	 */	
	public static String readResource(String resource){
		String content = "";
		try {
			ClassLoader cl = Thread.currentThread().getContextClassLoader();
			if(cl == null)
				cl = CommonUtil.class.getClassLoader();
			
			// Só para ter certeza...
			InputStream input = cl.getResourceAsStream(resource);
			if(input == null){
				input = cl.getResourceAsStream("/" +resource);
				if(input == null){
					input = cl.getResourceAsStream("resources/" +resource);
					if(input == null){
						input = cl.getResourceAsStream("/resources/" +resource);
						if(input == null)
							throw new IOException("Resource not found ("+resource+").");
					}
				}
			}

			ByteArrayOutputStream result = new ByteArrayOutputStream();
			byte[] buffer = new byte[1024];
			int length;
			while ((length = input.read(buffer)) != -1) {
			    result.write(buffer, 0, length);
			}
			
			content = result.toString("UTF-8");
		} catch (IOException e) {
			CommonLogger.fatalError(e);
		}
		return content;
	}
	
	/**
	 * Escreve o conteúdo passado em um arquivo.
	 * 
	 * @param path			Caminho para o arquivo que se quer salvar.
	 * @param content		Conteúdo que se quer salvar.
	 * @param options		Opções especificando como o arquivo será aberto, utilize a classe StandardOpenOption.
	 * @return				<b>TRUE</b> caso seja possivel escrever o arquivo, ou</br>
	 * 						<b>FALSE</b> caso contrario.
	 */
	public static boolean writeFile(String path, String content, OpenOption ... options){
		try {
			Files.write(Paths.get(path), content.getBytes(StandardCharsets.UTF_8), options);
			return true;
		} catch (IOException e) {
			CommonLogger.error(e);
			return false;
		}
	}
	
	public static boolean appendToFile(String path, String content){
		return writeFile(path, content, StandardOpenOption.CREATE, 
				StandardOpenOption.APPEND);
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
			CommonLogger.error(e);
		    return false;
		}
		return true;
	}
	
	/**
	 * Retorna um JSONObject da String {@code content} passada.
	 * 
	 * @param content	Conteúdo que se quer gerar um JSONObject.
	 * @return			Um JSONObject do conteúdo passado.
	 */
	public static JSONObject parseJson(String content){
		JSONObject obj = null;
		try{
			obj = new JSONObject(content);
		}catch(JSONException e){
			CommonLogger.fatalError(e);
		}
		return obj;
	}
	
	/**
	 * Remove espaços antes e depois da String {@code str}, incluindo os 
	 * caracteres '\u00a0' e '\ufeff'.
	 * 
	 * @param str	String que se quer remover os espaços em volta.
	 * @return		A String str sem espaços em volta.
	 */
	public static String trim(String str){
		return str.replaceAll("\u00a0", " ")
				.replaceAll("&nbsp;", " ")
				.replaceAll("&#65279", "")
				.replaceAll("\ufeff", "")
				.trim();
	}
	
	public static boolean startsWithUpperCase(String text){
		char c = text.charAt(0);
		c = c == '¿' ? text.charAt(1) : c;
		return Character.isUpperCase(c);
	}
	
	public static boolean startsWithDigit(String text){
		return Character.isDigit(text.charAt(0));
	}
	
	public static Timestamp getCurrentTime() {
		Calendar calendar = Calendar.getInstance();
		return new Timestamp(calendar.getTime().getTime());
	}
	
	public static String exceptionStacktraceToString(Throwable e){
	    ByteArrayOutputStream baos = new ByteArrayOutputStream();
	    PrintStream ps = new PrintStream(baos);
	    e.printStackTrace(ps);
	    ps.close();
	    return baos.toString();
	}
	
	/**
	 * Conta o tamanho do prefixo comum entre {@code d1} e {@code d2} 
	 * desconsiderando sinais negativos, '-'.
	 * 
	 * @param d1
	 * @param d2
	 * @return			Valor do tamanho do prefixo comum.
	 */
	public static int getPrefixLenght(DeweyExt d1, DeweyExt d2){
		return getPrefixLength(d1.getCommonPrefix(d2));
	}
	
	/**
	 * Conta o tamanho do prefixo comum {@code prefix}  
	 * desconsiderando sinais negativos, '-'.
	 * 
	 * @param prefix
	 * @return			Valor do tamanho do prefixo comum.
	 */
	public static int getPrefixLength(String prefix){
		int n = prefix.length();
		n -= CharMatcher.is('-').countIn(prefix);
		return n;
	}
	
	/**
	 * Adiciona zeros a frente de um numero para deixa-los com 
	 * exatamente 3 caracteres. Por exemplo, 1 vira 001, 10 vira 010 e 
	 * 100 continua como 100.
	 * 
	 * @param i			Numero que precisa de padding.
	 * @return			String com exatamente 3 caracteres do numero {@code i}.
	 */
	public static String padNumber(int i){
		return ((i < 0) ? "-" : "") + String.format("%03d", Math.abs(i)); 
	}
	
	public static String getNodeRepresentation(Node node){
		if(node == null) return "";
		
		String name = node.nodeName();
		switch(name){
		case "#text":
			name = CommonUtil.trim(node.toString());
			break;
		case "input":
			// O tipo padrão de um input é 'text', caso não seja especificado
			String type = node.attr("type");
			name += "[type="+ (type.isEmpty() ? "text" : type.toLowerCase()) +"]";
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
		if(text.isEmpty()) return text;
		
		text = CommonUtil.trim(text);
		return CommonUtil.trim(text.replaceAll(
				String.format("(?ism)(^%s|%s$)", 
						REQUIRED_REGEX, REQUIRED_REGEX), ""));
	}
	
	/**
	 * Verifica se o nodo passado é um componente de formulário, 
	 * uma imagem ou um texto.
	 * 
	 * @param node		Nodo que se quer verificar.
	 * @return			<b>True</b> caso este nodo seja um componente de formulário, 
	 * 					uma imagem ou um texto ou<br>
	 * 					<b>False</b> caso contrário.
	 */
	public static boolean isCompImgOrTextNode(Node node){
		if(node == null) return false;
		
		String name = removeRequiredAndTrim(getNodeRepresentation(node));
		return (!name.equals("") && node.nodeName().equals("#text")) || 
				((name.startsWith("img") || allComps.contains(name)) && 
						!node.attr("type").equals("hidden"));
	}
	
	/**
	 * Compara o {@code regex} passado com cada linha da String {@code txt}.
	 * 
	 * @param txt			
	 * @param regex
	 * @return				<b>True</b> caso o {@code regex} passado seja encontrado
	 * 						em alguma das linhas de {@code txt} ou<br>
	 * 						<b>False</b> caso contrario.
	 */
	public static boolean matchesWithLineBreak(String txt, String regex){
		String [] lines = txt.split("\n");
		Pattern p = Pattern.compile(regex, Pattern.CASE_INSENSITIVE);
		for(String line : lines){
			if(p.matcher(line).matches())
				return true;
		}
		return false;
	}
	
	public static boolean containsWithLineBreak(String text, String txtToCheck){
		text = text.toLowerCase();
		String [] lines = txtToCheck.split("\n");
		for(String line : lines){
			if(text.contains(line.toLowerCase()))
				return true;
		}
		return false;
	}
	
	/**
	 * Encontra e retorna todos os nodos de componente de formulário, imagem ou 
	 * texto a partir de um nodo {@code root}.
	 * 
	 * @param root			Nodo inicial da busca.
	 * @return				Lista com todos os nodos encontrados.
	 */
	public static List<MyNode> findCompsImgsAndTexts(Node root) {
		List<MyNode> ret = new ArrayList<>();
		if(root != null)
			findCompsImgsAndTexts(root, "001", ret);
		return ret;
	}

	private static void findCompsImgsAndTexts(Node root, String dewey, List<MyNode> ret) {
		if(isCompImgOrTextNode(root)){
			MyNode node = new MyNode(root, new DeweyExt(dewey));
			//Agrupa textos que foram separados por <br>
			if(!ret.isEmpty()){
				MyNode last = ret.get(ret.size()-1);
				if(node.getDewey().equals(last.getDewey()))
					last.setText(last.getText() +"\n"+ node.getText());
				else
					ret.add(node);
			}else
				ret.add(node);
		}
		
		int n = 1;
		List<Node> children = root.childNodes();
		for(int i = 0; i < children.size(); i++){
			Node child = children.get(i);
			
			// Ignora comentarios, inputs hidden, tags 'br', tags <p>/<a> sem texto e href e textos vazios
			if(child.nodeName().equals("br") && isBetweenTexts(children, i))
				n--;
			if(!child.nodeName().matches("#comment|br") &&
				!trim(child.toString()).isEmpty() && !CommonUtil.isEmptyAorP(child))
					findCompsImgsAndTexts(child, 
							dewey +"."+ padNumber(n++), 
							ret);
		}
	}
	
	/**
	 * Verifica se o nodo na posição {@code i} esta entre dois nodos de texto.
	 * 
	 * @param children
	 * @param i
	 * @return
	 */
	private static boolean isBetweenTexts(List<Node> children, int i) {
		if(i-1 < 0 || i+1 >= children.size())
			return false;
		Node c1 = children.get(i-1), c2 = children.get(i+1);
		if(!c1.nodeName().equals("#text") || 
				!c2.nodeName().equals("#text"))
			return false;
		return !trim(c1.toString()).isEmpty() && !trim(c2.toString()).isEmpty();
	}

	private static boolean isEmptyAorP(Node el){
		String txt = el.nodeName();
		return el.childNodeSize() == 0 && (txt.equals("p") || 
				(txt.equals("a") && !el.hasAttr("href"))); 
	}
}
