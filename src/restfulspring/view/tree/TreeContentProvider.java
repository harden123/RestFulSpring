package restfulspring.view.tree;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TreeContentProvider  implements ITreeContentProvider {
    public Object[] getElements(Object inputElement) {
        // 返回一级元素
        return ((MyTreeInput) inputElement).getFirstLevelElements().toArray();
    }

    public Object[] getChildren(Object parentElement) {
        // 返回指定元素的子元素
        return ((MyTreeElement) parentElement).getChildren().toArray();
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
    public void dispose() {}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Object getParent(Object element) {
		 // 返回给定子元素的父元素
		MyTreeElement node = (MyTreeElement) element;
        return node.getParent();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildren(Object element) {
		  // 返回给定元素是否有子元素
		MyTreeElement node = (MyTreeElement) element;
		return !node.getChildren().isEmpty();
	}
}