package restfulspring.view.listener.restSpring;

import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;

public class TreeExpandListener implements SelectionListener{

	private TreeViewer treeViewer;

	public TreeExpandListener(TreeViewer treeViewer) {
		this.treeViewer = treeViewer;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void widgetSelected(SelectionEvent e) {
		treeViewer.expandAll();
		
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		
	}

}
