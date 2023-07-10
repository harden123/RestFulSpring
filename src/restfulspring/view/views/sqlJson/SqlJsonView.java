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
		sqlText.setText("sql");
		sqlText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		Composite buttonRow = SWTFactory.createComposite(composite);
		GridData buttonRowData = new GridData(SWT.FILL, SWT.FILL, true, false);
		buttonRow.setLayoutData(buttonRowData);
		buttonRow.setLayout(SWTFactory.createGridLayout(1));


		Button changeBtn = new Button(buttonRow, SWT.NONE);
		changeBtn.setText("change");
		
		
		Composite resultRow = SWTFactory.createComposite(composite);
		GridData resultRowData = new GridData(SWT.FILL, SWT.FILL, true, true);
//		resultRowData.heightHint = 200;
		resultRow.setLayoutData(resultRowData);
		resultRow.setLayout(SWTFactory.createGridLayout(1));
		
		StyledText resultText = new StyledText(resultRow, SWT.MULTI   | SWT.WRAP | SWT.V_SCROLL);
		resultText.setText("result");
		resultText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		changeBtn.addSelectionListener(new SqlJsonChangeListener(sqlText,resultText));

	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
		
	}

}
