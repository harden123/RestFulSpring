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
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IPackageFragment;
import org.eclipse.jdt.core.IPackageFragmentRoot;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
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
				HashMap<String, Map<String, Object>> typeAnnotations = retrieveAnnotations(type.modifiers());
				if (!typeAnnotations.containsKey(RestConstant.RequestMapping)&&!typeAnnotations.containsKey(RestConstant.RestController)) {
					continue;
				}
				if (typeAnnotations.containsKey(RestConstant.FeignClient)) {
					continue;
				}
				JDTTypeDTO typeDTO = new JDTTypeDTO();
				typeDTO.setType(type);
				typeDTO.setAnnotations(typeAnnotations);
//				System.out.println("type name: " + type.getName().toString());
				HashMap<String, JDTMethodDTO> methodName2DTOMap = Maps.newHashMap();
				for (MethodDeclaration method : type.getMethods()) {
					// 获取方法中所有注解信息
					HashMap<String, Map<String, Object>> methodAnnotations = retrieveAnnotations(method.modifiers());
					if (!methodAnnotations.containsKey(RestConstant.RequestMapping)){
						continue;
					}
					JDTMethodDTO jDTMethodDTO = new JDTMethodDTO();
					jDTMethodDTO.setAnnotations(methodAnnotations);
//					System.out.println("Method name: " + method.getName().getIdentifier());
					// 获取所有方法参数的名称和类型
					for (Object p : method.parameters()) {
						SingleVariableDeclaration parameter = (SingleVariableDeclaration) p;
						jDTMethodDTO.getReqParams().add(parameter);
					}
					methodName2DTOMap.put(method.getName().getIdentifier(), jDTMethodDTO);
				}
				typeDTO.setMethodName2DTOMap(methodName2DTOMap);
				jDTTypeDTOs.add(typeDTO);
			}
		}
	}

	// 获取给定方法中所有注解
	/**
	 * <annoKey:<key,val>>
	 */
	private static HashMap<String, Map<String, Object>> retrieveAnnotations(List list) throws JavaModelException {
		HashMap<String,Map<String,Object>> hashMap = new HashMap<>();
		for (Object annotation : list) {
			if (annotation instanceof NormalAnnotation) {
				NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
				IAnnotationBinding binding = normalAnnotation.resolveAnnotationBinding();
				extractedAnnoBinds(hashMap, binding,annotation);
			}else if(annotation instanceof MarkerAnnotation) {
				MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
				IAnnotationBinding binding = markerAnnotation.resolveAnnotationBinding();
				extractedAnnoBinds(hashMap, binding,annotation);
			}else if (annotation instanceof SingleMemberAnnotation) {
				SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation) annotation;
				IAnnotationBinding binding = singleMemberAnnotation.resolveAnnotationBinding();
				extractedAnnoBinds(hashMap, binding,annotation);
			}
		}
		return hashMap;
	}

	private static void extractedAnnoBinds(HashMap<String, Map<String, Object>> hashMap, IAnnotationBinding binding, Object annotation) {
		if (binding != null) {
			IMemberValuePairBinding[] allMemberValuePairs = binding.getAllMemberValuePairs();
			HashMap<String,Object> values = new HashMap<>();
			for (IMemberValuePairBinding pair : allMemberValuePairs) {
				String name = pair.getName();
				Object value = pair.getValue();
				values.put(name, value);
			}
			hashMap.put(binding.getName(), values);
		}else {
			String string = annotation.toString();
			hashMap.put(string, null);
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

}
