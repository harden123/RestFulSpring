package tool.correctPackage;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.io.filefilter.SuffixFileFilter;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.ResourceUtil;

import com.google.common.collect.Maps;

import cn.hutool.core.io.FileUtil;
import lombok.SneakyThrows;
import restfulspring.utils.CollectionUtils;

public class CorrectPackage extends AbstractHandler{

	/** 
	 * {@inheritDoc}
	 */
	@Override
	@SneakyThrows
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		String path = null;
	    if (selection instanceof IStructuredSelection) {
	        IStructuredSelection structuredSelection = (IStructuredSelection) selection;
	        Object firstElement = structuredSelection.getFirstElement();
	        if (firstElement instanceof IPackageFragment) {
	        	path = ((IPackageFragment)firstElement).getResource().getLocation().toOSString();
	        }else if (firstElement instanceof IPackageFragmentRoot) {
	        	path = ((IPackageFragmentRoot)firstElement).getResource().getLocation().toOSString();
	        }else if(firstElement instanceof ICompilationUnit ) {
	        	ICompilationUnit cpu =  ((ICompilationUnit)firstElement);
	        	path = cpu.getUnderlyingResource().getLocation().toOSString();
	        }
	    }else if(selection instanceof ITextSelection) {
//	    	ITextSelection iTextSelection = (ITextSelection) selection;
	    	IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		    IFile file = ResourceUtil.getFile(editorPart.getEditorInput());
        	path = file.getLocation().toFile().getAbsolutePath();
	    }
	    doCorrectPackage(path);
		return null;
	}

	@SneakyThrows
	public void doCorrectPackage(String s) {
		String pathPrefix = "\\src\\main\\java\\";
		List<File> loopFiles = FileUtil.loopFiles(s, new SuffixFileFilter(".java"));
		HashMap<File, List<String>> newHashMap = Maps.newHashMap();
		HashMap<File, String> rightPackageMap = Maps.newHashMap();

//		ArrayList<File> newArrayList = Lists.newArrayList();
		for (File file : loopFiles) {
//			"C:\\env\\wit\\wit-base\\base-sys\\src\\main\\java\\cn\\com\\gogen\\wit\\sys\\dal\\domain\\ability\\BaseAbilityDO.java",
			String canonicalPath = file.getCanonicalPath();
			if (!StringUtils.contains(canonicalPath, pathPrefix)) {
				continue;
			}
//			cn\\com\\gogen\\wit\\sys\\dal\\domain\\ability\\BaseAbilityDO.java"
//			package cn.com.gogen.wit.sys.dal.domain.ability;
			String substringAfter = StringUtils.substringAfter(canonicalPath, pathPrefix);
			String substringBeforeLast = StringUtils.substringBeforeLast(substringAfter, "\\");
			String replaceAll = substringBeforeLast.replaceAll("\\\\", "\\.");
			String rightPackageStr = "package "+replaceAll+";";
			List<String> readUtf8Lines = FileUtil.readUtf8Lines(file);
			boolean contain = false;
			for (String line : readUtf8Lines) {
				if (StringUtils.startsWith(line.trim(), rightPackageStr)) {
					contain=true;
					break;
				}
			}
			if (contain) {
				continue;
			}
			rightPackageMap.put(file, rightPackageStr);
			newHashMap.put(file, readUtf8Lines);
		}
		List<String> showPathList = newHashMap.keySet().stream().map(x->{
			try {
				String canonicalPath = x.getCanonicalPath();
				int lastOrdinalIndexOf = StringUtils.lastOrdinalIndexOf(canonicalPath, File.separator, 3)+1;
				return StringUtils.substring(canonicalPath, lastOrdinalIndexOf, canonicalPath.length());
			} catch (IOException e) {
				e.printStackTrace();
			}
			try {
				return x.getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return null;
		}).collect(Collectors.toList());
		if (CollectionUtils.isEmpty(showPathList)) {
			return ;
		}
		Display display = Display.getDefault();
		String join = StringUtils.join(showPathList,System.getProperty("line.separator") );
		boolean result = MessageDialog.open(MessageDialog.QUESTION, display.getActiveShell(), "是否修改package", join, SWT.NONE);
         if (!result) {
        	  // 用户点击了取消按钮或关闭了对话框
 			return ;
         }
         // 用户点击了确定按钮
		for (Map.Entry<File, List<String>> entry : newHashMap.entrySet()) {
			File key = entry.getKey();
			List<String> value = entry.getValue();
			String packageStr = value.get(0);
			if (StringUtils.startsWith(packageStr, "package ")) {
				value.set(0, rightPackageMap.get(key));
			}
			FileUtil.writeUtf8Lines(value, key);
		}
		
	}
}
