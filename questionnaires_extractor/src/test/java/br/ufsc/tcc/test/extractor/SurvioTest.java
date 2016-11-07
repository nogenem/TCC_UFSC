package br.ufsc.tcc.test.extractor;

import org.junit.BeforeClass;
import org.junit.Test;

import br.ufsc.tcc.extractor.extractor.SurvioExtractor;
import br.ufsc.tcc.test.basic.BasicTest;

public class SurvioTest extends BasicTest {
	
	@BeforeClass
	public static void onStart(){
		System.out.println("SurvioTest:onStart()> ...");
		
		extractor = new SurvioExtractor();
	}
	
	@Test
	public void allTests() {
		System.out.println("SurvioTest:allTests()> ...");
		
		super.allTests("SurvioTests/AllElements.html", 
				"SurvioTests/Expected.json");
	}
}
