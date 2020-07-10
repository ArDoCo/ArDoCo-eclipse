package textAnalysis.core;

import org.eclipse.debug.ui.AbstractLaunchConfigurationTabGroup;
import org.eclipse.debug.ui.ILaunchConfigurationDialog;
import org.eclipse.debug.ui.ILaunchConfigurationTab;

/***
 * This Class manages which tab(s) will be loaded for the RunConfiguration.
 * @author Maike Rees (uoxix)
 *
 */
public class TabGroup extends AbstractLaunchConfigurationTabGroup {
	
    @Override
    public void createTabs(ILaunchConfigurationDialog arg0, String arg1) {
        setTabs(new ILaunchConfigurationTab[] { new MainTab() });
    }
}
