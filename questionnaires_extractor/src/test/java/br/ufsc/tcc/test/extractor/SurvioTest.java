package br.ufsc.tcc.test.extractor;

import static org.junit.Assert.assertEquals;

import java.util.ArrayList;

import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.database.connection.BasicConnection;
import br.ufsc.tcc.common.database.connection.PostgreConnection;
import br.ufsc.tcc.common.util.Util;
import br.ufsc.tcc.extractor.database.manager.FormaDaPerguntaManager;
import br.ufsc.tcc.extractor.extractor.SurvioExtractor;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Figura;
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
	
	@Test
	public void allTests() {
		html = Util.readResource("SurvioTests/AllElements.html");
		
		ArrayList<Questionario> qs = extractor.extract(html);
		assertEquals("Extrator deveria retornar 1 questionario", 1, qs.size());
		
		Questionario q = qs.get(0);
		assertEquals("Questionario com assunto errado", "Pesquisa de Preço do Produto", q.getAssunto());		
		
		ArrayList<Pergunta> perguntas = q.getPerguntas();
		assertEquals("Questionario deveria ter 13 perguntas", 13, q.getPerguntas().size());
		
		testPerguntaDeRadioInputComTextInput(perguntas.get(0));
		testPerguntaDeRadioInputSemTextInput(perguntas.get(1));
		testPerguntaDeRadioInputMatrix(perguntas.get(2));
		testPerguntaDeTextInputMatrix(perguntas.get(3));
		testPerguntaDeCheckboxInputComTextInput(perguntas.get(4));
		testPerguntaDeCheckboxInputSemTextInput(perguntas.get(5));
		testPerguntaDeTextarea(perguntas.get(6));
		testPerguntaDeNumberInput(perguntas.get(7));
		testPerguntaDeTextInput(perguntas.get(8));
		testPerguntaDeRangeInputGroup(perguntas.get(9));
		testPerguntaDeSelect(perguntas.get(10));
		testPerguntaDeStars(perguntas.get(11));
		testPerguntaDeImageRadioInput(perguntas.get(12), q.getFiguras());
	}

	private void testPerguntaDeRadioInputComTextInput(Pergunta p){
		assertEquals("Pergunta deveria ser um RADIO_INPUT", "RADIO_INPUT", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"Se você sabia que o produto \"product\" custava \"price\", você pagaria mais ou menos para comprá-lo?", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 1 pergunta filha", 1, p.getFilhas().size());		
		Pergunta filha = p.getFilhas().get(0);
		assertEquals("Pergunta filha deveria ser um TEXT_INPUT", "TEXT_INPUT", filha.getForma().getDescricao());				
		assertEquals("Descricao da ultima alternativa da pergunta esta errada", 
				"Outro (por favor, indique o valor)", filha.getDescricao());
		
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 2 alternativas", 2, alternativas.size());
		assertEquals("Descricao da primeira alternativa da pergunta esta errada",
				"10-20% mais",
				alternativas.get(0).getDescricao());
	}
	
	private void testPerguntaDeRadioInputSemTextInput(Pergunta p){
		assertEquals("Pergunta deveria ser um RADIO_INPUT", "RADIO_INPUT", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"Se você sabia que o produto \"product\" custava \"price\", você pagaria mais ou menos para comprá-lo?", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 0 perguntas filhas", 0, p.getFilhas().size());	
		
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 3 alternativas", 3, alternativas.size());
		assertEquals("Descricao da primeira alternativa da pergunta esta errada",
				"10-20% mais",
				alternativas.get(0).getDescricao());
		
		assertEquals("Descricao da ultima alternativa da pergunta esta errada",
				"nem menos nem mais",
				alternativas.get(alternativas.size()-1).getDescricao());
	}
	
	private void testPerguntaDeRadioInputMatrix(Pergunta p) {
		assertEquals("Pergunta deveria ser um RADIO_INPUT_MATRIX", "RADIO_INPUT_MATRIX", 
				p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"Se o preço do produto \"product\" fosse maior/menor, quantas unidades a mais/menos você compraria?\n"
				+ "Consider the next year at each price point listed.", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 6 perguntas filhas", 6, p.getFilhas().size());	
		Pergunta filha = p.getFilhas().get(0);
		assertEquals("Pergunta filha deveria ser um RADIO_INPUT", "RADIO_INPUT", filha.getForma().getDescricao());	
		assertEquals("Descricao da primeira pergunta filha esta errada", 
				"+20%", filha.getDescricao());
		
		ArrayList<Alternativa> alternativas = filha.getAlternativas();
		assertEquals("Pergunta filha deveria ter 4 alternativas", 4, alternativas.size());
		assertEquals("Descricao da primeira alternativa da primeira pergunta filha esta errada", 
				"4 ou mais a menos", alternativas.get(0).getDescricao());
	}
	
	private void testPerguntaDeTextInputMatrix(Pergunta p) {
		assertEquals("Pergunta deveria ser um TEXT_INPUT_MATRIX", "TEXT_INPUT_MATRIX", 
				p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"Quanto você pensou que era o preço do \"produto\"?", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 1 pergunta filha1", 1, p.getFilhas().size());	
		Pergunta filha = p.getFilhas().get(0);
		assertEquals("Pergunta filha deveria ser um TEXT_INPUT", "TEXT_INPUT", filha.getForma().getDescricao());
		assertEquals("Descricao da primeira pergunta filha esta errada", 
				"Digite o seu preço", filha.getDescricao());
		
		ArrayList<Alternativa> alternativas = filha.getAlternativas();
		assertEquals("Pergunta filha deveria ter 1 alternativa", 1, alternativas.size());
		assertEquals("Descricao da primeira alternativa da primeira pergunta filha esta errada", 
				"Preço", alternativas.get(0).getDescricao());
	}
	
	private void testPerguntaDeCheckboxInputComTextInput(Pergunta p){
		assertEquals("Pergunta deveria ser um CHECKBOX_INPUT", "CHECKBOX_INPUT", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"Qual das opções seguintes, tirando o próprio produto, lhe influenciaria mais na decisão de comprá-lo?", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 1 pergunta filha", 1, p.getFilhas().size());		
		Pergunta filha = p.getFilhas().get(0);
		assertEquals("Pergunta filha deveria ser um TEXT_INPUT", "TEXT_INPUT", filha.getForma().getDescricao());	
		assertEquals("Descricao da ultima alternativa da pergunta esta errada", 
				"Outro (por favor, especifique):", filha.getDescricao());
		
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 2 alternativas", 2, alternativas.size());
		assertEquals("Descricao da primeira alternativa da pergunta esta errada",
				"Experiência de outros clientes",
				alternativas.get(0).getDescricao());
	}
	
	private void testPerguntaDeCheckboxInputSemTextInput(Pergunta p){
		assertEquals("Pergunta deveria ser um CHECKBOX_INPUT", "CHECKBOX_INPUT", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"Qual das opções seguintes, tirando o próprio produto, lhe influenciaria mais na decisão de comprá-lo?", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 0 perguntas filhas", 0, p.getFilhas().size());		
	
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 3 alternativas", 3, alternativas.size());
		assertEquals("Descricao da primeira alternativa da pergunta esta errada",
				"Experiência de outros clientes",
				alternativas.get(0).getDescricao());
		
		assertEquals("Descricao da ultima alternativa da pergunta esta errada",
				"Tendências",
				alternativas.get(alternativas.size()-1).getDescricao());
	}
	
	private void testPerguntaDeTextarea(Pergunta p){
		assertEquals("Pergunta deveria ser um TEXTAREA", "TEXTAREA", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"Se há alguma coisa, o que você gosta mais (menos) sobre o \"produto\"?", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 0 perguntas filhas", 0, p.getFilhas().size());	
		
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 0 alternativas", 0, alternativas.size());
	}
	
	private void testPerguntaDeNumberInput(Pergunta p) {
		assertEquals("Pergunta deveria ser um NUMBER_INPUT", "NUMBER_INPUT", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"Por favor, especifique a sua idade:", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 0 perguntas filhas", 0, p.getFilhas().size());		
	
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 0 alternativas", 0, alternativas.size());
	}
	
	private void testPerguntaDeTextInput(Pergunta p) {
		assertEquals("Pergunta deveria ser um TEXT_INPUT", "TEXT_INPUT", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"Qual é o seu canal de TV favorito?", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 0 perguntas filhas", 0, p.getFilhas().size());		
	
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 0 alternativas", 0, alternativas.size());
	}
	
	private void testPerguntaDeRangeInputGroup(Pergunta p) {
		assertEquals("Pergunta deveria ser um RANGE_INPUT_GROUP", "RANGE_INPUT_GROUP", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"Por favor, especifique o número de horas que você gasta por semana:", 
				p.getDescricao());
		
		String[] descs = {
				"Trabalhando",
				"Dormindo ou descansando",
				"Em atividades esportivas ou de lazer"
		};
		ArrayList<Pergunta> filhas = p.getFilhas();
		Pergunta filha = null;

		assertEquals("Pergunta deveria ter 3 perguntas filhas", 3, filhas.size());	
		for(int i = 0; i<filhas.size(); i++){
			filha = filhas.get(i);
			assertEquals("Pergunta filha deveria ser um RANGE_INPUT", "RANGE_INPUT", filha.getForma().getDescricao());
			assertEquals("Descricao da pergunta filha esta errada", 
					descs[i], filha.getDescricao());
		}
	}
	
	private void testPerguntaDeSelect(Pergunta p) {
		assertEquals("Pergunta deveria ser um SELECT", "SELECT", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"Que tipo de roupa você gosta de usar na maioria dos esportes?", 
				p.getDescricao());
		
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 5 alternativas", 5, alternativas.size());
		assertEquals("Descricao da primeira alternativa da pergunta esta errada",
				"Escolher",
				alternativas.get(0).getDescricao());
		
		assertEquals("Descricao da ultima alternativa da pergunta esta errada",
				"Roupa de marca, estilo ou conforto realmente não me incomodam",
				alternativas.get(alternativas.size()-1).getDescricao());
	}
	
	private void testPerguntaDeStars(Pergunta p) {
		assertEquals("Pergunta deveria ser um STARS", "STARS", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"Como você avalia a publicidade no sentido de poder motivador?", 
				p.getDescricao());
		
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 1 alternativas", 1, alternativas.size());
		assertEquals("Descricao da primeira alternativa da pergunta esta errada",
				"[0, 5]",
				alternativas.get(0).getDescricao());
	}
	
	private void testPerguntaDeImageRadioInput(Pergunta p, ArrayList<Figura> figuras) {
		assertEquals("Pergunta deveria ser um RADIO_INPUT", "RADIO_INPUT", p.getForma().getDescricao());
		
		assertEquals("Descricao da pergunta esta errada", 
				"Quais impressões você tem da publicidade?", 
				p.getDescricao());
		
		assertEquals("Pergunta deveria ter 0 perguntas filhas", 0, p.getFilhas().size());	
		
		ArrayList<Alternativa> alternativas = p.getAlternativas();
		assertEquals("Pergunta deveria ter 2 alternativas", 2, alternativas.size());
		assertEquals("Descricao da primeira alternativa da pergunta esta errada",
				"Positivas",
				alternativas.get(0).getDescricao());
		
		assertEquals("Descricao da ultima alternativa da pergunta esta errada",
				"Negativas",
				alternativas.get(alternativas.size()-1).getDescricao());
		
		assertEquals("Questionario deveria ter 2 figuras", 2, figuras.size());
		assertEquals("Image_url da primeira figura esta errada", 
				"surviocdn.com/s1/user-img/options/c1e/c1e420e8860aa6a1aa97bcc94ee2bb6e98d38791.jpg", 
				figuras.get(0).getImage_url());
		assertEquals("Legenda da primeira figura esta errada", 
				"Positivas", 
				figuras.get(0).getLegenda());
	}
	
	@AfterClass
	public static void onExit(){
		conn.close();
	}
}
