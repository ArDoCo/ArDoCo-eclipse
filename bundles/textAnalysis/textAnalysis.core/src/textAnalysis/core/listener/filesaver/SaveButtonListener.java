package textAnalysis.core.listener.filesaver;

import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import textAnalysis.core.listener.FileButtonListener;

public abstract class SaveButtonListener extends FileButtonListener {

    public SaveButtonListener(Text field, String[] fileExtensions, String dialogTitle, Shell shell) {
        super(field, fileExtensions, dialogTitle, shell);
    }

    public abstract String saveFileDialog(Text textField, String[] fileExtension);

    /*
     * (non-Javadoc)
     *
     * @see org.eclipse.swt.events.SelectionAdapter#widgetSelected(org.eclipse.swt.events.SelectionEvent)
     */
    @Override
    public void widgetSelected(SelectionEvent e) {
        String selectedFile = saveFileDialog(getField(), getExtensions());
        if (selectedFile != null) {
            field.setText(selectedFile);
        }
    }

    protected Text getField() {
        return field;
    }

    protected String[] getExtensions() {
        return extensions;
    }

}
