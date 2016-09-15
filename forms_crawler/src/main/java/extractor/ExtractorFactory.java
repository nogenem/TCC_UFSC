package extractor;

public enum ExtractorFactory {
	
	// TODO: adicionar os outros extratores
	Survio("survio", new SurvioExtractor());
	
	private final String name;
	private final Extractor extractor;
	
	private ExtractorFactory(String name, Extractor extractor){
		this.name = name;
		this.extractor = extractor;
	}
	
	public static Extractor getInstanceFor(String extractorName){
		return get(extractorName).extractor;
	}
	
	private static ExtractorFactory get(String extractorName){
		for(ExtractorFactory fac : ExtractorFactory.values()){
			if(fac.name.equals(extractorName))
				return fac;
		}
		return null;
	}
}
