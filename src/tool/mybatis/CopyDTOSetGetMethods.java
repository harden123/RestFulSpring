package tool.mybatis;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.ResourceUtil;

import tool.utils.ViewUtil;

public class CopyDTOSetGetMethods extends AbstractHandler{

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		ICompilationUnit compilationUnit = null;
	    if(selection instanceof ITextSelection) {
	    	IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		    IFile file = ResourceUtil.getFile(editorPart.getEditorInput());
	        compilationUnit = JavaCore.createCompilationUnitFrom(file);
	    }
	   
	    ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setSource(compilationUnit);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		for (Object o : astRoot.types()) {
			if (o instanceof TypeDeclaration) {
				TypeDeclaration type = (TypeDeclaration) o;
				String typeName = type.getName().getIdentifier();
				FieldDeclaration[] fields = type.getFields();
				doExecute(fields,typeName);
			}
		}
		return null;
	}

	private void doExecute(FieldDeclaration[] fields, String typeName) {
		StringBuffer sb = new StringBuffer();
		for (FieldDeclaration field : fields) {
			String name = field.fragments().get(0).toString();
			sb.append(StringUtils.uncapitalize(typeName)+".set" +StringUtils.capitalize(name)  + "(dto.get" + StringUtils.capitalize(name) + "());\r\n");
		}
		ViewUtil.copyAndToolTip(sb.toString());
	}
}

