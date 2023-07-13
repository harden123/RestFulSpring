package restfulspring.view.views.mybatis;

import javax.xml.xpath.XPathExpressionException;

import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.ISelectionListener;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.wst.sse.core.internal.provisional.text.IStructuredDocumentRegion;
import org.eclipse.wst.xml.core.internal.document.AttrImpl;
import org.eclipse.wst.xml.core.internal.document.ElementImpl;
import org.eclipse.wst.xml.core.internal.document.TextImpl;
import org.eclipse.wst.xml.core.internal.provisional.document.IDOMDocument;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import restfulspring.config.Log;
import tool.config.mybatis.MapperNamespaceCache;
import tool.utils.MybatipseXmlUtil;
import tool.utils.XpathUtil;

@SuppressWarnings("restriction")
public class MyBatisSqlView extends ViewPart {

	private MyBatisSqlViewSelectionListener selectionListener;

	protected Text text;

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NULL);
		composite.setLayout(new FillLayout());
		text = new Text(composite, SWT.MULTI | SWT.READ_ONLY | SWT.H_SCROLL | SWT.V_SCROLL);

		selectionListener = new MyBatisSqlViewSelectionListener();
		IWorkbenchWindow workbenchWindow = getSite().getWorkbenchWindow();
		workbenchWindow.getSelectionService().addPostSelectionListener(selectionListener);
		setActiveEditorSelection(workbenchWindow);
	}

	private void setActiveEditorSelection(IWorkbenchWindow workbenchWindow) {
		// Use the active editor, instead of the global selection.
		// The global selection might be altered by another view.
		IEditorPart activeEditor = workbenchWindow.getActivePage().getActiveEditor();
		if (activeEditor != null) {
			ITextEditor editor = (ITextEditor) activeEditor.getAdapter(ITextEditor.class);
			if (editor != null) {
				ISelectionProvider provider = editor.getSelectionProvider();
				if (provider != null) {
					selectionListener.selectionChanged(editor, provider.getSelection());
				}
			}
		}
	}

	@Override
	public void setFocus() {
	}

	@Override
	public void dispose() {
		if (selectionListener != null) {
			getSite().getWorkbenchWindow().getSelectionService().removePostSelectionListener(selectionListener);
			selectionListener = null;
		}
		super.dispose();
	}

	private final class MyBatisSqlViewSelectionListener implements ISelectionListener {

		@Override
		public void selectionChanged(IWorkbenchPart part, ISelection selection) {
			if (!selection.isEmpty() && (selection instanceof IStructuredSelection)) {
				IStructuredSelection sel = (IStructuredSelection) selection;
				if (sel.size() == 1) {
					handleSingleSelection(sel);
				}
			}
		}

		private void handleSingleSelection(IStructuredSelection sel) {
			Object firstElement = sel.getFirstElement();
			if (firstElement != null && firstElement instanceof Node) {
				Node node = (Node) firstElement;
				if (node instanceof AttrImpl) {
					node = ((AttrImpl) node).getOwnerElement();
				}
				while (node != null && !(node instanceof ElementImpl)) {
					node = node.getParentNode();
				}
				if (node != null) {
					Node statementNode = MybatipseXmlUtil.findEnclosingStatementNode(node);
					if (statementNode != null) {
						StringBuilder buffer = new StringBuilder();
						computeStatementText((ElementImpl) statementNode, buffer);
						text.setText(buffer.toString());
					}
				}
			}
		}

		private void computeStatementText(ElementImpl currentNode, StringBuilder buffer) {
			if (currentNode == null)
				return;

			NodeList childNodes = currentNode.getChildNodes();
			for (int k = 0; k < childNodes.getLength(); k++) {
				Node childNode = childNodes.item(k);
				if (childNode instanceof TextImpl) {
					String text = ((TextImpl) childNode).getTextContent();
					buffer.append(text);
				} else if (childNode instanceof ElementImpl) {
					ElementImpl element = (ElementImpl) childNode;
					String elemName = element.getNodeName();
					if (element.hasChildNodes()) {
						IStructuredDocumentRegion startRegion = element.getStartStructuredDocumentRegion();
						if (startRegion != null)
							buffer.append(startRegion.getText());
						computeStatementText(element, buffer);
						IStructuredDocumentRegion endRegion = element.getEndStructuredDocumentRegion();
						if (endRegion != null)
							buffer.append(endRegion.getText());
					} else if ("include".equals(elemName)) {
						ElementImpl sqlElement = resolveInclude(element, buffer);
						computeStatementText(sqlElement, buffer);
					} else {
						buffer.append(element.getSource());
					}
				}
			}
		}

		private ElementImpl resolveInclude(ElementImpl includeElement, StringBuilder buffer) {
			String refId = includeElement.getAttribute("refid");
			if (refId.indexOf('$') > -1)
				return null;

			int lastDot = refId.lastIndexOf('.');
			try {
				if (lastDot == -1) {
					// Internal reference.
					Document domDoc = includeElement.getOwnerDocument();
					return (ElementImpl) XpathUtil.xpathNode(domDoc, "//sql[@id='" + refId + "']");
				} else if (lastDot + 1 < refId.length()) {
					// External reference.
					IJavaProject project = MybatipseXmlUtil.getJavaProject(includeElement.getStructuredDocument());
					String namespace = refId.substring(0, lastDot);
					String sqlId = refId.substring(lastDot + 1);
					for (IFile mapperFile : MapperNamespaceCache.getInstance().get(project, namespace, null)) {
						IDOMDocument mapperDocument = MybatipseXmlUtil.getMapperDocument(mapperFile);
						ElementImpl element = (ElementImpl) XpathUtil.xpathNode(mapperDocument, "//sql[@id='" + sqlId + "']");
						if (element != null)
							return element;
					}
				}
			} catch (XPathExpressionException e) {
				Log.error("Failed to resolve included sql element.", e);
			}
			return null;
		}
	}


}