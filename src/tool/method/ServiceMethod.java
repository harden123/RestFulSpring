package tool.method;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.TextSelection;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.ResourceUtil;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.eclipse.ui.texteditor.ITextEditor;

import tool.utils.FileUtils;
import tool.utils.StringUtils;

public class ServiceMethod extends AbstractHandler{

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		// IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		// MessageDialog.openInformation(
		// window.getShell(),
		// "FunPlugin",
		// "Hello, Eclipse world");
		IEditorPart iep = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		ITextEditor ite = null;
		TextSelection selection = null;
		IDocument document = null;
		ISelectionProvider selectionProvider = null;
		if (iep instanceof ITextEditor) {
			ite = (ITextEditor) iep;
			selectionProvider = ite.getSelectionProvider();
			selection = (TextSelection) selectionProvider.getSelection();
			document = ite.getDocumentProvider().getDocument(ite.getEditorInput());
		} else if (iep instanceof MultiPageEditorPart) {
			Object result = ((MultiPageEditorPart) iep).getSelectedPage();
			if (!(result instanceof ITextEditor)) {
				return null;
			}
			ite = (ITextEditor) result;
			selectionProvider = ite.getSelectionProvider();
			selection = (TextSelection) selectionProvider.getSelection();
			document = ite.getDocumentProvider().getDocument(ite.getEditorInput());
		}
		if (selection != null && document != null && selectionProvider != null) {
			String fileName = iep.getTitle();
			if (!StringUtils.endsWith(fileName, ".java")) {
				return null;
			}
			doPorcess(selection, document,ite);
		}

		return null;
	}

	private void doPorcess(TextSelection selection, IDocument document, ITextEditor ite) {
		int offset = selection.getOffset();
		int length = selection.getLength();
		String selectedStr = document.get().substring(offset, offset + length);
		if (StringUtils.isBlank(selectedStr)) {
			return;
		}
		String packageLine = "package ;";
		int indexOf = document.get().indexOf("\n");
		if (indexOf!=-1) {
			String packageLineTemp = document.get().substring(0, indexOf);
			if (packageLineTemp.startsWith("package ")) {
				packageLine=packageLineTemp;
			}
		}
		String reqDTOParam = selectedStr+"ReqDTO";
		String reqDTOType = StringUtils.capitalize(reqDTOParam);
		String resultDTOType = StringUtils.capitalize(selectedStr+"ResultDTO");
		boolean extendPageDTO = false;
		String returnTypeStr = resultDTOType;
		if (StringUtils.endsWithIgnoreCase(selectedStr, "page")) {
			returnTypeStr = "PageInfo<"+resultDTOType+">";
			extendPageDTO = true;
		}
		//方法
		String replaceFunc = 
				"/**\n"
				+ "\t * \n"
				+ "\t */\n"
				+"\t@Override\n"
				+ "\t@RequestMapping(value = \"/"+selectedStr+"\")\n"
				+ "\tpublic "+returnTypeStr+" "+selectedStr+"(@RequestBody "+reqDTOType+" "+reqDTOParam+") {\n"
				+ "\t\t\n"
				+ "\t}";
	   
		try {
			document.replace(offset, length, replaceFunc);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		//DTO
	    IFile file = ResourceUtil.getFile(ite.getEditorInput());
	    String absolutePath = file.getLocation().toFile().getAbsolutePath();
	    int lastIndexOf = StringUtils.lastIndexOf(absolutePath, File.separator);
	    if (lastIndexOf==-1) {
	    	return ;
		}
	    String dir = StringUtils.substring(absolutePath, 0, lastIndexOf+1);
		wirteBaseDTO(packageLine, resultDTOType, dir+resultDTOType+".java",false);
		wirteBaseDTO(packageLine, reqDTOType, dir+reqDTOType+".java",extendPageDTO);
	}

	
	private void wirteBaseDTO(String packageLine, String resultDTOType, String resultDTOPath,boolean extendPageDTO) {
		String resultDTOFileContent = packageLine+"\n"
				+(extendPageDTO==true?"import cn.com.gogen.wit.api.domain.BasePageDTO;\n":"import cn.com.gogen.wit.base.api.dto.BaseDTO;\n")
				+ "import lombok.Getter;\n"
				+ "import lombok.Setter;\n\n"
				+"/**\n"
				+ " * \n"
				+ " */\n"
				+ "@Getter\n"
				+ "@Setter\n"
				+ "@Builder\n"
				+ "@AllArgsConstructor\n"
				+ "@NoArgsConstructor\n"
				+ "public class "+resultDTOType+" extends "+(extendPageDTO==true?"BasePageDTO":"BaseDTO")+" {\n"
				+ "\n"
				+ "\n"
				+ "}";
	    try {
			FileUtils.writeStringToFile(new File(resultDTOPath), resultDTOFileContent, Charset.defaultCharset());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

}
