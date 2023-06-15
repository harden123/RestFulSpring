package restfulspring.view.listener;

import java.util.Optional;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import restfulspring.dto.JDTMethodDTO;
import restfulspring.utils.FileNameUtils;
import restfulspring.utils.TextUtil;
import restfulspring.view.tree.MyTreeElement;

public class TreeScrollListener implements SelectionListener{

	private TreeViewer treeViewer;

	public TreeScrollListener(TreeViewer treeViewer) {
		this.treeViewer = treeViewer;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		IWorkbench workbench = PlatformUI.getWorkbench();
		IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
		IEditorPart editor = Optional.ofNullable(window).map(x -> x.getActivePage()).map(x -> x.getActiveEditor()).orElse(null);
		String filename = editor.getTitle();
		IEditorInput editorInput = editor.getEditorInput();
	    if (editorInput instanceof IFileEditorInput) {
	        IFile file = ((IFileEditorInput) editorInput).getFile();
	        filename = file.getName();
	    }
		System.out.println(filename);
		IMethod cursorMethod = TextUtil.getCursorMethod(editor);
		if (cursorMethod!=null) {
			System.out.println(cursorMethod.getElementName());
		}
		selectTreeView(FileNameUtils.getPrefix(filename),treeViewer,cursorMethod);
	}


	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		
	}
	

	private void selectTreeView(String editorTitle, TreeViewer treeViewer, IMethod cursorMethod) {
		// 获取所有节点
		Tree tree = treeViewer.getTree();
		TreeItem[] items = tree.getItems();

		for (int i = 0; i < items.length; i++) {
		    TreeItem item = items[i];
		    MyTreeElement myTreeElement = (MyTreeElement)item.getData();
		    if (myTreeElement.getName().equals(editorTitle)) {
		    	treeViewer.expandToLevel(item.getData(), TreeViewer.ALL_LEVELS);
		    	treeViewer.reveal(item.getData());
		    	if (cursorMethod != null) {
		    		traverseTreeItem(cursorMethod,treeViewer,item);
				}
			}
		}
		
	}
	
	// 遍历树节点
	private void traverseTreeItem(IMethod cursorMethod, TreeViewer treeViewer, TreeItem item) {
		TreeItem[] items = item.getItems();
		for (TreeItem childTreeItem : items) {
			MyTreeElement child = ((MyTreeElement)childTreeItem.getData());
			JDTMethodDTO jdtMethodDTO = child.getJDTMethodDTO();
			IMethod method = jdtMethodDTO.getMethod();
			if (method.isSimilar(cursorMethod)) {
		    	treeViewer.reveal(childTreeItem.getData());
			}
		}
	}

}
