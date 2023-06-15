package restfulspring.handlers;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IResource;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.ui.JavaUI;
import org.eclipse.ui.IEditorDescriptor;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.FileEditorInput;

import lombok.SneakyThrows;
import restfulspring.dto.JDTMethodDTO;
import restfulspring.dto.JDTTypeDTO;
import restfulspring.view.tree.MyTreeElement;

public class OpenEditorHandlers {

    @SneakyThrows
	public static void openEditor(MyTreeElement node) {
    	JDTTypeDTO jdtTypeDTO = node.getJDTTypeDTO();
    	if (node.getParent()!=null) {
    		jdtTypeDTO = node.getParent().getJDTTypeDTO();
		}
    	JDTMethodDTO jdtMethodDTO = node.getJDTMethodDTO();
    	ICompilationUnit typeUnit = jdtTypeDTO.getTypeUnit();
    	IResource resource = typeUnit.getCorrespondingResource();
    	if (resource != null && resource.exists() && resource.getType() == IResource.FILE&&jdtMethodDTO!=null) {
  		  // 获取要打开的文件的 IEditorInput
    	    IFile file = (IFile) resource;
    	    FileEditorInput fileInput = new FileEditorInput(file);

            // 打开 EditorPart，并将输入文件传递给它
            IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
            IEditorDescriptor desc = PlatformUI.getWorkbench().getEditorRegistry().getDefaultEditor(fileInput.getName());
            IEditorPart editor = page.openEditor(fileInput, desc.getId());
//            TextEditor editor = (TextEditor) page.openEditor(fileInput, desc.getId());
            // 定位到方法
            IMethod method = jdtMethodDTO.getMethod();
            if (method != null) {
                JavaUI.revealInEditor(editor, (IJavaElement) method);
            }
    	}
    	
     
	}
}
