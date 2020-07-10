package textAnalysis.core;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.ui.AbstractLaunchConfigurationTab;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import textAnalysis.core.listener.fileopening.OpenLocalFileSystemButtonListener;
import textAnalysis.provider.AnalysisProvider;

public class MainTab extends AbstractLaunchConfigurationTab {
	
	private Label introText;
	private Label analysisText;
	private static final String TEXT_LOAD_TXT_FILE = "Load txt File";
	private Text textTxtIn;
	private static final String[] txtFileExtensions = new String[] { "*.txt" };

	private Map<String, Button> analysisButtons;
	private List<AnalysisProvider> analysisList;

	public MainTab() {
		super();
		analysisList = new LinkedList<>();
		analysisButtons = new HashMap<>();
	}

	@Override
	public void createControl(Composite parent) {

		// ---------- Setup Tab
		final Composite container = new Composite(parent, SWT.NONE);
		setControl(container);
		Shell shell = getShell();
		GridLayoutFactory.swtDefaults().applyTo(container);

		introText = new Label(container, SWT.LEFT);
		introText.setText("This Run Configuration lets you analyze text files. \n" + "Please choose a file here:");

		// ---------- Create Listeners
		final ModifyListener modifyListener = (ModifyEvent e) -> {
			setDirty(true);
			updateLaunchConfigurationDialog();
		};

		final SelectionListener checkboxSelectionListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				setDirty(true);
				updateLaunchConfigurationDialog();
			}
		};

		// ---------- For Input File Loading
		textTxtIn = new Text(container, SWT.SINGLE | SWT.BORDER);
		textTxtIn.setToolTipText("Input txt file(s) [*.txt]");
		GridData grid1 = new GridData(800, 30);
		textTxtIn.setLayoutData(grid1);
		textTxtIn.addModifyListener(modifyListener);

		SelectionListener localFileSystemListener = new OpenLocalFileSystemButtonListener(textTxtIn, txtFileExtensions,
				TEXT_LOAD_TXT_FILE, shell, true);

		final Button localFileSystemButton = new Button(container, SWT.NONE);
		localFileSystemButton.setText("Choose txt file...");
		localFileSystemButton.addSelectionListener(localFileSystemListener);

		
		// ----------- Load Analysis and create Checkboxes
		analysisText = new Label(container, SWT.LEFT);
		analysisText.setText("Please choose the Analysis here:");

		try {
			String dir = AnalysisLoader.getAnalysisSrcDirectory();

			if (dir == null) { 
				addRedLabel(AnalysisLoader.ERROR_MSG_DIRECTORY, container);
			} else {
				List<AnalysisProvider> serviceProviders = AnalysisLoader.loadAnalysis(dir);

				if (serviceProviders.size() == 0) {
					addRedLabel("No Analysis found. Check if the folder is correctly specified in the config file in: "
							+ AnalysisLoader.CONFIG_FILE_LOCATION + AnalysisLoader.DEFAULT_CONFIG_FILE_NAME, container);
				} else { // create a checkbox for every analysis
					for (AnalysisProvider analysis : serviceProviders) {
						Button b1 = new Button(container, SWT.CHECK);
						b1.setText(analysis.getName());
						b1.addSelectionListener(checkboxSelectionListener);
						this.analysisButtons.put(analysis.getName(), b1);
						this.analysisList.add(analysis);
					}
				}
			}
		} catch (MalformedURLException e2) {
			addRedLabel(AnalysisLoader.ERROR_MSG_MALFORMED_URL, container);
		} catch (IOException e1) {
			addRedLabel(AnalysisLoader.ERROR_MSG_IO_DIR, container);
		} catch (ClassNotFoundException e1) {
			addRedLabel(AnalysisLoader.ERROR_MSG_PROVIDER_I, container);
		}
	}

	/***
	 * Adds a red Label (a Warning) to the Tab
	 * 
	 * @param message   The message of the Label / Warning
	 * @param container The container of the tab
	 */
	private void addRedLabel(String message, Composite container) {
		Label noFolderText = new Label(container, SWT.LEFT);
		noFolderText.setText(message);
		noFolderText.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
	}

	@Override
	public String getName() {
		return "Text-Analysis";
	}

	@Override
	public void initializeFrom(ILaunchConfiguration configuration) {
		
		try {
			// ---- Files
			List<String> fileNames = configuration.getAttribute(PluginAttributes.FILE_NAME, new ArrayList<>());
			textTxtIn.setText(String.join(";", fileNames));

			// ---- Analysis Checkbox status
			Map<String, String> analysis_checkbox_status = configuration
					.getAttribute(PluginAttributes.CHECKBOX_ACTIVATION_STATUS, new HashMap<String, String>());
			for (Map.Entry<String, String> analysisEntry : analysis_checkbox_status.entrySet()) {
				// Only currently available Analyzes get a checkbox.
				if (AnalysisLoader.getAnalysisFrom(analysisList, analysisEntry.getKey()) != null) {
					analysisButtons.get(analysisEntry.getKey()).setSelection(Boolean.valueOf(analysisEntry.getValue()));
				}
			}
		} catch (CoreException e) {
			e.printStackTrace();
		}

	}

	@Override
	public void performApply(ILaunchConfigurationWorkingCopy configuration) {
		System.out.println("perform Apply");

		// ---- Files
		List<String> file_names = Arrays.asList(textTxtIn.getText().split(";"));
		configuration.setAttribute(PluginAttributes.FILE_NAME, file_names);

		// ---- Analysis
		// bools have to be converted to strings because attributes can either save only
		// bool or a map of string,string.
		Map<String, String> checkbox_activation = new HashMap<>();

		for (Map.Entry<String, Button> buttonEntry : analysisButtons.entrySet()) {
			checkbox_activation.put(buttonEntry.getKey(), Boolean.valueOf(buttonEntry.getValue().getSelection()).toString());
		}
		configuration.setAttribute(PluginAttributes.CHECKBOX_ACTIVATION_STATUS, checkbox_activation);
	}

	@Override
	public void setDefaults(ILaunchConfigurationWorkingCopy arg0) {
		// no defaults!
	}

	@Override
	public boolean isValid(ILaunchConfiguration launchConfig) {

		// if at least 1 file is specified and if all specified files are valid
		String[] files = textTxtIn.getText().split(";");
		boolean filesAreValid = true;
		for (String file_text : files) {
			File file = new File(file_text);

			for (String f : txtFileExtensions) {
				if (!((file.exists()) && (file.getName().endsWith(f.substring(1))))) {
					filesAreValid = false;
				}
			}
		}

		// if at least one Analysis is chosen
		boolean atLeast1 = false;
		for (Button b : analysisButtons.values()) {
			if (b.getSelection()) {
				atLeast1 = true;
			}
		}
		
		// if the chosen analysis is / are valid
		boolean chosenAnalysisAreValid = true;
		for (Map.Entry<String, Button> buttonEntry : analysisButtons.entrySet()) {
			if (buttonEntry.getValue().getSelection()) {
				Optional<AnalysisProvider> analysis = AnalysisLoader.getAnalysisFrom(analysisList, buttonEntry.getKey());
				if (!analysis.isPresent() || !analysis.get().isValid()) {
					chosenAnalysisAreValid = false;
				}
			}
		}

		return ((files.length > 0) && filesAreValid && atLeast1 && chosenAnalysisAreValid);
	}

	@Override
	protected boolean isDirty() { 
		// Seems like this method is never called. And the still functionality works.
		return true;
	}

	@Override
	public boolean canSave() {
		return true;
	}

}
