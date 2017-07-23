package br.ufsc.tcc.extractor.extractor;

import br.ufsc.tcc.extractor.builder.RulesChecker;
import br.ufsc.tcc.extractor.extractor.impl.ChoiceInputExtractor;
import br.ufsc.tcc.extractor.extractor.impl.ChoiceInputWithHeaderExtractor;
import br.ufsc.tcc.extractor.extractor.impl.MultiCompExtractor;
import br.ufsc.tcc.extractor.extractor.impl.RatingExtractor;
import br.ufsc.tcc.extractor.extractor.impl.SelectExtractor;
import br.ufsc.tcc.extractor.extractor.impl.SimpleMatrixExtractor;
import br.ufsc.tcc.extractor.extractor.impl.SingleInputExtractor;
import br.ufsc.tcc.extractor.extractor.impl.TextAreaExtractor;
import br.ufsc.tcc.extractor.model.Pergunta;
import br.ufsc.tcc.extractor.model.Questionario;

public abstract class PerguntaExtractorFactory {
	
	public static PerguntaExtractor getExtractor(String extractor, Questionario currentQ, 
			Pergunta currentP, RulesChecker checker) {
		switch(extractor) {
		case "SELECT":
			return new SelectExtractor(currentQ, currentP, checker);
		case "CHOICE_INPUT_WITH_HEADER":
			return new ChoiceInputWithHeaderExtractor(currentQ, currentP, checker);
		case "CHECKBOX_INPUT":
		case "RADIO_INPUT":
			return new ChoiceInputExtractor(currentQ, currentP, checker);
		case "TEXT_INPUT":
		case "NUMBER_INPUT":
		case "EMAIL_INPUT":
		case "DATE_INPUT":
		case "TEL_INPUT":
		case "TIME_INPUT":
		case "URL_INPUT":
			return new SingleInputExtractor(currentQ, currentP, checker);
		case "TEXTAREA":
			return new TextAreaExtractor(currentQ, currentP, checker);
		case "RANGE_INPUT":
			return null;
		case "SIMPLE_MATRIX":
			return new SimpleMatrixExtractor(currentQ, currentP, checker);
		case "MULTI_COMP":
			return new MultiCompExtractor(currentQ, currentP, checker);
		case "RATING":
			return new RatingExtractor(currentQ, currentP, checker);
		default:
			return null;
		}
	}
	
}
