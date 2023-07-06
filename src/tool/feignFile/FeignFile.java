package tool.feignFile;

import java.io.File;
import java.io.InputStream;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.ResourceUtil;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.io.IoUtil;
import cn.hutool.core.io.LineHandler;
import lombok.SneakyThrows;

public class FeignFile extends AbstractHandler{

	/** 
	 * {@inheritDoc}
	 */
	@Override
	@SneakyThrows
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		String path = null;
		IResource resource = null;
	    if (selection instanceof IStructuredSelection) {
	        IStructuredSelection structuredSelection = (IStructuredSelection) selection;
	        Object firstElement = structuredSelection.getFirstElement();
	        if(firstElement instanceof ICompilationUnit ) {
	        	ICompilationUnit cpu =  ((ICompilationUnit)firstElement);
	        	path = cpu.getUnderlyingResource().getLocation().toOSString();
	        	resource = cpu.getUnderlyingResource();
	        }
	    }else if(selection instanceof ITextSelection) {
	    	IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		    IFile file = ResourceUtil.getFile(editorPart.getEditorInput());
        	path = file.getLocation().toFile().getAbsolutePath();
        	resource = file;
	    }
	    String feignClientName = getFeignClientName(resource);
	    doFeignFile(path,feignClientName);
		return null;
	}


	private IProject getParentProject(IResource resource) {
		IProject project = null;
		if (resource != null) {
		    while (resource.getType() != IResource.PROJECT) {
		        resource = resource.getParent();
		        if (resource == null) {
		            break;
		        }
		    }
		    if (resource != null && resource.getType() == IResource.PROJECT) {
		        project = (IProject) resource;
		    }
		}
		return project;
	}

	@SneakyThrows
	private void doFeignFile(String filePath, String feignClientName) {
		if (StringUtils.isBlank(filePath)) {
			return ;
		}
		if (FileUtil.isDirectory(filePath)) {
	        List<File> loopFiles = FileUtil.loopFiles(filePath, file -> file.getName().endsWith(".java"));
			for (File file : loopFiles) {
				String name = file.getName();
				if (StringUtils.endsWithIgnoreCase(name, "Service.java")) {
					FeginGeneratorUtil.generator(file.getCanonicalPath(),feignClientName);
				}
			}
		}else {
			FeginGeneratorUtil.generator(filePath,feignClientName);
		}
		
	}
	

	private String getFeignClientName(IResource resource) {
		if (resource == null) {
			return null;
		}
		IProject project = getParentProject(resource);
		String feignClientName =null;
		do {
		    feignClientName = doGetFeignClientName(project);
		    IProject projectParent = getProjectParent(project);
		    project = projectParent;
		} while (StringUtils.isBlank(feignClientName)&&project!=null);
		return feignClientName;
	}


	private IProject getProjectParent(IProject project) {
		  // 获取当前项目的位置
	    IPath projectLocation = project.getLocation();
        projectLocation = projectLocation.removeLastSegments(1);
        IProject parent = null;
        while (!projectLocation.append("pom.xml").toFile().exists()) {
	        projectLocation = projectLocation.removeLastSegments(1);
	        // 如果已经到达文件系统的根目录，退出循环
	        if (projectLocation.segmentCount() == 0) {
	            break;
	        }
		}
        if (projectLocation.append("pom.xml").toFile().exists()) {
    	    IProject topLevelProject = project.getWorkspace().getRoot().getProject(projectLocation.lastSegment());
    	    if (topLevelProject != null) {
    	    	parent=topLevelProject;
			}
		}
		return parent;
	}


	private String doGetFeignClientName(IProject parentProject) {
		if (parentProject == null) {
			return null;
		}
        AtomicReference<String> feignClientName = new AtomicReference<>();
		IResourceVisitor visitor = new IResourceVisitor() {
	        @Override
	        public boolean visit(IResource resource) throws CoreException {
	            // 在这里判断资源是否为文件，并检查其路径是否匹配目标文件路径
                if (feignClientName.get()!=null) {
                	return false; // 返回 false 停止继续遍历
				}
	            if (resource.getType() == IResource.FILE && StringUtils.endsWith(resource.getFileExtension(), "properties")) {
	                // 找到目标文件
	                IFile targetFile = (IFile) resource;
	                InputStream contents = targetFile.getContents();
	                IoUtil.readUtf8Lines(contents, new LineHandler() {
						
						@Override
						public void handle(String line) {
							if (StringUtils.contains(line, "spring.application.name")) {
								feignClientName.set(line.split("=")[1].trim());
							}
						}
					});
	                if (feignClientName.get()!=null) {
	                	return false; // 返回 false 停止继续遍历
					}
	            }
	            return true; // 返回 true 继续遍历其他资源
	        }
	    };
	    try {
			parentProject.accept(visitor);
		} catch (CoreException e) {
			e.printStackTrace();
		}
		return feignClientName.get();
	}
	




}
