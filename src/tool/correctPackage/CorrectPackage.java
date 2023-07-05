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
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.ResourceUtil;

import com.alibaba.fastjson.JSON;
import com.google.common.collect.Maps;

import cn.com.gogen.alps.alps.feign.generator.utils.ScannerUtils;
import cn.hutool.core.io.FileUtil;
import lombok.SneakyThrows;

public class CorrectPackage extends AbstractHandler{

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		String path = null;
	    if (selection instanceof IStructuredSelection) {
	        IStructuredSelection structuredSelection = (IStructuredSelection) selection;
	        Object firstElement = structuredSelection.getFirstElement();
	        if (firstElement instanceof IPackageFragment) {
	        	IPath packagePath = ((IPackageFragment)firstElement).getPath();
	        	path = packagePath.toFile().getAbsolutePath();
	        }else if(firstElement instanceof CompilationUnit) {
	        	CompilationUnit cpu =  ((CompilationUnit)firstElement);
	        	String absolutePath = cpu.getJavaElement().getResource().getLocation().toFile().getAbsolutePath();
	        	path = absolutePath;

	        }
	    }else if(selection instanceof ITextSelection) {
//	    	ITextSelection iTextSelection = (ITextSelection) selection;
	    	IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		    IFile file = ResourceUtil.getFile(editorPart.getEditorInput());
        	path = file.getLocation().toFile().getAbsolutePath();
	    }
		return null;
	}

	@SneakyThrows
	public static void main(String[] args) {
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
		List<String> collect = newHashMap.keySet().stream().map(x->{
			try {
				return x.getCanonicalPath();
			} catch (IOException e) {
				e.printStackTrace();
			}
			return pathPrefix;
		}).collect(Collectors.toList());
		System.out.println(JSON.toJSONString(collect, true));
		System.out.print("是否修改package,确认请输入yes: ");
		String readLine = ScannerUtils.readLine();
		if (!StringUtils.equalsIgnoreCase(readLine, "yes")) {
			System.out.println("不修改package");
			return ;
		}
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
