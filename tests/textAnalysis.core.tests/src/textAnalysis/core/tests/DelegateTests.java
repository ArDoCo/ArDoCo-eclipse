package textAnalysis.core.tests;

import textAnalysis.core.Delegate;

import static org.junit.Assert.assertTrue;
import org.junit.Test;

public class DelegateTests {
	
	@Test
	public void testLaunchWithTextAnnotation() {
		Delegate textAnalysisDelegate = new Delegate();
    	String containerName = "/ardoco-eclipse/example/annotation-example-project";
    	String fileNameProfile = "testAnalysisAnnotationProfile01";
    	String fileNameTaf = "testAnaylsisAnnotationTaf01";
    	String profileName = "Requirements Document";
		textAnalysisDelegate.launchWithTextAnnotation(containerName, fileNameProfile, fileNameTaf, profileName);
	}

}
