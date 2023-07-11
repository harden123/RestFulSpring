package restfulspring.view.views.sqlJson;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import restfulspring.Activator;
import restfulspring.constant.RestConstant;
import restfulspring.constant.YmdTypeEnum;
import restfulspring.view.SWTFactory;
import restfulspring.view.listener.sqlJson.JsonSqlChangeListener;
import restfulspring.view.listener.sqlJson.SqlJsonChangeListener;

public class SqlJsonView extends ViewPart {

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void createPartControl(Composite parent) {
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
		buttonRow.setLayout(SWTFactory.createGridLayout(3));


		Button changeBtn = new Button(buttonRow, SWT.NONE);
		changeBtn.setText("toJson");
		

		Button sqlBtn = new Button(buttonRow, SWT.NONE);
		sqlBtn.setText("toSql");
		
		Combo ymdCombo = new Combo(buttonRow, SWT.READ_ONLY);
		String[] arr = YmdTypeEnum.toDescArray();
		ymdCombo.setItems(arr);
		
		String selectedText = Activator.getDefault().getPreferenceStore().getString(RestConstant.SqlJson_ymd);
		Integer selectIndex = YmdTypeEnum.ymdhmsz.getKey();
		if (StringUtils.isNotBlank(selectedText)) {
			Integer temp = YmdTypeEnum.getKeyByDesc(selectedText);
			if (temp != null) {
				selectIndex = temp;
			}
		}
		ymdCombo.select(selectIndex);
		
		// 添加选项变更监听器
		ymdCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo selectedCombo = (Combo) e.getSource();
	            String selectedText = selectedCombo.getText();
	            Activator.getDefault().getPreferenceStore().putValue(RestConstant.SqlJson_ymd, selectedText);
			}
		});     
        
		
		
		Composite resultRow = SWTFactory.createComposite(composite);
		GridData resultRowData = new GridData(SWT.FILL, SWT.FILL, true, true);
//		resultRowData.heightHint = 200;
		resultRow.setLayoutData(resultRowData);
		resultRow.setLayout(SWTFactory.createGridLayout(1));
		
		StyledText resultText = new StyledText(resultRow, SWT.MULTI   | SWT.WRAP | SWT.V_SCROLL);
		resultText.setText("result");
		resultText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		changeBtn.addSelectionListener(new SqlJsonChangeListener(sqlText,resultText,ymdCombo));
		sqlBtn.addSelectionListener(new JsonSqlChangeListener(sqlText,resultText));
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
		
	}

}
