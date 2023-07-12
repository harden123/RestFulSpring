package tool.quickAssist.mybatis;

import java.io.IOException;
import java.io.UnsupportedEncodingException;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.Region;
import org.eclipse.wst.sse.core.StructuredModelManager;
import org.eclipse.wst.sse.core.internal.provisional.IStructuredModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMModel;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMNode;
import org.eclipse.wst.xml.core.internal.provisional.format.FormatProcessorXML;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.Text;

import lombok.SneakyThrows;
import restfulspring.Activator;
import restfulspring.config.Log;
import tool.config.mybatis.MapperNamespaceCache;
import tool.dto.mybatis.FindXmlNodeDTO;
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
		IFile xmlMapperFile = findXmlMapperFile(method);
		addXmlStatement(xmlMapperFile,sqlFragment);
	}


	
	@SneakyThrows
	public static IFile findXmlMapperFile(MapperMethod method) {
		MethodDeclaration methodDeclaration = method.getMethodDeclaration();
		IMethodBinding resolveBinding = methodDeclaration.resolveBinding();
		IJavaElement srcElement = resolveBinding.getJavaElement();
		switch (srcElement.getElementType()) {
			case IJavaElement.METHOD:
				IMethod m = (IMethod) srcElement;
				IType type = m.getDeclaringType();
				if (type.isInterface()) {
					IJavaProject project = type.getJavaProject();
					if (project == null)
						return null;
					for (IFile mapperFile : MapperNamespaceCache.getInstance().get(project, type.getFullyQualifiedName(), null)) {
						IDOMDocument mapperDocument = MybatipseXmlUtil.getMapperDocument(mapperFile);
						if (mapperDocument == null)
							continue;
						return mapperFile;
					}
				}
			default:
				break;
		}
		return null;
	}

	public static FindXmlNodeDTO findXmlNode(MapperMethod method) {
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
	private static FindXmlNodeDTO findNodeByExpression(IType type, IType triggerType, String expression, IRegion srcRegion) {
		if (type.isInterface() && (triggerType == null || type.equals(triggerType))) {
			IJavaProject project = type.getJavaProject();
			if (project == null)
				return null;
			FindXmlNodeDTO findXmlNodeDTO = new FindXmlNodeDTO();
			for (IFile mapperFile : MapperNamespaceCache.getInstance().get(project, type.getFullyQualifiedName(), null)) {
				IDOMDocument mapperDocument = MybatipseXmlUtil.getMapperDocument(mapperFile);
				if (mapperDocument == null)
					continue;
				try {
					IDOMNode domNode = (IDOMNode) XpathUtil.xpathNode(mapperDocument, expression);
					if (domNode != null) {
						findXmlNodeDTO.setDomNode(domNode);
						findXmlNodeDTO.setMapperFile(mapperFile);
						return findXmlNodeDTO;
					}
				} catch (XPathExpressionException e) {
					Log.error(e.getMessage(), e);
				}
			}
		}
		return null;
	}
	
	
	private boolean addXmlStatement(IFile xmlMapperFile, StringBuffer sqlFragment) throws IOException, CoreException, UnsupportedEncodingException, XPathExpressionException {
		IStructuredModel model = StructuredModelManager.getModelManager().getModelForEdit(xmlMapperFile);
		if (model == null) {
			Activator.openDialog(MessageDialog.ERROR, "Cannot move statement to XML mapper",
					"Failed to create a model for the XML mapper " + xmlMapperFile.getProjectRelativePath().toString());
			return false;
		}

		try {
			model.beginRecording(this);
			model.aboutToChangeModel();
			if (model instanceof IDOMModel) {
				String delimiter = model.getStructuredDocument().getLineDelimiter();
				IDOMDocument mapperDoc = ((IDOMModel) model).getDocument();
				String id = method.getMethodDeclaration().getName().getFullyQualifiedName();
				Node domNode = XpathUtil.xpathNode(mapperDoc, "//*[@id='" + id + "']");
				if (domNode != null) {
					Activator.openDialog(MessageDialog.ERROR, "Cannot move statement to XML mapper",
							"An element with id '" + id + "' is already defined in " + xmlMapperFile.getProjectRelativePath().toString());
					return false;
				}
				Element root = mapperDoc.getDocumentElement();
				Element element = createStatementElement(mapperDoc, delimiter,sqlFragment);
				root.appendChild(mapperDoc.createTextNode(delimiter));
				root.appendChild(element);
				root.appendChild(mapperDoc.createTextNode(delimiter));
				new FormatProcessorXML().formatNode(element);
			}
		} finally {
			model.changedModel();
			if (!model.isSharedForEdit() && model.isSaveNeeded()) {
				model.save();
			}
			model.endRecording(this);
			model.releaseFromEdit();
		}
		return true;
	}

	private Element createStatementElement(IDOMDocument mapperDoc, String delimiter, StringBuffer sqlFragment) {
		MethodDeclaration methodDeclaration = method.getMethodDeclaration();
		ASTNode parent = methodDeclaration.getParent();
		String doName = null;
		if (parent instanceof TypeDeclaration) {
			TypeDeclaration clzTypeDeclaration = ((TypeDeclaration) parent);
			ITypeBinding[] interfaces = clzTypeDeclaration.resolveBinding().getInterfaces();
			if (interfaces!=null&&interfaces.length>0&&interfaces[0].getTypeArguments()!=null&&interfaces[0].getTypeArguments().length>0) {
				doName = interfaces[0].getTypeArguments()[0].getName();
			}
		}
		String methodName = methodDeclaration.resolveBinding().getName();
		Element element = null;
		if (StringUtils.startsWithIgnoreCase(methodName, "update")||StringUtils.startsWithIgnoreCase(methodName, "modify")||StringUtils.startsWithIgnoreCase(methodName, "upsert")) {
			element = mapperDoc.createElement("update");
		}else {
			element = mapperDoc.createElement("select");
		}
		element.setAttribute("id", methodDeclaration.getName().toString());
		
		String returnTypeStr = method.getReturnTypeStr();
		if (StringUtils.isNotBlank(returnTypeStr)&&!StringUtils.equalsIgnoreCase(returnTypeStr, "void")) {
			if (StringUtils.equals(returnTypeStr, doName)||(doName==null&&returnTypeStr.endsWith("DO"))) {
				element.setAttribute("resultMap", "BaseResultMap");
			}else {
				element.setAttribute("resultType", returnTypeStr);
			}
		}
		Text sqlText = mapperDoc.createCDATASection(delimiter + sqlFragment.toString() + delimiter);
		element.appendChild(sqlText);
		return element;
	}

}
