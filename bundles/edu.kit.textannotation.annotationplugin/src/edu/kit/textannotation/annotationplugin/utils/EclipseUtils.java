package edu.kit.textannotation.annotationplugin.utils;

import java.util.List;

import org.apache.log4j.Appender;
import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.wizard.IWizard;
import org.eclipse.jface.wizard.WizardDialog;
import org.eclipse.jgit.annotations.Nullable;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.ui.wizards.IWizardDescriptor;
import org.osgi.framework.Bundle;

import edu.kit.textannotation.annotationplugin.PluginConfig;

/**
 * Utility methods regarding the eclipse framework.
 */
public class EclipseUtils {
    protected static final Level LOGGING_LEVEL = Level.DEBUG;
    private static final String PATTERN = "%d{HH:mm:ss} [%-5p | %c]: %m%n";
    private static final Logger logger = Logger.getLogger(EclipseUtils.class);

    static {
        logger.addAppender(getLoggerConsoleAppender());
    }

    /**
     * Remove all children from the supplied SWT composite.
     *
     * @param parent
     *            the composite from which all children will be removed.
     * @see Control::dispose
     */
    public static void clearChildren(Composite parent) {
        for (Control child : parent.getChildren()) {
            child.dispose();
        }
    }

    /**
     * Display an error message to the eclipse user.
     *
     * @param message
     *            the error message to display in textual form.
     */
    public static void reportError(String message) {
        logger.error("Reported error: " + message);
        StatusManager.getManager()
                     .handle(new Status(IStatus.ERROR, PluginConfig.PLUGIN_ID, message), StatusManager.SHOW);
    }

    /**
     * Get an file reference on the file currenlty opened by the supplied editor.
     */
    public static IFile getFileForEditor(IEditorPart editor) {
        IFile file = null;
        if (editor != null && editor.getEditorInput() instanceof IFileEditorInput) {
            IFileEditorInput input = (IFileEditorInput) editor.getEditorInput();
            file = input.getFile();
        }
        return file;
    }

    /**
     * Get the path to the workspace that is currently opened by the eclipse user.
     *
     * @param bundle
     *            the eclipse environment bundle.
     * @return the path to the workspace in textual form.
     */
    public static String getCurrentWorkspaceDirectory(Bundle bundle) {
        IPath bundlePath = Platform.getStateLocation(bundle); // workspace/.metadata/.plugins/.textannotation

        String[] pieces = bundlePath.toString()
                                    .split("" + Path.SEPARATOR);
        StringBuilder result = new StringBuilder();

        for (int i = 0; i < pieces.length - 3; i++) {
            result.append(pieces[i])
                  .append(Path.SEPARATOR);
        }

        return result.substring(0, result.length() - 1);
    }

    /**
     * Get all projects currently loaded in the workspace.
     *
     * @param bundle
     *            the eclipse environment bundle.
     * @return a list containing the projects of the workspace.
     */
    public static List<IProject> getAllWorkspaceProjects() {
        IWorkspace workspace = ResourcesPlugin.getWorkspace();
        IWorkspaceRoot root = workspace.getRoot();

        return List.of(root.getProjects());
    }

    /**
     * Open the eclipse create-wizard with the specified ID. The wizard has to be specified as an contribution by either
     * the eclipse core platform or by some currently loaded plugin. This call is blocking until the wizard is closed.
     *
     * @param id
     *            the ID of the wizard to be opened.
     * @return a reference on the wizard if it could be found, or null otherwise.
     */
    @Nullable
    public static IWizard openWizard(String id) {
        // https://resheim.net/2010/07/invoking-eclipse-wizard.html

        // First see if this is a "new wizard".
        IWizardDescriptor descriptor = PlatformUI.getWorkbench()
                                                 .getNewWizardRegistry()
                                                 .findWizard(id);
        // If not check if it is an "import wizard".
        if (descriptor == null) {
            descriptor = PlatformUI.getWorkbench()
                                   .getImportWizardRegistry()
                                   .findWizard(id);
        }
        // Or maybe an export wizard
        if (descriptor == null) {
            descriptor = PlatformUI.getWorkbench()
                                   .getExportWizardRegistry()
                                   .findWizard(id);
        }
        try {
            // Then if we have a wizard, open it.
            if (descriptor != null) {
                IWizard wizard = descriptor.createWizard();
                WizardDialog wd = new WizardDialog(Display.getDefault()
                                                          .getActiveShell(),
                        wizard);
                wd.setTitle(wizard.getWindowTitle());
                wd.open();
                return wizard;
            }
        } catch (CoreException e) {
            logger.error(e);
        }

        return null;
    }

    /**
     * Cap the length of the supplied string. Does return the source string unchanged if it already is short enough.
     *
     * @param str
     *            the original unchanged string.
     * @param length
     *            the length to which the string will be capped.
     * @return the string shortened to the supplied length.
     */
    public static String capString(String str, int length) {
        return str.length() >= length ? str : str.substring(0, length);
    }

    public static Appender getLoggerConsoleAppenderWithLevel(Level level) {
        ConsoleAppender console = new ConsoleAppender();

        console.setLayout(new PatternLayout(PATTERN));
        console.setThreshold(level);
        console.activateOptions();
        return console;
    }

    public static Appender getLoggerConsoleAppender() {
        return getLoggerConsoleAppenderWithLevel(LOGGING_LEVEL);
    }
}
