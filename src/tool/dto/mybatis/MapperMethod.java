package tool.dto.mybatis;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.ArrayType;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.ParameterizedType;
import org.eclipse.jdt.core.dom.PrimitiveType;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.Type;

import tool.config.SupertypeHierarchyCache;
import tool.utils.NameUtil;



public class MapperMethod {
	private MethodDeclaration methodDeclaration;

	private Annotation statementAnno;

	private String statement;

	private String resultMap;

	private boolean hasMapKey;

	private Annotation resultsAnno;

	private String resultsId;

	private Annotation constructorArgsAnno;

	private List<ResultAnno> constructorArgs = new ArrayList<ResultAnno>();

	private List<ResultAnno> resultAnnos = new ArrayList<ResultAnno>();

	public MethodDeclaration getMethodDeclaration() {
		return methodDeclaration;
	}

	public void setMethodDeclaration(MethodDeclaration methodDeclaration) {
		this.methodDeclaration = methodDeclaration;
	}

	public Annotation getStatementAnno() {
		return statementAnno;
	}

	public void setStatementAnno(Annotation statementAnno) {
		this.statementAnno = statementAnno;
	}

	public String getStatement() {
		return statement;
	}

	public void setStatement(String statement) {
		this.statement = statement;
	}

	public String getResultMap() {
		return resultMap;
	}

	public void setResultMap(String resultMap) {
		this.resultMap = resultMap;
	}

	public boolean isHasMapKey() {
		return hasMapKey;
	}

	public void setHasMapKey(boolean hasMapKey) {
		this.hasMapKey = hasMapKey;
	}

	public Annotation getResultsAnno() {
		return resultsAnno;
	}

	public void setResultsAnno(Annotation resultsAnno) {
		this.resultsAnno = resultsAnno;
	}

	public String getResultsId() {
		return resultsId;
	}

	public void setResultsId(String resultsId) {
		this.resultsId = resultsId;
	}

	public Annotation getConstructorArgsAnno() {
		return constructorArgsAnno;
	}

	public void setConstructorArgsAnno(Annotation constructorArgsAnno) {
		this.constructorArgsAnno = constructorArgsAnno;
	}

	public List<ResultAnno> getConstructorArgs() {
		return constructorArgs;
	}

	public void setConstructorArgs(List<ResultAnno> constructorArgs) {
		this.constructorArgs = constructorArgs;
	}

	public List<ResultAnno> getResultAnnos() {
		return resultAnnos;
	}

	public void setResultAnnos(List<ResultAnno> resultAnnos) {
		this.resultAnnos = resultAnnos;
	}

	@SuppressWarnings("rawtypes")
	public List parameters() {
		return this.methodDeclaration.parameters();
	}

	public String getStatementAnnoName() {
		Name annoName = this.statementAnno.getTypeName();
		if (annoName.isQualifiedName())
			return ((QualifiedName) annoName).getName().getIdentifier();
		else
			return ((SimpleName) annoName).getIdentifier();
	}

	public boolean isSelect() {
		return "Select".equals(getStatementAnnoName());
	}

	public String getReturnTypeStr() {
		Type returnType = methodDeclaration.getReturnType2();
		if (returnType == null || isVoid(returnType)) {
			return null;
		} else if (returnType.isPrimitiveType()) {
			return "_" + returnType.toString();
		} else if (returnType.isArrayType()) {
			Type componentType = ((ArrayType) returnType).getElementType();
			if (componentType.isPrimitiveType())
				return "_" + returnType.toString();
			else
				return NameUtil.stripTypeArguments(componentType.resolveBinding().getQualifiedName());
		} else if (returnType.isParameterizedType()) {
			ParameterizedType parameterizedType = (ParameterizedType) returnType;
			@SuppressWarnings("unchecked")
			List<Type> typeArgs = parameterizedType.typeArguments();
			IType rawType = (IType) parameterizedType.getType().resolveBinding().getJavaElement();
			if (SupertypeHierarchyCache.getInstance().isCollection(rawType)) {
				if (typeArgs.size() == 1)
					return NameUtil.stripTypeArguments(typeArgs.get(0).resolveBinding().getQualifiedName());
			} else if (SupertypeHierarchyCache.getInstance().isMap(rawType)) {
				if (!isHasMapKey())
					return rawType.getFullyQualifiedName();
				else if (typeArgs.size() == 2)
					return NameUtil.stripTypeArguments(typeArgs.get(0).resolveBinding().getQualifiedName());
			}
		}
		ITypeBinding binding = returnType.resolveBinding();
		return binding == null ? null : binding.getQualifiedName();
	}

	private boolean isVoid(Type type) {
		return type.isPrimitiveType() && PrimitiveType.VOID.equals(((PrimitiveType) type).getPrimitiveTypeCode());
	}
}