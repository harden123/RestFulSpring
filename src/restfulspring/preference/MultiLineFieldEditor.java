package restfulspring.preference;

import org.eclipse.jface.preference.FieldEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

public class MultiLineFieldEditor extends FieldEditor {

    private Text text;

    public MultiLineFieldEditor(String name, String labelText, Composite parent) {
        super(name, labelText, parent);
    }

    @Override
    protected void adjustForNumColumns(int numColumns) {
        ((GridData)text.getLayoutData()).horizontalSpan = numColumns - 1;
    }

    @Override
    protected void doFillIntoGrid(Composite parent, int numColumns) {
        Label label = getLabelControl(parent);
        label.setLayoutData(new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING | GridData.VERTICAL_ALIGN_BEGINNING));

        text = getTextControl(parent);
        GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.VERTICAL_ALIGN_BEGINNING);
        gd.grabExcessVerticalSpace = true;
        gd.verticalAlignment = SWT.BEGINNING;
        gd.horizontalSpan = numColumns - 1;
        gd.heightHint = text.getLineHeight() * 5; // 设置高度
        text.setLayoutData(gd);
    }

    @Override
    protected void doLoad() {
        if (text != null) {
            String value = getPreferenceStore().getString(getPreferenceName());
            text.setText(value);
        }
    }

    @Override
    protected void doLoadDefault() {
        if (text != null) {
            String defaultValue = getPreferenceStore().getDefaultString(getPreferenceName());
            text.setText(defaultValue);
        }
    }

    @Override
    protected void doStore() {
        getPreferenceStore().setValue(getPreferenceName(), text.getText());
    }

    @Override
    public int getNumberOfControls() {
        return 2;
    }

    public Text getTextControl(Composite parent) {
        if (text == null) {
            text = new Text(parent, SWT.MULTI | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
            text.setFont(parent.getFont());
//            text.addModifyListener(new ModifyListener() {
//                public void modifyText(ModifyEvent e) {
//                    valueChanged();
//                }
//            });
        } else {
            checkParent(text, parent);
        }
        return text;
    }
}