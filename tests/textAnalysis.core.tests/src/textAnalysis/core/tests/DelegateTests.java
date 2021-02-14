package textAnalysis.core.tests;

import textAnalysis.core.Delegate;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DelegateTests {
	
	@Test
	public void testLaunchWithTextAnnotation() {
		Delegate textAnalysisDelegate = new Delegate();
    	String containerName = "/ardoco-eclipse/example/annotation-example-project";
    	String fileName = "testAnalysisAnnotation01.taf";
    	String profileName = "Requirements Document";
    	String profileID = "01";
		textAnalysisDelegate.launchWithTextAnnotation(containerName, fileName, profileName, profileID);
	}

}
