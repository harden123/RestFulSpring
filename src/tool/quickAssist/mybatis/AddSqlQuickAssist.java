package tool.quickAssist.mybatis;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;

import lombok.SneakyThrows;
import restfulspring.config.Log;
import tool.config.mybatis.MapperNamespaceCache;
import tool.dto.mybatis.MapperMethod;
import tool.quickAssist.QuickAssistCompletionProposal;
import tool.utils.MybatipseXmlUtil;
import tool.utils.XpathUtil;

public class AddSqlQuickAssist extends QuickAssistCompletionProposal {

	private MapperMethod method;
	private CompilationUnit astNode;

	public AddSqlQuickAssist(String displayString, MapperMethod mapperMethod, CompilationUnit astNode) {
		super(displayString);
		this.method = mapperMethod;
		this.astNode = astNode;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	@SneakyThrows
	public void apply(IDocument document) {
		StringBuffer sqlFragment = CopyParamQuickAssist.getSqlFragment(method);
		IDOMNode findXmlNode = findXmlNode(method);
		System.out.println(findXmlNode);
	}


	
	public static IDOMNode findXmlNode(MapperMethod method) {
		MethodDeclaration methodDeclaration = method.getMethodDeclaration();
		IRegion region = new Region(methodDeclaration.getStartPosition(), methodDeclaration.getLength());
		IMethodBinding resolveBinding = methodDeclaration.resolveBinding();
		IJavaElement srcElement = resolveBinding.getJavaElement();
		switch (srcElement.getElementType()) {
			case IJavaElement.METHOD:
				IMethod m = (IMethod) srcElement;
				return findNodeByExpression(m.getDeclaringType(), null, "//*[@id='" + srcElement.getElementName() + "']", region);
			default:
				break;
		}
		return null;
	}

	
	@SneakyThrows
	private static IDOMNode findNodeByExpression(IType type, IType triggerType, String expression, IRegion srcRegion) {
		if (type.isInterface() && (triggerType == null || type.equals(triggerType))) {
			IJavaProject project = type.getJavaProject();
			if (project == null)
				return null;
			for (IFile mapperFile : MapperNamespaceCache.getInstance().get(project, type.getFullyQualifiedName(), null)) {
				IDOMDocument mapperDocument = MybatipseXmlUtil.getMapperDocument(mapperFile);
				if (mapperDocument == null)
					continue;
				try {
					IDOMNode domNode = (IDOMNode) XpathUtil.xpathNode(mapperDocument, expression);
					if (domNode != null) {
						return domNode;
					}
				} catch (XPathExpressionException e) {
					Log.error(e.getMessage(), e);
				}
			}
		}
		return null;
	}
	

}
