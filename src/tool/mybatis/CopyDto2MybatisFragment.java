package tool.mybatis;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.dom.AST;
import org.eclipse.jdt.core.dom.ASTParser;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.FieldDeclaration;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.Type;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.ResourceUtil;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import tool.utils.ViewUtil;

public class CopyDto2MybatisFragment extends AbstractHandler{
	public static Pattern setPattern = Pattern.compile("^[^\\.]*Set$");
	public static Pattern listPattern = Pattern.compile("^[^\\.]*List$");

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
				Map<String, String> name2fullyQualified = getname2fullyQualified(type);
				StringBuffer sb = copySqlMethods(name2fullyQualified,null);
				ViewUtil.copyAndToolTip(sb.toString());
			}
		}
		return null;
	}
	
	public static Map<String,String> getname2fullyQualified(TypeDeclaration clztype) {
		FieldDeclaration[] fields = clztype.getFields();
		HashMap<String,String> name2fullyQualifiedNameMap = Maps.newLinkedHashMap();
		for (FieldDeclaration field : fields) {
			Type type = field.getType();
			String name = field.fragments().get(0).toString();
			if (name.equals("pageSize") || name.equals("pageNum")) {
				continue;
			}
			String typeName = null;
			if (type.isSimpleType()) {
	            SimpleType simpleType = (SimpleType) type;
	            typeName = simpleType.resolveBinding().getName();
	        } else if (type.isParameterizedType()) {
	        	ParameterizedType parameterizedType = (ParameterizedType)type;
	        	typeName = parameterizedType.resolveBinding().getErasure().getName();
	        } else if (type.isArrayType()) {
	        	ArrayType arrayType = (ArrayType)type;
	        	typeName = arrayType.resolveBinding().getName();
	        }
			name2fullyQualifiedNameMap.put(name, typeName);
		}
		return name2fullyQualifiedNameMap;
	}
	
	
	public static StringBuffer copySqlMethods(Map<String, String> name2SimpleTypeMap,String prefix) {
		StringBuffer sb = new StringBuffer();
		Set<Entry<String, String>> entrySet = name2SimpleTypeMap.entrySet();
		ArrayList<String> leftFieldNames = Lists.newArrayList();
		for (Entry<String, String> entry : entrySet) {
			String name = entry.getKey();
			if (StringUtils.isNotBlank(prefix)) {
				name = prefix+"."+name;
			}
			String simpleTypeName = entry.getValue();
			String camel2underName = camel2under(name);
			if (String.class.getSimpleName().equals(simpleTypeName)) {
				if (StringUtils.startsWith(name,"like")||StringUtils.endsWithIgnoreCase(name, "like")) {
					sb.append("<if test=\"" + name + " != null and " + name + " != ''\">\r\n" + "   and " + camel2underName + " LIKE CONCAT#{" + name + "},'%')\r\n"
							+ "</if>\r\n");
				}else {
					sb.append("<if test=\"" + name + " != null and " + name + " != ''\">\r\n" + "   and " + camel2underName + " = #{" + name + "}\r\n"
							+ "</if>\r\n");
				}
			} else if (Date.class.getSimpleName().equals(simpleTypeName)) {
				if (StringUtils.containsIgnoreCase(name, "end")) {
					sb.append("<if test=\"" + name + " != null \" >\r\n" + "   <![CDATA[\r\n" + "   and " + camel2underName + " < #{" + name
							+ "}\r\n" + "   ]]>\r\n" + "</if>\r\n");
				} else {
					sb.append("<if test=\"" + name + " != null \" >\r\n" + "   and " + camel2underName + " >= #{" + name + "}\r\n" + "</if>\r\n");
				}
			} else if (Integer.class.getSimpleName().equals(simpleTypeName) || Long.class.getSimpleName().equals(simpleTypeName) || Double.class.getSimpleName().equals(simpleTypeName)
					|| Float.class.getSimpleName().equals(simpleTypeName) || Short.class.getSimpleName().equals(simpleTypeName)) {
				sb.append("<if test=\"" + name + " != null \">\r\n" + "   and " + camel2underName + " = #{" + name + "}\r\n" + "</if>\r\n");
			} else if (listPattern.matcher(simpleTypeName).matches()
					||setPattern.matcher(simpleTypeName).matches()){
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
				leftFieldNames.add(name);
			}
		}
		for (String string : leftFieldNames) {
			sb.append(string + "\r\n");
		}
		return sb;
	}

	

	public static String camel2under(String c) {
		return c.replaceAll("([a-z])([A-Z])", "$1_$2").toLowerCase();
	}
	
	public static void main(String[] args) {
		System.out.println(listPattern.matcher("ArrayList").matches());
		
	}

}
