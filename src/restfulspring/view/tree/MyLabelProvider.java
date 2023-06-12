package restfulspring.view.tree;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.ILabelProviderListener;
import org.eclipse.swt.graphics.Image;

public class MyLabelProvider implements ILabelProvider {
    public String getText(Object element) {
        return ((MyElement) element).getName();
    }

    public Image getImage(Object element) {
        return ((MyElement) element).getImage();
    }

    public void addListener(ILabelProviderListener listener) {}
    public void removeListener(ILabelProviderListener listener) {}
    public boolean isLabelProperty(Object element, String property) { return false; }
    public void dispose() {}
}