package br.ufsc.tcc.extractor.extractor;

public enum ExtractorFactory {
	
	//TODO botar o resto dos extratores
	Survio("survio", new SurvioExtractor()),
	SurveyMonkey("surveymonkey", new SurveyMonkeyExtractor());
	
	private final String name;
	private final IExtractor extractor;
	
	private ExtractorFactory(String name, IExtractor extractor){
		this.name = name;
		this.extractor = extractor;
	}
	
	public static IExtractor getInstanceFor(String extractorName){
		ExtractorFactory fac = get(extractorName);
		return fac != null ? fac.extractor : null;
	}
	
	private static ExtractorFactory get(String extractorName){
		for(ExtractorFactory fac : ExtractorFactory.values()){
			if(fac.name.equals(extractorName))
				return fac;
		}
		return null;
	}
}
