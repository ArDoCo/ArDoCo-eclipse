package textAnalysis.provider;

import java.util.List;

import javax.xml.stream.events.XMLEvent;


public interface AnalysisProvider {

    String getName();

    boolean isValid();
    
    List<XMLEvent> getXMLEvents(List<String> textToAnalyze); 

}
