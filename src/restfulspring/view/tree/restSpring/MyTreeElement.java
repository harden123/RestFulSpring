package restfulspring.view.tree.restSpring;

import java.util.List;

import org.eclipse.swt.graphics.Image;

import com.google.common.collect.Lists;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import restfulspring.dto.JDTMethodDTO;
import restfulspring.dto.JDTTypeDTO;

@Getter
@Setter
@NoArgsConstructor
public class MyTreeElement {
	private String name;
	private Image image;
	private JDTTypeDTO jDTTypeDTO;
	private JDTMethodDTO jDTMethodDTO;
	
	private List<MyTreeElement> children = Lists.newArrayList();
	private MyTreeElement parent;


}
