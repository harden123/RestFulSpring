package tool.quickAssist.mybatis;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.List;

import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.ASTVisitor;
import org.eclipse.jdt.core.dom.Annotation;
import org.eclipse.jdt.core.dom.AnonymousClassDeclaration;
import org.eclipse.jdt.core.dom.ArrayInitializer;
import org.eclipse.jdt.core.dom.BooleanLiteral;
import org.eclipse.jdt.core.dom.Expression;
import org.eclipse.jdt.core.dom.ITypeBinding;
import org.eclipse.jdt.core.dom.InfixExpression;
import org.eclipse.jdt.core.dom.MemberValuePair;
import org.eclipse.jdt.core.dom.MethodDeclaration;
import org.eclipse.jdt.core.dom.Name;
import org.eclipse.jdt.core.dom.NormalAnnotation;
import org.eclipse.jdt.core.dom.QualifiedName;
import org.eclipse.jdt.core.dom.SimpleName;
import org.eclipse.jdt.core.dom.SingleMemberAnnotation;
import org.eclipse.jdt.core.dom.StringLiteral;
import org.eclipse.jdt.core.dom.TextBlock;
import org.eclipse.jdt.core.dom.TypeDeclaration;
import org.eclipse.jdt.core.dom.TypeLiteral;

import restfulspring.config.Log;
import tool.dto.mybatis.MapperMethod;
import tool.dto.mybatis.ResultAnno;


public class QuickAssistMapperMethodVisitor extends ASTVisitor {
	private IMethod targetMethod;

	private MapperMethod mapperMethod;

	private int nestLevel;

	private Deque<Annotation> annoStack = new ArrayDeque<Annotation>();

	private ResultAnno resultAnno;

	public QuickAssistMapperMethodVisitor(IMethod targetMethod) {
		this.targetMethod = targetMethod;
	}

	@Override
	public boolean visit(TypeDeclaration node) {
		String targetType = targetMethod.getDeclaringType().getFullyQualifiedName().replace('$', '.');
		String currentType = node.resolveBinding().getQualifiedName();
		if (targetType.equals(currentType))
			nestLevel = 1;
		else if (nestLevel > 0)
			nestLevel++;

		return true;
	}

	@Override
	public boolean visit(AnonymousClassDeclaration node) {
		return false;
	}

	@Override
	public boolean visit(MethodDeclaration node) {
		if (nestLevel != 1)
			return false;
		if (targetMethod.getElementName().equals(node.getName().getFullyQualifiedName())) {
			IMethod method = (IMethod) node.resolveBinding().getJavaElement();
			if (targetMethod.isSimilar(method)) {
				mapperMethod = new MapperMethod();
				mapperMethod.setMethodDeclaration(node);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean visit(NormalAnnotation node) {
		if (nestLevel != 1)
			return false;
		pushAnno(node);
		return true;
	}

	@Override
	public boolean visit(SingleMemberAnnotation node) {
		if (nestLevel != 1)
			return false;
		pushAnno(node);
		parseMemberValuePair(node, "value", node.getValue());
		return true;
	}

	private void pushAnno(Annotation annotation) {
		String annoName = getAnnotationName(annotation);
		if ("Result".equals(annoName) || "Arg".equals(annoName)) {
			resultAnno = new ResultAnno();
		}
		annoStack.push(annotation);
	}

	private String getAnnotationName(Annotation node) {
		Name typeName = node.getTypeName();
		if (typeName.isQualifiedName())
			return ((QualifiedName) typeName).getName().getIdentifier();
		else
			return ((SimpleName) typeName).getIdentifier();
	}

	@Override
	public boolean visit(MemberValuePair node) {
		String memberName = node.getName().getIdentifier();
		Expression memberValue = node.getValue();
		Annotation currentAnno = annoStack.peek();
		parseMemberValuePair(currentAnno, memberName, memberValue);
		return true;
	}

	private void parseMemberValuePair(Annotation annotation, String memberName, Expression memberValue) {
		String annoName = getAnnotationName(annotation);
		if ("Select".equals(annoName) || "Insert".equals(annoName) || "Update".equals(annoName) || "Delete".equals(annoName)) {
			mapperMethod.setStatementAnno(annotation);
			mapperMethod.setStatement(parseStringValue(memberValue, " "));
		} else if ("ResultMap".equals(annoName)) {
			mapperMethod.setResultMap(parseStringValue(memberValue, ","));
		} else if ("MapKey".equals(annoName)) {
			mapperMethod.setHasMapKey(true);
		} else if ("ConstructorArgs".equals(annoName)) {
			mapperMethod.setConstructorArgsAnno(annotation);
		} else if ("Results".equals(annoName)) {
			mapperMethod.setResultsAnno(annotation);
			if ("id".equals(memberName)) {
				mapperMethod.setResultsId(((StringLiteral) memberValue).getLiteralValue());
			}
		} else if ("Result".equals(annoName) || "Arg".equals(annoName)) {
			if ("id".equals(memberName)) {
				resultAnno.setId(((BooleanLiteral) memberValue).booleanValue());
			} else if ("column".equals(memberName)) {
				resultAnno.setColumn(((StringLiteral) memberValue).getLiteralValue());
			} else if ("property".equals(memberName)) {
				resultAnno.setProperty(((StringLiteral) memberValue).getLiteralValue());
			} else if ("one".equals(memberName)) {
				resultAnno.setAssociation(true);
			} else if ("many".equals(memberName)) {
				resultAnno.setCollection(true);
			} else if ("jdbcType".equals(memberName)) {
				resultAnno.setJdbcType(((QualifiedName) memberValue).getName().getIdentifier());
			} else if ("javaType".equals(memberName)) {
				// Class<E>
				ITypeBinding binding = ((TypeLiteral) memberValue).resolveTypeBinding();
				resultAnno.setJavaType(binding.getTypeArguments()[0].getQualifiedName());
			} else if ("typeHandler".equals(memberName)) {
				// Class<E>
				ITypeBinding binding = ((TypeLiteral) memberValue).resolveTypeBinding();
				resultAnno.setTypeHandler(binding.getTypeArguments()[0].getQualifiedName());
			} else if ("resultMap".equals(memberName)) {
				resultAnno.setResultMap(((StringLiteral) memberValue).getLiteralValue());
			}
		} else if ("One".equals(annoName) || "Many".equals(annoName)) {
			if ("select".equals(memberName)) {
				resultAnno.setSelectId(((StringLiteral) memberValue).getLiteralValue());
			} else if ("fetchType".equals(memberName)) {
				String fetchType = ((QualifiedName) memberValue).getName().getIdentifier();
				if (!"DEFAULT".equals(fetchType)) {
					resultAnno.setFetchType(fetchType.toLowerCase());
				}
			}
		}
	}

	private String parseStringValue(Expression value, String separator) {
		int valueType = value.getNodeType();
		if (valueType == ASTNode.STRING_LITERAL) {
			return ((StringLiteral) value).getLiteralValue();
		} else if (valueType == ASTNode.TEXT_BLOCK) {
			return ((TextBlock) value).getLiteralValue();
		} else if (valueType == ASTNode.ARRAY_INITIALIZER) {
			StringBuilder buffer = new StringBuilder();
			@SuppressWarnings("unchecked")
			List<Expression> expressions = (List<Expression>) ((ArrayInitializer) value).expressions();
			for (Expression expression : expressions) {
				int expressionType = expression.getNodeType();
				if (expressionType == ASTNode.STRING_LITERAL) {
					if (buffer.length() > 0)
						buffer.append(separator);
					buffer.append(((StringLiteral) expression).getLiteralValue());
				} else if (expressionType == ASTNode.INFIX_EXPRESSION) {
					buffer.append(parseInfixExpression((InfixExpression) expression));
				}
			}
			return buffer.toString();
		} else if (valueType == ASTNode.INFIX_EXPRESSION) {
			return parseInfixExpression((InfixExpression) value);
		}
		Log.error("Unsupported node type " + valueType,null);
		return null;
	}

	private String parseInfixExpression(InfixExpression expression) {
		// will implement if someone really wants it...
		return expression.toString();
	}

	@Override
	public void endVisit(NormalAnnotation node) {
		popAnno();
	}

	@Override
	public void endVisit(SingleMemberAnnotation node) {
		popAnno();
	}

	private void popAnno() {
		String annoName = getAnnotationName(annoStack.pop());
		if ("Result".equals(annoName)) {
			mapperMethod.getResultAnnos().add(resultAnno);
			resultAnno = null;
		} else if ("Arg".equals(annoName)) {
			mapperMethod.getConstructorArgs().add(resultAnno);
			resultAnno = null;
		}
	}

	@Override
	public void endVisit(TypeDeclaration node) {
		nestLevel--;
	}

	public MapperMethod getMapperMethod() {
		return mapperMethod;
	}
}
