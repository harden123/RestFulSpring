package restfulspring.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Pattern;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jdt.core.dom.Type;

import com.alibaba.fastjson.JSONArray;
import com.alibaba.fastjson.JSONObject;

import lombok.SneakyThrows;
import restfulspring.constant.RestConstant;
import restfulspring.dto.JDTMethodDTO;
import restfulspring.dto.RestParamDTO;
import restfulspring.view.tree.restSpring.MyTreeElement;

public class AstUtil {
	public static Pattern listPattern = Pattern.compile("^java.util.[^\\.]*List$");
	public static Pattern setPattern = Pattern.compile("^java.util.[^\\.]*Set$");
    private static Pattern mapPattern = Pattern.compile("^java.util.[^\\.]*Map$");
    private static final String DepencyLineSplitor = "->";


	// 获取给定modifiers中所有注解
	/**
	 * <annoKey:<key,val>>
	 */
	@SneakyThrows
	public static HashMap<String, Map<String, Object>> retrieveAnnoByModifiers(List list) {
		HashMap<String, Map<String, Object>> hashMap = new HashMap<>();
		for (Object annotation : list) {
			if (annotation instanceof NormalAnnotation) {
				NormalAnnotation normalAnnotation = (NormalAnnotation) annotation;
				IAnnotationBinding binding = normalAnnotation.resolveAnnotationBinding();
				extractedAnnoBinds(hashMap, binding, annotation);
			} else if (annotation instanceof MarkerAnnotation) {
				MarkerAnnotation markerAnnotation = (MarkerAnnotation) annotation;
				IAnnotationBinding binding = markerAnnotation.resolveAnnotationBinding();
				extractedAnnoBinds(hashMap, binding, annotation);
			} else if (annotation instanceof SingleMemberAnnotation) {
				SingleMemberAnnotation singleMemberAnnotation = (SingleMemberAnnotation) annotation;
				IAnnotationBinding binding = singleMemberAnnotation.resolveAnnotationBinding();
				extractedAnnoBinds(hashMap, binding, annotation);
			}
		}
		return hashMap;
	}

	private static void extractedAnnoBinds(HashMap<String, Map<String, Object>> hashMap, IAnnotationBinding binding, Object annotation) {
		if (binding != null) {
			IMemberValuePairBinding[] allMemberValuePairs = binding.getAllMemberValuePairs();
			HashMap<String, Object> values = new HashMap<>();
			for (IMemberValuePairBinding pair : allMemberValuePairs) {
				String name = pair.getName();
				Object value = pair.getValue();
				values.put(name, value);
			}
			hashMap.put(binding.getName(), values);
		} else {
			String string = annotation.toString();
			hashMap.put(string, null);
		}
	}

	// public static List<FieldDeclaration> getFields(TypeDeclaration type) {
	// List<FieldDeclaration> fields = new ArrayList<>();
	//
	// for (Object obj : type.bodyDeclarations()) {
	// if (obj instanceof FieldDeclaration) {
	// FieldDeclaration field = (FieldDeclaration) obj;
	// fields.add(field);
	// }
	// }
	//
	// return fields;
	// }

	public static Object getValByAnoAndKey(HashMap<String, Map<String, Object>> annotations, String anno, String annoKey) {
		if (CollectionUtils.isEmpty(annotations)) {
			return null;
		}
		Map<String, Object> map = annotations.get(anno);
		if (CollectionUtils.isEmpty(map)) {
			return null;
		}
		return parseAnoVal(map.get(annoKey));
	}

	private static Object parseAnoVal(Object annoVal) {
		if (annoVal != null) {
			// 根据注解成员变量的类型进行相应处理
			if (annoVal instanceof Integer) {
				int intValue = ((Integer) annoVal).intValue();
				// 处理基本类型值
				return intValue;
			} else if (annoVal instanceof ITypeBinding) {
				ITypeBinding typeBinding = (ITypeBinding) annoVal;
				return typeBinding.getName();
				// 处理 Class 类型值
			} else if (annoVal instanceof String) {
				// 处理字符串类型值
				String stringValue = (String) annoVal;
				return stringValue;
			} else if (annoVal instanceof IVariableBinding) {
				// 处理枚举类型值
				IVariableBinding enumBinding = (IVariableBinding) annoVal;
				return enumBinding.getName();
			} else if (annoVal instanceof IAnnotationBinding) {
				// 处理嵌套注解类型值
				IAnnotationBinding nestedAnnotationBinding = (IAnnotationBinding) annoVal;
				return nestedAnnotationBinding.getName();
			} else if (annoVal instanceof Object[]) {
				// 处理数组类型值
				Object[] arrayValue = (Object[]) annoVal;
				if (arrayValue != null && arrayValue.length > 0) {
					for (int i = 0; i < arrayValue.length; i++) {
						Object object = arrayValue[i];
						Object valByObject = parseAnoVal(object);
						if (valByObject != null) {
							return valByObject;
						}
					}
				}
			}
		}
		return null;
	}

	/**
	 * type获取json格式内容
	 */
	public static Object getFields(Type type) {
		if (type.isPrimitiveType()) {
			ITypeBinding resolveBinding = type.resolveBinding();
			return initJdkVal(resolveBinding);
		}
		if (type instanceof ParameterizedType) {
			// 如果是泛型类型，需要先获取原始类型
			type = ((ParameterizedType) type).getType();
		}
		if (type instanceof SimpleType) {
			ITypeBinding typeBinding = ((SimpleType) type).resolveBinding();
			if (typeBinding == null) {
				return null;
			}
			String dependencyLine = "";
			return iterParse(typeBinding,dependencyLine);
		}
		return null;
	}

	public static Object iterParse(ITypeBinding typeBinding,String dependencyLine) {
		if (typeBinding == null) {
			return null;
		}
		if (typeBinding.isArray() ) {
			JSONArray array = new JSONArray();
			ITypeBinding iTypeBinding = typeBinding.getElementType();
			if (iTypeBinding == null||iTypeBinding.isWildcardType()||judgeDependencyFieldConflict(dependencyLine, iTypeBinding.getQualifiedName())) {
				return array;
			}
			Object iterParse = iterParse(iTypeBinding,dependencyLine);
			if (iterParse != null) {
				array.add(iterParse);
			}
			return array;
		}else if(isQuailifyList(typeBinding)) {
			  // 获取泛型参数
			 JSONArray array = new JSONArray();
			 if (typeBinding.getTypeArguments()!=null&&typeBinding.getTypeArguments().length>0) {
				 ITypeBinding iTypeBinding = typeBinding.getTypeArguments()[0];
			    // 如果泛型参数为通配符类型，则返回 "?"，否则返回参数类型的全名
			    if (iTypeBinding.isWildcardType()||judgeDependencyFieldConflict(dependencyLine, iTypeBinding.getQualifiedName())) {
					return array;
				}
			    Object iterParse = iterParse(iTypeBinding,dependencyLine);
				if (iterParse != null) {
					array.add(iterParse);
				}
			}
			return array;
		}else{
			if (isJdkType(typeBinding)) {
				 // 处理JDK内置类...
				return initJdkVal(typeBinding);
		    }else {
				JSONObject obj = new JSONObject(true);
		    	if (judgeDependencyFieldConflict(dependencyLine, typeBinding.getQualifiedName())) {
					return obj;
				}
		    	dependencyLine = appendToDependencyLine(dependencyLine, typeBinding.getQualifiedName());
		    	// 处理非JDK内置类...
				//防止迭代循环
		    	IVariableBinding[] fields = typeBinding.getDeclaredFields();
				for (IVariableBinding field : fields) {
					obj.put(field.getName(), iterParse(field.getType(),dependencyLine));
				}
				return obj;
		    }
		}
	}

	private static boolean isQuailifyList(ITypeBinding typeBinding) {
		ITypeBinding erasure = typeBinding.getErasure();
		String qualifiedName = Optional.ofNullable(erasure).orElse(typeBinding).getQualifiedName();
		return listPattern.matcher(qualifiedName).matches();
	}
	
	private static boolean isQuailifyMap(ITypeBinding typeBinding) {
		ITypeBinding erasure = typeBinding.getErasure();
		String qualifiedName = Optional.ofNullable(erasure).orElse(typeBinding).getQualifiedName();
		
		return mapPattern.matcher(qualifiedName).matches();
	}

	public static boolean isJdkType(ITypeBinding typeBinding) {
		if (typeBinding.getPackage()==null||typeBinding.isPrimitive()) {
			return true;
		}
		return typeBinding.getPackage().getName().startsWith("java.");
	}
	

	private static Object initJdkVal(ITypeBinding typeBinding) {
		ITypeBinding erasure = typeBinding.getErasure();
		String qualifiedName = Optional.ofNullable(erasure).orElse(typeBinding).getQualifiedName();
		if (RestConstant.java_lang_String.equals(qualifiedName)||"char".equals(qualifiedName)||"java.lang.Character".equals(qualifiedName)) {
			return "";
		}else if( "byte".equals(qualifiedName)||"java.lang.Byte".equals(qualifiedName)
		            || "short".equals(qualifiedName)||"java.lang.Short".equals(qualifiedName)
		            || "int".equals(qualifiedName)||"java.lang.Integer".equals(qualifiedName)
		            || "long".equals(qualifiedName)||"java.lang.Long".equals(qualifiedName)
				) {
		  // 判断类型名称是否为数字类型"byte".equals(typeName)
		    return 0;
		}else if( "float".equals(qualifiedName)||"java.lang.Float".equals(qualifiedName)) {
			 return 1.0f;
		}else if( "double".equals(qualifiedName)||"java.lang.Double".equals(qualifiedName)) {
			 return 1.0d;
		}else if("boolean".equals(qualifiedName)||"java.lang.Boolean".equals(qualifiedName)) {
			return false;
		}else if(isQuailifyList(typeBinding)) {
			 return new JSONArray();
		}else if(isQuailifyMap(typeBinding)) {
			 return new JSONObject(true);
		}else if(typeBinding.isArray()) {
			 return new JSONArray();
		}
		return null;
		
	}
	
	
	public static RestParamDTO computeParam(JDTMethodDTO jdtMethodDTO) {
		RestParamDTO restParamDTO = new RestParamDTO();
		AtomicReference<String> bodyStr = restParamDTO.getBodyStr();
		Map<String, Object> getParamKVMap = restParamDTO.getGetParamKVMap();
		List<SingleVariableDeclaration> reqVariable = jdtMethodDTO.getReqParams();
		for (SingleVariableDeclaration singleVariableDeclaration : reqVariable) {
			Type type = singleVariableDeclaration.getType();
			List modifiers = singleVariableDeclaration.modifiers();
			String paramName = singleVariableDeclaration.getName().toString();
			HashMap<String, Map<String, Object>> retrieveAnnotations = AstUtil.retrieveAnnoByModifiers(modifiers);
			if (retrieveAnnotations.containsKey(RestConstant.RequestBody)) {
				Object obj = AstUtil.getFields(type);
				bodyStr.set(TextUtil.prettyJSON(obj));
			}else if (retrieveAnnotations.containsKey(RestConstant.RequestParam)) {
				Object param = AstUtil.getValByAnoAndKey(retrieveAnnotations, RestConstant.RequestParam, RestConstant.RequestMapping_value);
				if (StringUtils.isBlank(Objects.toString(param, null))) {
					param = paramName;
				}
				Object obj = AstUtil.getFields(type);
				getParamKVMap.put(param.toString(), obj);
			}else {
				Object obj = AstUtil.getFields(type);
				getParamKVMap.put(paramName, obj);
			}
		}
		return restParamDTO;
	}
	
	public static String getMethodUrl(MyTreeElement methodNode) {
		Object type_url = null;//   /micro/accessory
		MyTreeElement typeNode = methodNode.getParent();
		if (typeNode!=null&&typeNode.getJDTTypeDTO()!=null) {
			HashMap<String, Map<String, Object>> type_Annotations = typeNode.getJDTTypeDTO().getAnnotations();
			type_url = AstUtil.getValByAnoAndKey(type_Annotations, RestConstant.RequestMapping, RestConstant.RequestMapping_value);
		}
		JDTMethodDTO jdtMethodDTO = methodNode.getJDTMethodDTO();
		HashMap<String, Map<String, Object>> m_annotations = jdtMethodDTO.getAnnotations();
		Object method_url = AstUtil.getValByAnoAndKey(m_annotations, RestConstant.RequestMapping, RestConstant.RequestMapping_value);//  /add
		String url = normalizeReq(type_url)+normalizeReq(method_url);
		return url;
	}
	
	private static String normalizeReq(Object type_url) {
		String string = StringUtils.trim(Objects.toString(type_url, ""));
		String removeEnd = StringUtils.removeEnd(string, "/");
		if (!StringUtils.startsWith(removeEnd, "/")) {
			removeEnd="/"+removeEnd;
		}
		return removeEnd;
	}
	
	/**
	 * fieldName加入到dependcyFieldLine
	 */
	private static String appendToDependencyLine(String dependencyFieldLine, String fieldName) {
		if (StringUtils.isBlank(dependencyFieldLine)) {
			return fieldName;
		}
		return dependencyFieldLine+=DepencyLineSplitor+fieldName;
	}
	
	/**
	 * 判断死循环冲突
	 */
	private static boolean judgeDependencyFieldConflict(String dependencyFieldLine,String fieldName) {
		String[] split = dependencyFieldLine.split(DepencyLineSplitor);
		int length = split.length;
		for (int i = 0; i < length; i++) {
			String string = split[i].trim();
			if (string.equals(fieldName)&&i!=length-1) {
				return true;
			}
		}
		return false;
	}
}


