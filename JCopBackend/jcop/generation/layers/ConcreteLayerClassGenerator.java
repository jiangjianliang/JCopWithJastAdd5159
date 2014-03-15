/**
 * 
 */
package jcop.generation.layers;

import jcop.Globals.ID;
import AST.Expr;
import AST.FieldDeclaration;
import AST.LayerDeclaration;
import AST.List;
import AST.MethodDecl;
import AST.ParameterDeclaration;
import AST.ThisAccess;
import AST.VarAccess;

/**
 * Documented by wander,
 * 
 * <pre>
 * generate constructs for {@link jcop.lang.ConcreteLayer ConcreteLayer} of {@link LayerDeclaration}
 * </pre>
 * 
 */
public class ConcreteLayerClassGenerator extends LayerClassGenerator {

	public ConcreteLayerClassGenerator(LayerDeclaration layer) {
		super(layer);
	}

	/**
	 * generate {@link MethodDecl} named {@code <generatedName>} for base method
	 * named {@code <baseMethodDecl>}
	 * 
	 * @param baseMethodDecl
	 * @param generatedMethodName
	 * @return
	 */
	public MethodDecl genDelegationMethod(MethodDecl baseMethodDecl,
			String generatedMethodName) {
		Expr delegation = genDelegationExpr(
				baseMethodDecl.getParameterListNoTransform(),
				generatedMethodName);
		MethodDecl method = super.generateDelegationMethod(baseMethodDecl,
				generatedMethodName, delegation);
		return method;
	}

	/**
	 * generate {@link MethodDecl} enclosing {@code toBeWrapped} of
	 * {@link FieldDeclaration}
	 * 
	 * @param toBeWrapped
	 * @param generatedMethodName
	 * @return
	 */
	public MethodDecl genDelegationMethod(FieldDeclaration toBeWrapped,
			String generatedMethodName) {
		Expr delegation = genDelegationExpr(generatedMethodName);
		MethodDecl method = super.createDelegationMethod(toBeWrapped,
				generatedMethodName, delegation);
		return method;
	}

	private Expr genDelegationExpr(String genMethodName) {
		return genDelegationExpr(new List<ParameterDeclaration>(),
				genMethodName);
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
	 * 
	 * @param generatedMethodName
	 * @param argsWithTarget
	 * @return
	 */
	private Expr genCallToNextLayerExpression(String generatedMethodName,
			List<? extends Expr> argsWithTarget) {
		return genNextLayerProxyAccess().qualifiesAccess(
				createMethodAccess("get").qualifiesAccess(
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