

import static org.junit.Assert.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.junit.BeforeClass;
import org.junit.Test;

import br.ufsc.tcc.common.config.ProjectConfigs;
import br.ufsc.tcc.common.util.CommonLogger;
import br.ufsc.tcc.crawler.checker.RulesChecker;

public class HaveQuestionnaireTest {
	
	private static final boolean oneOfEachDomain = false;
	//Use um numero menor que 1 para usar todos os links
	private static final int maxLinksToTest = -1;
	
	private static final String configsPath = "./crawler_configs.json";
	private static RulesChecker checker;
	
	@BeforeClass
	public static void onStart(){
		CommonLogger.setDebugEnabled(false);
		ProjectConfigs.loadConfigs(configsPath);
		checker = new RulesChecker();
		
		System.out.println("HaveQuestionnarieTest::onStart()> ...");
	}
	
	@SuppressWarnings("unused")
	@Test
	public void test() throws IOException {
		long inicio, fim;
		Document doc = null;
		
		if(oneOfEachDomain){
			System.out.println("Tamanho antes do 'oneOfEachDomain': " +links.size());
			final List<String> domains = new ArrayList<>();
			links = links.stream().filter((link) -> {
				link = link.replaceAll("^((http|https)://)", "");
				String domain = link.substring(0, link.indexOf('/'));
				if(domains.contains(domain))
					return false;
				else{
					domains.add(domain);
					return true;
				}
			}).collect(Collectors.toList());
			System.out.println("Tamanho depois do 'oneOfEachDomain': " +links.size());
		}
		
		int max = links.size();
		if(maxLinksToTest >= 1){
			Collections.shuffle(links);
			max = maxLinksToTest;
			System.out.println("Numero de links para testar: " +max);
		}
		
		System.out.println();
		for(int i = 0; i<max; i++){
			String link = links.get(i);
			link = link.replaceAll("%20", " ");
			inicio = System.currentTimeMillis();
			
			doc = Jsoup.connect(link)
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.82 Safari/537.36")
				.validateTLSCertificates(false)
				.get();		
			assertTrue("Link: \n>"+link+"\ndeveria ser considerada um questionario!", 
					checker.shouldSave(doc));
			
			fim = System.currentTimeMillis();
			System.out.println(link +"> Time expend: " +(fim-inicio)+ "ms");
		}
		System.out.println();
	}
	
	private static List<String> links = Arrays.asList(
//		"https://surveynuts.com/surveys/take?id=2668&c=85393DRVH", //erro https
//		"http://www.liveabberlyvillage.com/resident-survey.htm"
//		"http://www.almaderma.com.br/formulario/florais/entrevista02/contato.php",
//		"http://www.negocioselectronicos.biz/empresa/trabaja-con-nosotros",
		"http://www.zarca.com/Online-Surveys-Customer/customer-service-brief-version.html",
		"https://survey.zoho.com/surveytemplate/health%20care%20survey-drinking%20habits%20survey",
		"https://www.surveymonkey.com/r/online-photo-sharing-template",
		"https://polldaddy.com/s/d5564eb1c42db4d1",
		"http://www.questionpro.com/survey-templates/consumer-demographics-interests/",
		"http://cs.createsurvey.com/publish/survey?s=17&m=MM1Kro",
		"https://www.smartsurvey.co.uk/s/customer-service-satisfaction-template?sample-provider=true",
		"https://esurv.org/online-survey.php?survey_ID=OLINFJ_7efd5b22&sdem",
		"https://survs.com/survey-templates/concept-testing-survey/",
		"https://www.surveycrest.com/template_preview/poPA2Hd40wJKCzI62P_6QuNPuAw",
		"http://www.surveyshare.com/template/3231/Medical-Web-Use",
		"https://www.jotform.com/form-templates/preview/31633965174863?preview=true",
		"https://www.surveyforbusiness.com/survey-industry/real-estate.html",
		"https://www.survio.com/modelo-de-pesquisa/pesquisa-de-recebimento-do-produto",
		"https://www.survey-maker.com/Template-Customer-Satisfaction",
		"http://anpei.tempsite.ws/intranet/mediaempresa",
		"https://docs.google.com/forms/d/e/1FAIpQLSfX9lqkf4k4lYH9S9vrJmOox6LCC3pb0jq9YcnGv3RIw_lVug/viewform?c=0&w=1",
		"http://www.surveyexpression.com/Survey.aspx?id=5a363f87-e1e8-47f3-aba0-9a7d916ca71d",
		"http://www.websurveymaster.com/t/18/Samples",
		"http://infopoll.net/live/surveys/s32933.htm",
		"https://survey.zohopublic.com/zs/3yDXfv",
		"https://statpac.com/online-surveys/customer_service_commitment_survey.htm",
		"https://www.mysurvey.com/index.cfm?action=Main.join",
		"https://www.mdparentsurvey.com/survey.html?s_id=2"
	);
}
