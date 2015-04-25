package jcop.generation.compositelayer;

import AST.CompositeLayerDeclaration;
import AST.CompositeMethodDecl;
import AST.Expr;
import AST.List;
import AST.MethodAccess;
import AST.MethodDecl;
import AST.ParameterDeclaration;
import AST.VarAccess;
import jcop.Globals;
import jcop.Globals.ID;

public class RootLayerClassGenerator extends LayerClassGenerator {
	//private OpenCompositeLayerDecl compositeLayer;
	
	public RootLayerClassGenerator(CompositeLayerDeclaration compositeLayer) {
		super(compositeLayer);
	}
	/**
	 * generate {@link MethodDecl} named {@code <generatedMethodName>} for base
	 * method named {@code <originalMethodWrapperName>}
	 * 
	 * @param compositeMethod
	 * @param generatedMethodName
	 * @param originalMethodWrapperName
	 * @return
	 */
	public MethodDecl genDelegationMethod(CompositeMethodDecl compositeMethod,
			String generatedMethodName, String originalMethodWrapperName) {
		List<ParameterDeclaration> originalParams = compositeMethod
				.getParameterList();
		Expr callToOriginalMethod = generateCallToBaseMethod(
				originalMethodWrapperName, originalParams);
		MethodDecl method = super.generateDelegationMethod(compositeMethod,
				generatedMethodName, callToOriginalMethod);
		return method;
	}
	
	/**
	 * generate {@link Expr} of call to base method.
	 * 
	 * <pre>
	 * <code>
	 *   __target__.{@code <id>}({@code <orignalParams>})
	 * </code>
	 * </pre>
	 * 
	 * @param id
	 * @param originalParams
	 * @return
	 */
	private Expr generateCallToBaseMethod(String id,
			List<ParameterDeclaration> originalParams) {
		return new VarAccess(ID.targetParameterName)
				.qualifiesAccess(new MethodAccess(id,
						generateArgs(originalParams)));
	}
	
	public MethodDecl genDelegationMethodToSuperLayer(
			CompositeMethodDecl compositeMethod, String genMethodName) {
		// List<Expr> args =
		// generateArgsFromLayeredMethod(originalMethodDecl.getParameterList());
		String methodName = Globals.ID.superlayer + genMethodName;
		Expr methodBody = generateCallToBaseMethod(genMethodName,
				compositeMethod.getParameterList());
		MethodDecl method = generateDelegationMethod(compositeMethod,
				methodName, methodBody);
		return method;
	}
}
