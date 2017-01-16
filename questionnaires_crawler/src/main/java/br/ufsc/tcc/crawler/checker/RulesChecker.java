package br.ufsc.tcc.crawler.checker;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.model.MyNodeType;
import br.ufsc.tcc.common.util.CommonUtil;
import br.ufsc.tcc.common.util.DistanceMatrix;
import edu.uci.ics.crawler4j.parser.HtmlParseData;

public class RulesChecker {
	
	private static final Pattern SURVEY_WORDS_REGEX = Pattern.compile("(.*surveys?.*|.*questionnaires?.*|"
			+ ".*question(a|á)rios?.*|.*pesquisas?.*|.*testes?\\s+para.*|.*b(u|ú)squedas?.*)", 
			Pattern.CASE_INSENSITIVE|Pattern.MULTILINE);
	private static final Pattern LOGIN_WORDS_REGEX = Pattern.compile("(register|login|password|forgot password\\?|"
				+ "keep me logged in|reset password|search:?|registre(\\-se)?|logar|entrar|esqueceu sua senha\\?|"
				+ "me mantenha logado|recupere sua senha|buscar:?)", 
			Pattern.CASE_INSENSITIVE);

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
		
		clusters.forEach(System.out::println);
		
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
		
		for(int i = 0; i<nodes.size(); i++){
			MyNode nTmp = nodes.get(i);
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
		double tCount, cCount, qCount = 0.0;
		boolean isLogin = false;
		
		//Um questionario deve ter pelo menos 1 cluster com 4 componentes ou
		//3 clusters com pelo menos 1 componente
		for(Cluster c : clusters){
			tCount = 0; cCount = 0; isLogin = false;
			
			ArrayList<MyNode> nodes = c.getGroup();
			for(int i = 0; i < nodes.size(); i++){
				MyNode node = nodes.get(i);
				if(node.isText()){ 
					if(LOGIN_WORDS_REGEX.matcher(node.getText()).matches())
						isLogin = true;
					tCount++;
				}else if(node.isComponent() && node.getType() != MyNodeType.OPTION){
					//todo componente deve ter pelo menos um texto acima dele
					if((i-1) >= 0 && nodes.get(i-1).isImgOrText()){
						if(CommonUtil.getMultiComps().contains(node.getText()))
							cCount += 0.5;
						else
							cCount++;
					}
				}
			}
			//Todo cluster deve ter pelo menos 1 texto e não deve ser um cluster de login/registro/busca
			if(!isLogin && tCount >= 1){
				if(cCount >= 4){
					System.out.println("\t\tMIN 4 COMPONENTES EM UM CLUSTER");
					return true;
				}else if(cCount >= 1)
					qCount++;
			}
			if(qCount == 3){
				System.out.println("\t\tMIN 3 CLUSTERS COM COMPONENTES");
				return true;
			}
		}
		return false;
	}

}
