package restfulspring.view.tree.restSpring;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class TreeLabelProvider implements ILabelProvider {
    public String getText(Object element) {
        return ((MyTreeElement) element).getName();
    }

    public Image getImage(Object element) {
        return ((MyTreeElement) element).getImage();
    }

    public void addListener(ILabelProviderListener listener) {}
    public void removeListener(ILabelProviderListener listener) {}
    public boolean isLabelProperty(Object element, String property) { return false; }
    public void dispose() {}
}