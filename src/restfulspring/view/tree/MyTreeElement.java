package restfulspring.view.tree;

import org.eclipse.swt.graphics.Image;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class MyTreeElement {
	private String name;
	private Image image;
	private Object[] children;
	private MyTreeElement parent;


}
