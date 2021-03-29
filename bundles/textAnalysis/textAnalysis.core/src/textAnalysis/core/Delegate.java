package textAnalysis.core;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.xml.stream.XMLEventFactory;
import javax.xml.stream.XMLEventWriter;
import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.events.EndElement;
import javax.xml.stream.events.StartDocument;
import javax.xml.stream.events.StartElement;
import javax.xml.stream.events.XMLEvent;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.LaunchConfigurationDelegate;

import textAnalysis.provider.AnalysisProvider;
import edu.kit.textannotation.annotationplugin.wizards.TextAnnotationFileWizard;
import edu.kit.textannotation.annotationplugin.wizards.ProfileWizard;
import edu.kit.textannotation.annotationplugin.PluginConfig;

/***
 * This class handles what happens when the runConfig is run. It calls the Analysis and collects their output in the
 * output file
 *
 * @author Maike Rees (uoxix)
 *
 */
public class Delegate extends LaunchConfigurationDelegate {

    private static final String ANALYSIS_XML_TAG = "textAnalysis";
    private static final String ANALYSIS_FILE_ENDING = "_analysis.xml";
    private static final String ERROR_FILE_ENDING = "_error.txt";

    private static final XMLEventFactory eventFactory = XMLEventFactory.newInstance();
    private static final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();
    private static final XMLEvent end = eventFactory.createDTD("\n");
    private static final EndElement ANALYSIS_END_TAG = eventFactory.createEndElement("", "", ANALYSIS_XML_TAG);
    private static final StartElement ANALYSIS_START_TAG = eventFactory.createStartElement("", "", ANALYSIS_XML_TAG);

    @Override
    public void launch(ILaunchConfiguration configuration, String arg1, ILaunch arg2, IProgressMonitor arg3)
            throws CoreException {
    	// TODO: Set this variables in a GUI test or implement a UI for it later.
    	String containerName = "/ardoco-eclipse/example/annotation-example-project";
    	String fileNameProfile = "testAnalysisAnnotationProfile01";
    	String fileNameTaf = "testAnaylsisAnnotationTaf01";
    	String profileName = "Requirements Document";
    	String templateFile = "";
    	
    	// Combine TextAnnotation Plugin with TextAnalysis
    	// When you launch the TextAnalysis, it should automatically provide sample tap and taf files.
    	this.launchWithTextAnnotation(containerName, fileNameProfile, fileNameTaf, profileName, templateFile);
    	
        List<String> fileNames = configuration.getAttribute(PluginAttributes.FILE_NAME, new ArrayList<String>());
        Map<String, String> analysisCheckboxesStatus = configuration.getAttribute(
                PluginAttributes.CHECKBOX_ACTIVATION_STATUS, new HashMap<String, String>());

        List<AnalysisProvider> analysisToExecute = getAnalysisToExecute(analysisCheckboxesStatus, fileNames.get(0));

        // ------ Compute Analysis for each input file
        for (String fileName : fileNames) {

            // create file name of output File
            String outputFileName = getFileNameWithoutExtension(fileName) + ANALYSIS_FILE_ENDING;

            try {
                XMLEventWriter eventWriter = setupEventWriter(outputFileName);
                List<String> textToAnalyze = getLinesInFile(fileName);

                // Add Analysis outputs
                for (AnalysisProvider aP : analysisToExecute) {
                    List<XMLEvent> events = getElementsFromAnalysis(aP, textToAnalyze);
                    for (XMLEvent e : events) {
                        eventWriter.add(e);
                    }
                }
                closeEventWriter(eventWriter);

            } catch (FileNotFoundException | XMLStreamException e1) {
            	System.out.println(e1.getMessage());
                String error = "Problem with xml Generation: " + e1.toString();
                saveErrorFile(getFileNameWithoutExtension(fileName), error);
            }
        }
    }
    
    /**
     * This method serves as a connector between TextAnalysis and TextAnnotation plugin.
     * It creates a new text annotation profile (tap) and a text annotation file (taf).
     * @param containerName: the directory for the created files
     * @param fileNameProfile: file name for tap file
     * @param fileNameTaf: file name for taf file
     * @param profileName: a name for the profile specifying its annotation purpose (e.g. Requirements, Modules etc.)
     * @param templateFile: possibility to create taf file from a template file
     */
    public void launchWithTextAnnotation(String containerName, String fileNameProfile, String fileNameTaf, String profileName, String templateFile) {
    	this.createNewTextAnnotationProfile(containerName, fileNameProfile, profileName);
    	this.createTextAnnotationFile(containerName, fileNameTaf, profileName, templateFile);
    }
    
    /**
     * Creating a text annotation file (taf) by calling methods from @edu.kit.textannotation.annotationplugin.wizards.TextAnnotationFileWizard
     * File name will be extended with appropriate file extension for taf files.
     */
    private void createTextAnnotationFile(String containerName, String fileName, String profileName, String templateFile) {
    	TextAnnotationFileWizard tafWizard = new TextAnnotationFileWizard();
    	tafWizard.performCreationTextAnnotationFile(containerName, fileName + PluginConfig.ANNOTATABLE_FILE_EXTENSION, profileName, templateFile);
    }
    
    /**
     * Creating a text annotation profile (tap) by calling methods from @edu.kit.textannotation.annotationplugin.wizards.ProfileWizards
     * File name will be extended with appropriate file extension for tap files.
     */
    private void createNewTextAnnotationProfile(String containerName, String fileName, String profileName) {
    	//TODO: Resolve _analysis.xml (Zugriff verweigert)
    	ProfileWizard profileWizard = new ProfileWizard();
    	profileWizard.performCreationProfile(containerName, fileName + "." +  PluginConfig.ANNOTATION_PROFILE_EXTENSION, profileName);
    }

    /***
     * Retrieves the XML Events from the specified Analysis and enrich with structural Elements
     *
     * @param aP
     *            the Analysis
     * @param textToAnalyze
     *            The lines of the File to analyze.
     * @return A List of Events
     */
    private List<XMLEvent> getElementsFromAnalysis(AnalysisProvider aP, List<String> textToAnalyze) {
        List<XMLEvent> events = new LinkedList<>();
        events.add(eventFactory.createStartElement("", "", aP.getName()));
        events.add(end);
        events.addAll(aP.getXMLEvents(textToAnalyze));
        events.add(eventFactory.createEndElement("", "", aP.getName()));
        events.add(end);
        return events;
    }

    /***
     * Close the EventWriter including necessary end tags
     *
     * @param eventWriter
     *            the EventWriter that should be closed
     * @throws XMLStreamException
     */
    private void closeEventWriter(XMLEventWriter eventWriter) throws XMLStreamException {
        eventWriter.add(ANALYSIS_END_TAG);
        eventWriter.add(end);
        eventWriter.add(eventFactory.createEndDocument());
        eventWriter.close();
    }

    /***
     * Setup the EventWriter for one output XML.
     *
     * @param outputFileName
     *            The Name of the output file
     * @return The configured EventWriter including the starting tag
     * @throws XMLStreamException
     * @throws FileNotFoundException
     */
    private XMLEventWriter setupEventWriter(String outputFileName) throws XMLStreamException, FileNotFoundException {
        XMLEventWriter eventWriter = outputFactory.createXMLEventWriter(new FileOutputStream(outputFileName));

        // create and write Start Tag
        StartDocument startDocument = eventFactory.createStartDocument();
        eventWriter.add(startDocument);

        // create analysis open tag
        eventWriter.add(end);
        eventWriter.add(ANALYSIS_START_TAG);
        eventWriter.add(end);
        return eventWriter;
    }

    /***
     * Find out which Analysis should be executed.
     *
     * @param analysisCheckboxesStatus
     *            The Checkbox Status from the Config
     * @param analysisObjs
     *            The Analysis
     * @return A List with instantiated Analysis that should be executed
     * @throws CoreException
     */
    private List<AnalysisProvider> getAnalysisToExecute(Map<String, String> analysisCheckboxesStatus,
            String fileNameForError) throws CoreException {
        List<AnalysisProvider> analysisObjs = getAnalysisObjects(fileNameForError);
        List<AnalysisProvider> analysisToCompute = new LinkedList<>();

        for (Map.Entry<String, String> entry : analysisCheckboxesStatus.entrySet()) {
            if (Boolean.valueOf(entry.getValue())) {
                Optional<AnalysisProvider> s = AnalysisLoader.getAnalysisFrom(analysisObjs, entry.getKey());
                if (s.isPresent()) {
                    analysisToCompute.add(s.get());
                }
            }
        }
        return analysisToCompute;
    }

    /***
     * Load Analysis Objects and save an error file if loading is not successful.
     *
     * @param fileNameForError
     *            a FileName (and path) for the possible error file.
     * @return The Analysis Objects, or an empty List if there was an error or no Analysis were found.
     */
    private List<AnalysisProvider> getAnalysisObjects(String fileNameForError) {
        String folder = null;
        List<AnalysisProvider> analysisProviders = new LinkedList<>();

        String errorFile = getFileNameWithoutExtension(fileNameForError);

        try {
            folder = AnalysisLoader.getAnalysisSrcDirectory();
            if (folder == null) {
                String error = AnalysisLoader.ERROR_MSG_DIRECTORY;
                saveErrorFile(errorFile, error);
            } else {
                analysisProviders = AnalysisLoader.loadAnalysis(folder);
            }
        } catch (MalformedURLException e2) {
            String error = AnalysisLoader.ERROR_MSG_MALFORMED_URL + "\n" + e2.toString();
            saveErrorFile(errorFile, error);
        } catch (IOException e2) {
            String error = AnalysisLoader.ERROR_MSG_IO_DIR + "\n" + e2.toString();
            saveErrorFile(errorFile, error);
        } catch (ClassNotFoundException e2) {
            String error = AnalysisLoader.ERROR_MSG_PROVIDER_I + "\n" + e2.toString();
            saveErrorFile(errorFile, error);
        }
        return analysisProviders;
    }

    /***
     * Extract the filename without the file extension.
     *
     * @param fileName
     *            The specific FileName
     * @return The fileName without extension, e.g. /example/directory/file.txt -> /example/directory/file
     */
    private String getFileNameWithoutExtension(String fileName) {
        int i = fileName.lastIndexOf('.');
        int extensionLength = (i > 0) ? fileName.substring(i)
                                                .length()
                : 0;
        return fileName.substring(0, fileName.length() - extensionLength);
    }

    /***
     * Creates a file with the specified name and error message.
     *
     * @param fileNameWithoutExtension
     *            The name and path of the file
     * @param error
     *            The error Message
     */
    private void saveErrorFile(String fileNameWithoutExtension, String error) {
        try {
            PrintWriter out = new PrintWriter(fileNameWithoutExtension + ERROR_FILE_ENDING);
            out.println(error);
            out.close();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
    }

    /***
     * Read lines of file into a String-List. Saves an error file in case of error.
     *
     * @param filename
     *            The path and filename to read
     * @return A List of the lines in the file. An empty list if the file is empty or couldn't be read.
     */
    private List<String> getLinesInFile(String filename) {
        List<String> linesInFile = new LinkedList<>();

        try {
            BufferedReader reader = new BufferedReader(new FileReader(filename));
            String line;
            while ((line = reader.readLine()) != null) {
                linesInFile.add(line);
            }
            reader.close();
        } catch (Exception e) {
            saveErrorFile(getFileNameWithoutExtension(filename),
                    "Exception occurred trying to read" + filename + ":\n" + e.toString());
        }
        return linesInFile;
    }

}
