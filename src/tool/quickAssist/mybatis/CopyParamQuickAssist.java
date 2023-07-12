package tool.quickAssist.mybatis;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import org.eclipse.jdt.core.IField;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.Signature;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.core.dom.IAnnotationBinding;
import org.eclipse.jdt.core.dom.IExtendedModifier;
import org.eclipse.jdt.core.dom.IMemberValuePairBinding;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MarkerAnnotation;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.SingleVariableDeclaration;
import org.eclipse.jface.text.IDocument;

import com.google.common.collect.Maps;

import lombok.SneakyThrows;
import restfulspring.constant.RestConstant;
import restfulspring.utils.AstUtil;
import restfulspring.utils.CollectionUtils;
import tool.dto.mybatis.MapperMethod;
import tool.mybatis.CopyDto2MybatisFragment;
import tool.quickAssist.QuickAssistCompletionProposal;
import tool.utils.ViewUtil;

public class CopyParamQuickAssist extends QuickAssistCompletionProposal {

	private MapperMethod method;
	private CompilationUnit astNode;

	public CopyParamQuickAssist(String displayString, MapperMethod mapperMethod, CompilationUnit astNode) {
		super(displayString);
		this.method = mapperMethod;
		this.astNode = astNode;
	}

	/** 
	 * {@inheritDoc}
	 */
	@Override
	@SneakyThrows
	public void apply(IDocument document) {
		List<SingleVariableDeclaration> params = method.parameters();
		StringBuffer sb = new StringBuffer();
		for (SingleVariableDeclaration param : params) {
			ITypeBinding type = param.resolveBinding().getType();
			String typeQualifiedName = type.getQualifiedName();
			String typeName = Optional.ofNullable(type.getErasure()).orElse(type).getName();
			if (RestConstant.TYPE_ROW_BOUNDS.equals(typeQualifiedName))
				continue;
			List<IExtendedModifier> modifiers = param.modifiers();
			String paramName = param.getName().getFullyQualifiedName();
			String paramAlias = getParamName(modifiers);
			paramName = Optional.ofNullable(paramAlias).orElse(paramName);
			if (AstUtil.isJdkType(type)) {
				HashMap<String, String> name2TypeName = Maps.newHashMapWithExpectedSize(1);
				name2TypeName.put(paramName, typeName);
				StringBuffer copySqlMethods = CopyDto2MybatisFragment.copySqlMethods(name2TypeName);
				sb.append(copySqlMethods);
			}else {
			    IType type2 = (IType) type.getJavaElement();
		        if (type2 != null && type2.exists()) {
		        	IField[] fields = type2.getFields();
					HashMap<String, String> name2TypeName = Maps.newHashMap();
		        	for (IField im : fields) {
		        		name2TypeName.put(im.getElementName(), Signature.toString(im.getTypeSignature()));
					}
					StringBuffer copySqlMethods = CopyDto2MybatisFragment.copySqlMethods(name2TypeName);
					sb.append(copySqlMethods);
		        }
			}
		}
		ViewUtil.copyAndToolTip(sb.toString());

	}
	
	private String getParamName(List<IExtendedModifier> modifiers) {
		if (CollectionUtils.isEmpty(modifiers)) {
			return null;
		}
		for (IExtendedModifier annotation : modifiers) {
			if (annotation.isAnnotation() && "Param".equals(((Annotation) annotation).getTypeName().getFullyQualifiedName())) {
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
					for (IMemberValuePairBinding pair : allMemberValuePairs) {
						String name = pair.getName();
						Object value = pair.getValue();
						if ("value".equals(name)&&value!=null) {
							return value+"";
						}
					}
				}
			}
		}
		return null;
	}


}
