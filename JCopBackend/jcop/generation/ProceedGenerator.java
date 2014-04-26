package jcop.generation;

import jcop.Globals;
import jcop.Globals.ID;
import jcop.Globals.Types;
import jcop.generation.layermembers.LayeredMethodGenerator;
import AST.Access;
import AST.AddExpr;
import AST.ArrayAccess;
import AST.ClassInstanceExpr;
import AST.Expr;
import AST.IntegerLiteral;
import AST.List;
import AST.MethodDecl;
import AST.NullLiteral;
import AST.ParameterDeclaration;
import AST.ProceedExpr;
import AST.Stmt;
import AST.SuperAccess;
import AST.ThisAccess;
import AST.TypeAccess;
import AST.TypeDecl;
import AST.VarAccess;
import AST.VariableDeclaration;

/**
 * Documented by wander
 * 
 * <pre>
 * generate constructs for {@link ProceedExpr}
 * </pre>
 * 
 */
public class ProceedGenerator extends Generator {
	private ProceedExpr proceed;
	private MethodDecl enclosingPmd;
	private List<Expr> pmdArgs;

	public ProceedGenerator(ProceedExpr proceed) {
		this.proceed = proceed;
		this.enclosingPmd = (MethodDecl) proceed.enclosingBodyDecl();
		this.pmdArgs = generateArgsFromOriginalParams(enclosingPmd
				.getParameterList());
	}

	private List<Expr> generateArgsFromOriginalParams(
			List<ParameterDeclaration> parameterList) {
		List<ParameterDeclaration> newParams = (List<ParameterDeclaration>) parameterList
				.fullCopy();
		newParams = removeLookupParams(newParams);
		return generateArgs(newParams);
	}

	private List<ParameterDeclaration> removeLookupParams(
			List<ParameterDeclaration> newParams) {
		newParams.removeChild(0);
		newParams.removeChild(0);
		newParams.removeChild(0);
		return newParams;
	}

	/**
	 * generate {@link Access} for {@link ProceedExpr}
	 * 
	 * <pre>
	 * <code>
	 * jcop.lang.JCop.current().next(__proxy__).get().{@code <methodName>}({@code <mergedArgs>})
	 * </code>
	 * is replaced by
	 * __arr__[__current__+1].get().{@code <methodName>}({@code <mergedArgs>})
	 * WANDER we should check constrain that __current__+1 < __arr__.length
	 * </pre>
	 * 
	 * @return
	 */
	public Access generateAccess() {
		LayeredMethodGenerator gen = new LayeredMethodGenerator(enclosingPmd);
		String methodName = gen.generateDelegationMethodName(enclosingPmd);
		// List<Expr> enclMethodArgs =
		// getArgsWithObjectAndCompositionReference(createNextLayerMethodAccess());
		List<Expr> enclMethodArgs = getArgsWithObjectAndCompositionReference(null);
		List<Expr> mergedArgs = mergeProceedArgs(enclMethodArgs,
				proceed.getArgList());
		// return createNextLayerMethodAccess().qualifiesAccess(
		// createMethodAccess("get").qualifiesAccess(
		// createMethodAccess(methodName, mergedArgs)));
		Expr curIncExpr = new AddExpr(
				new VarAccess(ID.wander_CurrentLayerIndex), new IntegerLiteral(
						1));
		return new VarAccess(ID.wander_Composition)
				.qualifiesAccess(new ArrayAccess(curIncExpr))
				.qualifiesAccess(
						createMethodAccess(ID.wander_LayerProxyToLayerMethodName))
				.qualifiesAccess(createMethodAccess(methodName, mergedArgs));
	}

	/**
	 * generate {@link Access} of super for {@link ProceedExpr}
	 * 
	 * <pre>
	 * <code>
	 * thislayer.__superlayer__{@code <delegationMethod>}(__target__, {@code <otherArgs>}, {@code <proceedArgs>})
	 * </code>
	 * or
	 * <code>
	 * thislayer.__superlayer__{@code <delegationMethod>}({@code <otherArgs>}, {@code <proceedArgs>})
	 * </code>
	 * </pre>
	 * 
	 * 
	 * @return
	 */
	public Access generateSuperAccess() {
		LayeredMethodGenerator gen = new LayeredMethodGenerator(enclosingPmd);
		String methodName = Globals.ID.superlayer
				+ gen.generateDelegationMethodName(enclosingPmd);
		// List<Expr> enclMethodArgs =
		// getArgsWithObjectAndCompositionReference(new VarAccess(
		// ID.layerProxyParameterName));
		List<Expr> enclMethodArgs = getArgsWithObjectAndCompositionReference(null);
		List<Expr> mergedArgs = mergeProceedArgs(enclMethodArgs,
				proceed.getArgList());
		return new VarAccess(Globals.ID.layerParameterName)
				.qualifiesAccess(createMethodAccess(methodName, mergedArgs));

	}

	private List<Expr> mergeProceedArgs(List<Expr> enclMethodArgs,
			List<Expr> proceedArgs) {
		int lookupObjectOffset = 3;
		List<Expr> mergedArgs = enclMethodArgs.fullCopy();
		for (int i = 0; i < proceedArgs.getNumChild(); i++) {
			mergedArgs
					.setChild(proceedArgs.getChild(i), i + lookupObjectOffset);
		}
		return mergedArgs;
	}

	/**
	 * generate {@link Expr} of access to next layer method
	 * 
	 * <pre>
	 * <code>
	 * jcop.lang.JCop.current().next(__proxy__)
	 * </code>
	 * </pre>
	 * 
	 * @deprecated in new-feature
	 * @return
	 */
	private Expr createNextLayerMethodAccess() {
		return createCurrentCompositionAccess().qualifiesAccess(
				createMethodAccess(ID.nextLayer, generateLayerProxyAccess()));
	}

	/**
	 * @deprecated in new-feature
	 * @return
	 */
	private Access generateLayerProxyAccess() {
		return new VarAccess(Globals.ID.layerProxyParameterName);
	}

	/**
	 * generate list of {@link Expr}
	 * 
	 * @param layerProxyExpr
	 *            this param is not used
	 * @return
	 */
	private List<Expr> getArgsWithObjectAndCompositionReference(
			Expr layerProxyExpr) {
		List<Expr> finalArgs = createArgsWithThisOrNullReference();

		// finalArgs.addChild(layerProxyExpr);
		// finalArgs.addChild(genCompositionReference());
		finalArgs.addChild(new AddExpr(new VarAccess(
				ID.wander_CurrentLayerIndex), new IntegerLiteral(1)));
		finalArgs.addChild(new VarAccess(ID.wander_Composition));

		for (Expr arg : pmdArgs)
			finalArgs.add(arg);
		return finalArgs;
	}

	/**
	 * generate list of {@link Expr} containing this or null depending on
	 * whether {@code <enclosingPmd>} is static
	 * 
	 * @return
	 */
	private List<Expr> createArgsWithThisOrNullReference() {
		List<Expr> args = new List<Expr>();

		if (!enclosingPmd.isStatic())
			args.addChild(new ThisAccess());
		else
			args.addChild(new NullLiteral("null"));
		return args;
	}

	/*
	 * full qualified partial methods are not transformed but replaced by an
	 * advice thus the original (non-transformed) method does not already
	 * contain the Composition parameter
	 */
	/**
	 * FIXME wander: not used
	 * 
	 * @param args
	 * @param paramID
	 * @return
	 */
	private boolean containsReference(List<Expr> args, String paramID) {
		for (int i = 0; i < args.getNumChild(); i++) {
			VarAccess arg = (VarAccess) args.getChild(i);
			if (arg.getID().equals(paramID))
				return true;
		}
		return false;
	}

	private Expr genCompositionReference() {
		return new VarAccess(Globals.ID.composition);
	}

	/**
	 * FIXME wander: not used
	 * 
	 * @return
	 */
	private Expr getClassInstanceExpr() {
		TypeDecl host = enclosingPmd.hostType();
		return new ClassInstanceExpr(new TypeAccess(host.packageName(),
				host.getID()), new List<ParameterDeclaration>());
	}

}
