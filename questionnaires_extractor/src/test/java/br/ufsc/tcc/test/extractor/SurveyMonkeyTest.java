package br.ufsc.tcc.test.extractor;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.BeforeClass;
import org.junit.Test;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.connection.PostgreConnection;
import br.ufsc.tcc.common.util.Util;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;
import br.ufsc.tcc.extractor.extractor.SurveyMonkeyExtractor;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;

public class SurveyMonkeyTest {

	private static BasicConnection conn;
	private static SurveyMonkeyExtractor extractor;

	private String html;
	
	@BeforeClass
	public static void onStart(){
		// Necessario para carregar as informações necessarias
		// do banco de dados
		conn = new PostgreConnection(ProjectConfigs.getDatabaseConfigs());
		FormaDaPerguntaManager.loadFormas(conn);
		
		extractor = new SurveyMonkeyExtractor();
	}
	
	@Test
	public void allTests() {
		html = Util.readResource("SurveyMonkeyTests/AllElements.html");
		
		ArrayList<Questionario> qs = extractor.extract(html);
		assertEquals("Extrator deveria retornar 1 questionario", 1, qs.size());
		
		Questionario q = qs.get(0);
		assertEquals("Questionario com assunto errado", "Internet Relationships Template", q.getAssunto());		
		
		ArrayList<Pergunta> perguntas = q.getPerguntas();
		assertEquals("Questionario deveria ter 2 perguntas", 2, q.getPerguntas().size());
		
		testPerguntaDeTextarea(perguntas.get(0));
		testPerguntaDeRadioInput(perguntas.get(1));
	}

	private void testPerguntaDeTextarea(Pergunta p) {
		assertEquals("Pergunta deveria ser um TEXTAREA", "TEXTAREA", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"1. Which internet website do you use most often?", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 0 perguntas filhas", 0, p.getFilhas().size());	
		
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 0 alternativas", 0, alternativas.size());
	}
	
	private void testPerguntaDeRadioInput(Pergunta p) {
		assertEquals("Pergunta deveria ser um RADIO_INPUT", "RADIO_INPUT", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"2. When you're on social networking websites, about how much of your time do you spend posting things about yourself?", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 0 perguntas filhas", 0, p.getFilhas().size());	
		
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 5 alternativas", 5, alternativas.size());
		assertEquals("Descricao da primeira alternativa da pergunta esta errada",
				"All of it",
				alternativas.get(0).getDescricao());
		
		assertEquals("Descricao da ultima alternativa da pergunta esta errada",
				"None of it",
				alternativas.get(alternativas.size()-1).getDescricao());
	}
}
