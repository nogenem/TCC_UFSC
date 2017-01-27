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
	private static Pattern LOGIN_WORDS_REGEX = null;
	private static int MIN_COMPS_IN_ONE_CLUSTER = 0;
	private static int MIN_CLUSTERS_WITH_COMP = 0;
	private static int HEIGHT_BETWEEN_QUESTIONS = 0;

	private DistanceMatrix distMatrix;
	
	// Construtores
	public RulesChecker(){
		this.distMatrix = new DistanceMatrix();
	}
	
	// Demais métodos
	//TODO remover isso ao final do desenvolvimento!
	public boolean shouldSave(Document doc){
		Element root = doc.select("body").get(0);
		
		if(!SURVEY_WORDS_REGEX.matcher(root.text()).matches() && 
				!SURVEY_WORDS_REGEX.matcher(doc.title()).matches()){
			this.distMatrix.clear();
			return false;
		}
		
		List<MyNode> nodes = CommonUtil.findCompsImgsAndTexts(root);
		List<Cluster> clusters = groupNearNodes(nodes, distMatrix);
		groupClustersByHeuristics(clusters);
		
		CommonLogger.debug(clusters);
		
		boolean ret = hasQuestionnarie(clusters);
		this.distMatrix.clear();
		
		return ret;
	}
		
	public boolean shouldSave(HtmlParseData htmlParseData) {
		String html = htmlParseData.getHtml();
		Document doc = Jsoup.parse(html);
		Element root = doc.select("body").get(0);
		
		if(!SURVEY_WORDS_REGEX.matcher(root.text()).matches() && 
				!SURVEY_WORDS_REGEX.matcher(doc.title()).matches()){
			this.distMatrix.clear();
			return false;
		}
		
		List<MyNode> nodes = CommonUtil.findCompsImgsAndTexts(root);
		List<Cluster> clusters = groupNearNodes(nodes, distMatrix);
		groupClustersByHeuristics(clusters);
		
		boolean ret = hasQuestionnarie(clusters);
		this.distMatrix.clear();
		
		return ret;
	}

	private List<Cluster> groupNearNodes(List<MyNode> nodes, DistanceMatrix distMatrix2) {
		Cluster cTmp = new Cluster();
		ArrayList<Cluster> ret = new ArrayList<>();
		
		for(MyNode nTmp : nodes){
			if(!cTmp.isEmpty() && !distMatrix.areNear(cTmp.last(), nTmp)){
				ret.add(cTmp);
				cTmp = new Cluster();
			}
			cTmp.add(nTmp);
		}
		if(!cTmp.isEmpty())
			ret.add(cTmp);
		return ret;
	}

	private void groupClustersByHeuristics(List<Cluster> clusters) {
		Cluster tmp1, tmp2, tmp3;
		int i, size;
		
		do{
			size = clusters.size();
			i = 0;
			
			while(i < clusters.size()-1){
				int tmpSize = clusters.size();
				
				tmp1 = clusters.get(i);
				tmp2 = clusters.get(i+1);
				tmp3 = i+2 < tmpSize ? clusters.get(i+2) : null;
				
				if(shouldGroup(tmp1, tmp2, tmp3)){
					tmp1.join(tmp2);
					clusters.remove(i+1);
					i--;
				}
				i++;
			}
		}while(size != clusters.size());
	}

	private boolean shouldGroup(Cluster c1, Cluster c2, Cluster c3) {
		//Deve-se juntar grupos de input
		//PS: pode ter uma img na frente
		String starterInputRegex = "(?s)^(img\\[alt=.*\\]\\n)?"
				+ "(input\\[type=.+\\]).*";
		String c1Text = c1.getAllNodesText(),
				c2Text = c2.getAllNodesText();
		if(c1Text.matches(starterInputRegex) && c2Text.matches(starterInputRegex))
			return true;
		
		//Deve-se juntar padrões de cluster texto seguidos de cluster elementos OU
		//cluster com 1 elemento apenas (casos raros?)
		//PS: deve-se priorizar a união de elementos a frente desta regra
		//    -por isso o c3-
		if((c3 == null || c3.first().isText()) &&
				((c1.isAllText() && !c2.first().isText()) || 
						(!c2.first().isText() && c2.size() == 1)))
			return true;
		return false;
	}

	private boolean hasQuestionnarie(List<Cluster> clusters) {
		//Contador de componentes, Contador de 'questões'
		double cCount, qCount = 0.0;
		Cluster lastCluster = null;
		boolean isLogin = false, hasTextAbove = false;
		String txtTmp = "";
		
		//Um questionario deve ter pelo menos 1 cluster com 4 componentes ou
		//3 clusters com pelo menos 1 componente
		for(Cluster c : clusters){
			cCount = 0; isLogin = false; hasTextAbove = false;
			
			//Conta quantos textos e componentes tem no cluster
			ArrayList<MyNode> nodes = c.getGroup();
			for(int i = 0; i < nodes.size(); i++){
				MyNode node = nodes.get(i);
				if(node.isText()){ 
					if(LOGIN_WORDS_REGEX.matcher(node.getText()).matches()){ 
						isLogin = true;
						break;
					}
					txtTmp = node.getText();
					hasTextAbove = txtTmp.length() >= 4 || 
							txtTmp.matches("(\\d{1,2}(\\s{1,2})?(\\.|\\:|\\)|\\-)?)");
				}else if(node.isComponent() && node.getType() != MyNodeType.OPTION){
					//todo componente deve ter pelo menos um texto/img acima dele
					if(hasTextAbove){
						if(CommonUtil.getMultiComps().contains(node.getText()))
							cCount += 0.5;
						else
							cCount++;
					}
					hasTextAbove = false;
				}
			}
			//Toda questão deve ter pelo menos 1 componente e não deve pertencer a 
			//um cluster de login/registro/busca
			if(!isLogin && cCount >= 1){
				if(cCount >= MIN_COMPS_IN_ONE_CLUSTER){
					CommonLogger.debug("\t\tMinimo {} componentes em um cluster!", MIN_COMPS_IN_ONE_CLUSTER);
					CommonLogger.debug(c.toString());//TODO remover isso
					return true;
				}else{
					if(lastCluster != null){
						//Os clusters com componentes devem estar 'próximo' um dos outros
						Dewey dist = this.distMatrix.getDist(lastCluster.last(), c.first());
						if(dist.getHeight() <= HEIGHT_BETWEEN_QUESTIONS)
							qCount++;
						else
							qCount = 1;
					}else
						qCount++;
				}
			}
			if(qCount == MIN_CLUSTERS_WITH_COMP){
				CommonLogger.debug("\t\tMinimo {} clusters com componentes!", MIN_CLUSTERS_WITH_COMP);
				CommonLogger.debug(c.toString());//TODO remover isso
				return true;
			}
			lastCluster = c;
		}
		return false;
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
			txtTmp = h.optString("loginWordsRegex", "");
			if(!txtTmp.isEmpty()){
				LOGIN_WORDS_REGEX = Pattern.compile(txtTmp, 
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
		if(LOGIN_WORDS_REGEX == null)
			LOGIN_WORDS_REGEX = Pattern.compile("(register|login|password|forgot password\\?|"
					+ "keep me logged in|reset password|search:?|registre(\\-se)?|logar|entrar|esqueceu sua senha\\?|"
					+ "me mantenha logado|recupere sua senha|buscar:?)", 
				Pattern.CASE_INSENSITIVE);
		if(MIN_COMPS_IN_ONE_CLUSTER <= 0) MIN_COMPS_IN_ONE_CLUSTER = 4;
		if(MIN_CLUSTERS_WITH_COMP <= 0) MIN_CLUSTERS_WITH_COMP = 3;
		if(HEIGHT_BETWEEN_QUESTIONS <= 0) HEIGHT_BETWEEN_QUESTIONS = 4;
		
		CommonLogger.debug("RULESCHECKER {} / {}", MIN_COMPS_IN_ONE_CLUSTER, MIN_CLUSTERS_WITH_COMP);
	}

}
