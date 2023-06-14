package restfulspring.view.tab;

import org.eclipse.swt.custom.CTabFolder;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolItem;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import restfulspring.view.tree.MyTreeElement;
@Getter
@Setter
@NoArgsConstructor
public class TabGroupDTO {
	private CTabFolder folder;
	private Text headText;
	private Text bodyText;
	private StyledText responseText;
	private ToolItem resetItem;
	private ToolItem formatItem;
	
	private MyTreeElement selectedTreeNode ;
}
