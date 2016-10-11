package extractor;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;

import edu.uci.ics.crawler4j.url.WebURL;
import manager.FormaDaPerguntaManager;
import model.Alternativa;
import model.FormaDaPergunta;
import model.Pergunta;
import model.Questionario;

public class AlmadermaExtractor implements Extractor {
	
	private ArrayList<Questionario> questionarios;
	private Questionario currentQ;
	private Pergunta currentP;
	
	@Override
	public boolean shouldExtract(WebURL url) {
		String href = url.getURL().toLowerCase();
		return href.matches("http://www\\.almaderma\\.com\\.br/formulario/florais/.+/contato\\.php");
	}

	@Override
	public ArrayList<Questionario> extract(String html) {
		questionarios = new ArrayList<>();
		currentQ = new Questionario();
		
		Document doc = Jsoup.parse(html);
		String tmpTxt = "";
		FormaDaPergunta forma = FormaDaPerguntaManager.getForma("RADIO_INPUT");
		
		// Assunto questionario
		tmpTxt = this.getAssuntoQuestionario(doc);
		currentQ.setAssunto(tmpTxt);
		System.out.println("\t\tAssunto Questionario: " + tmpTxt);
		
		Elements trs = doc.select("form[name=form1] > table > tbody > tr"),
				tmp = null;
		
		// As primeiras perguntas são sempre as mesmas
		this.getPrimeirasPerguntas();
		
		// Outras perguntas
		int n = 0;
		List<TextNode> tnList = null;
		TextNode node = null;
		Alternativa tmpAlt = null;
		
		for(int i = 6; i<trs.size()-1; i++){
			tmp = trs.get(i).select("td");
			n = tmp.get(0).childNodeSize();
			if(n == 1 || n == 2)//Em algumas perguntas tem um <p> dentro da <td>
				tmp = tmp.select("p");
			
			tnList = new ArrayList<>(tmp.get(0).textNodes());
			
			// Pergunta
			tmpTxt = tnList.get(0).toString().trim();
			System.out.println("\t\t\tDescricao Pergunta: " + tmpTxt);
			currentP = new Pergunta(tmpTxt, "FECHADO", forma);
			
			// Alternativas
			for(int j = 1; j<tnList.size(); j++){
				node = tnList.get(j);
				if(!node.isBlank()){
					tmpTxt = node.toString().trim();
					System.out.println("\t\t\t\t\t" +tmpTxt);
					tmpAlt = new Alternativa(tmpTxt);
					currentP.addAlternativa(tmpAlt);
				}
			}
			currentQ.addPergunta(currentP);
		}
		questionarios.add(currentQ);
		return questionarios;
	}

	private void getPrimeirasPerguntas() {
		FormaDaPergunta forma = FormaDaPerguntaManager.getForma("TEXT_INPUT");
		
		//TODO fazer com um for?
		currentP = new Pergunta("Nome:", "ABERTO", forma);
		currentQ.addPergunta(currentP);

		currentP = new Pergunta("Telefone:", "ABERTO", forma);
		currentQ.addPergunta(currentP);

		currentP = new Pergunta("Celular:", "ABERTO", forma);
		currentQ.addPergunta(currentP);

		currentP = new Pergunta("E-mail:", "ABERTO", forma);
		currentQ.addPergunta(currentP);

		currentP = new Pergunta("Cidade:", "ABERTO", forma);
		currentQ.addPergunta(currentP);

		currentP = new Pergunta("Estado:", "ABERTO", forma);
		currentQ.addPergunta(currentP);
	}

	private String getAssuntoQuestionario(Document doc) {
		Elements tmp = doc.select("form[name=form1] > table > tbody > tr:nth-child(6) > td > div > font > strong");
		if(tmp.isEmpty()) return "";
		return tmp.get(0).ownText().trim();
	}

}
