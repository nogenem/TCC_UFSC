package br.ufsc.tcc.extractor.builder;

import java.util.ArrayList;
import java.util.List;
import java.util.Stack;
import java.util.regex.Pattern;

import org.json.JSONException;
import org.json.JSONObject;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.model.Cluster;
import br.ufsc.tcc.common.model.DeweyExt;
import br.ufsc.tcc.common.model.MyNode;
import br.ufsc.tcc.common.model.MyNodeType;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.common.util.CommonUtil;
import br.ufsc.tcc.common.util.DistanceMatrix;
import br.ufsc.tcc.extractor.model.Alternativa;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;

public class RulesChecker {
	
	private static JSONObject CONFIGS = null;
	private DistanceMatrix distMatrix;
	
	// Regex usados para extrair coisas como:
	//		[ ] / month [ ] / day [ ] year
	public static final String DATE_REGEX1 = "(/|\\-)",
			DATE_REGEX2 = "(month|day|year|m(ê|e)s|dia|ano)";
	
	public RulesChecker() {
		this.distMatrix = new DistanceMatrix();
	}
	
	// Getters e Setters
	public DistanceMatrix getDistMatrix(){
		return this.distMatrix;
	}
	
	public static JSONObject getConfigs(){
		return CONFIGS;
	}
	
	// Demais métodos
	
	// Métodos usados pela classe QuestionarioBuilder
	public boolean shouldStartNewQuestionario(Cluster lastDesc, MyNode newNode) {
		if(lastDesc == null || lastDesc.isEmpty() || newNode == null) 
			return false;
		JSONObject obj = CONFIGS.getJSONObject("distBetweenTextsInsideQuestionnaire");
		DeweyExt dist = this.distMatrix.getDist(lastDesc.last(), newNode);	
		if(dist.getHeight() > obj.getInt("height"))
			return true;
		
		//Verifica se o 1* container, depois do Body, é diferente
		DeweyExt d1 = lastDesc.last().getDewey(), d2 = newNode.getDewey();
		return d1.getNumbers().get(1) != d2.getNumbers().get(1);
	}
	
	
	public boolean shouldCreateNewCluster(Cluster lastCluster, MyNode newNode, List<MyNode> nodes, int i) {
		if(lastCluster == null || lastCluster.isEmpty() || newNode == null || 
				i+1 >= nodes.size()) 
			return false;
		
		if(!this.distMatrix.areNear(lastCluster.last(), newNode))
			return true;
		
		//Todos os elementos do cluster devem ter o mesmo prefixo
		//comum ao proximo nodo encontrado
		String tTmp1 = lastCluster.last().getDewey().getCommonPrefix(nodes.get(i+1).getDewey()), 
				tTmp2 = newNode.getDewey().getCommonPrefix(nodes.get(i+1).getDewey());
		return !tTmp1.equals(tTmp2);		
	}
	
	public boolean isValidQuestionnaire(Questionario q){
		if(q.getAssunto().isEmpty() || 
				q.getPerguntas().size() < CONFIGS.getInt("minQuestionsOnQuestionnaire"))
			return false;
		if(CommonUtil.matchesWithLineBreak(q.getAssunto(), 
				CONFIGS.getString("phrasesToIgnoreRegex")))
			return false;
		for(Pergunta p : q.getPerguntas()){
			if(CommonUtil.matchesWithLineBreak(p.getDescricao(), 
					CONFIGS.getString("phrasesToIgnoreRegex")))
				return false;
		}
		return true;
	}

	// Métodos usados pela classe PerguntaBuilder e Pergunta Extractor
	public boolean isOnlyOneImg(Cluster c) {
		return c.size() == 1 && c.first().isImage();
	}

	public boolean areCompAndTextNear(MyNode comp, MyNode text) {
		if(comp == null || text == null) 
			return false;
		JSONObject obj = CONFIGS.getJSONObject("distBetweenCompAndText");
		DeweyExt dist = this.distMatrix.getDist(comp, text);
		return dist.getHeight() <= obj.getInt("height") && 
				dist.getMaxHeight() <= obj.getInt("maxHeight");
	}

	public Cluster getCorrectDescription(Cluster desc, ArrayList<Alternativa> tmpAlts, MyNode firstNode,
			Stack<Cluster> cStack) {
		if(desc == null || desc.isEmpty()) return desc;
		
		boolean flag = true;
		String altsTxt = "";
		
		//Pega o texto de todas as alternativas da perguta
		for(Alternativa alt : tmpAlts){
			altsTxt += alt.getDescricao()+"\n";
		}
		
		//Ex: https://www.surveymonkey.com/r/General-Event-Feedback-Template [questão 1]
		while(true){
			for(MyNode node : desc.getGroup()){
				String txt = node.getText();
				flag = flag && !txt.isEmpty() && altsTxt.contains(txt);
			}
			if(flag && !cStack.isEmpty()){
				desc = cStack.pop();
			}else
				break;
		}
		return desc;
	}
	
	public Cluster checkIfDescIsComplete(Cluster desc, Stack<Cluster> cStack, List<MyNode> nodes, int i){
		if(desc == null || desc.isEmpty() || cStack.isEmpty() || i+1 >= nodes.size())
			return desc;
		
		Cluster tmp = cStack.peek();
		if(!tmp.getText().isEmpty() && this.isDescriptionsNear(tmp, desc) &&
				!this.distMatrix.areNear(tmp.last(), nodes.get(i+1))){
			tmp = cStack.pop();
			tmp = tmp.join(desc);
			return tmp;
		}
		return desc;
	}
	
	public boolean isDescriptionsNear(Cluster firstDesc, Cluster secondDesc){
		JSONObject obj = CONFIGS.getJSONObject("distBetweenPartsOfDescription");
		DeweyExt dist = this.distMatrix.getDist(firstDesc.last(), secondDesc.first());
		return dist.getHeight() <= obj.getInt("height") && 
				dist.getMaxHeight() <= obj.getInt("maxHeight") &&
				dist.getWidth() <= obj.getInt("width");
	}
	
	public boolean areDescAndPergNear(Cluster desc, MyNode perg) {
		if(desc == null || desc.isEmpty())
			return false;
		return areDescAndPergNear(desc.last(), perg);
	}
	
	public boolean areDescAndPergNear(MyNode desc, MyNode perg){
		if(desc == null || perg == null)
			return false;
		JSONObject obj = CONFIGS.getJSONObject("distBetweenDescAndQuestion");
		DeweyExt dist = this.distMatrix.getDist(desc, perg);
		return dist.getHeight() <= obj.getInt("height") && 
				dist.getMaxHeight() <= obj.getInt("maxHeight");
	}
	
	public boolean isGroupText(Cluster cTmp, Cluster desc, Cluster firstGroupOfQuestionnaire) {
		if(cTmp == null || cTmp.isEmpty() || desc == null || desc.isEmpty())
			return false;
		
		String txt = cTmp.getText();
		txt = CommonUtil.trim(txt);
		//Cluster deve ter um e APENAS um texto
		if(txt.isEmpty() || txt.contains("\n"))
			return false;
		
		JSONObject obj = CONFIGS.getJSONObject("distBetweenGroupAndFirstQuestion");
		DeweyExt dist = distMatrix.getDist(cTmp.last(), desc.first());
		
		if(dist.getHeight() <= obj.getInt("height") && dist.getWidth() <= obj.getInt("width")){
			//O texto do grupo deve ter no maximo X palavras 
			if(txt.split(" ").length <= CONFIGS.getInt("maxWordsInAGroupDescription")){
				if(firstGroupOfQuestionnaire != null){
					//O tamanho do Dewey dos grupos de um questionario, geralmente,
					//é o mesmo
					if(firstGroupOfQuestionnaire.last().getDewey().toString().length() == 
							cTmp.last().getDewey().toString().length())
						return true;
				}else{
					return true;
				}
			}
		}
		return false;
	}

	public boolean checkComplementaryText(List<MyNode> nodes, int i) {
		if(i+2 < nodes.size()){
			MyNode nTmp1 = nodes.get(i),
				nTmp2 = nodes.get(i+1),
				nTmp3 = nodes.get(i+2);
			DeweyExt dist = distMatrix.getDist(nTmp2, nTmp3);

			//Lida com casos aonde se tem 2 textos complementares
			//	Ex: https://www.survio.com/modelo-de-pesquisa/feedback-sobre-servico
			//TODO e se não tiver mais nada embaixo? (i+2 = max)
			if(i+3 < nodes.size() && nTmp3.isImgOrText() && 
					dist.getMaxHeight() == 1 && dist.getWidth() == 2){
				nTmp3 = nodes.get(i+3);
			}
			JSONObject obj = CONFIGS.getJSONObject("distBetweenDescAndComplementaryText");
			
			if(nTmp2.isText() && nTmp3.isImgOrText()){
				dist = distMatrix.getDist(nTmp1, nTmp2);
				if(dist.getHeight() <= obj.getInt("height") && dist.getMaxHeight() <= obj.getInt("maxHeight") && 
						dist.getWidth() <= obj.getInt("width")){
					
					String prefix1 = nTmp1.getDewey().getCommonPrefix(nTmp3.getDewey()),
							prefix2 = nTmp2.getDewey().getCommonPrefix(nTmp3.getDewey()),
							prefix3 = nTmp1.getDewey().getCommonPrefix(nTmp2.getDewey());
					
					//O prefixo3 deve ser maior que o prefix1, pois isso indica que nTmp1 e nTmp2 estão, 
					//pelo menos, um elemento a mais juntos
					if(prefix1.equals(prefix2) && 
							CommonUtil.getPrefixLength(prefix3) > CommonUtil.getPrefixLength(prefix1))
						return true;
				}
			}
		}
		return false;
	}

	public boolean isAbove(MyNode n1, MyNode n2) {
		//Verifica se n1 esta acima de n2
		DeweyExt dist = this.distMatrix.getDist(n1, n2);
		return dist.isNegative();
	}

	public boolean hasSameTexts(Pergunta currentP, Cluster cTmp2) {
		//Verifica se as alternativas/filhas da currentP contêm todos os textos
		//do cTmp2, ou se a descrição da currentP contém o texto de cTmp2
		if(cTmp2 == null || cTmp2.getText().isEmpty()) return false;
		
		ArrayList<Alternativa> alts = currentP.getAlternativas();
		ArrayList<Pergunta> filhas = currentP.getFilhas();
		String txt = cTmp2.getText(), txtTmp = "";
		boolean flag = false;
		int count = filhas.size()+alts.size();
		
		if(count == 0 && cTmp2.size() == 1){//Ex: https://www.survio.com/modelo-de-pesquisa/pesquisa-de-preco-do-produto
			txtTmp = Pattern.quote(cTmp2.getText());
			flag = CommonUtil.matchesWithLineBreak(currentP.getDescricao(), txtTmp);
		}else if(count == cTmp2.size()){
			flag = true;
			for(int j = 0; j < alts.size(); j++){
				txtTmp = alts.get(j).getDescricao();
				txtTmp = Pattern.quote(txtTmp);
				flag = flag && CommonUtil.matchesWithLineBreak(txt, txtTmp);
			}
			for(int j = 0; j < filhas.size(); j++){
				txtTmp = filhas.get(j).getDescricao();
				txtTmp = Pattern.quote(txtTmp);
				flag = flag && CommonUtil.matchesWithLineBreak(txt, txtTmp);
			}
		}
		
		return flag;
	}

	public boolean isSimpleMatrix(List<MyNode> nodes, int i, Stack<Cluster> cStack) {
		int count = 0;
		Cluster head = !cStack.isEmpty() ? cStack.peek() : null;
		MyNode nTmp = nodes.get(i);
		
		//Conta a quantidade de componentes em sequência
		do{ 
			count++;
			if(i+count < nodes.size())
				nTmp = nodes.get(i+count);
			else
				nTmp = null;
		}while(nTmp != null && nTmp.isComponent());
		
		//A quantidade encontrada acima deve ser a mesma de textos no head da matriz
		return head != null && head.isAllText() && head.size() == count;
	}
	
	public boolean isRadioInputOrCheckboxWithHeader(List<MyNode> nodes, Stack<Cluster> cStack, int i, Cluster desc){
		int count = 0;
		MyNode nTmp = nodes.get(i);
		String type = nTmp.getType().toString();
		Cluster head = desc;
		
		if(!type.matches("RADIO_INPUT|CHECKBOX_INPUT"))
			return false;
		
		//Conta a quantidade de componentes em sequência
		do{ 
			count++;
			if(i+count < nodes.size())
				nTmp = nodes.get(i+count);
			else
				nTmp = null;
		}while(nTmp != null && nTmp.isA(type));
		
		//A quantidade encontrada acima deve ser a mesma de textos no head da matriz
		return head != null && head.isAllText() && head.size() == count;
	}
	
	public boolean checkPrefixForQuestionGroup(MyNode n1, MyNode n2, String prefix) {
		if(n1 == null || n2 == null || prefix.isEmpty()) return false;
		
		String tmp = n1.getDewey().getCommonPrefix(n2.getDewey());
		return tmp.equals(prefix);
	}
	
	public boolean checkDistForQWithSubQs(MyNode n1, MyNode n2) {
		JSONObject obj = CONFIGS.getJSONObject("distBetweenTextsInQuestionWithSubQuestions");
		DeweyExt dist = this.distMatrix.getDist(n1, n2);
		return dist.getHeight() == obj.getInt("height") && dist.getWidth() <= obj.getInt("width");
	}
	
	// Middle e bottom devem esta perto um do outro
	// o prefixo entre middle e qWithSubQsDesc e entre bottom e qWithSubQsDesc deve ser o mesmo
	// o prefixo acima deve ser menor que o prefixo entre middle e bottom
	public boolean checkQWithSubQs(MyNode middle, MyNode qWithSubQsDesc, List<MyNode> nodes, int currentI) {
		if(currentI+1 >= nodes.size()) return false;
		
		String prefix1 = middle.getDewey().getCommonPrefix(qWithSubQsDesc.getDewey());
		MyNode bottom = nodes.get(currentI+1);
		
		if(checkDistForQWithSubQs(middle, bottom) && 
				checkPrefixForQuestionGroup(bottom, qWithSubQsDesc, prefix1)){
			String prefix2 = middle.getDewey().getCommonPrefix(bottom.getDewey());
			return CommonUtil.getPrefixLength(prefix1) < CommonUtil.getPrefixLength(prefix2);
		}
		return false;
	}
	
	public boolean checkDistForTextsOfAlternative(MyNode n1, MyNode n2){
		JSONObject obj = CONFIGS.getJSONObject("distBetweenTextsOfSameAlternative");
		DeweyExt dist = this.distMatrix.getDist(n1, n2);
		return dist.getHeight() <= obj.getInt("height") &&
				dist.getMaxHeight() <= obj.getInt("maxHeight") &&
				dist.getWidth() <= obj.getInt("width");
	}
	
	//Checagem para coisas do tipo: Hora: [ ] : [ ] ou Data: [ ] / [ ] / [ ] ou Telefone: ( [ ] ) [ ]
	public int checkCompositeInput(List<MyNode> nodes, String type, int currentI) {
		if(type.matches("(TEXT|NUMBER|TEL|DATE|TIME)_INPUT")){
			Cluster c = new Cluster();
			//Cria um cluster com no max os próximos 7 nodes
			for(int i = 1; i<=7; i++){
				int j = currentI+i;
				if(j < nodes.size())
					c.add(nodes.get(j));
			}
			
			String tmpType = type.replace("_INPUT", "").toLowerCase(),
					inputTxt = "input\\[type="+tmpType+"\\]",
					txt = c.getAllNodesText();
			String r1 = DATE_REGEX1,
					r2 = DATE_REGEX2;
			
			//Check [ ] : [ ] (: [ ])?
			//	Ex: https://www.bioinfo.mpg.de/mctq/core_work_life/core/core.jsp?language=por_b
			String regex = ":\n"+inputTxt+"(\n:"+inputTxt+")?.*";
			if(txt.matches("(?ism)"+regex))
				return 0;

			//Check ( [ ] ) [ ]
			//	Ex: http://www.almaderma.com.br/formulario/florais/infantil/contato.php
			regex = "\\)\n"+inputTxt+".*";
			if(txt.matches("(?ism)"+regex))
				return 1;

			//Check [ ] (/|-) [ ] (/|-) [ ]
			regex = r1+"\n"+inputTxt+"\n"+r1+"\n"+inputTxt+".*";
			if(txt.matches("(?ism)"+regex))
				return 2;

			//Check [ ] (/|-) Month [ ] (/|-) Day [ ] Year
			//	Ex: https://www.jotform.com/form-templates/preview/21014328614342?preview=true
			regex = r1+"\n"+r2+"\n"+inputTxt+"\n"+r1+"\n"+r2+"\n"+inputTxt+"\n"+r2+".*";
			if(txt.matches("(?ism)"+regex))
				return 3;
		}
		
		return -1;
	}

	public boolean isSelectGroup(List<MyNode> nodes, int currentI) {
		MyNode opt = null, text = null;
		int i = currentI;
		
		opt = nodes.get(++i);
		text = nodes.get(++i);
		while(opt != null && opt.getType() == MyNodeType.OPTION){
			if(text.isA("OPTION")){
				if(i+1 < nodes.size()){
					opt = text;
					text = nodes.get(++i);
					continue;
				}else{
					opt = null;
					break;
				}
			}
			
			if(i+2 < nodes.size()){
				opt = nodes.get(++i);
				text = nodes.get(++i);
			}else
				opt = null;
		}
		
		if(opt != null)
			i -= 1;
		
		text = nodes.get(i);
		return text.isA("SELECT") || (text.isText() && text.getText().matches(DATE_REGEX1));
	}
	
	public boolean checkIfTextIsAbove(List<MyNode> nodes, int currentI) {
		MyNode input = null, text = null, img = null;
		int i = currentI;
		boolean hasImgAbove = false;
		
		input = nodes.get(i);
		
		// Verifica se os elementos acima podem ser considerado
		// a descrição da alternativa e da pergunta
		text = i-1 <= nodes.size()-1 ? nodes.get(i-1) : null;
		if((text == null || !text.isA("text")) || !areCompAndTextNear(input, text))
			return false;
		text = i-2 <= nodes.size()-1 ? nodes.get(i-2) : null;
		if(text != null && text.isA("img")){
			text = i-3 <= nodes.size()-1 ? nodes.get(i-3) : null;
			hasImgAbove = true;
		}
		if((text == null || !text.isA("text")) || !areDescAndPergNear(text, input))
			return false;
		
		// Verifica se segue o padrão: img -> text -> input -> img,
		// se sim, então deve ser seguro retornar true
		img = i+1 < nodes.size() ? nodes.get(i+1) : null;
		if(hasImgAbove && img != null && img.isA("img"))
			return true;
		
		// Só para garantir, verifica se seguindo o padrão:
		//		input -> text -> input -> text
		// da erro no final
		text = nodes.get(++i);
		while(input != null && 
				(input.getType() == MyNodeType.CHECKBOX_INPUT || input.getType() == MyNodeType.RADIO_INPUT) &&
				text.getType() == MyNodeType.TEXT){
			if(!areCompAndTextNear(input, text))
				return true;
			
			if(i+2 < nodes.size()){
				input = nodes.get(++i);
				text = nodes.get(++i);
			}else
				input = null;
		}

		return false;
	}
	
	// Métodos/Blocos estáticos
	static {
		//Load heuristics
		JSONObject h = ProjectConfigs.getHeuristics(), tmp = null;
		String lastObj = "";
		
		// Verifica se todas as heurísticas foram declaradas
		try{
			h.getInt("minQuestionsOnQuestionnaire");
			h.getInt("maxWordsInAGroupDescription");
			h.getString("phrasesToIgnoreRegex");
			
			lastObj = "distBetweenTextsInsideQuestionnaire";
			tmp = h.getJSONObject(lastObj);
				tmp.getInt("height");
				
			lastObj = "distBetweenCompAndText";
			tmp = h.getJSONObject(lastObj);
				tmp.getInt("height");
				tmp.getInt("maxHeight");
			
			lastObj = "distBetweenDescAndQuestion";
			tmp = h.getJSONObject(lastObj);	
				tmp.getInt("height");
				tmp.getInt("maxHeight");
				
			lastObj = "distBetweenGroupAndFirstQuestion";
			tmp = h.getJSONObject(lastObj);	
				tmp.getInt("height");
				tmp.getInt("width");
				
			lastObj = "distBetweenDescAndComplementaryText";
			tmp = h.getJSONObject(lastObj);	
				tmp.getInt("height");
				tmp.getInt("maxHeight");
				tmp.getInt("width");
				
			lastObj = "distBetweenTextsInQuestionWithSubQuestions";
			tmp = h.getJSONObject(lastObj);	
				tmp.getInt("height");
				tmp.getInt("width");
				
			lastObj = "distBetweenPartsOfDescription";
			tmp = h.getJSONObject(lastObj);	
				tmp.getInt("height");
				tmp.getInt("width");
				tmp.getInt("maxHeight");
				
			lastObj = "distBetweenTextsOfSameAlternative";
			tmp = h.getJSONObject(lastObj);	
				tmp.getInt("height");
				tmp.getInt("width");
				tmp.getInt("maxHeight");
		}catch(JSONException exp){
			String msg = exp.getMessage();
			String value = msg.substring(msg.indexOf('[')+2, msg.lastIndexOf(']')-1);
			lastObj += lastObj.equals("") ? "" : ".";
			msg = "Valor '"+(lastObj+value)+
					"' não encontrado no arquivo de configuração!";
			CommonLogger.fatalError(new JSONException(msg));
		}
		CONFIGS = h;
		
		CommonLogger.debug("RulesChecker:> Static block executed!");
	}
}
