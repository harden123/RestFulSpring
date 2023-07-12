package restfulspring;

import java.util.HashMap;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import com.google.common.collect.Maps;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "RestFulSpring"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
    private static IPreferenceStore preferenceStore;
    public static HashMap<String, String> MethodUrl2BodyTextCacheMap = Maps.newHashMap();

	
	public Activator() {
	}

	@Override
	public void start(BundleContext context) throws Exception {
		super.start(context);
		plugin = this;
	}

	@Override
	public void stop(BundleContext context) throws Exception {
		plugin = null;
		super.stop(context);
	}

	public static Activator getDefault() {
		return plugin;
	}
	
	/** 
	 * {@inheritDoc}
	 */
	@Override
	protected void initializeDefaultPreferences(IPreferenceStore store) {
	    // 初始化偏好存储对象
        preferenceStore = getPreferenceStore();
        // 初始化所有控件的默认值
//        preferenceStore.setDefault("control1.value", /* 控件1的默认值 */);
//        preferenceStore.setDefault("control2.value", /* 控件2的默认值 */);
	}
	
	
	public static Image getIcon() {
		return getImageDescriptor("/icons/sample.png").createImage(); //$NON-NLS-1$
	}

	public static Image getIcon(String path) {
		return getImageDescriptor(path).createImage();
	}

	public static ImageDescriptor getImageDescriptor(String path) {
		return imageDescriptorFromPlugin(PLUGIN_ID, path);
	}
	

	public static boolean openDialog(int kind, String title, String message) {
		Shell shell = Display.getDefault().getActiveShell();
		return MessageDialog.open(kind, shell, title, message, SWT.SHEET);
	}

}
