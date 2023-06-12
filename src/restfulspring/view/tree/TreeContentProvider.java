package restfulspring.view.tree;

import org.eclipse.jface.viewers.ITreeContentProvider;
import org.eclipse.jface.viewers.Viewer;

public class TreeContentProvider  implements ITreeContentProvider {
    public Object[] getElements(Object inputElement) {
        // 返回一级元素
        return ((MyInput) inputElement).getFirstLevelElements();
    }

    public Object[] getChildren(Object parentElement) {
        // 返回指定元素的子元素
        return ((MyElement) parentElement).getChildren();
    }

    public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {}
    public void dispose() {}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Object getParent(Object element) {
		 // 返回给定子元素的父元素
		MyElement node = (MyElement) element;
        return node.getParent();
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasChildren(Object element) {
		  // 返回给定元素是否有子元素
		MyElement node = (MyElement) element;
        return node.getChildren()!=null&&node.getChildren().length!=0;
	}
}