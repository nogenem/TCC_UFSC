package br.ufsc.tcc.test.basic;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.json.JSONArray;
import org.json.JSONObject;
import org.junit.AfterClass;
import org.junit.BeforeClass;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.connection.PostgreConnection;
import br.ufsc.tcc.common.util.Util;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;
import br.ufsc.tcc.extractor.extractor.IExtractor;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Figura;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;

public abstract class BasicTest {
	
	private static BasicConnection conn;
	protected static IExtractor extractor;
	
	@BeforeClass
	public static void onBasicStart(){
		// Necessario para carregar as informações necessarias
		// do banco de dados
		conn = new PostgreConnection(ProjectConfigs.getDatabaseConfigs());
		FormaDaPerguntaManager.loadFormas(conn);
		
		System.out.println("BasicTest:onBasicStart()> ...");
	}
	
	@AfterClass
	public static void onBasicExit(){
		if(conn != null)
			conn.close();
		System.out.println("BasicTest:onBasicExit()> ...");
	}
	
	protected void allTests(String htmlPath, String expectedPath){
		String html = Util.readResource(htmlPath);
		JSONObject expected = Util.parseJson(Util.readResource(expectedPath)),
				tmpObj = null;
		JSONArray expQs = expected.getJSONArray("questionarios"),
				tmpArr = null;
		Questionario tmpQ = null;
		
		ArrayList<Questionario> qs = extractor.extract(html);
		assertEquals("Extrator deveria retornar "+expQs.length()+" questionario(s)",
				expQs.length(), qs.size());
		
		ArrayList<Pergunta> perguntas = null;
		for(int i = 0; i<expQs.length(); i++){
			tmpObj = expQs.getJSONObject(i);
			tmpQ = qs.get(i);
			
			assertEquals("Questionario "+i+" com assunto errado",
					tmpObj.getString("assunto"), tmpQ.getAssunto());
			
			tmpArr = tmpObj.getJSONArray("perguntas");
			perguntas = tmpQ.getPerguntas();
			assertEquals("Questionario "+i+" deveria ter "+tmpArr.length()+" pergunta(s)",
					tmpArr.length(), perguntas.size());
			
			for(int j = 0; j<tmpArr.length(); j++){
				testPerguntas(tmpArr.getJSONObject(j), perguntas.get(j));
			}
			
			testFiguras(tmpObj.optJSONObject("figuras"), tmpQ.getFiguras());
		}
 	}

	private void testPerguntas(JSONObject pergObj, Pergunta p) {
		if(pergObj.optString("forma").equals("")) return;
		
		// Teste pergunta
		assertEquals("Pergunta deveria ser um " +pergObj.getString("forma"), 
				pergObj.getString("forma"), p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				pergObj.getString("descricao"), 
				p.getDescricao());
		
		// Teste filhas
		testFilhas(pergObj, p);
		
		// Teste alternativas
		testAlternativas(pergObj, p);
	}

	private void testFilhas(JSONObject pergObj, Pergunta p) {
		JSONObject filhasObj, tmpObj;
		ArrayList<Pergunta> filhas;
		Pergunta tmpPerg;
		
		filhasObj = pergObj.optJSONObject("filhas");
		filhas = p.getFilhas();
		if(filhasObj != null){
			assertEquals("Pergunta deveria ter "+filhasObj.getInt("quantidade")+" pergunta(s) filha(s)", 
					filhasObj.getInt("quantidade"), filhas.size());
			
			if(filhas.size() >= 1){
				tmpObj = filhasObj.optJSONObject("primeira");
				if(tmpObj != null){
					tmpPerg = filhas.get(0);
					
					assertEquals("1* Pergunta filha deveria ser um "+tmpObj.getString("forma"), 
							tmpObj.getString("forma"), 
							tmpPerg.getForma().getDescricao());				
					assertEquals("Descricao da primeira Pergunta filha esta errada", 
							tmpObj.getString("descricao"), tmpPerg.getDescricao());
					
					tmpObj = tmpObj.optJSONObject("alternativas");
					if(tmpObj != null)
						testAlternativas(tmpObj, tmpPerg);
				}
				
				tmpObj = filhasObj.optJSONObject("ultima");
				if(tmpObj != null){
					tmpPerg = filhas.get(filhas.size()-1);
					
					assertEquals("Ultima Pergunta filha deveria ser um "+tmpObj.getString("forma"), 
							tmpObj.getString("forma"), 
							tmpPerg.getForma().getDescricao());				
					assertEquals("Descricao da ultima Pergunta filha esta errada", 
							tmpObj.getString("descricao"), tmpPerg.getDescricao());
					
					tmpObj = tmpObj.optJSONObject("alternativas");
					if(tmpObj != null)
						testAlternativas(tmpObj, tmpPerg);
				}
			}
		}else{
			assertEquals("Pergunta deveria ter 0 perguntas filhas", 
					0, filhas.size());	
		}
	}
	
	private void testAlternativas(JSONObject pergObj, Pergunta p) {
		JSONObject altsObj, tmpObj;
		ArrayList<Alternativa> alts;
		Alternativa tmpAlt;
		
		altsObj = pergObj.optJSONObject("alternativas");
		alts = p.getAlternativas();
		if(altsObj != null){
			assertEquals("Pergunta deveria ter "+altsObj.getInt("quantidade")+" alternativa(s)", 
					altsObj.getInt("quantidade"), alts.size());
			
			if(alts.size() >= 1){
				tmpObj = altsObj.optJSONObject("primeira");
				if(tmpObj != null){
					tmpAlt = alts.get(0);
				
					assertEquals("Descricao da primeira Alternativa esta errada", 
							tmpObj.getString("descricao"), tmpAlt.getDescricao());
				}
				
				tmpObj = altsObj.optJSONObject("ultima");
				if(tmpObj != null){
					tmpAlt = alts.get(alts.size()-1);
				
					assertEquals("Descricao da ultima Alternativa esta errada", 
							tmpObj.getString("descricao"), tmpAlt.getDescricao());
				}
			}else{
				assertEquals("Pergunta deveria ter 0 alternativas", 
						0, alts.size());	
			}
		}
	}
	
	private void testFiguras(JSONObject figObj, ArrayList<Figura> figuras) {
		JSONObject tmpObj;
		Figura tmpFig;
		
		if(figObj != null){
			assertEquals("Questionario deveria ter "+figObj.getInt("quantidade")+" figura(s)", 
					figObj.getInt("quantidade"), figuras.size());
			
			if(figuras.size() >= 1){
				tmpObj = figObj.optJSONObject("primeira");
				if(tmpObj != null){
					tmpFig = figuras.get(0);
					
					assertEquals("URL da primeira Figura do questionario esta errada",
							tmpObj.getString("url"), tmpFig.getImage_url());
					
					assertEquals("Legenda da primeira Figura do questionario esta errada",
							tmpObj.getString("legenda"), tmpFig.getLegenda());
				}
				
				tmpObj = figObj.optJSONObject("ultima");
				if(tmpObj != null){
					tmpFig = figuras.get(figuras.size()-1);
					
					assertEquals("URL da ultima Figura do questionario esta errada",
							tmpObj.getString("url"), tmpFig.getImage_url());
					
					assertEquals("Legenda da ultima Figura do questionario esta errada",
							tmpObj.getString("legenda"), tmpFig.getLegenda());
				}
			}
		}else{
			assertEquals("Questionario deveria ter 0 figuras", 
					0, figuras.size());
		}
	}
	
}
