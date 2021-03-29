package edu.kit.textannotation.annotationplugin.wizards;

import edu.kit.textannotation.annotationplugin.profile.AnnotationProfile;
import edu.kit.textannotation.annotationplugin.profile.AnnotationProfileRegistry;
import edu.kit.textannotation.annotationplugin.textmodel.xmlinterface.AnnotationProfileXmlInterface;
import edu.kit.textannotation.annotationplugin.utils.EclipseUtils;
import edu.kit.textannotation.annotationplugin.utils.EventManager;
import edu.kit.textannotation.annotationplugin.views.EditProfileDialog;
import org.eclipse.core.resources.*;
import org.eclipse.core.runtime.*;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.*;
import org.osgi.framework.FrameworkUtil;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.UUID;

/**
 * This wizard is responsible for creating new profile files. It is registered as an direct
 * contribution by the eclipse plugin, and references {@link ProfileWizardPage} as UI.
 */

public class ProfileWizard extends Wizard implements INewWizard {
	private ProfileWizardPage page;
	private ISelection selection;

	/** This event fires when the wizard finishes and a new profile is created. */
	public final EventManager<EventManager.EmptyEvent> onFinish = new EventManager<>("profilewizard:finish");

	/**
	 * Constructor for ProfileWizard.
	 */
	public ProfileWizard() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * Adding the page to the wizard.
	 */
	@Override
	public void addPages() {
		page = new ProfileWizardPage(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	@Override
	public boolean performFinish() {
		final String containerName = page.getContainerName();
		final String fileName = page.getFileName();
		final String profileName = page.getProfile();
		
		return this.performCreationProfile(containerName, fileName, profileName);
	}
	
	private String generateProfileIdFromProfileName(String profileName) {
		String generatedProfileId;
		int maxLengthProfileName = 8;	
		generatedProfileId = String.format(
				"%s_%s",
				EclipseUtils.capString(profileName.toLowerCase().replaceAll("\\s", "-"), maxLengthProfileName),
				EclipseUtils.capString(UUID.randomUUID().toString().toLowerCase().replaceAll("\\s", "-"), 4)
			);
		return generatedProfileId;
	}

	public boolean performCreationProfile(String containerName, String fileName, String profileName) {
		System.out.println("Perform creation profile");
		final String profileId = generateProfileIdFromProfileName(profileName);
		IRunnableWithProgress op = monitor -> {
			System.out.println("Before try");
			try {
				System.out.println("Try");
				doFinish(containerName, fileName, profileName, profileId, monitor);
			} catch (CoreException e) {
				throw new InvocationTargetException(e);
			} finally {
				System.out.println("Finally");
				monitor.done();
			}
		};
		try {
			if(getContainer() != null) {
				System.out.println("getContainer != null");
				getContainer().run(true, false, op);
			} else {
				System.out.println("getContainer == null");
				doFinishWithoutContainer(containerName, fileName, profileName, profileId);
				
			}
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		onFinish.fire(new EventManager.EmptyEvent());

		Display.getDefault().asyncExec(() -> EditProfileDialog.openWindow(
				AnnotationProfileRegistry.createNew(FrameworkUtil.getBundle(this.getClass())),
				profileId,
				annotationProfile -> {},
				null
		));

		return true;
	}

	/**
	 * The worker method. It will find the container and create the
	 * file if missing or just replace its contents.
	 */
	private void doFinish(
		String containerName,
		String fileName,
		String profileName,
		String profileId,
		IProgressMonitor monitor)
		throws CoreException {
		// create a sample file
		monitor.beginTask("Creating " + fileName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		try {
			InputStream stream = openContentStream(profileName, profileId);
			if (file.exists()) {
				file.setContents(stream, true, true, monitor);
			} else {
				file.create(stream, true, monitor);
			}
			stream.close();
		} catch (IOException e) {
		}
	}
	
	private void doFinishWithoutContainer(String containerName, String fileName,String profileName,String profileId) throws CoreException {
		System.out.println("Perform without container");
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		IContainer container = (IContainer) resource;
		final IFile file = container.getFile(new Path(fileName));
		File javaIOFile = file.getLocation().toFile();
		System.out.println(javaIOFile);
		try {
			InputStream stream = openContentStream(profileName, profileId);
			byte[] buffer = new byte[stream.available()];
			stream.read(buffer);
			
			OutputStream outStream = new FileOutputStream(javaIOFile);
			outStream.write(buffer);
			outStream.close();
		} catch (IOException e) {
			
		}
	}
	
	/**
	 * We will initialize file contents with a sample text.
	 */
	private InputStream openContentStream(String profileName, String profileId) {
		String content = "";

		content = (new AnnotationProfileXmlInterface())
				.buildXml(new AnnotationProfile(profileId, profileName));

		return new ByteArrayInputStream(content.getBytes());
	}

	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "edu.kit.textannotation.annotationplugin", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	@Override
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}
}