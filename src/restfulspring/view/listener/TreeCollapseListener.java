package restfulspring.view.listener;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public class TreeCollapseListener implements SelectionListener{

	private TreeViewer treeViewer;

	public TreeCollapseListener(TreeViewer treeViewer) {
		this.treeViewer = treeViewer;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		treeViewer.collapseAll();

	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		
	}

}
