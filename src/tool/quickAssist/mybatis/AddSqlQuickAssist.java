package tool.quickAssist.mybatis;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import javax.xml.xpath.XPathExpressionException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.IDocument;
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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.SneakyThrows;
import restfulspring.Activator;
import restfulspring.config.Log;
import restfulspring.utils.CollectionUtils;
import tool.config.mybatis.MapperNamespaceCache;
import tool.dto.mybatis.FindXmlNodeDTO;
import tool.dto.mybatis.MapperMethod;
import tool.hyperlink.mybatis.ToXmlHyperlink;
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
		IFile xmlMapperFile = findMapperFileByMapperMethod(method);
		boolean addXmlStatement = addXmlStatement(xmlMapperFile,sqlFragment);
		if (addXmlStatement) {
			List<FindXmlNodeDTO> findNodeByMapperMethod = findNodeByMapperMethod(method);
			if (CollectionUtils.isNotEmpty(findNodeByMapperMethod)) {
				IDOMNode domNode = (IDOMNode)findNodeByMapperMethod.get(0).getDomNode();
				IFile mapperFile = findNodeByMapperMethod.get(0).getMapperFile();
				Region destRegion = new Region(domNode.getStartOffset(), domNode.getEndOffset() - domNode.getStartOffset());
				String label = "Open <" + domNode.getNodeName() + "/> in " + mapperFile.getFullPath();
				ToXmlHyperlink toXmlHyperlink = new ToXmlHyperlink(mapperFile, null, label, destRegion);
				toXmlHyperlink.open();
			}
		}
		
	}


	
	@SneakyThrows
	public static IFile findMapperFileByMapperMethod(MapperMethod method) {
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

	public static List<FindXmlNodeDTO> findNodeByMapperMethod(MapperMethod method) {
		MethodDeclaration methodDeclaration = method.getMethodDeclaration();
//		IRegion region = new Region(methodDeclaration.getStartPosition(), methodDeclaration.getLength());
		IMethodBinding resolveBinding = methodDeclaration.resolveBinding();
		IJavaElement srcElement = resolveBinding.getJavaElement();
		switch (srcElement.getElementType()) {
			case IJavaElement.METHOD:
				IMethod m = (IMethod) srcElement;
				return findNodeByExpression(m.getDeclaringType(), "//*[@id='" + srcElement.getElementName() + "']");
			default:
				break;
		}
		return null;
	}

	
	@SneakyThrows
	public static List<FindXmlNodeDTO> findNodeByExpression(IType type, String expression) {
		if (type.isInterface()) {
			IJavaProject project = type.getJavaProject();
			if (project == null)
				return null;
			List<FindXmlNodeDTO> findXmlNodeDTOs = Lists.newArrayList();
			for (IFile mapperFile : MapperNamespaceCache.getInstance().get(project, type.getFullyQualifiedName(), null)) {
				IDOMDocument mapperDocument = MybatipseXmlUtil.getMapperDocument(mapperFile);
				if (mapperDocument == null)
					continue;
				try {
					IDOMNode domNode = (IDOMNode) XpathUtil.xpathNode(mapperDocument, expression);
					if (domNode != null) {
						FindXmlNodeDTO findXmlNodeDTO = new FindXmlNodeDTO();
						findXmlNodeDTO.setDomNode(domNode);
						findXmlNodeDTO.setMapperFile(mapperFile);
						findXmlNodeDTOs.add(findXmlNodeDTO);
						return findXmlNodeDTOs;
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
		IMethodBinding resolveBinding = methodDeclaration.resolveBinding();
		IJavaElement srcElement = resolveBinding.getJavaElement();
		IMethod m = (IMethod) srcElement;
		IType type = m.getDeclaringType();
		List<FindXmlNodeDTO> findNodeByExpression = findNodeByExpression(type, "/mapper/resultMap");
		HashMap<String, String> type2IdResultMap = Maps.newHashMap();
		for (FindXmlNodeDTO findXmlNodeDTO : findNodeByExpression) {
			Node domNode = findXmlNodeDTO.getDomNode();
			String resultMapId = MybatipseXmlUtil.getAttribute(domNode, "id");
			String resultMapType = MybatipseXmlUtil.findEnclosingType(domNode);
			type2IdResultMap.put(resultMapId, resultMapType);
		}
//		ASTNode parent = methodDeclaration.getParent();
//		String doName = null;
//		if (parent instanceof TypeDeclaration) {
//			TypeDeclaration clzTypeDeclaration = ((TypeDeclaration) parent);
//			ITypeBinding[] interfaces = clzTypeDeclaration.resolveBinding().getInterfaces();
//			if (interfaces!=null&&interfaces.length>0&&interfaces[0].getTypeArguments()!=null&&interfaces[0].getTypeArguments().length>0) {
//				doName = interfaces[0].getTypeArguments()[0].getName();
//			}
//		}
		String methodName = methodDeclaration.resolveBinding().getName();
		Element element = null;
		if (StringUtils.startsWithIgnoreCase(methodName, "update")||StringUtils.startsWithIgnoreCase(methodName, "modify")||StringUtils.startsWithIgnoreCase(methodName, "upsert")) {
			element = mapperDoc.createElement("update");
		}else {
			element = mapperDoc.createElement("select");
		}
		element.setAttribute("id", methodDeclaration.getName().toString());
		
		String returnTypeStr = method.getReturnTypeStr();
		if (StringUtils.isNotBlank(returnTypeStr)) {
			Optional<String> findAny = type2IdResultMap.keySet().stream().filter(x->StringUtils.endsWith(x, returnTypeStr)).findAny();
			if (findAny.isPresent()) {
				element.setAttribute("resultMap", type2IdResultMap.get(findAny.get()));
			}else {
				element.setAttribute("resultType", returnTypeStr);
			}
		}
		Text sqlText = mapperDoc.createCDATASection(delimiter + sqlFragment.toString() + delimiter);
		element.appendChild(sqlText);
		return element;
	}

}
