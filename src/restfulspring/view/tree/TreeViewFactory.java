package restfulspring.view.tree;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class TreeViewFactory {

	
	public static TreeViewer create(Composite parent,ITreeContentProvider contentProvider,IBaseLabelProvider labelProvider
			,Object firstInput) {
		 // 创建第一级 TreeViewer
		 TreeViewer firstLevelViewer = new TreeViewer(parent, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		 GridData firstLevelData = new GridData(SWT.FILL, SWT.FILL, true, true);
		 firstLevelData.heightHint = 100;
		 firstLevelViewer.getControl().setLayoutData(firstLevelData);
		 firstLevelViewer.setContentProvider(contentProvider);
		 firstLevelViewer.setLabelProvider(labelProvider);
		 firstLevelViewer.setInput(firstInput);
		 return firstLevelViewer;

	}
}
