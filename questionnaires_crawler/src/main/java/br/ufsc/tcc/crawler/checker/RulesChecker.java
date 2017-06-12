package br.ufsc.tcc.crawler.checker;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.DeweyExt;
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
	private static int MAX_CLUSTERS_BETWEEN_CLUSTERS_WITH_COMP = 0;
	private static int HEIGHT_BETWEEN_QUESTIONS = 0;

	private DistanceMatrix distMatrix;
	private ClusterBuilder builder;
	private boolean lastWasAMatrix;
	
	// Construtores
	public RulesChecker(){
		this.distMatrix = new DistanceMatrix();
		this.builder = new ClusterBuilder();
		this.lastWasAMatrix = false;
	}
	
	// Demais métodos
	public boolean shouldSave(HtmlParseData htmlParseData) {
		String html = htmlParseData.getHtml();
		Document doc = Jsoup.parse(html);
		return this.shouldSave(doc);
	}
	
	public boolean shouldSave(Document doc){
		Elements tmp = doc.select("body");
		if(tmp.isEmpty()) return false;
		
		Element root = tmp.get(0);
		
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
		//Contador de componentes, Contador de 'questões', Contador de clusters sem componentes
		double cCount = 0.0;
		int qCount = 0, ncCount = 0;
		Cluster lastCluster = null;
		
		//Um questionario deve ter pelo menos 1 cluster com X componentes ou
		//X clusters com pelo menos 1 componente
		for(int i = 0; i<clusters.size(); i++){
			Cluster c = clusters.get(i);
			cCount = getCountOfComps(c);
			
			CommonLogger.debug("<{}>cCount: {}; qCount: {};", i, cCount, qCount);
			
			//Atualiza o contador de clusters sem componentes e verifica se ja 
			//chego no limite
			if(cCount == 0){
				ncCount++;
				if(qCount > 0 && ncCount == MAX_CLUSTERS_BETWEEN_CLUSTERS_WITH_COMP)
					qCount = 0;
			}
			
			//Toda questão deve ter pelo menos 1 componente e não deve possuir
			//frases contidas no: PHRASES_TO_IGNORE_REGEX
			if(cCount >= 1){
				if(cCount < MIN_COMPS_IN_ONE_CLUSTER){
					if(lastCluster != null){
						if(!isStartingANewQuestionnaire(lastCluster, c))
							qCount++;
						else
							qCount = 1;
					}else
						qCount++;
				}else{
					CommonLogger.debug("\n\t\t===> Minimo {} componentes em um cluster!", MIN_COMPS_IN_ONE_CLUSTER);
					CommonLogger.debug("\nLast cluster: \n" +c.toString());//TODO remover isso
					return true;
				}
				
				if(qCount == MIN_CLUSTERS_WITH_COMP){
					CommonLogger.debug("\n\t\t===> Minimo {} clusters com componentes!", MIN_CLUSTERS_WITH_COMP);
					CommonLogger.debug("\nLast cluster: \n" +c.toString());//TODO remover isso
					return true;
				}
			}
			if(cCount >= 0.5){
				ncCount = 0;
				lastCluster = c;
			}
		}
		return false;
	}
	
	private double getCountOfComps(Cluster c){
		// Os clusters de perguntas sempre começam com um texto ou imagem !?
		if(c.first().isComponent())
			return 0.0;
		
		if(c.isAllTextOrImg())
			this.lastWasAMatrix = false;
		
		double count = 0.0;
		boolean hasDescriptionAbove = false, hasQuestionAbove = false, isMultiComp = false;
		String text = "";
		ArrayList<MyNode> nodes = c.getGroup();
		
		for(int i = 0; i < nodes.size(); i++){
			MyNode node = nodes.get(i);
			if(node.isText()){ 
				if(PHRASES_TO_IGNORE_REGEX.matcher(node.getText()).matches()){ 
					count = -1;
					break;
				}
				text = fixText(node.getText());
				hasDescriptionAbove = isLikelyADescription(text, isMultiComp);
				hasQuestionAbove = hasDescriptionAbove && isLikelyAQuestion(text);
				isMultiComp = false;
			}else if(node.isComponent() && !node.isA("OPTION")){
				if(hasDescriptionAbove){
					isMultiComp = CommonUtil.getMultiComps().contains(node.getText());
					if(isMultiComp){
						if(this.isRatingOrMatrix(nodes, i)){
							if(this.lastWasAMatrix)
								return 0.5;
							this.lastWasAMatrix = true;
							return 1.0;
						}else if(hasQuestionAbove)
							count += 0.75;
						else
							count += 0.25;
					}else if(hasQuestionAbove)
						count += 1.0;
				}
				hasDescriptionAbove = false;
				hasQuestionAbove = false;
				this.lastWasAMatrix = false;
			}
		}
		return count;
	}
	
	private boolean isRatingOrMatrix(ArrayList<MyNode> nodes, int i) {
		MyNodeType type = nodes.get(i).getType();
		
		int count = 1;
		for(int j = i+1; j<nodes.size(); j++){
			if(nodes.get(j).getType() == type)
				count++;
			else
				break;
		}		
		return count >= 3;
	}

	private boolean isLikelyADescription(String text, boolean lastCompIsMultiComp){
		return (((!lastCompIsMultiComp && text.length() >= 4 && text.contains(" ")) || lastCompIsMultiComp) && 
				(CommonUtil.startsWithUpperCase(text) || CommonUtil.startsWithDigit(text))) 
				|| text.matches("(\\d{1,3}(\\s{1,2})?(\\.|\\:|\\)|\\-)?)");
	}
	
	private boolean isLikelyAQuestion(String text){
		return text.contains("?") || text.contains(":");
	}
	
	private String fixText(String text){
		text = text.replaceAll("( )?\\*", "");
		text = text.endsWith(" :") ? text.substring(0, text.length()-2) + ":" : text;
		return text;
	}
	
	private boolean isStartingANewQuestionnaire(Cluster lastCluster, Cluster newCluster) {
		DeweyExt dist = this.distMatrix.getDist(lastCluster.last(), newCluster.first());	
		if(dist.getHeight() > HEIGHT_BETWEEN_QUESTIONS)
			return true;
		
		//Verifica se o 1* container, depois do BODY, é diferente
		DeweyExt d1 = lastCluster.last().getDewey(), d2 = newCluster.first().getDewey();
		return d1.getNumbers().get(1) != d2.getNumbers().get(1);
	}
	
	// Métodos/Blocos estáticos
	static {
		//Load parameters
		JSONObject p = ProjectConfigs.getParameters(), tmp = null;
		
		try{
			String txtTmp = p.getString("surveyWordsRegex");
			SURVEY_WORDS_REGEX = Pattern.compile(txtTmp, 
					Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
			
			txtTmp = p.getString("phrasesToIgnoreRegex");
			PHRASES_TO_IGNORE_REGEX = Pattern.compile(txtTmp, 
					Pattern.CASE_INSENSITIVE);
			
			MIN_COMPS_IN_ONE_CLUSTER = p.getInt("minCompsInOneCluster");
			if(MIN_COMPS_IN_ONE_CLUSTER <= 0) MIN_COMPS_IN_ONE_CLUSTER = 5;
			
			MIN_CLUSTERS_WITH_COMP = p.getInt("minClustersWithComp");
			if(MIN_CLUSTERS_WITH_COMP <= 0) MIN_CLUSTERS_WITH_COMP = 3;
			
			MAX_CLUSTERS_BETWEEN_CLUSTERS_WITH_COMP = p.getInt("maxClustersBetweenClustersWithComp");
			if(MAX_CLUSTERS_BETWEEN_CLUSTERS_WITH_COMP <= 0) MAX_CLUSTERS_BETWEEN_CLUSTERS_WITH_COMP = 5;
	
			tmp = p.getJSONObject("distBetweenNearQuestions");
			HEIGHT_BETWEEN_QUESTIONS = tmp.optInt("height");
			if(HEIGHT_BETWEEN_QUESTIONS <= 0) HEIGHT_BETWEEN_QUESTIONS = 4;
		}catch(JSONException exp){
			CommonLogger.fatalError(exp);
		}
		CommonLogger.debug("RulesChecker:> Static block executed!");
	}

}
