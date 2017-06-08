

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.util.ArrayList;

import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.connection.PostgreConnection;
import br.ufsc.tcc.common.util.CommonUtil;
import br.ufsc.tcc.extractor.builder.QuestionarioBuilder;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Figura;
import br.ufsc.tcc.extractor.model.Grupo;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;

public class BasicTest {
	
	private String currentPath;
	private String currentQ;
	private String currentP;
	
	private static BasicConnection conn;
	private static QuestionarioBuilder qBuilder;
	private static String configsPath = "./extractor_configs.json";
	
	@BeforeClass
	public static void onBasicStart(){
		// Necessario para carregar as informações 
		// do banco de dados
		ProjectConfigs.loadConfigs(configsPath);
		conn = new PostgreConnection(ProjectConfigs.getExtractorDatabaseConfigs());
		FormaDaPerguntaManager.loadFormas(conn);
		
		qBuilder = new QuestionarioBuilder();
		
		System.out.println("BasicTest:onBasicStart()> ...");
	}
	
	@AfterClass
	public static void onBasicExit(){
		if(conn != null)
			conn.close();
		System.out.println("BasicTest:onBasicExit()> ...");
	}
	
	protected final void executeTest(String expectedPath) throws IOException {
		long inicio = System.currentTimeMillis();
		
		this.currentPath = expectedPath;
		JSONObject expected = CommonUtil.parseJson(CommonUtil.readResource(expectedPath));
		String url = expected.getString("link_doc");
		Document doc = Jsoup.connect(url)
				.validateTLSCertificates(false)
				.get();
		
		Element root = doc.select("body").get(0);
		qBuilder.setCurrentLink(url);
		
		JSONObject expQs = expected.getJSONObject("questionarios");
		ArrayList<Questionario> qs = qBuilder.build(root, doc.title());
		//Isto permite a escolha de quais perguntas se quer testar
		//e quais não
		for(String key : expQs.keySet()){
			this.currentQ = this.currentP = "";
			int i = Integer.parseInt(key);
			
			assertTrue(wrap("Key '" +i+ "' fora do intervalo [0, " +qs.size()+ ")!"),
					i >= 0 && i < qs.size());
			
			this.currentQ = key;
			testQuestionario(qs.get(i), expQs.getJSONObject(key));
		}
		
		long fim = System.currentTimeMillis();
		System.out.println(this.currentPath +"> Time expend: " +((fim-inicio)/1000)+ "s");
	}
	
	private void testQuestionario(Questionario q, JSONObject expected){
		assertEquals(wrap("Questionario com assunto errado!"), 
				expected.getString("assunto"), q.getAssunto());
		
		testFiguras(q.getFiguras(), expected.optJSONObject("figuras"));
		
		JSONObject expPs = expected.getJSONObject("perguntas");
		ArrayList<Pergunta> ps = q.getPerguntas();
		for(String key : expPs.keySet()){
			int i = Integer.parseInt(key);
			
			assertTrue(wrap("Key '" +i+ "' fora do intervalo [0, " +ps.size()+ ")!"),
					i >= 0 && i < ps.size());
			
			this.currentP = key;
			testPergunta(ps.get(i), expPs.getJSONObject(key));
		}
	}

	private void testFiguras(ArrayList<Figura> figs, JSONObject expected){
		int quantidade = expected == null ? 0 : expected.getInt("quantidade");
		assertEquals(wrap("Quantidade errada de figuras no questionario!"), 
				quantidade, figs.size());
		
		JSONObject tmpObj;
		Figura tmpFig;
		if(figs.size() >= 1){
			tmpObj = expected.optJSONObject("primeira");
			if(tmpObj != null){
				tmpFig = figs.get(0);
				assertEquals(wrap("URL da primeira Figura do questionario esta errada!"),
						tmpObj.getString("url"), tmpFig.getImage_url());
				
				assertEquals(wrap("Legenda da primeira Figura do questionario esta errada!"),
						tmpObj.optString("legenda"), tmpFig.getLegenda());
			}
			tmpObj = expected.optJSONObject("ultima");
			if(tmpObj != null){
				tmpFig = figs.get(figs.size()-1);
				assertEquals(wrap("URL da ultima Figura do questionario esta errada!"),
						tmpObj.getString("url"), tmpFig.getImage_url());
				
				assertEquals(wrap("Legenda da ultima Figura do questionario esta errada!"),
						tmpObj.optString("legenda"), tmpFig.getLegenda());
			}
		}
	}
	
	private void testPergunta(Pergunta p, JSONObject expected) {
		assertEquals(wrap("Forma da pergunta errada!"),
				expected.getString("forma"), p.getForma().getDescricao());
		
		Grupo g = p.getGrupo();
		assertEquals(wrap("Grupo da pergunta esta errado!"),
				expected.optString("grupo"), g == null ? "" : g.getAssunto());
		
		assertEquals(wrap("Descricao da pergunta errada!"),
				expected.getString("descricao"), p.getDescricao());
		
		testAlternativas(p.getAlternativas(), expected.optJSONObject("alternativas"), false);
		testFilhas(p.getFilhas(), expected.optJSONObject("filhas"));
	}

	private void testAlternativas(ArrayList<Alternativa> as, JSONObject expected, boolean filha) {
		String txtPerg = filha ? "pergunta filha" : "pergunta";
		
		int quantidade = expected == null ? 0 : expected.getInt("quantidade");
		assertEquals(wrap("Quantidade errada de alternativas na "+txtPerg+"!"), 
				quantidade, as.size());
		
		JSONObject tmpObj;
		Alternativa tmpAlt;
		if(as.size() >= 1){
			tmpObj = expected.optJSONObject("primeira");
			if(tmpObj != null){
				tmpAlt = as.get(0);
				assertEquals(wrap("Descricao da primeira alternativa da "+txtPerg+" esta errada!"),
						tmpObj.getString("descricao"), tmpAlt.getDescricao());
			}
			tmpObj = expected.optJSONObject("ultima");
			if(tmpObj != null){
				tmpAlt = as.get(as.size()-1);
				assertEquals(wrap("Descricao da ultima alternativa da "+txtPerg+" esta errada!"),
						tmpObj.getString("descricao"), tmpAlt.getDescricao());
			}
		}
	}
	
	private void testFilhas(ArrayList<Pergunta> fs, JSONObject expected) {
		int quantidade = expected == null ? 0 : expected.getInt("quantidade");
		assertEquals(wrap("Quantidade errada de filhas na pergunta!"), 
				quantidade, fs.size());
		
		JSONObject tmpObj;
		Pergunta tmpPerg;
		if(fs.size() >= 1){
			tmpObj = expected.optJSONObject("primeira");
			if(tmpObj != null){
				tmpPerg = fs.get(0);
				assertEquals(wrap("Forma da primeira filha da pergunta esta errada!"),
						tmpObj.getString("forma"), tmpPerg.getForma().toString());
				assertEquals(wrap("Descricao da primeira filha da pergunta esta errada!"),
						tmpObj.getString("descricao"), tmpPerg.getDescricao());
				
				testAlternativas(tmpPerg.getAlternativas(), tmpObj.optJSONObject("alternativas"), true);
				testFilhas(tmpPerg.getFilhas(), tmpObj.optJSONObject("filhas"));
			}
			tmpObj = expected.optJSONObject("ultima");
			if(tmpObj != null){
				tmpPerg = fs.get(fs.size()-1);
				assertEquals(wrap("Forma da ultima filha da pergunta esta errada!"),
						tmpObj.getString("forma"), tmpPerg.getForma().toString());
				assertEquals(wrap("Descricao da ultima filha da pergunta esta errada!"),
						tmpObj.getString("descricao"), tmpPerg.getDescricao());
				
				testAlternativas(tmpPerg.getAlternativas(), tmpObj.optJSONObject("alternativas"), true);
				testFilhas(tmpPerg.getFilhas(), tmpObj.optJSONObject("filhas"));
			}
		}
	}

	private String wrap(String msg){
		StringBuilder builder = new StringBuilder();
		builder.append("\n<" +this.currentPath);
		if(!this.currentQ.equals("")){
			builder.append(" -> Questionarios[" +this.currentQ+ "]");
			if(!this.currentP.equals(""))
				builder.append(" -> Perguntas[" +this.currentP+ "]");
		}
		builder.append(">\n   " +msg+"\n      ");
		return builder.toString();
	}
}
