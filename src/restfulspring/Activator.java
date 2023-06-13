package restfulspring;

import org.eclipse.jface.preference.IPreferenceStore;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

/**
 * The activator class controls the plug-in life cycle
 */
public class Activator extends AbstractUIPlugin {

	// The plug-in ID
	public static final String PLUGIN_ID = "RestFulSpring"; //$NON-NLS-1$

	// The shared instance
	private static Activator plugin;
	
    private static IPreferenceStore preferenceStore;

	
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

}
