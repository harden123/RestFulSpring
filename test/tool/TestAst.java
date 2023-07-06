package tool;

import java.lang.reflect.Modifier;
import java.util.List;

import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Block;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.IMethodBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.ImportDeclaration;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.PackageDeclaration;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import  org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.junit.Test;

import cn.hutool.core.io.FileUtil;

public class TestAst {
	@Test
	public void testASTParser() throws Exception {
		ASTParser parser = ASTParser.newParser(AST.getJLSLatest());
//		String sourceCode = "List<String> list;";
//      IWorkspace workspace = ResourcesPlugin.getWorkspace();
//      IProject project = workspace.getRoot().getProject("RestFulSpring");
//      IJavaProject javaProject = JavaCore.create(project);
//		parser.setProject(javaProject);
		String sourceCode = FileUtil.readUtf8String("C:\\env\\wit\\wit-baseService\\baseService-app\\src\\main\\java\\cn\\com\\gogen\\wit\\service\\impl\\authCount\\NewDoorPermServiceImpl.java");
//		String sourceCode = FileUtil.readUtf8String("C:\\env\\wit\\RestFulSpring\\src\\tool\\mybatis\\CopyDTOSetMethods.java");
		parser.setResolveBindings(true);
		parser.setSource(sourceCode.toCharArray());
		String[] classpathEntries = new String[] {
//				"C:\\env\\wit\\RestFulSpring\\bin"
//				,"C:\\dev\\eclipse-jee-2022-12-R-win32-x86_64\\eclipse\\plugins\\org.eclipse.core.commands_3.10.300.v20221024-2137.jar"
//				"C:\\env\\wit\\wit-baseService\\baseService-app\\target",
//				"C:\\env\\wit\\wit-baseService\\baseService-api\\target"

		}; // 替换为类路径的需要
		String[] sourceEntries = new String[] { 
//				"C:\\env\\wit\\RestFulSpring\\src" 
				"C:\\env\\wit\\wit-baseService\\baseService-api\\src\\main\\java",
				"C:\\env\\wit\\wit-baseService\\baseService-app\\src\\main\\java",
				
		}; // 替换为源文件目录的位置
		String unitName = "NewDoorPermServiceImpl.java"; // 替换为编译单元的名称
		parser.setUnitName(unitName);
		parser.setEnvironment(classpathEntries, sourceEntries, null, true);
		CompilationUnit compilationUnit = (CompilationUnit) parser.createAST(null);
		PackageDeclaration packageDeclaration = compilationUnit.getPackage();
		String packageName = "";
		if (packageDeclaration != null) {
		    packageName = packageDeclaration.getName().getFullyQualifiedName();
		}
//		System.out.println("包名：" + packageName);
		
//		获取导入的类：
		List<ImportDeclaration> importDeclarations = compilationUnit.imports();
		for (ImportDeclaration importDeclaration : importDeclarations) {
		    String importedClassName = importDeclaration.getName().getFullyQualifiedName();
//		    System.out.println("导入的类：" + importedClassName);
		}

//		获取类型声明（类、接口、枚举等）：
		List<TypeDeclaration> types = compilationUnit.types();
		for (TypeDeclaration type : types) {
		    String typeName = type.getName().getIdentifier();
		    System.out.println("类型声明：" + typeName);
		}

		//自定义对 AST 节点的访问行为
		compilationUnit.accept(new ASTVisitor() {
	        @Override
	        public boolean visit(MethodDeclaration node) {
//	            System.out.println("Method: " + node.getName());
	            return super.visit(node);
	        }
	    });
		
		
//		获取所有方法：
        TypeDeclaration typeDeclaration = (TypeDeclaration) compilationUnit.types().get(0);
		MethodDeclaration[] methods = typeDeclaration.getMethods();
		for (MethodDeclaration methodDeclaration : methods) {
			
			 // 获取方法的名称
		    String methodName = methodDeclaration.getName().getIdentifier();
		    
		    // 判断方法是否是构造函数
		    boolean isConstructor = methodDeclaration.isConstructor();

		    // 获取方法的返回类型
		    Type returnType = methodDeclaration.getReturnType2();

		    // 获取方法的修饰符
		    int modifiers = methodDeclaration.getModifiers();
		    Modifier.isPublic(modifiers);
		    
//		    获取所有注解
		    List annos = methodDeclaration.modifiers();
		    for (Object annotation : annos) {
		    	IAnnotationBinding binding = null;
				if (annotation instanceof NormalAnnotation) {
					NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
					binding = normalAnnotation.resolveAnnotationBinding();
				} else if (annotation instanceof MarkerAnnotation) {
					MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
					binding = markerAnnotation.resolveAnnotationBinding();
				} else if (annotation instanceof SingleMemberAnnotation) {
					SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation) annotation;
					binding = singleMemberAnnotation.resolveAnnotationBinding();
				}
				if (binding != null) {
					IMemberValuePairBinding[] allMemberValuePairs = binding.getAllMemberValuePairs();	
			        // ...
				}else {
//					System.out.println(annotation.toString());
				}
			}

		    // 获取方法的参数列表
		    List<SingleVariableDeclaration> parameters = methodDeclaration.parameters();
		    for (SingleVariableDeclaration parameter : parameters) {
		        // 处理参数信息
		        String parameterName = parameter.getName().getIdentifier();
		        Type parameterType = parameter.getType();
		        // ...
		    }

		    // 获取方法体
		    Block methodBody = methodDeclaration.getBody();

		    // 获取方法声明中所抛出的异常列表
		    List<Type> exceptions = methodDeclaration.thrownExceptionTypes();
		    for (Type exception : exceptions) {
		        // 处理异常信息
		        // ...
		    }
		    
		    // 获取方法声明对应的 IMethodBinding
		    IMethodBinding methodBinding = methodDeclaration.resolveBinding();
		    if (methodBinding != null) {
		        // 处理绑定信息
		         ITypeBinding declaringClass = methodBinding.getDeclaringClass();
		         String name = methodBinding.getName();
		         System.out.println(methodBinding.toString());
		        // ...
		    }
		}
	}
}
