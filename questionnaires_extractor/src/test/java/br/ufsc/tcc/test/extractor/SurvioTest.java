package br.ufsc.tcc.test.extractor;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.connection.PostgreConnection;
import br.ufsc.tcc.common.util.Util;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;
import br.ufsc.tcc.extractor.extractor.SurvioExtractor;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;

public class SurvioTest {
	
	private static BasicConnection conn;
	private static SurvioExtractor extractor;

	private String html;
	
	@BeforeClass
	public static void onStart(){
		// Necessario para carregar as informações necessarias
		// do banco de dados
		conn = new PostgreConnection(ProjectConfigs.getDatabaseConfigs());
		FormaDaPerguntaManager.loadFormas(conn);
		
		extractor = new SurvioExtractor();
	}
	
	//TODO testar DATE_INPUT?
	//TODO refinar os testes verificando texto tb
	//TODO terminar os q faltam
	
	@Test
	public void allTests() {
		html = Util.readResource("SurvioTests/AllElements.html");
		
		ArrayList<Questionario> qs = extractor.extract(html);
		assertEquals("Extrator deveria retornar 1 questionario", 1, qs.size());
		
		Questionario q = qs.get(0);
		assertEquals("Questionario com assunto errado", "Pesquisa de Preço do Produto", q.getAssunto());		
		
		ArrayList<Pergunta> perguntas = q.getPerguntas();
		assertEquals("Questionario deve ter 9 perguntas", 9, q.getPerguntas().size());
		
		testRadioInputComTextInputPergunta(perguntas.get(0));
		testRadioInputSemTextInputPergunta(perguntas.get(1));
		testRadioInputMatrixPergunta(perguntas.get(2));
		testTextInputMatrixPergunta(perguntas.get(3));
		testCheckboxInputComTextInputPergunta(perguntas.get(4));
		testCheckboxInputSemTextInputPergunta(perguntas.get(5));
		testTextareaPergunta(perguntas.get(6));
		testNumberInputPergunta(perguntas.get(7));
		testTextInputPergunta(perguntas.get(8));
	}

	public void testRadioInputComTextInputPergunta(Pergunta p){
		assertEquals("Pergunta deveria ser um RADIO_INPUT", "RADIO_INPUT", p.getForma().getDescricao());
		assertEquals("Pergunta deveria ter 1 pergunta filha", 1, p.getFilhas().size());		
		
		Pergunta filha = p.getFilhas().get(0);
		assertEquals("Pergunta filha deveria ser um TEXT_INPUT", "TEXT_INPUT", filha.getForma().getDescricao());				
		
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 2 alternativas", 2, alternativas.size());
	}
	
	public void testRadioInputSemTextInputPergunta(Pergunta p){
		assertEquals("Pergunta deveria ser um RADIO_INPUT", "RADIO_INPUT", p.getForma().getDescricao());
		assertEquals("Pergunta deveria ter 0 perguntas filhas", 0, p.getFilhas().size());		
	
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 3 alternativas", 3, alternativas.size());
	}
	
	private void testRadioInputMatrixPergunta(Pergunta p) {
		assertEquals("Pergunta deveria ser um RADIO_INPUT_MATRIX", "RADIO_INPUT_MATRIX", 
				p.getForma().getDescricao());
		assertEquals("Pergunta deveria ter 6 perguntas filhas", 6, p.getFilhas().size());	
		
		Pergunta filha = p.getFilhas().get(0);
		assertEquals("Pergunta filha deveria ser um RADIO_INPUT", "RADIO_INPUT", filha.getForma().getDescricao());				
		
		ArrayList<Alternativa> alternativas = filha.getAlternativas();
		assertEquals("Pergunta filha deveria ter 4 alternativas", 4, alternativas.size());
	}
	
	private void testTextInputMatrixPergunta(Pergunta p) {
		assertEquals("Pergunta deveria ser um TEXT_INPUT_MATRIX", "TEXT_INPUT_MATRIX", 
				p.getForma().getDescricao());
		assertEquals("Pergunta deveria ter 1 pergunta filha1", 1, p.getFilhas().size());	
		
		Pergunta filha = p.getFilhas().get(0);
		assertEquals("Pergunta filha deveria ser um TEXT_INPUT", "TEXT_INPUT", filha.getForma().getDescricao());				
		
		ArrayList<Alternativa> alternativas = filha.getAlternativas();
		assertEquals("Pergunta filha deveria ter 1 alternativa",1, alternativas.size());
	}
	
	public void testCheckboxInputComTextInputPergunta(Pergunta p){
		assertEquals("Pergunta deveria ser um CHECKBOX_INPUT", "CHECKBOX_INPUT", p.getForma().getDescricao());
		assertEquals("Pergunta deveria ter 1 pergunta filha", 1, p.getFilhas().size());		
		
		Pergunta filha = p.getFilhas().get(0);
		assertEquals("Pergunta filha deveria ser um TEXT_INPUT", "TEXT_INPUT", filha.getForma().getDescricao());				
		
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 2 alternativas", 2, alternativas.size());
	}
	
	public void testCheckboxInputSemTextInputPergunta(Pergunta p){
		assertEquals("Pergunta deveria ser um CHECKBOX_INPUT", "CHECKBOX_INPUT", p.getForma().getDescricao());
		assertEquals("Pergunta deveria ter 0 perguntas filhas", 0, p.getFilhas().size());		
	
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 3 alternativas", 3, alternativas.size());
	}
	
	public void testTextareaPergunta(Pergunta p){
		assertEquals("Pergunta deveria ser um TEXTAREA", "TEXTAREA", p.getForma().getDescricao());
		assertEquals("Pergunta deveria ter 0 perguntas filhas", 0, p.getFilhas().size());	
		
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 0 alternativas", 0, alternativas.size());
	}
	
	private void testNumberInputPergunta(Pergunta p) {
		assertEquals("Pergunta deveria ser um NUMBER_INPUT", "NUMBER_INPUT", p.getForma().getDescricao());
		assertEquals("Pergunta deveria ter 0 perguntas filhas", 0, p.getFilhas().size());		
	
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 0 alternativas", 0, alternativas.size());
	}
	
	private void testTextInputPergunta(Pergunta p) {
		assertEquals("Pergunta deveria ser um TEXT_INPUT", "TEXT_INPUT", p.getForma().getDescricao());
		assertEquals("Pergunta deveria ter 0 perguntas filhas", 0, p.getFilhas().size());		
	
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 0 alternativas", 0, alternativas.size());
	}
	
	@AfterClass
	public static void onExit(){
		conn.close();
	}
}
