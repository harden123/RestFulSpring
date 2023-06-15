package restfulspring.handlers;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IProject;
import org.eclipse.jdt.core.IAnnotation;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import lombok.SneakyThrows;
import restfulspring.constant.RestConstant;
import restfulspring.dto.JDTMethodDTO;
import restfulspring.dto.JDTTypeDTO;
import restfulspring.utils.AstUtil;
import restfulspring.utils.CollectionUtils;
import restfulspring.view.RestFulSpringView;

public class JdtSourceHandlers {
	
	private static List<JDTTypeDTO> list;
	private static AtomicBoolean running = new AtomicBoolean();
	private static final ExecutorService executor = new ThreadPoolExecutor(8, 8, 0L, TimeUnit.MICROSECONDS, new LinkedBlockingQueue<>(100),
			new ThreadPoolExecutor.CallerRunsPolicy());

	public static void handle() {
		boolean compareAndSet = running.compareAndSet(false, true);
		if (compareAndSet) {
				try {
					long s1 = System.currentTimeMillis();
					IWorkbench workbench = PlatformUI.getWorkbench();
					IWorkbenchWindow window = workbench.getActiveWorkbenchWindow();
					IEditorPart editor = Optional.ofNullable(window).map(x -> x.getActivePage()).map(x -> x.getActiveEditor()).orElse(null);
					Thread thread = new Thread(new Runnable() {
						@SneakyThrows
						@Override
						public void run() {
							try {
								if (editor == null) {
									return ;
								}
								IFile file = (IFile) editor.getEditorInput().getAdapter(IFile.class);
								IProject project = file.getProject();
								if (project == null) {
									return ;
								}
								if (!project.isOpen() || !project.hasNature(JavaCore.NATURE_ID)) {
									return ;
								}
								System.out.println("parseJDTing");
								IJavaProject javaProject = JavaCore.create(project);
								IPackageFragmentRoot[] packageFragmentRoots = javaProject.getPackageFragmentRoots();
								List<ICompilationUnit> allJavaFiles = new ArrayList<>();
								for (IPackageFragmentRoot iPackageFragmentRoot : packageFragmentRoots) {
									boolean open = iPackageFragmentRoot.isOpen();
									if (!open) {
										continue;
									}
									String path = iPackageFragmentRoot.getPath().toString();
									if (path.endsWith("/src/test/java") || path.endsWith("/src/main/resources")) {
										continue;
									}
									getAllJavaFiles(iPackageFragmentRoot, allJavaFiles);
								}
								JdtSourceHandlers.setList(parseAllJavaFiles(allJavaFiles));
								long s2 = System.currentTimeMillis();
								System.out.println("parseJDTUsed:"+(s2-s1)/1000);
								RestFulSpringView.notifyRefreshTree();
							}catch (Exception e) {
								System.out.println(e.getMessage());
							}
							finally {
								running.set(false);
							}
						}
					});
					thread.start();
				} catch (Exception e) {
					running.set(false);
					throw e;
				}
			
		}
	}

	@SneakyThrows
	public static List<ICompilationUnit> getAllJavaFiles(IPackageFragmentRoot packageFragmentRoot, List<ICompilationUnit> javaFiles) {
		// Recursively traverse the package fragment root
		if (packageFragmentRoot.getKind() == IPackageFragmentRoot.K_SOURCE) {
			IJavaElement[] children = packageFragmentRoot.getChildren();
			for (IJavaElement javaElement : children) {
				if (javaElement.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
					IPackageFragment packageFragment = (IPackageFragment) javaElement;
					traversePackageFragment(packageFragment, javaFiles);
				} else if (javaElement.getElementType() == IJavaElement.COMPILATION_UNIT) {
					ICompilationUnit iCompilationUnit = (ICompilationUnit) javaElement;
					javaFiles.add(iCompilationUnit);
				}
			}
		}
		return javaFiles;
	}

	@SneakyThrows
	private static void traversePackageFragment(IPackageFragment packageFragment, List<ICompilationUnit> javaFiles) {
		for (ICompilationUnit compilationUnit : packageFragment.getCompilationUnits()) {
			javaFiles.add(compilationUnit);
		}
		for (IJavaElement element : packageFragment.getChildren()) {
			if (element.getElementType() == IJavaElement.PACKAGE_FRAGMENT) {
				IPackageFragment subPackage = (IPackageFragment) element;
				traversePackageFragment(subPackage, javaFiles);
			}
		}
	}

	@SneakyThrows
	private static List<JDTTypeDTO> parseAllJavaFiles(List<ICompilationUnit> allJavaFiles) {
		List<JDTTypeDTO> lists = Lists.newArrayListWithExpectedSize(allJavaFiles.size());
		ArrayList<Future<?>> submits = Lists.newArrayListWithExpectedSize(allJavaFiles.size());
		for (ICompilationUnit iCompilationUnit : allJavaFiles) {
			String elementName = iCompilationUnit.getElementName();
			if (StringUtils.containsIgnoreCase(elementName, RestConstant.Controller)||StringUtils.containsIgnoreCase(elementName, RestConstant.Service)) {
				Future<?> submit = executor.submit(()->{
					parseAllMethods(iCompilationUnit,lists);
				});
				submits.add(submit);
			}
		}
		for (Future<?> f : submits) {
			f.get(1, TimeUnit.MINUTES);
		}
		//sort
		List<JDTTypeDTO> collect = lists.stream().sorted(Comparator.comparing(JDTTypeDTO::getType, ((o1, o2) -> {
			return o1.getName().toString().compareTo(o2.getName().toString());
		}))).collect(Collectors.toList());
		return collect;
	}

	// 获取给定 ICompilationUnit 中所有方法及其参数和注解的方法
	@SneakyThrows
	public static void parseAllMethods(ICompilationUnit cu, List<JDTTypeDTO> jDTTypeDTOs) {
		ArrayList<String> unitAnnos = getUnitAnnos(cu);
		if (!unitAnnos.contains(RestConstant.RequestMapping)&&!unitAnnos.contains(RestConstant.RestController)) {
			return ;
		}
		if (unitAnnos.contains(RestConstant.FeignClient)) {
			return ;
		}
		// 创建 ASTParser 对象，并以给定的 ICompilationUnit 作为源代码输入
		ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
		parser.setSource(cu);
		parser.setKind(ASTParser.K_COMPILATION_UNIT);
		parser.setResolveBindings(true);
		CompilationUnit astRoot = (CompilationUnit) parser.createAST(null);
		// 遍历所有方法，并打印其名称、参数和注解信息
		for (Object o : astRoot.types()) {
			if (o instanceof TypeDeclaration) {
				TypeDeclaration type = (TypeDeclaration) o;
				HashMap<String, Map<String, Object>> typeAnnotations = AstUtil.retrieveAnnoByModifiers(type.modifiers());
				
				JDTTypeDTO typeDTO = new JDTTypeDTO();
				typeDTO.setType(type);
				typeDTO.setAnnotations(typeAnnotations);
				typeDTO.setTypeUnit(cu);
//				System.out.println("type name: " + type.getName().toString());
				HashMap<String, JDTMethodDTO> methodName2DTOMap = Maps.newHashMap();
				for (MethodDeclaration method : type.getMethods()) {
					// 获取方法中所有注解信息
					HashMap<String, Map<String, Object>> methodAnnotations = AstUtil.retrieveAnnoByModifiers(method.modifiers());
					if (!methodAnnotations.containsKey(RestConstant.RequestMapping)){
						continue;
					}
					JDTMethodDTO jDTMethodDTO = new JDTMethodDTO();
					IBinding binding = method.resolveBinding();
				    if (binding != null && binding.getKind() == IBinding.METHOD) {
						jDTMethodDTO.setMethod((IMethod) binding.getJavaElement());
				    }
					jDTMethodDTO.setAnnotations(methodAnnotations);
//					System.out.println("Method name: " + method.getName().getIdentifier());
					// 获取所有方法参数的名称和类型
					for (Object p : method.parameters()) {
						SingleVariableDeclaration parameter = (SingleVariableDeclaration) p;
						jDTMethodDTO.getReqParams().add(parameter);
					}
					methodName2DTOMap.put(method.getName().getIdentifier(), jDTMethodDTO);
				}
				if (CollectionUtils.isNotEmpty(methodName2DTOMap)) {
					typeDTO.setMethodName2DTOMap(methodName2DTOMap);
					jDTTypeDTOs.add(typeDTO);
				}
			}
		}
	}

	
	
	public static void setList(List<JDTTypeDTO> list) {
		if (CollectionUtils.isNotEmpty(JdtSourceHandlers.list)) {
			for (JDTTypeDTO jdtTypeDTO : JdtSourceHandlers.list) {
				jdtTypeDTO.getAnnotations().clear();
				jdtTypeDTO.getMethodName2DTOMap().clear();
			}
			JdtSourceHandlers.list.clear();
		}
		JdtSourceHandlers.list = list;
	}

	public static List<JDTTypeDTO>  getList() {
		return list;
	}

	@SneakyThrows
	private static ArrayList<String> getUnitAnnos(ICompilationUnit unit) {
		ArrayList<String> annos = Lists.newArrayList();
		IJavaElement[] elements = unit.getChildren();  // 获取编译单元中的所有 Java 元素
		for (IJavaElement element : elements) {
		    if (element instanceof IType) {  // 如果是一个类
		        IType type = (IType) element;
		        IAnnotation[] annotations = type.getAnnotations();  // 获取类型定义上的注解
		        for (IAnnotation annotation : annotations) {
		            String annotationName = annotation.getElementName();
		            // 处理注解信息...
		            annos.add(annotationName);
		        }
		    }
		}
		return annos;
	}
	
	

}
