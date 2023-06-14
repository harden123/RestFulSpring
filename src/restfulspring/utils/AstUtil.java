package restfulspring.utils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.IVariableBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.SimpleType;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.Type;

import lombok.SneakyThrows;
import restfulspring.dto.VariableBindingField;

public class AstUtil {

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

	public static VariableBindingField getFields(Type type) {
		VariableBindingField obj = new VariableBindingField();
		if (type instanceof ParameterizedType) {
			// 如果是泛型类型，需要先获取原始类型
			type = ((ParameterizedType) type).getType();
		}
		if (type instanceof SimpleType) {
			ITypeBinding typeBinding = ((SimpleType) type).resolveBinding();
			if (typeBinding != null && typeBinding.isClass()) {
				IVariableBinding[] fields = typeBinding.getDeclaredFields();
				for (IVariableBinding field : fields) {
					obj.getCur().add(field);
				}
			}
		}
		return obj;

	}

}
