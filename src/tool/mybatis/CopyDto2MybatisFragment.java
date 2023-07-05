package tool.mybatis;

import java.util.Date;
import java.util.List;

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
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.ResourceUtil;

import tool.utils.ViewUtil;

public class CopyDto2MybatisFragment extends AbstractHandler{

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
				FieldDeclaration[] fields = type.getFields();
				testSqlMethods(fields);
			}
		}
		return null;
	}
	
	public void testSqlMethods(FieldDeclaration[] fields) {
		StringBuffer sb = new StringBuffer();
		for (FieldDeclaration field : fields) {
				Type type = field.getType();
				String name = field.fragments().get(0).toString();
				if (name.equals("pageSize") || name.equals("pageNum")) {
					continue;
				}
				String fullyQualifiedName = null;
				if (type.isSimpleType()) {
		            SimpleType simpleType = (SimpleType) type;
		            fullyQualifiedName = simpleType.resolveBinding().getQualifiedName();
		        } else if (type.isParameterizedType()) {
		        	fullyQualifiedName = type.resolveBinding().getBinaryName();
		        } else if (type.isArrayType()) {
		        	System.out.println(type);
		        }
				
				
				String camel2underName = camel2under(name);
				if (String.class.getName().equals(fullyQualifiedName)) {
					if (StringUtils.startsWith(name,"like")||StringUtils.endsWithIgnoreCase(name, "like")) {
						sb.append("<if test=\"" + name + " != null and " + name + " != ''\">\r\n" + "   and " + camel2underName + " LIKE CONCAT#{" + name + "},'%')\r\n"
								+ "</if>\r\n");
					}else {
						sb.append("<if test=\"" + name + " != null and " + name + " != ''\">\r\n" + "   and " + camel2underName + " = #{" + name + "}\r\n"
								+ "</if>\r\n");
					}
				} else if (Date.class.getName().equals(fullyQualifiedName)) {
					if (StringUtils.containsIgnoreCase(name, "end")) {
						sb.append("<if test=\"" + name + " != null \" >\r\n" + "   <![CDATA[\r\n" + "   and " + camel2underName + " < #{" + name
								+ "}\r\n" + "   ]]>\r\n" + "</if>\r\n");
					} else {
						sb.append("<if test=\"" + name + " != null \" >\r\n" + "   and " + camel2underName + " >= #{" + name + "}\r\n" + "</if>\r\n");
					}
				} else if (Integer.class.getName().equals(fullyQualifiedName) || Long.class.getName().equals(fullyQualifiedName) || Double.class.getName().equals(fullyQualifiedName)
						|| Float.class.getName().equals(fullyQualifiedName) || Short.class.getName().equals(fullyQualifiedName)) {
					sb.append("<if test=\"" + name + " != null \">\r\n" + "   and " + camel2underName + " = #{" + name + "}\r\n" + "</if>\r\n");
				} else if (List.class.getName().equals(fullyQualifiedName)){
					if (StringUtils.startsWith(name,"simpleAreaCode")||StringUtils.startsWith(name,"like")||StringUtils.endsWithIgnoreCase(name, "like")) {
						sb.append("<if test=\""+name+" != null and "+name+".size > 0\">\r\n"
								+ "    AND\r\n"
								+ "   <foreach collection=\""+name+"\" item=\""+name+"Item\" open=\"(\" close=\")\" separator=\"or\" index=\"index\">\r\n"
								+ "      "+camel2underName+" LIKE CONCAT(#{"+name+"Item},'%')\r\n"
								+ "   </foreach>\r\n"
								+ "</if>");
					}else {
						sb.append("<if test=\"" + name + " != null and " + name + ".size>0\">\r\n"
								+ "   and "+camel2underName+" in\r\n"
								+ "   <foreach collection=\""+name+"\" item=\""+name+"Item\" index=\"index\" open=\"(\" close=\")\" separator=\",\">\r\n"
								+ "      #{"+name+"Item}\r\n"
								+ "   </foreach>\r\n"
								+ "</if>\r\n"
								);
					}
				}else{
					sb.append("name:" + name + "\r\n");
				}
		}
		
		ViewUtil.copyAndToolTip(sb.toString());
	}

	

	public static String camel2under(String c) {
		return c.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
	}

}
