package br.ufsc.tcc.crawler.checker;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.Dewey;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.model.MyNodeType;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.common.util.CommonUtil;
import br.ufsc.tcc.common.util.DistanceMatrix;
import edu.uci.ics.crawler4j.parser.HtmlParseData;

public class RulesChecker {
	
	private static Pattern SURVEY_WORDS_REGEX = null;
	private static Pattern PHRASES_TO_IGNORE_REGEX = null;
	private static int MIN_COMPS_IN_ONE_CLUSTER = 0;
	private static int MIN_CLUSTERS_WITH_COMP = 0;
	private static int HEIGHT_BETWEEN_QUESTIONS = 0;

	private DistanceMatrix distMatrix;
	private ClusterBuilder builder;
	
	// Construtores
	public RulesChecker(){
		this.distMatrix = new DistanceMatrix();
		this.builder = new ClusterBuilder();
	}
	
	// Demais métodos
	public boolean shouldSave(HtmlParseData htmlParseData) {
		String html = htmlParseData.getHtml();
		Document doc = Jsoup.parse(html);
		return this.shouldSave(doc);
	}
	
	public boolean shouldSave(Document doc){
		Element root = doc.select("body").get(0);
		
		//TODO realmente deixar assim? [tentar encontrar um questionario que quebre essa regra]
		if(!SURVEY_WORDS_REGEX.matcher(root.text()).matches() && 
				!SURVEY_WORDS_REGEX.matcher(doc.title()).matches()){
			this.distMatrix.clear();
			return false;
		}
		
		List<MyNode> nodes = CommonUtil.findCompsImgsAndTexts(root);
		List<Cluster> clusters = this.builder.build(nodes, this.distMatrix);
		
		CommonLogger.debug(clusters);
		
		boolean ret = hasQuestionnaire(clusters);
		this.distMatrix.clear();
		
		return ret;
	}
	
	private boolean hasQuestionnaire(List<Cluster> clusters) {
		//Contador de componentes, Contador de 'questões'
		double cCount = 0.0, qCount = 0.0;
		Cluster lastCluster = null;
		
		//Um questionario deve ter pelo menos 1 cluster com X componentes ou
		//X clusters com pelo menos 1 componente
		for(Cluster c : clusters){
			cCount = getCountOfComps(c);
			
			//Toda questão deve ter pelo menos 1 componente e não deve possuir
			//frases contidas no: PHRASES_TO_IGNORE_REGEX
			CommonLogger.debug("cCount: " +cCount+ "; qCount: " +qCount);
			if(cCount >= 1){
				if(cCount >= MIN_COMPS_IN_ONE_CLUSTER){
					CommonLogger.debug("\n\t\t===> Minimo {} componentes em um cluster!", MIN_COMPS_IN_ONE_CLUSTER);
					CommonLogger.debug("\nLast cluster: \n" +c.toString());//TODO remover isso
					return true;
				}else{
					if(lastCluster != null){
						if(!isStartingANewQuestionnaire(lastCluster, c))
							qCount++;
						else
							qCount = 1;
					}else
						qCount++;
				}
			}
			if(qCount == MIN_CLUSTERS_WITH_COMP){
				CommonLogger.debug("\n\t\t===> Minimo {} clusters com componentes!", MIN_CLUSTERS_WITH_COMP);
				CommonLogger.debug("\nLast cluster: \n" +c.toString());//TODO remover isso
				return true;
			}
			lastCluster = c;
		}
		return false;
	}
	
	private double getCountOfComps(Cluster c){
		double count = 0.0;
		boolean hasDescriptionAbove = false, multiComp = false;
		ArrayList<MyNode> nodes = c.getGroup();
		
		for(int i = 0; i < nodes.size(); i++){
			MyNode node = nodes.get(i);
			if(node.isText()){ 
				if(PHRASES_TO_IGNORE_REGEX.matcher(node.getText()).matches()){ 
					count = -1;
					break;
				}
				if(multiComp){
					hasDescriptionAbove = true;
					multiComp = false;
				}else
					hasDescriptionAbove = isLikelyADescription(node.getText());
			}else if(node.isComponent() && node.getType() != MyNodeType.OPTION){
				//Toda pergunta deve ter pelo menos uma descricao acima dela
				if(hasDescriptionAbove){
					//RADIO_INPUT e CHECKBOX sempre aparecem em 2 ou + em uma pergunta, por isso
					//cada um conta como 0.5
					if(CommonUtil.getMultiComps().contains(node.getText())){
						count += 0.5;
						multiComp = true;
					}else{
						count++;
						multiComp = false;
					}
				}
				//Checa se tem um componente também abaixo, para casos de RATING ou MULTI_COMP
				//Ex: https://www.survio.com/modelo-de-pesquisa/avaliacao-de-um-e-shop [9* questão]
				hasDescriptionAbove = (multiComp && i+1 < nodes.size()) ? 
						nodes.get(i+1).isComponent() : false;
			}
		}
		return count;
	}
	
	private boolean isLikelyADescription(String text){
		return (text.length() >= 4 && text.contains(" ")) || 
				text.matches("(\\d{1,3}(\\s{1,2})?(\\.|\\:|\\)|\\-)?)");
	}
	
	private boolean isStartingANewQuestionnaire(Cluster lastCluster, Cluster newCluster) {
		Dewey dist = this.distMatrix.getDist(lastCluster.last(), newCluster.first());	
		if(dist.getHeight() > HEIGHT_BETWEEN_QUESTIONS)
			return true;
		
		//Verifica se o 1* container, depois do BODY, é diferente
		Dewey d1 = lastCluster.last().getDewey(), d2 = newCluster.first().getDewey();
		return d1.getNumbers().get(1) != d2.getNumbers().get(1);
	}
	
	// Métodos/Blocos estáticos
	static {
		//Load heuristics
		JSONObject h = ProjectConfigs.getHeuristics(), tmp = null;
		
		try{
			String txtTmp = h.getString("surveyWordsRegex");
			SURVEY_WORDS_REGEX = Pattern.compile(txtTmp, 
					Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
			
			txtTmp = h.getString("phrasesToIgnoreRegex");
			PHRASES_TO_IGNORE_REGEX = Pattern.compile(txtTmp, 
					Pattern.CASE_INSENSITIVE);
			
			MIN_COMPS_IN_ONE_CLUSTER = h.getInt("minCompsInOneCluster");
			if(MIN_COMPS_IN_ONE_CLUSTER <= 0) MIN_COMPS_IN_ONE_CLUSTER = 4;
			
			MIN_CLUSTERS_WITH_COMP = h.getInt("minClustersWithComp");
			if(MIN_CLUSTERS_WITH_COMP <= 0) MIN_CLUSTERS_WITH_COMP = 3;
	
			tmp = h.getJSONObject("distBetweenNearQuestions");
			HEIGHT_BETWEEN_QUESTIONS = tmp.optInt("height");
			if(HEIGHT_BETWEEN_QUESTIONS <= 0) HEIGHT_BETWEEN_QUESTIONS = 4;
		}catch(JSONException exp){
			CommonLogger.fatalError(exp);
		}
		CommonLogger.debug("RulesChecker:> Static block executed!");
	}

}
