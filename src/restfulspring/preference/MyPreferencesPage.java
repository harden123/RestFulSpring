package restfulspring.preference;
import java.util.ArrayList;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.preference.FieldEditorPreferencePage;
import org.eclipse.jface.preference.StringFieldEditor;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;

import com.google.common.collect.Lists;

import restfulspring.Activator;
import restfulspring.constant.RestConstant;
import restfulspring.view.views.restSpring.RestFulSpringView;

public class MyPreferencesPage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {


	public MyPreferencesPage() {
        super(GRID);
    }

    @Override
    protected void createFieldEditors() {
        addField(new StringFieldEditor(RestConstant.LabelPrefix, "labelPrefix:", getFieldEditorParent()));
        addField(new StringFieldEditor(RestConstant.UrlPrefix, "urlPrefix:", getFieldEditorParent()));
        
        addField(new StringFieldEditor(RestConstant.LabelPrefix2, "labelPrefix2:", getFieldEditorParent()));
        addField(new StringFieldEditor(RestConstant.UrlPrefix2, "urlPrefix2:", getFieldEditorParent()));
        
        addField(new StringFieldEditor(RestConstant.LabelPrefix3, "labelPrefix3:", getFieldEditorParent()));
        addField(new StringFieldEditor(RestConstant.UrlPrefix3, "urlPrefix3:", getFieldEditorParent()));
        // 创建多行文本框
        MultiLineFieldEditor mt = new MultiLineFieldEditor(RestConstant.Headers,
                "headers:",
                getFieldEditorParent());

        addField(mt);

    }

    @Override
    public void init(IWorkbench workbench) {
        setPreferenceStore(Activator.getDefault().getPreferenceStore());
        setDescription("DefaultSettings");
    }
    
    /** 
     * {@inheritDoc}
     */
    @Override
    public boolean performOk() {
    		super.performOk();
    	   ViewPart customView = getCustomView(RestFulSpringView.id);
           if (customView != null) {
	           	RestFulSpringView rs  = (RestFulSpringView)customView ;
	           	Combo urlPrfixCombo = rs.getUrlPrfixCombo();
	           	initUrlPrfixCombo(urlPrfixCombo);
           }
           return true;
    }

    public static void initUrlPrfixCombo(Combo urlPrfixCombo) {
    	if (urlPrfixCombo == null) {
    		return ;
		}
		int selectionIndex = urlPrfixCombo.getSelectionIndex();
		if (selectionIndex==-1) {
			selectionIndex=0;
		}
		String LabelPrefix = Activator.getDefault().getPreferenceStore().getString(RestConstant.LabelPrefix);
		String LabelPrefix2 = Activator.getDefault().getPreferenceStore().getString(RestConstant.LabelPrefix2);
		String LabelPrefix3 = Activator.getDefault().getPreferenceStore().getString(RestConstant.LabelPrefix3);
		ArrayList<String> itemList = Lists.newArrayList();
		if (StringUtils.isNotBlank(LabelPrefix)) {
			itemList.add(LabelPrefix);
		}
		if (StringUtils.isNotBlank(LabelPrefix2)) {
			itemList.add(LabelPrefix2);
		}
		if (StringUtils.isNotBlank(LabelPrefix3)) {
			itemList.add(LabelPrefix3);
		}
		String[] array = itemList.stream().toArray(String[]::new);
		urlPrfixCombo.setItems(array);
		if (itemList.size()!=0&&selectionIndex<itemList.size()) {
			urlPrfixCombo.select(selectionIndex);
		}
	}
    
    private ViewPart getCustomView(String viewPartId) {
        IWorkbenchPage activePage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
        return (ViewPart) activePage.findView(viewPartId);
    }
    
	public static String getUrlPrfixByCombo(Combo urlPrfixCombo) {
		if (urlPrfixCombo == null) {
			return null;
		}
		String text = urlPrfixCombo.getText();
		String LabelPrefix = Activator.getDefault().getPreferenceStore().getString(RestConstant.LabelPrefix);
		String LabelPrefix2 = Activator.getDefault().getPreferenceStore().getString(RestConstant.LabelPrefix2);
		String LabelPrefix3 = Activator.getDefault().getPreferenceStore().getString(RestConstant.LabelPrefix3);
	    String url = null;
		if (StringUtils.equals(text, LabelPrefix)) {
			url = Activator.getDefault().getPreferenceStore().getString(RestConstant.UrlPrefix);
		}else if (StringUtils.equals(text, LabelPrefix2)) {
			url = Activator.getDefault().getPreferenceStore().getString(RestConstant.UrlPrefix2);
		}else if (StringUtils.equals(text, LabelPrefix3)) {
			url = Activator.getDefault().getPreferenceStore().getString(RestConstant.UrlPrefix3);
		}
		return url;

	}
}
