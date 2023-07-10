package restfulspring.view.views.sqlJson;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import restfulspring.view.SWTFactory;
import restfulspring.view.listener.sqlJson.SqlJsonChangeListener;

public class SqlJsonView extends ViewPart {

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent) {
		// TODO:hsl Auto-generated method stub
		Composite composite = SWTFactory.createComposite(parent);
		composite.setLayout(SWTFactory.createGridLayout(1));
		
		
		Composite sqlRow = SWTFactory.createComposite(composite);
		GridData treeRowData = new GridData(SWT.FILL, SWT.FILL, true, false);
		treeRowData.heightHint = 200;
		sqlRow.setLayoutData(treeRowData);
		sqlRow.setLayout(SWTFactory.createGridLayout(1));

		StyledText sqlText = new StyledText(sqlRow, SWT.MULTI  | SWT.WRAP | SWT.V_SCROLL);
		sqlText.setText("sql text");
		sqlText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite buttonRow = SWTFactory.createComposite(composite);
		GridData buttonRowData = new GridData(SWT.FILL, SWT.FILL, true, false);
		buttonRow.setLayoutData(buttonRowData);
		buttonRow.setLayout(SWTFactory.createGridLayout(1));


		Button refresh = new Button(buttonRow, SWT.NONE);
		refresh.setText("change");
		refresh.addSelectionListener(new SqlJsonChangeListener());

	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
		
	}

}
