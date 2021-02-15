package textAnalysis.core.tests;

import textAnalysis.core.Delegate;

import static org.junit.Assert.assertTrue;
import org.junit.Test;
import org.junit.BeforeClass;

public class DelegateTests {
	private String containerName = "/ardoco-eclipse/example/annotation-example-project";
	private String fileNameProfile = "testAnalysisAnnotationProfile01";
	private String fileNameTaf = "testAnaylsisAnnotationTaf01";
	private String profileName = "Requirements Document";
	private String templateFile = "";
	private static Delegate textAnalysisDelegate;
	
	@BeforeClass
	public static void setup() {
		textAnalysisDelegate = new Delegate();
	}
	
	@Test
	public void testLaunchWithTextAnnotation() {
		textAnalysisDelegate.launchWithTextAnnotation(containerName, fileNameProfile, fileNameTaf, profileName, templateFile);
	}
	
	@Test
	public void testCreateTextAnnotationFile() {
		textAnalysisDelegate.createTextAnnotationFile(containerName, fileNameTaf, profileName, templateFile);
	}
	
	@Test
	public void testCreateTextAnnotationProfile() {
		textAnalysisDelegate.createNewTextAnnotationProfile(containerName, fileNameProfile, profileName);
	}

}
