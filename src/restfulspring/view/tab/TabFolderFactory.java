package restfulspring.view.tab;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import restfulspring.view.SWTFactory;

public class TabFolderFactory {

	public static void create(Composite parent) {

		// 创建 CTabFolder 控件
		CTabFolder folder = new CTabFolder(parent, SWT.BORDER);
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		/*------------------------- head-----------------------------*/
		// 创建第一个标签页
		CTabItem tab1 = new CTabItem(folder, SWT.NONE);
		tab1.setText("head");

		Composite composite1 = SWTFactory.createComposite(folder);
		composite1.setLayout(new GridLayout());

		Text text1 = new Text(composite1, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		text1.setText("input head here.");
		text1.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		tab1.setControl(composite1);
		/*------------------------- body-----------------------------*/

		// 创建第二个标签页
		CTabItem tab2 = new CTabItem(folder, SWT.NONE);
		tab2.setText("body");

		Composite composite2 = SWTFactory.createComposite(folder);
		composite2.setLayout(new GridLayout());

		Text text2 = new Text(composite2, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		text2.setText("input head here.");
		text2.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		
		tab2.setControl(composite2);
		
		/*------------------------- response-----------------------------*/
		// 创建第二个标签页
		CTabItem tab3 = new CTabItem(folder, SWT.NONE);
		tab3.setText("response");

		Composite composite3 = SWTFactory.createComposite(folder);
		composite3.setLayout(new GridLayout());

		StyledText styledText = new StyledText(composite3, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		styledText.setText("Multiline read-only text");
		styledText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		tab3.setControl(composite3);
		
		folder.setSelection(0);

	}
}
