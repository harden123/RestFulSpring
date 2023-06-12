package restfulspring.view;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;

public class SWTFactory {

	static int commonStyle = SWT.BORDER;

	public static GridLayout createGridLayout(int column) {
		GridLayout gridLayout = new GridLayout(column, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginBottom=0;
		gridLayout.marginTop=0;
		gridLayout.marginLeft = 0;
		gridLayout.marginRight = 0;
		return gridLayout;
	}
	
	public static GridLayout createGridLayout(int column,int marginHeight,int marginWidth) {
		GridLayout gridLayout = new GridLayout(column, false);
		gridLayout.marginHeight = marginHeight;
		gridLayout.marginWidth = marginWidth;
		gridLayout.marginBottom=0;
		gridLayout.marginTop=0;
		gridLayout.marginLeft = 0;
		gridLayout.marginRight = 0;
		return gridLayout;

	}


	public static Composite createComposite(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE|commonStyle);
		return composite;
	}

	public static RowLayout createRowLayout() {
		RowLayout rowLayout = new RowLayout();
		rowLayout.marginTop = 0;
		rowLayout.marginBottom = 0;
		rowLayout.marginLeft = 0;
		rowLayout.marginRight = 0;
		return rowLayout;
	}
}
