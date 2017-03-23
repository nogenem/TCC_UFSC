package br.ufsc.tcc.crawler.checker;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

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
		boolean hasTextAbove = false, multiComp = false;
		ArrayList<MyNode> nodes = c.getGroup();
		
		for(int i = 0; i < nodes.size(); i++){
			MyNode node = nodes.get(i);
			if(node.isText()){ 
				if(PHRASES_TO_IGNORE_REGEX.matcher(node.getText()).matches()){ 
					count = -1;
					break;
				}
				if(multiComp){
					hasTextAbove = true;
					multiComp = false;
				}else
					hasTextAbove = isLikelyDescription(node.getText());
			}else if(node.isComponent() && node.getType() != MyNodeType.OPTION){
				//Todo componente deve ter pelo menos um texto acima dele
				if(hasTextAbove){
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
				hasTextAbove = (multiComp && i+1 < nodes.size()) ? 
						nodes.get(i+1).isComponent() : false;
			}
		}
		return count;
	}
	
	private boolean isLikelyDescription(String text){
		return (text.length() >= 4 && text.contains(" ")) || 
				text.matches("(\\d{1,3}(\\s{1,2})?(\\.|\\:|\\)|\\-)?)");
	}
	
	private boolean isStartingANewQuestionnaire(Cluster lastCluster, Cluster newCluster) {
		Dewey dist = this.distMatrix.getDist(lastCluster.last(), newCluster.first());	
		if(dist.getHeight() > HEIGHT_BETWEEN_QUESTIONS)
			return true;
		
		//Verifica se o 1* container é diferente
		Dewey d1 = lastCluster.last().getDewey(), d2 = newCluster.first().getDewey();
		return d1.getNumbers().get(1) != d2.getNumbers().get(1);
	}
	
	// Métodos/Blocos estáticos
	static {
		//Load heuristics
		JSONObject h = ProjectConfigs.getHeuristics(), tmp = null;
		if(h != null){
			String txtTmp = h.optString("surveyWordsRegex", "");
			if(!txtTmp.isEmpty()){
				SURVEY_WORDS_REGEX = Pattern.compile(txtTmp, 
						Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
			}
			txtTmp = h.optString("phrasesToIgnoreRegex", "");
			if(!txtTmp.isEmpty()){
				PHRASES_TO_IGNORE_REGEX = Pattern.compile(txtTmp, 
						Pattern.CASE_INSENSITIVE);
			}
			MIN_COMPS_IN_ONE_CLUSTER = h.optInt("minCompsInOneCluster");
			MIN_CLUSTERS_WITH_COMP = h.optInt("minClustersWithComp");
			
			tmp = h.optJSONObject("distBetweenNearQuestions");
			if(tmp != null)
				HEIGHT_BETWEEN_QUESTIONS = tmp.optInt("height");
		}
		
		if(SURVEY_WORDS_REGEX == null)
			SURVEY_WORDS_REGEX = Pattern.compile("(.*surveys?.*|.*questionnaires?.*|"
					+ ".*question(a|á)rios?.*|.*pesquisas?.*|.*testes?\\s+para.*|.*b(u|ú)squedas?.*)", 
					Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
		if(PHRASES_TO_IGNORE_REGEX == null)
			PHRASES_TO_IGNORE_REGEX = Pattern.compile("(register|login|password|forgot password\\?|"
					+ "keep me logged in|reset password|search:?|registre(\\-se)?|logar|entrar|esqueceu sua senha\\?|"
					+ "me mantenha logado|recupere sua senha|buscar:?)", 
				Pattern.CASE_INSENSITIVE);
		if(MIN_COMPS_IN_ONE_CLUSTER <= 0) MIN_COMPS_IN_ONE_CLUSTER = 4;
		if(MIN_CLUSTERS_WITH_COMP <= 0) MIN_CLUSTERS_WITH_COMP = 3;
		if(HEIGHT_BETWEEN_QUESTIONS <= 0) HEIGHT_BETWEEN_QUESTIONS = 4;
		
		CommonLogger.debug("RULESCHECKER {} / {}", MIN_COMPS_IN_ONE_CLUSTER, MIN_CLUSTERS_WITH_COMP);
	}

}
