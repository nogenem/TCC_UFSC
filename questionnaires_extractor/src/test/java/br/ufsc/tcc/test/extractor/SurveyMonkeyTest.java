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
	
	//TODO PAREI EM 'Education Demographics Survey'
	
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
		assertEquals("Questionario deveria ter 12 perguntas", 12, q.getPerguntas().size());
		
		testPerguntaDeTextarea(perguntas.get(0));
		testPerguntaDeRadioInput(perguntas.get(1));
		testPerguntaDeRating1(perguntas.get(2));
		testPerguntaDeCheckboxInputSemTextInput(perguntas.get(3));
		testPerguntaDeRadioInputComTextInput(perguntas.get(4));
		testPerguntaDeTextInput(perguntas.get(5));
		testPerguntaDeRating2(perguntas.get(6));
		testPerguntaDeCheckboxInputComTextInput(perguntas.get(7));
		testPerguntaDeRangeInput(perguntas.get(8));
		testPerguntaDeSelect(perguntas.get(9));
		testPerguntaDeTextInputGroup1(perguntas.get(10));
		testPerguntaDeTextInputGroup2(perguntas.get(11));
	}

	//Ex: https://www.surveymonkey.com/r/customer-satisfaction-survey-template?sm=d9yyh03hx%2fRxh26ptsvay03MP0ZkErSidp5ni5TkqGw%3d
	private void testPerguntaDeTextarea(Pergunta p) {
		assertEquals("Pergunta deveria ser um TEXTAREA", "TEXTAREA", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"1. Which internet website do you use most often?", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 0 perguntas filhas", 0, p.getFilhas().size());	
		
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 0 alternativas", 0, alternativas.size());
	}
	
	//Ex: https://www.surveymonkey.com/r/customer-satisfaction-survey-template?sm=d9yyh03hx%2fRxh26ptsvay03MP0ZkErSidp5ni5TkqGw%3d
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
	
	//Ex: https://www.surveymonkey.com/r/customer-satisfaction-survey-template?sm=d9yyh03hx%2fRxh26ptsvay03MP0ZkErSidp5ni5TkqGw%3d
	private void testPerguntaDeRating1(Pergunta p) {
		assertEquals("Pergunta deveria ser um RATING", "RATING", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"1. How likely is it that you would recommend this company to a friend or colleague?", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 0 perguntas filhas", 0, p.getFilhas().size());	
		
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 11 alternativas", 11, alternativas.size());
		assertEquals("Descricao da primeira alternativa da pergunta esta errada",
				"Not at all likely - 0",
				alternativas.get(0).getDescricao());
		
		assertEquals("Descricao da ultima alternativa da pergunta esta errada",
				"Extremely likely - 10",
				alternativas.get(alternativas.size()-1).getDescricao());
	}
	
	//Ex: https://www.surveymonkey.com/r/customer-satisfaction-survey-template?sm=d9yyh03hx%2fRxh26ptsvay03MP0ZkErSidp5ni5TkqGw%3d
	private void testPerguntaDeCheckboxInputSemTextInput(Pergunta p){
		assertEquals("Pergunta deveria ser um CHECKBOX_INPUT", "CHECKBOX_INPUT", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"3. Which of the following words would you use to describe our products? Select all that apply.", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 0 perguntas filhas", 0, p.getFilhas().size());		
	
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 10 alternativas", 10, alternativas.size());
		assertEquals("Descricao da primeira alternativa da pergunta esta errada",
				"Reliable",
				alternativas.get(0).getDescricao());
		
		assertEquals("Descricao da ultima alternativa da pergunta esta errada",
				"Unreliable",
				alternativas.get(alternativas.size()-1).getDescricao());
	}
	
	//Ex: https://www.surveymonkey.com/r/Net-Promoter-Score-Template [pag 3]
	private void testPerguntaDeRadioInputComTextInput(Pergunta p){
		assertEquals("Pergunta deveria ser um RADIO_INPUT", "RADIO_INPUT", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"6. In what country do you currently reside?", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 2 pergunta filha", 2, p.getFilhas().size());		
		Pergunta filha = p.getFilhas().get(0);
		assertEquals("Primeira pergunta filha deveria ser um TEXT_INPUT", "TEXT_INPUT", filha.getForma().getDescricao());				
		assertEquals("Descricao da penultima alternativa da pergunta esta errada", 
				"Other (please specify)", filha.getDescricao());
		
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 1 alternativa", 1, alternativas.size());
		assertEquals("Descricao da primeira alternativa da pergunta esta errada",
				"United States",
				alternativas.get(0).getDescricao());
	}
	
	//Ex: https://www.surveymonkey.com/r/Bullying-Survey?sm=vPn5rCklmOnA1n%2bf7b1KISJfj7qNDk%2bViYRPQEMncWM%3d
	private void testPerguntaDeTextInput(Pergunta p) {
		assertEquals("Pergunta deveria ser um TEXT_INPUT", "TEXT_INPUT", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"1. During the last week, how many times, if any, has someone made fun of you, called you names, or insulted you?", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 0 perguntas filhas", 0, p.getFilhas().size());		
	
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 0 alternativas", 0, alternativas.size());
	}
	
	//Ex: https://www.surveymonkey.com/r/CAHPS-Dental-Plan-Survey-Template
	private void testPerguntaDeRating2(Pergunta p) {
		assertEquals("Pergunta deveria ser um RATING", "RATING", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"10. Using any number from 0 to 10, where 0 is the worst regular dentist possible and 10 is the best "
						+ "regular dentist possible, what number would you use to rate your regular dentist?", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 0 perguntas filhas", 0, p.getFilhas().size());	
		
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 11 alternativas", 11, alternativas.size());
		assertEquals("Descricao da primeira alternativa da pergunta esta errada",
				"10 Best regular dentist possible",
				alternativas.get(0).getDescricao());
		
		assertEquals("Descricao da ultima alternativa da pergunta esta errada",
				"0 Worst regular dentist possible",
				alternativas.get(alternativas.size()-1).getDescricao());
	}
	
	//Ex: https://www.surveymonkey.com/r/Net-Promoter-Score-Template [pag 3]
	private void testPerguntaDeCheckboxInputComTextInput(Pergunta p){
		assertEquals("Pergunta deveria ser um CHECKBOX_INPUT", "CHECKBOX_INPUT", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"39. How did that person help you? Check all that apply.", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 1 pergunta filha", 1, p.getFilhas().size());		
		Pergunta filha = p.getFilhas().get(0);
		assertEquals("Primeira pergunta filha deveria ser um TEXT_INPUT", "TEXT_INPUT", filha.getForma().getDescricao());				
		assertEquals("Descricao da ultima alternativa da pergunta esta errada", 
				"Helped in some other way", filha.getDescricao());
		
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 4 alternativas", 4, alternativas.size());
		assertEquals("Descricao da primeira alternativa da pergunta esta errada",
				"Read the questions to me",
				alternativas.get(0).getDescricao());
	}
	
	//Ex: https://www.surveymonkey.com/r/Diet-Exercise-Template
	private void testPerguntaDeRangeInput(Pergunta p) {
		assertEquals("Pergunta deveria ser um RANGE_INPUT", "RANGE_INPUT", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"1. How physically healthy are you?", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 0 perguntas filhas", 0, p.getFilhas().size());
		
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 1 alternativa", 1, alternativas.size());
		assertEquals("Descricao da alternativa da pergunta esta errada",
				"[Not at all healthy, Extremely healthy]",
				alternativas.get(0).getDescricao());
	}
	
	//Ex: https://www.surveymonkey.com/r/Education-Demographics-Template
	private void testPerguntaDeSelect(Pergunta p) {
		assertEquals("Pergunta deveria ser um SELECT", "SELECT", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"15. What is the highest level of education you have completed?", 
				p.getDescricao());
		
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 20 alternativas", 20, alternativas.size());
		assertEquals("Descricao da primeira alternativa da pergunta esta errada",
				"",
				alternativas.get(0).getDescricao());
		
		assertEquals("Descricao da ultima alternativa da pergunta esta errada",
				"Completed graduate school",
				alternativas.get(alternativas.size()-1).getDescricao());
	}
	
	//Ex: https://www.surveymonkey.com/r/US-Voting-HIstory-Template3
	private void testPerguntaDeTextInputGroup1(Pergunta p) {
		assertEquals("Pergunta deveria ser um TEXT_INPUT_GROUP", "TEXT_INPUT_GROUP", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"1. Of all the political candidates you voted for during the last 10 years, "
				+ "what percent of them were Republicans, what percent were Democrats, what percent "
				+ "were Independents, and what percent were something else? (Your answers should add up to 100%.)", 
				p.getDescricao());
		
		String[] descs = {
				"Percent Republicans",
				"Percent Democrats",
				"Percent Independents",
				"Percent something else"
		};
		ArrayList<Pergunta> filhas = p.getFilhas();
		Pergunta filha = null;

		assertEquals("Pergunta deveria ter 4 perguntas filhas", 4, filhas.size());	
		for(int i = 0; i<filhas.size(); i++){
			filha = filhas.get(i);
			assertEquals("Pergunta filha deveria ser um TEXT_INPUT", "TEXT_INPUT", filha.getForma().getDescricao());
			assertEquals("Descricao da pergunta filha esta errada", 
					descs[i], filha.getDescricao());
		}
	}
	
	//Ex: https://www.surveymonkey.com/r/online-photo-sharing-template
	private void testPerguntaDeTextInputGroup2(Pergunta p) {
		assertEquals("Pergunta deveria ser um TEXT_INPUT_GROUP", "TEXT_INPUT_GROUP", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"4. In a typical week, about how much time do you spend using photo sharing websites?", 
				p.getDescricao());
		
		String[] descs = {
				"Hours",
				"Minutes"
		};
		ArrayList<Pergunta> filhas = p.getFilhas();
		Pergunta filha = null;

		assertEquals("Pergunta deveria ter 2 perguntas filhas", 2, filhas.size());	
		for(int i = 0; i<filhas.size(); i++){
			filha = filhas.get(i);
			assertEquals("Pergunta filha deveria ser um TEXT_INPUT", "TEXT_INPUT", filha.getForma().getDescricao());
			assertEquals("Descricao da pergunta filha esta errada", 
					descs[i], filha.getDescricao());
		}
	}
}
