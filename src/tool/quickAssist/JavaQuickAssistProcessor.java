package tool.quickAssist;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.jdt.core.ICompilationUnit;
import org.eclipse.jdt.core.IJavaElement;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IMethod;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.dom.ASTNode;
import org.eclipse.jdt.core.dom.CompilationUnit;
import org.eclipse.jdt.ui.text.java.IInvocationContext;
import org.eclipse.jdt.ui.text.java.IJavaCompletionProposal;
import org.eclipse.jdt.ui.text.java.IProblemLocation;
import org.eclipse.jdt.ui.text.java.IQuickAssistProcessor;

import restfulspring.utils.AstUtil;
import tool.dto.mybatis.MapperMethod;
import tool.quickAssist.mybatis.CopyParamQuickAssist;
import tool.quickAssist.mybatis.QuickAssistMapperMethodVisitor;

public class JavaQuickAssistProcessor implements IQuickAssistProcessor {

	/** 
	 * {@inheritDoc}
	 */
	@Override
	public IJavaCompletionProposal[] getAssists(IInvocationContext context, IProblemLocation[] locations) throws CoreException {
		ICompilationUnit compilationUnit = context.getCompilationUnit();
		IType primaryType = compilationUnit.findPrimaryType();
		if (primaryType == null || !primaryType.isInterface())
			return null;

		final String mapperFqn = primaryType.getFullyQualifiedName();
		IJavaProject project = compilationUnit.getJavaProject();
		if (project == null)
			return null;

		IJavaElement[] elements = compilationUnit.codeSelect(context.getSelectionOffset(), context.getSelectionLength());
		for (IJavaElement element : elements) {
			if (element.getElementType() == IJavaElement.METHOD) {
				IMethod method = (IMethod) element;
				if (!method.getDeclaringType().isInterface())
					return null;

				CompilationUnit astNode = AstUtil.getAstNode(compilationUnit);
				astNode.recordModifications();
				final MapperMethod mapperMethod = getMapperMethod(astNode, method);
				if (mapperMethod == null)
					return null;

				List<IJavaCompletionProposal> proposals = new ArrayList<IJavaCompletionProposal>();
				if (method.getParameters().length > 0) {
					proposals.add(new CopyParamQuickAssist("Copy @Param statement to clipboard", mapperMethod, astNode));
				}
				return proposals.toArray(new IJavaCompletionProposal[proposals.size()]);
			}
		}
		return null;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public boolean hasAssists(IInvocationContext arg0) throws CoreException {
		return false;
	}

	private MapperMethod getMapperMethod(ASTNode node, IMethod method) {
		if (method == null)
			return null;

		QuickAssistMapperMethodVisitor visitor = new QuickAssistMapperMethodVisitor(method);
		node.accept(visitor);
		return visitor.getMapperMethod();
	}

}
