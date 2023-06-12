package restfulspring.view.tree;

import org.eclipse.jface.viewers.IBaseLabelProvider;
import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

public class TreeViewFactory {

	
	public static void create(Composite parent,ITreeContentProvider contentProvider,IBaseLabelProvider labelProvider
			,Object firstInput) {
		 // 创建第一级 TreeViewer
		 TreeViewer firstLevelViewer = new TreeViewer(parent, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
		 GridData firstLevelData = new GridData(SWT.FILL, SWT.FILL, true, true);
		 firstLevelData.heightHint = 100;
		 firstLevelViewer.getControl().setLayoutData(firstLevelData);
		 firstLevelViewer.setContentProvider(contentProvider);
		 firstLevelViewer.setLabelProvider(labelProvider);
		 firstLevelViewer.setInput(firstInput);
	
		 // 创建第二级 TreeViewer
//		 Composite secondLevelComposite = new Composite(parent, SWT.NONE);
//		 secondLevelComposite.setLayout(new GridLayout());
//		 GridData secondLevelData = new GridData(SWT.FILL, SWT.FILL, true, true);
//		 secondLevelData.heightHint = 100;
//		 secondLevelComposite.setLayoutData(secondLevelData);
//		 TreeViewer secondLevelViewer = new TreeViewer(secondLevelComposite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
//		 secondLevelViewer.setContentProvider(contentProvider);
//		 secondLevelViewer.setLabelProvider(labelProvider);
//		 secondLevelViewer.setInput(secInput);
//		 GridData secondLevelViewerData = new GridData(SWT.FILL, SWT.FILL, true, true);
//		 secondLevelViewer.getControl().setLayoutData(secondLevelViewerData);
	
		 // 创建第三级 TreeViewer
//		 Composite thirdLevelComposite = new Composite(secondLevelComposite, SWT.NONE);
//		 thirdLevelComposite.setLayout(new GridLayout());
//		 GridData thirdLevelData = new GridData(SWT.FILL, SWT.FILL, true, true);
//		 thirdLevelData.heightHint = 100;
//		 thirdLevelComposite.setLayoutData(thirdLevelData);
//		 TreeViewer thirdLevelViewer = new TreeViewer(thirdLevelComposite, SWT.BORDER | SWT.SINGLE | SWT.H_SCROLL | SWT.V_SCROLL);
//		 thirdLevelViewer.setContentProvider(contentProvider);
//		 thirdLevelViewer.setLabelProvider(labelProvider);
//		 thirdLevelViewer.setInput(thirdInput);
//		 GridData thirdLevelViewerData = new GridData(SWT.FILL, SWT.FILL, true, true);
//		 thirdLevelViewer.getControl().setLayoutData(thirdLevelViewerData);

	}
}
