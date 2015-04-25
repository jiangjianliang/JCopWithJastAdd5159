package jcop.generation.compositelayer;

import AST.CompositeLayerDeclaration;
import AST.CompositeMethodDecl;
import AST.Expr;
import AST.List;
import AST.MethodDecl;
import AST.ParameterDeclaration;
import AST.VarAccess;
import jcop.Globals.ID;

public class ConcreteLayerClassGenerator extends LayerClassGenerator {

	public ConcreteLayerClassGenerator(CompositeLayerDeclaration compositeLayer) {
		super(compositeLayer);
	}

	/**
	 * generate {@link MethodDecl} named {@code <generatedName>} for base method
	 * named {@code <baseMethodDecl>}
	 * 
	 * @param compositeMethod
	 * @param generatedMethodName
	 * @return
	 */
	public MethodDecl genDelegationMethod(CompositeMethodDecl compositeMethod,
			String generatedMethodName) {
		Expr delegation = genDelegationExpr(
				compositeMethod.getParameterListNoTransform(),
				generatedMethodName);
		MethodDecl method = super.generateDelegationMethod(compositeMethod,
				generatedMethodName, delegation);
		return method;
	}
	
	/**
	 * generate {@link Expr} for delegation
	 * 
	 * @param originalParams
	 * @param genMethodName
	 * @return
	 */
	private Expr genDelegationExpr(List<ParameterDeclaration> originalParams,
			String genMethodName) {
		List<Expr> argsWithTarget = genLayerParams(generateArgs(originalParams));
		Expr delegation = genCallToNextLayerExpression(genMethodName,
				argsWithTarget);
		return delegation;
	}
	
	
	/**
	 * add three Expr to {@code <toBeExtended>}
	 * 
	 * <pre>
	 * <code>
	 *  __target__, __composition__.nextLayer(__proxy__), __compostion__, {@code <otherExpr>}
	 * </code>
	 * </pre>
	 * 
	 * @param toBeExtended
	 * @return
	 */
	protected List<Expr> genLayerParams(List<Expr> toBeExtended) {
		toBeExtended.insertChild(new VarAccess(ID.targetParameterName), 0);
		toBeExtended.insertChild(genNextLayerProxyAccess(), 1);
		toBeExtended.insertChild(new VarAccess(ID.composition), 2);
		return toBeExtended;
	}

	/**
	 * <pre>
	 * <code>
	 *   __composition__.nextLayer(__proxy__).get().{@code <generatedName>}({@code <argsWithTarget>})
	 * </code>
	 * </pre>
	 * replaced with
	 * <pre>
	 * <code>
	 *   __composition__.nextLayer(__proxy__).get(target).{@code <generatedName>}({@code <argsWithTarget>})
	 * </code>
	 * </pre>
	 * @param generatedMethodName
	 * @param argsWithTarget
	 * @return
	 */
	private Expr genCallToNextLayerExpression(String generatedMethodName,
			List<? extends Expr> argsWithTarget) {
		return genNextLayerProxyAccess().qualifiesAccess(
				createMethodAccess("get", new VarAccess(ID.targetParameterName)).qualifiesAccess(
						createMethodAccess(generatedMethodName,
								(List<Expr>) argsWithTarget)));
	}

	/**
	 * <pre>
	 * <code>
	 *  __composition__.nextLayer(__proxy__)
	 * </code>
	 * </pre>
	 * 
	 * @return
	 */
	private Expr genNextLayerProxyAccess() {
		return new VarAccess(ID.composition)
				.qualifiesAccess(createMethodAccess(ID.nextLayer,
						new VarAccess(ID.layerProxyParameterName)));
	}
}
