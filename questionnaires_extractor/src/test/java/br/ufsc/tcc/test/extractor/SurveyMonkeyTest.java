package br.ufsc.tcc.test.extractor;

import org.junit.BeforeClass;
import org.junit.Test;

import br.ufsc.tcc.extractor.extractor.SurveyMonkeyExtractor;
import br.ufsc.tcc.test.basic.BasicTest;

public class SurveyMonkeyTest extends BasicTest {
	
	@BeforeClass
	public static void onStart(){
		System.out.println("SurveyMonkeyTest:onStart()> ...");
		
		extractor = new SurveyMonkeyExtractor();
	}
	
	@Test
	public void allTests() {
		System.out.println("SurveyMonkeyTest:allTests()> ...");
		
		super.allTests("SurveyMonkeyTests/AllElements.html", 
				"SurveyMonkeyTests/Expected.json");
	}
}
