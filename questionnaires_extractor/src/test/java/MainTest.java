import java.io.IOException;

import org.junit.Test;

import br.ufsc.tcc.common.util.CommonLogger;

public class MainTest extends BasicTest {

	@Test
	public void allTests() {
		CommonLogger.setDebugEnabled(false);
		try {
			//TODO dar jeito na descrição da ultima pergunta!
			super.executeTest("Survio1.json");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
