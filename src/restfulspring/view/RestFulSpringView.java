package restfulspring.view;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.viewers.TreeViewer;
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
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.part.ViewPart;

import com.google.common.collect.Lists;

import restfulspring.constant.RestTypeEnum;
import restfulspring.dto.JDTMethodDTO;
import restfulspring.dto.JDTTypeDTO;
import restfulspring.handlers.JdtSourceHandlers;
import restfulspring.utils.CollectionUtils;
import restfulspring.view.listener.SendButtonListener;
import restfulspring.view.listener.TreeDoubleClickLinstener;
import restfulspring.view.tab.TabFolderFactory;
import restfulspring.view.tab.TabGroupDTO;
import restfulspring.view.tree.MyTreeElement;
import restfulspring.view.tree.MyTreeInput;
import restfulspring.view.tree.TreeContentProvider;
import restfulspring.view.tree.TreeLabelProvider;
import restfulspring.view.tree.TreeViewFactory;

public class RestFulSpringView extends ViewPart {

	int commonStyle = SWT.BORDER;
	private static MyTreeInput treeData;
	private static TreeViewer treeViewer;

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
		Button treeScroll = new Button(toolRow, SWT.NONE);//<代码关联树滚动>将树滚动到当前激活类,并展开
		treeScroll.setText("treeScroll");

//		Button editorScroll = new Button(toolRow, SWT.NONE);
//		editorScroll.setText("editorScroll");

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
		treeData = MyTreeInput.mockMyTreeInput(10);
		treeViewer = TreeViewFactory.create(treeRow, new TreeContentProvider(), new TreeLabelProvider(), treeData);

		/*-------------------------queryRow -----------------------------*/
		Composite queryRow = SWTFactory.createComposite(composite);
		GridData row3Data = new GridData(SWT.FILL, SWT.FILL, true, false);
		queryRow.setLayoutData(row3Data);

		GridLayout rowLayout = SWTFactory.createGridLayout(3);
		queryRow.setLayout(rowLayout);

		Combo getCombo = new Combo(queryRow, SWT.READ_ONLY);
		getCombo.setItems(new String[] { RestTypeEnum.GET.toString(), RestTypeEnum.POST.toString()});
		getCombo.select(RestTypeEnum.GET.getKey()); // 设置选中第一个选项
		// 添加选项变更监听器
		getCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
//				System.out.println("选择了：" + getCombo.getText());
			}
		});

		//urlText
		Text urlText = new Text(queryRow, SWT.BORDER);
		urlText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false)); // 设置布局数据

		// 添加文本变更监听器
		urlText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
//				System.out.println("文本内容已修改为：" + urlText.getText());
			}
		});

		Button send = new Button(queryRow, SWT.NONE);
		send.setText("send");
		/*-------------------------resultRow -----------------------------*/
		Composite resultRow = SWTFactory.createComposite(composite);
		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, true);
		resultRow.setLayoutData(gridData);
		resultRow.setLayout(SWTFactory.createGridLayout(1));
		TabGroupDTO tabGroupDTO = TabFolderFactory.create(resultRow);

		
		
		/*-------------------------initial -----------------------------*/
		createActions();
		// Uncomment if you wish to add code to initialize the toolbar
		// initializeToolBar();
		initializeMenu();
		
		initialDatas();
		
		
		/*-------------------------linsteners -----------------------------*/

		
		treeViewer.addDoubleClickListener(new TreeDoubleClickLinstener(getCombo,urlText,tabGroupDTO));
		send.addSelectionListener(new SendButtonListener(getCombo,urlText,tabGroupDTO));

	}


	private void initialDatas() {
		if (CollectionUtils.isEmpty(JdtSourceHandlers.getList())) {
			JdtSourceHandlers.handle();
		}else if (CollectionUtils.isNotEmpty(JdtSourceHandlers.getList())) {
			notifyRefreshTree();
		}
	}

	public static void notifyRefreshTree() {
		Display.getDefault().asyncExec(new Runnable() {
		    public void run() {
		    	if (treeViewer==null) {
					return ;
				}
		    	List<JDTTypeDTO> jDTTypeDTOs = JdtSourceHandlers.getList();
				if (CollectionUtils.isEmpty(jDTTypeDTOs)) {
					treeData.setFirstLevelElements(Lists.newArrayList());
					treeViewer.refresh();
					return ;
				}
				List<MyTreeElement> typeElements = Lists.newArrayListWithExpectedSize(jDTTypeDTOs.size());
				for (JDTTypeDTO jDTTypeDTO : jDTTypeDTOs) {
					MyTreeElement typeElement = new MyTreeElement();
					typeElement.setName(jDTTypeDTO.getType().getName().toString());
					typeElement.setJDTTypeDTO(jDTTypeDTO);
					List<MyTreeElement> children = typeElement.getChildren();
					HashMap<String, JDTMethodDTO> methodName2DTOMap = jDTTypeDTO.getMethodName2DTOMap();
					for (Map.Entry<String, JDTMethodDTO> entry : methodName2DTOMap.entrySet()) {
						String methodName = entry.getKey();
						JDTMethodDTO jDTMethodDTO = entry.getValue();
						MyTreeElement child = new MyTreeElement();
						child.setName(methodName);
						child.setJDTMethodDTO(jDTMethodDTO);
						child.setParent(typeElement);
						children.add(child);
					}
					typeElements.add(typeElement);
				}
				treeData.setFirstLevelElements(typeElements);
				treeViewer.refresh();
		    }
		});
		
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
