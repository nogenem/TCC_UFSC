import java.io.IOException;

import org.junit.Test;

import br.ufsc.tcc.common.util.CommonLogger;

public class MainTest extends BasicTest {

	@Test
	public void allTests() {
		CommonLogger.setDebugEnabled(false);
		try {
			super.executeTest("Survio1.json");//TODO dar jeito na descrição da ultima pergunta!
			super.executeTest("Survio2.json");
			super.executeTest("SurveyMonkey1.json");
			super.executeTest("SurveyMonkey2.json");
			super.executeTest("SurveyMonkey3.json");
			super.executeTest("SurveyMonkey4.json");
			super.executeTest("Anpei1.json");
			super.executeTest("SurveyCrest1.json");
			super.executeTest("SurveyCrest2.json");
			super.executeTest("SurveyCrest3.json");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
