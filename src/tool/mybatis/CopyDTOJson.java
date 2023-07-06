package tool.mybatis;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.ResourceUtil;

import restfulspring.utils.AstUtil;
import restfulspring.utils.TextUtil;
import tool.utils.ViewUtil;

public class CopyDTOJson extends AbstractHandler{

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
				ITypeBinding iTypeBinding = type.resolveBinding();
				Object iterParse = AstUtil.iterParse(iTypeBinding, "");
				ViewUtil.copyAndToolTip(TextUtil.prettyJSON(iterParse));
			}
		}
		return null;
	}

}