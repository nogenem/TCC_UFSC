package extractor;

public enum ExtractorFactory {
	
	// TODO: adicionar os outros extratores
	Survio("survio", new SurvioExtractor()),
	Google("google", new GoogleExtractor()),
	VarkLearn("vark-learn", new VarkLearnExtractor()),
	SaiaDoEscuro("saiadoescuro", new SaiaDoEscuroExtractor()),
	Reisearch("reisearch", new ReisearchExtractor()),
	GalenoAlvarenga("galenoalvarenga", new GalenoAlvarengaExtractor()),
	Agendor("agendor", new AgendorExtractor());
	
	private final String name;
	private final Extractor extractor;
	
	private ExtractorFactory(String name, Extractor extractor){
		this.name = name;
		this.extractor = extractor;
	}
	
	public static Extractor getInstanceFor(String extractorName){
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
