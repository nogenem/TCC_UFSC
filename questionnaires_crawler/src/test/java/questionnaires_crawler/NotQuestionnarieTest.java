package questionnaires_crawler;

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

public class NotQuestionnarieTest {
	
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
		
		System.out.println("NotQuestionnarieTest::onStart()> ...");
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
			System.out.println("Numero de links para testar: " +max+"\n");
		}
		
		for(int i = 0; i<max; i++){
			String link = links.get(i);
			inicio = System.currentTimeMillis();
			
			doc = Jsoup.connect(link)
				.userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/59.0.3071.82 Safari/537.36")
				.validateTLSCertificates(false)
				.get();		
			assertFalse("Link: \n>"+link+"\nnao deveria ser considerada um questionario!", 
					checker.shouldSave(doc));
			
			fim = System.currentTimeMillis();
			System.out.println(link +"> Time expend: " +((fim-inicio)/1000)+ "s");
		}		
	}
	
	private static List<String> links = Arrays.asList(
		//"https://www.questionpro.com",
		//"https://www.questionpro.com/de/",
		//"https://public.zohosurvey.com/offline/pPy2YJ",
		"https://www.bettercloud.com/monitor/the-academy/how-to-create-a-survey-using-excel/",
		"https://www.clicktools.com/resources/?filter-customer-case-study",
		"https://www.clicktools.com/customer-feedback/customer-service-satisfaction-surveys",
		"https://www.bceleva.com.br/page/2/",
		"https://www.bceleva.com.br/produto/instalacao-de-template-em-outras-hospedagens-de-site/",
		"http://www.surveytool.com/employee-satisfaction-survey/",
		"http://www.surveytool.com/feedback-survey/",
		"http://www.webcrawler.com/info.wbcrwl.udog/search/images?aid=4b3b2a56-4cc5-4417-9e5a-9fc92a4cbc7a&fcoid=408&fcop=topnav&fpid=2&q=survey%2Btemplate&ridx=1&ss=t",
		"http://www.webcrawler.com/info.wbcrwl.udog/search/news?q=survey%2Bexamples%2Btemplates",
		"http://www.zarca.com/About-Zarca/locations.html",
		"http://www.zarca.com/Online-Surveys-Non-Profit/sample-surveys-for-associations.html",
		"https://www.surveycrest.com/",
		"https://www.lrs.org/interactive/randomdate.php",
		"https://www.bettercloud.com/monitor/the-academy/how-to-create-a-survey-using-excel/",
		"https://www.zendesk.com.br/help-center/",
		"https://www.zendesk.com.mx/help-center/",
		"http://www.geekwire.com/2012/questionpro-gobbles-pollbob/",
		"https://forums.zoho.com/zoho-survey/newtopic",
		"https://help.surveymonkey.com/contact",
		"https://www.sogosurvey.com/static/packages.aspx?blnpackages=false",
		"http://www.laptopmag.com/articles/create-survey-google-forms",
		"http://www.wikihow.com/Calculate-Confidence-Interval",
		"http://m.wikihow.com/Create-a-Survey",
		"https://www.smartsurvey.co.uk/signup",
		"https://www.ahrq.gov/professionals/quality-patient-safety/patientsafetyculture/medical-office/userguide/mosopstxt.html",
		"http://researchaccess.com/",
		"http://opinionmeter.com/products/mobile-survey-apps/windows-survey-app/",
		"http://opinionmeter.com/services/survey-dashboards/",
		"http://www.smallbusinesscomputing.com/News/Software/make-custom-surveys-with-microsofts-excel-web-app.html",
		"http://www.survey-calls.com/survey-calls-contact.htm",
		"https://smforms.wufoo.com/forms/contact-surveymonkey-press-center/",
		"http://site.pfaw.org/site/Survey?ACTION_REQUIRED=URI_ACTION_USER_REQUESTS&NONCE_TOKEN=19C9199BE0BDE6F4856E83F793D56886&SURVEY_ID=9721",
		"http://www.sciencebuddies.org/science-fair-projects/project_ideas/Soc_survey.shtml",
		"https://survmetrics.com/request-demo/",
		"https://mva.microsoft.com/en-us/training-courses/create-a-survey-in-excel-online-10562?l=ig57p997_304984382",
		"https://www.jotform.com/help",
		"http://jobs.lycos.es/advanced-search",
		"https://efcarletti.wordpress.com/2011/12/28/50-good-questions-to-ask-yourself-and-others/",
		"https://docs.servicenow.com/bundle/istanbul-servicenow-platform/page/administer/survey-administration/concept/c_SurveyQuestionTemplate.html",
		"https://www.slideshare.net/heenapathan1/steps-in-questionnaire-design",
		"https://achilleaskostoulas.com/2014/02/10/designing-better-questionnaires-layout/",
		"https://www.zendesk.com/apps/surveymonkey-create/?ut_source=app_dir&ut_source2=featured&ut_source3=tile",
		"https://www.surveyforbusiness.com/audience_buy_signup.php",
		"https://www3.technologyevaluation.com/research/article/zoho-branches-into-the-mid-market-with-latest-crm-solution-launch.html",
		"https://www.zoho.com/crm/crmplus/?utm_source=blog&utm_medium=link&utm_campaign=thankyou",
		"http://www.prodigm.ca/6-pt.html",
		"http://www.prodigm.ca/success-stories.html",
		"http://www.cmswire.com/customer-experience/zoho-crm-upgrade-digs-deep-with-multiple-releases/",
		"http://www.cmswire.com/digital-workplace/zoho-adds-ai-sales-process-automation-to-software-suite/",
		"http://www.softskills.com/home.html",
		"http://www.thesmartceo.in/growth-enterprise/zoho-now-built-from-rural-india.html",
		"https://survey.zohopublic.com/zs/L2yRok",
		"http://promo.otentecnologia.com.br/promo/jive-voip/",
		"http://www.a2zcloud.com/zoho-advanced-solutions-provider/",
		"https://www.getapp.com/operations-management-software/inventory-management/",
		"https://www.getapp.com/marketing-software/a/zoho-social/integrations/",
		"http://www.aquora.es/",
		"http://wedelroos.se/kontakt/visningar.html",
		"http://www.giadans.com.mx/solicitud-alta.html",
		"http://www.giadans.com.mx/registro-cursos.html",
		"http://www.neointec.com/trabaja-con-nosotros/",
		"https://forums.manageengine.com/ad360?utm_source=PitStop&utm_medium=forum&utm_campaign=lefttoc&utm_term=ActiveDirectory",
		"https://forums.manageengine.com/ondemand-service-desk-plus/filter/discussions",
		"https://resources.manageengine.com/resources/forum/opmanager/device-templates?PSHP",
		"https://www.eventbrite.es/e/entradas-curso-taller-de-marketing-digital-en-guadalajara-33825239201?aff=erelexpmlt",
		"http://www.pcmag.com/article2/0,2817,2398080,00.asp",
		"http://www.pcmag.com/roundup/251078/the-best-mirrorless-cameras",
		"https://reports.zoho.com/ZDBDataSheetView.cc?DBID=4000000021992",
		"https://reports.zoho.com/ZDBDataSheetView.cc?DBID=779360000006744002",
		"https://www.microsoft.com/pt-br/store/p/zoho-creator/9nblgggzhvq7?rtc=1",
		"http://www.businessinsider.in/Jaitley-just-announced-a-National-Testing-Agency-so-CBSE-doesnt-have-to-bother-about-academic-entrance-exams/articleshow/56915398.cms",
		"http://marketing2win.de/zoho-support-marketing2win/zoho-webkonferenz-anfrage.html",
		"https://www.creditdonkey.com/fear-of-public-speaking-statistics.html",
		"http://www.moneysavingexpert.com/family/make-money-surveys",
		"http://www.easycounter.com/report/surveynuts.com",
		"http://www.surveymoneymachines.com/home",
		"http://www.surveymoneymachines.com/unbounce/landing_b.php?s0=GoogleAdWords&c=%7Bparam1%7D&ag=%7Bparam2%7D&ad=%7Bcreative%7D&k=%7Bkeyword%7D&m=%7Bmatchtype%7D&n=%7Bnetwork%7D&t=%7Btarget%7D&p=%7Bplacement%7D",
		"https://www.redlobstersurvey-me.com/(X(1)S(aiy0l0lsef1usz5s5foopdol))/Index.aspx?LanguageID=US&AspxAutoDetectCookieSupport=1",
		"http://moneysavingmom.com/2013/06/6-companies-that-will-pay-you-for-taking-online-surveys.html",
		"http://www.surveyh.com/retail-survey/www-kohls-com-survey/",
		"http://surveydownline.com/login.aspx?ReturnUrl=%2F",
		"http://beta.fortune.com/fortune500/kohls-145/",
		"https://surveyanyplace.com/survey/online-survey-creator.html",
		"http://www.savethestudent.org/make-money/best-paid-online-survey-sites.html",
		"https://www.yelp.com/biz/kohls-san-leandro-2?adjust_creative=rzFMrUTXCh2Wc77Kgact2g&utm_campaign=yelp_api&utm_medium=api&utm_source=ask",
		"http://scamxposer.com/business-review/npd-online-research/",
		"http://www.artswire.org/take-surveys-for-cash-review/",
		"https://www.sitejabber.com/reviews/www.mysurvey.com",
		"http://www.onlinehomeincome.in/work-from-home-free-online-surveys.php",
		"http://www.workathomenoscams.com/2015/03/30/survey-club-review-is-it-legit/",
		"https://www.getpaidto.com/points/surveys/response",
		"https://www.globaltestmarket.com/gtm_recruiting/join2.php?utm_source=LIGHTSPEED&utm_medium=CrossRecruitment&utm_campaign=lsrclosuremye&p=lsrclosuremye&lang=E&CONTACT_COUNTRY=MY&redirect=false",
		"http://www.mysurvey.com.hk/index.cfm?action=Main.lobbyGeneral&myContent=enquiryform",
		"http://www.watchforscams.com/survey_scams.html",
		"https://www.shrm.org/hr-today/trends-and-forecasting/research-and-surveys/Pages/default.aspx",
		"https://answers.yahoo.com/question/index?qid=20080131123103AAUZgym",
		"http://www.surveyfriends.co.uk/index.php?Part=registration",
		"https://kohls.pissedconsumer.com/was-asked-to-fill-out-a-survey-to-receive-a-500-gift-card-from-kohls-did-so-several-times-no-gift-c-20121130363610.html",
		"http://flightsglobal.net/how-to-get-paid-to-take-online-surveys/",
		"https://www.mysurveyasia.co.kr/index.cfm?action=Main.lobbyGeneral&myContent=enquiryform",
		"https://www.surveypolice.com/pick-a-perk/",
		"https://www.yelp.com/biz/kohls-redlands-3",
		"http://surveychris.com/cashcrate-review/",
		"http://download.cnet.com/Paid-Survey-Reviews/3000-2648_4-10543475.html",
		"http://www.takesurveysforcash.com/affiliates.html",
		"http://www.paid-surveys-at-home.com/",
		"https://aussieonlinesurveys.com/top-20-paid-survey-sites/",
		"http://www.mysurvey.com.sg/index.cfm?action=Main.lobbyGeneral&myContent=enquiryform",
		"http://www.mysurvey.com.sg/index.cfm?action=Main.lobbyGeneral&myContent=enquiryform",
		"http://www.thegreatcourses.com/",
		"http://www.careerbuilder.com/jobs-work-from-home-surveys",
		"http://merchandisingmatters.com/2012/05/15/harris-survey-ranks-kohls-nordstrom-target-walgreens-highest-categories/",
		"http://obsurvey.com/buy-survey-responses/",
		"http://unbouncepages.com/wanted-dark-responsive-21/",
		"https://smforms.wufoo.com/forms/contact-surveymonkey-press-center/?ut_source=blog",
		"https://survmetrics.com/request-demo/",
		"https://www.mouthsofmums.com.au/mom-answer/online-surveys-good-way-make-money/",
		"https://www.limesurvey.org/forgot-login",
		"https://www.limesurvey.org/stable-release",
		"https://www.cognitoforms.com/pricing",
		"https://myentertowin.com/submit-sweepstakes",
		"https://myentertowin.com/promote-your-sweepstakes",
		"http://www.loopsurvey.com/franchise-survey.html",
		"http://www.loopsurvey.com/hipaa-compliant-patient-surveys.html",
		"http://onlinelandsurveyors.net/public/RequestQuote.aspx",
		"http://onlinelandsurveyors.net/public/DemoRequest.aspx",
		"http://www.savethestudent.org/make-money/best-paid-online-survey-sites.html?desktop=1",
		"http://www.savethestudent.org/part-time-student-jobs",
		"http://fox17online.com/2016/05/24/do-not-share-this-kohls-75-coupon-on-facebook/",
		"https://kirbysmarketing.com/click-4-surveys-review-just-another-bad-survey-site/",
		"http://www.moneybluebook.com/how-to-make-money-with-paid-online-surveys-and-avoid-internet-scams/"
	);
}
