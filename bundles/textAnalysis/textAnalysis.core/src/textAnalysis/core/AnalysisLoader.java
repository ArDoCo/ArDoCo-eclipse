package textAnalysis.core;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.ServiceLoader;

import textAnalysis.provider.AnalysisProvider;

/**
 * This Class provides helper methods for the dynamic loading of Analysis that are provided by the user.
 * @author Maike Rees (uoxix)
 *
 */
public class AnalysisLoader {
	protected static final String DEFAULT_ANALYSIS_DIRECTORY_NAME = "\\.textanalysis\\";
	protected static final String DEFAULT_CONFIG_FILE_NAME = "\\.textanalysisconfig";
	protected static final String CONFIG_FILE_LOCATION = System.getProperty("user.home");
	private static final String ANALYSIS_FILE_TYPE = ".jar";

	private static final String PROVIDER_INTERFACE = "textAnalysis.provider.AnalysisProvider";
	private static final Class<AnalysisProvider> ANALYSIS_INTERFACE = textAnalysis.provider.AnalysisProvider.class;
	
	protected static final String ERROR_MSG_DIRECTORY = "There is a Problem with the Analysis-Directory: \n"
			+ "It is either not existing, falsy specified (wrong name) in the config file,"
			+ "or not a directory. \n" 
			+ "Find the configuration File that specifies the folder in: "
			+ CONFIG_FILE_LOCATION + DEFAULT_CONFIG_FILE_NAME;
	protected static final String ERROR_MSG_MALFORMED_URL = "The analysis files in the given directory can not be converted to URLs:";
	protected static final String ERROR_MSG_IO_DIR = "IO Exception when loading the Analysis Source Directory:";
	protected static final String ERROR_MSG_PROVIDER_I = "The PROVIDER_INTERFACE can not be found:";
	
	
    /**
     * Reads the configuration file and returns the name of the folder that is specified in it.
     * This is the folder where the Analysis-Jars are supposed to be.
     * 
     * The configuration file is searched in the CONFIG_FILE_LOCATION and has to have the 
     * DEFAULT_CONFIG_FILE_NAME.
     * 
     * If the configuration file can not be found, it is created and contains the path to the 
     * DEFAULT_ANALYSIS_DIRECTORY_NAME in the CONFIG_FILE_LOCATION.
     * 
     * @return The path+folder name if it exists and is a directory else null 
     * @throws IOException
     */
	protected static String getAnalysisSrcDirectory() throws IOException {

		File configFile = new File(CONFIG_FILE_LOCATION + DEFAULT_CONFIG_FILE_NAME);
		
		if (!configFile.exists()) { 
			// default directory
			String analysisDirName = CONFIG_FILE_LOCATION + DEFAULT_ANALYSIS_DIRECTORY_NAME;
			
			// create new default configuration file
			FileOutputStream fos = new FileOutputStream(configFile);
			fos.write(analysisDirName.getBytes());
			fos.flush();
			fos.close();
			
			// create default directory if it does not exist
			File analysisDir = new File(analysisDirName);
			if (!analysisDir.exists()) {
				analysisDir.mkdir();
			}
		}

		BufferedReader br = new BufferedReader(new FileReader(configFile));
		String configDir = br.readLine();
		br.close();

		if (configDir == null || !(new File(configDir).exists()) 
				|| !(new File(configDir).isDirectory())) {
			return null; 
		} else {
			return configDir;
		}
	}
    
    
    /***
     * Creates a ClassLoader that loads the Analysis-files from the specified configuration Directory
     * and knows the needed PROVIDER_INTERFACE.
     * The Analysis should be of type ANALYSIS_FILE_TYPE
     * @param configDir The directory where the analysis files should be.
     * @return The created ClassLoader
     * @throws MalformedURLException If the files in the directory can not be converted to urls
     * @throws ClassNotFoundException If the PROVIDER_INTERFACE can not be found.
     */
    protected static URLClassLoader createProviderClassLoader(String configDir) throws MalformedURLException, ClassNotFoundException {
    	
    	File dir = new File(configDir);
        File[] flist = dir.listFiles((FileFilter) file -> file.getPath().toLowerCase()
        		.endsWith(ANALYSIS_FILE_TYPE));
        URL[] urls = new URL[flist.length];
                
        for (int i = 0; i < flist.length; i++) {
            urls[i] = flist[i].toURI().toURL();
        }
        	
		ClassLoader cLoader = Class.forName(PROVIDER_INTERFACE).getClassLoader();
		return new URLClassLoader(urls, cLoader);
    }
   
    /***
     * Loads analysis from specified source directory
     * @param srcDir: The source directory where the analysis can be found
     * @return A list of analysis objects
     * @throws MalformedURLException If the files in the directory can not be converted to urls
     * @throws ClassNotFoundException If the PROVIDER_INTERFACE can not be found.
     */
    protected static List<AnalysisProvider> loadAnalysis(String srcDir) throws MalformedURLException, ClassNotFoundException {
        ClassLoader classloader = createProviderClassLoader(srcDir);
        ServiceLoader<AnalysisProvider> foundAnalysis = ServiceLoader.load(ANALYSIS_INTERFACE, classloader);
	    
        List<AnalysisProvider> providerList = new LinkedList<>();
        for (AnalysisProvider service : foundAnalysis) { 
		    providerList.add(service);
	    }	
        return providerList;
    }
    
    /***
     * Finds the first Analysis-Object whose name matches the provided one
     * @param analysisList A List of Analysis Objects. 
     * @param analysisName The Name that should be matching
     * @return An Optional with the matching Analysis-Object if one was found. 
     */
    protected static Optional<AnalysisProvider> getAnalysisFrom(List<AnalysisProvider> analysisList, String analysisName) {
        return analysisList.stream().filter(s -> s.getName().equals(analysisName)).findFirst();
    }
	
}
