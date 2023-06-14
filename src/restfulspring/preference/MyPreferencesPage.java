package restfulspring.preference;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import restfulspring.Activator;
import restfulspring.constant.RestConstant;

public class MyPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {

    public MyPreferencesPage() {
        super(GRID);
    }

    @Override
    protected void createFieldEditors() {
        addField(new StringFieldEditor(RestConstant.UrlPrefix, "请求前缀:", getFieldEditorParent()));
    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("RestfulSpring配置");
    }
}
