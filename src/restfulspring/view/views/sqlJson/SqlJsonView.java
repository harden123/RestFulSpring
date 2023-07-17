package restfulspring.view.views.sqlJson;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
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
		
		/*------------------------- buttonRow-----------------------------*/

		Composite buttonRow = SWTFactory.createComposite(composite);
		GridData buttonRowData = new GridData(SWT.FILL, SWT.FILL, true, false);
		buttonRow.setLayoutData(buttonRowData);
		buttonRow.setLayout(SWTFactory.createGridLayout(8));


		Button toJsonBtn = new Button(buttonRow, SWT.NONE);
		toJsonBtn.setText("toJson");
		

		Button toSqlBtn = new Button(buttonRow, SWT.NONE);
		toSqlBtn.setText("toSql");
		
		
		Label toJsonDateLabel = new Label(buttonRow, SWT.NONE);
		toJsonDateLabel.setText("toJsonDate:");
		 
		Combo toJsonDateCombo = new Combo(buttonRow, SWT.READ_ONLY);
		String[] arr = YmdTypeEnum.toDescArray();
		toJsonDateCombo.setItems(arr);
		
		String selectedText = Activator.getDefault().getPreferenceStore().getString(RestConstant.SqlJson_ymd);
		Integer selectIndex = YmdTypeEnum.ymdhmsz.getKey();
		if (StringUtils.isNotBlank(selectedText)) {
			Integer temp = YmdTypeEnum.getKeyByDesc(selectedText);
			if (temp != null) {
				selectIndex = temp;
			}
		}
		toJsonDateCombo.select(selectIndex);
		
		// 添加选项变更监听器
		toJsonDateCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Combo selectedCombo = (Combo) e.getSource();
	            String selectedText = selectedCombo.getText();
	            Activator.getDefault().getPreferenceStore().putValue(RestConstant.SqlJson_ymd, selectedText);
			}
		});     
        
		
		
		
		Label toSqlDateLabel = new Label(buttonRow, SWT.NONE);
		toSqlDateLabel.setText("toSqlDate:");
		
		Combo toSqlDateCombo = new Combo(buttonRow, SWT.READ_ONLY);
		toSqlDateCombo.setItems(YmdTypeEnum.toDescArray());
		
		String selectedText2 = Activator.getDefault().getPreferenceStore().getString(RestConstant.JsonSql_ymd);
		Integer selectIndex2 = YmdTypeEnum.ymdhms.getKey();
		if (StringUtils.isNotBlank(selectedText2)) {
			Integer temp = YmdTypeEnum.getKeyByDesc(selectedText2);
			if (temp != null) {
				selectIndex2 = temp;
			}
		}
		toSqlDateCombo.select(selectIndex2);
		
		// 添加选项变更监听器
		toSqlDateCombo.addSelectionListener(new SelectionAdapter() {
		@Override
			public void widgetSelected(SelectionEvent e) {
				Combo selectedCombo = (Combo) e.getSource();
	            String selectedText = selectedCombo.getText();
	            Activator.getDefault().getPreferenceStore().putValue(RestConstant.JsonSql_ymd, selectedText);
			}
		});     

		
		Button clearBtn = new Button(buttonRow, SWT.NONE);
		toSqlBtn.setText("clear");
		
		
		/*------------------------- resultRow-----------------------------*/
		
		Composite resultRow = SWTFactory.createComposite(composite);
		GridData resultRowData = new GridData(SWT.FILL, SWT.FILL, true, true);
//		resultRowData.heightHint = 200;
		resultRow.setLayoutData(resultRowData);
		resultRow.setLayout(SWTFactory.createGridLayout(1));
		
		StyledText resultText = new StyledText(resultRow, SWT.MULTI   | SWT.WRAP | SWT.V_SCROLL);
		resultText.setText("json");
		resultText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		toJsonBtn.addSelectionListener(new SqlJsonChangeListener(sqlText,resultText,toJsonDateCombo));
		toSqlBtn.addSelectionListener(new JsonSqlChangeListener(sqlText,resultText,toSqlDateCombo));
		clearBtn.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				sqlText.setText("");
				resultText.setText("");
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				
			}
		});
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void setFocus() {
		
	}
}
