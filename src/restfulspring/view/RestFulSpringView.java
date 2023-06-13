package restfulspring.view;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.part.ViewPart;

import restfulspring.handlers.JdtSourceHandlers;
import restfulspring.view.tab.TabFolderFactory;
import restfulspring.view.tree.MyInput;
import restfulspring.view.tree.MyLabelProvider;
import restfulspring.view.tree.TreeContentProvider;
import restfulspring.view.tree.TreeViewFactory;

public class RestFulSpringView extends ViewPart {

	int commonStyle = SWT.BORDER;

	public RestFulSpringView() {
	}

	/**
	 * Create contents of the view part.
	 * 
	 * @param parent
	 */
	@Override
	public void createPartControl(Composite parent) {
		Composite composite = SWTFactory.createComposite(parent);
		composite.setLayout(SWTFactory.createGridLayout(1));

		/*------------------------- toolRow-----------------------------*/

		Composite toolRow = SWTFactory.createComposite(composite);
		GridData row1Data = new GridData(SWT.FILL, SWT.FILL, true, false);
		// row1.heightHint = 200;
		toolRow.setLayoutData(row1Data);
		toolRow.setLayout(SWTFactory.createGridLayout(6));

		Button refresh = new Button(toolRow, SWT.NONE);
		refresh.setText("refresh");
		refresh.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				// 按钮被单击时执行的代码
				JdtSourceHandlers.handle();
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent arg0) {
				// 如果按钮被默认选中，则执行的代码
				// 在大多数情况下，忽略此方法即可
			}
		});
		// Label label1 = new Label(row1, SWT.NONE);
		// label1.setText("Label 1");
		// label1.setBackground(new Color(100, 100, 100));
		// label1.setLayoutData(new GridData(50, 50)); // 设置高度为 50
		// GridData FILLGridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		Button treeScroll = new Button(toolRow, SWT.NONE);
		treeScroll.setText("treeScroll");

		Button editorScroll = new Button(toolRow, SWT.NONE);
		editorScroll.setText("editorScroll");

		Button expand = new Button(toolRow, SWT.NONE);
		expand.setText("expand");

		Button collapse = new Button(toolRow, SWT.NONE);
		collapse.setText("collapse");

		Combo workSpaceCombo = new Combo(toolRow, SWT.READ_ONLY);
		workSpaceCombo.setToolTipText("select workSpace");
		workSpaceCombo.setItems(new String[] { "workSpace1", "workSpace2" });
		workSpaceCombo.select(0); // 设置选中第一个选项
		// 添加选项变更监听器
		workSpaceCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("选择了：" + workSpaceCombo.getText());
			}
		});

		/*------------------------- treeRow-----------------------------*/

		Composite treeRow = SWTFactory.createComposite(composite);
		GridData treeRowData = new GridData(SWT.FILL, SWT.FILL, true, false);
		treeRowData.heightHint = 200;
		treeRow.setLayoutData(treeRowData);
		treeRow.setLayout(SWTFactory.createGridLayout(1));
		TreeViewFactory.create(treeRow, new TreeContentProvider(), new MyLabelProvider(), MyInput.init());

		/*-------------------------queryRow -----------------------------*/
		Composite queryRow = SWTFactory.createComposite(composite);
		GridData row3Data = new GridData(SWT.FILL, SWT.FILL, true, false);
		queryRow.setLayoutData(row3Data);

		GridLayout rowLayout = SWTFactory.createGridLayout(3);
		queryRow.setLayout(rowLayout);

		Combo combo = new Combo(queryRow, SWT.READ_ONLY);
		combo.setItems(new String[] { "GET", "POST" });
		combo.select(0); // 设置选中第一个选项
		// 添加选项变更监听器
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				System.out.println("选择了：" + combo.getText());
			}
		});

		// 创建 Text 控件
		Text text = new Text(queryRow, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false)); // 设置布局数据

		// 添加文本变更监听器
		text.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				System.out.println("文本内容已修改为：" + text.getText());
			}
		});

		Button send = new Button(queryRow, SWT.NONE);
		send.setText("send");
		/*-------------------------resultRow -----------------------------*/
		Composite resultRow = SWTFactory.createComposite(composite);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		resultRow.setLayoutData(gridData);
		resultRow.setLayout(SWTFactory.createGridLayout(1));
		TabFolderFactory.create(resultRow);

		createActions();
		// Uncomment if you wish to add code to initialize the toolbar
		// initializeToolBar();
		initializeMenu();
	}

	/**
	 * Create the actions.
	 */
	private void createActions() {
		// Create the actions
	}

	/**
	 * Initialize the toolbar.
	 */
	private void initializeToolBar() {
		IToolBarManager toolbarManager = getViewSite().getActionBars().getToolBarManager();
	}

	/**
	 * Initialize the menu.
	 */
	private void initializeMenu() {
		IMenuManager menuManager = getViewSite().getActionBars().getMenuManager();
	}

	@Override
	public void setFocus() {
		// Set the focus
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void saveState(IMemento memento) {
		System.out.println("saveState");
		super.saveState(memento);
	}

}
