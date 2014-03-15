/**
 * 
 */
package jcop.generation.layers;

import static jcop.Globals.Types.InvalidMethodAccessException;
import jcop.Globals;
import jcop.Globals.ID;
import AST.Access;
import AST.ArrayInit;
import AST.ArrayTypeAccess;
import AST.Expr;
import AST.FieldDeclaration;
import AST.LayerDeclaration;
import AST.List;
import AST.MethodAccess;
import AST.MethodDecl;
import AST.NullLiteral;
import AST.ParameterDeclaration;
import AST.ReturnStmt;
import AST.SingleTypeImportDecl;
import AST.SuperAccess;
import AST.TypeAccess;
import AST.VarAccess;
import AST.VariableDeclaration;

/**
 * Documented by wander,
 * 
 * <pre>
 * generate constructs for {@link jcop.lang.Layer Layer}
 * </pre>
 */
public class RootLayerClassGenerator extends LayerClassGenerator {
	public RootLayerClassGenerator(LayerDeclaration layer) {
		super(layer);
	}

	/**
	 * generate {@link MethodDecl} named {@code <generatedMethodName>} for base
	 * method named {@code <originalMethodWrapperName>}
	 * 
	 * @param originalMethodDecl
	 * @param generatedMethodName
	 * @param originalMethodWrapperName
	 * @return
	 */
	public MethodDecl genDelegationMethod(MethodDecl originalMethodDecl,
			String generatedMethodName, String originalMethodWrapperName) {
		List<ParameterDeclaration> originalParams = originalMethodDecl
				.getParameterList();
		Expr callToOriginalMethod = generateCallToBaseMethod(
				originalMethodWrapperName, originalParams);
		MethodDecl method = super.generateDelegationMethod(originalMethodDecl,
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

	/**
	 * generate delegation method named {@code <wrapperMethodName>}
	 * 
	 * @param baseFieldDecl
	 * @param wrapperMethodName
	 * @param wrapperFieldID
	 * @return
	 */
	public MethodDecl generateDelegationMethod(FieldDeclaration baseFieldDecl,
			String wrapperMethodName, String wrapperFieldID) {
		// String originalMethodWrapperName = Identifiers.wrappedMethodPrefix +
		// baseFieldDecl.name();
		Expr callToOriginalMethod = generateCallToBaseMethod(wrapperFieldID);

		MethodDecl method = super.createDelegationMethod(baseFieldDecl,
				wrapperMethodName, callToOriginalMethod);
		// addDelegationMethodToLayer(method, Identifiers.Layer);cr
		return method;
	}

	/**
	 * generate {@link Expr} for call to base method FIXME wander: something
	 * wrong here
	 * 
	 * <pre>
	 * <code>
	 *   __target__.{@code <id>}
	 * </code>
	 * </pre>
	 * 
	 * @param id
	 * @return
	 */
	private Expr generateCallToBaseMethod(String id) {
		return new VarAccess(ID.targetParameterName)
				.qualifiesAccess(createMethodAccess(id));
	}

	/**
	 * {@inheritDoc}
	 */
	public MethodDecl genDelegationMethodToSuperLayer(
			MethodDecl originalMethodDecl, String genMethodName) {
		// List<Expr> args =
		// generateArgsFromLayeredMethod(originalMethodDecl.getParameterList());
		String methodName = Globals.ID.superlayer + genMethodName;
		Expr methodBody = generateCallToBaseMethod(genMethodName,
				originalMethodDecl.getParameterList());
		MethodDecl method = generateDelegationMethod(originalMethodDecl,
				methodName, methodBody);
		return method;
	}

	// protected List<ParameterDeclaration> getTransformedParamsFor(MethodDecl
	// baseMethodDecl) {
	// return baseMethodDecl.getParameterList();
	// }

}