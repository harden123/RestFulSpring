package restfulspring.view.tab;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.CTabItem;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import restfulspring.view.SWTFactory;

public class TabFolderFactory {

	public static TabGroupDTO create(Composite parent) {
		// 创建 CTabFolder 控件
		CTabFolder folder = new CTabFolder(parent, SWT.BORDER);
		folder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		/*------------------------- head-----------------------------*/
		// 创建第一个标签页
		CTabItem tab1 = new CTabItem(folder, SWT.NONE);
		tab1.setText("head");

		Composite composite1 = SWTFactory.createComposite(folder);
		composite1.setLayout(new GridLayout());

		Text headText = new Text(composite1, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		headText.setMessage("key1:value1\r\nkey2:value2");
		headText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));

		tab1.setControl(composite1);
		/*------------------------- body-----------------------------*/

		// 创建第二个标签页
		CTabItem tab2 = new CTabItem(folder, SWT.NONE);
		tab2.setText("body");

		Composite composite2 = SWTFactory.createComposite(folder);
		composite2.setLayout(new GridLayout());

		Text bodyText = new Text(composite2, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		bodyText.setMessage("input json body here.");
		bodyText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		FontData fontData = bodyText.getFont().getFontData()[0];
        Font font = new Font(composite2.getDisplay(), fontData.getName(), 10, fontData.getStyle());
        bodyText.setFont(font);
		tab2.setControl(composite2);
		
		/*------------------------- response-----------------------------*/
		// 创建第二个标签页
		CTabItem tab3 = new CTabItem(folder, SWT.NONE);
		tab3.setText("response");

		Composite composite3 = SWTFactory.createComposite(folder);
		composite3.setLayout(new GridLayout());

		StyledText responseText = new StyledText(composite3, SWT.MULTI | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL);
		responseText.setText("response read-only text");
		responseText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		tab3.setControl(composite3);
		
		folder.setSelection(0);
		
		/*------------------------- ToolItem-----------------------------*/
		  // 创建工具栏按钮并添加到 CTabFolder 中
        ToolBar toolBar = new ToolBar(folder, SWT.FLAT);
        ToolItem resetItem = new ToolItem(toolBar, SWT.PUSH);
        resetItem.setText("reset");
        ToolItem formatItem = new ToolItem(toolBar, SWT.PUSH);
        formatItem.setText("format");
        
        folder.setTopRight(toolBar);
        // 添加工具栏按钮的事件监听器
        resetItem.addSelectionListener(new SelectionAdapter() {
            @Override
            public void widgetSelected(SelectionEvent e) {
            	CTabItem selection = folder.getSelection();
            	System.out.println(selection.getText());
            }
        });
        
        TabGroupDTO tabGroupDTO = new TabGroupDTO();
        tabGroupDTO.setFormatItem(formatItem);
        tabGroupDTO.setHeadText(headText);
        tabGroupDTO.setResponseText(responseText);
        tabGroupDTO.setBodyText(bodyText);
        tabGroupDTO.setFolder(folder);
        tabGroupDTO.setResetItem(resetItem);
        return tabGroupDTO;


	}
}
