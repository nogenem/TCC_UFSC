package br.ufsc.tcc.test.main;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

import br.ufsc.tcc.test.extractor.SurveyMonkeyTest;
import br.ufsc.tcc.test.extractor.SurvioTest;

@RunWith(Suite.class)
@SuiteClasses({SurvioTest.class,SurveyMonkeyTest.class})
public class AllTests {

}
