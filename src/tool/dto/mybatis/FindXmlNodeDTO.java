package tool.dto.mybatis;

import org.eclipse.core.resources.IFile;
import org.w3c.dom.Node;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class FindXmlNodeDTO {
	private IFile mapperFile;
	private Node domNode;//org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode
}
