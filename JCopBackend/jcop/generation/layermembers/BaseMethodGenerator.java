package jcop.generation.layermembers;

import jcop.Globals;
import jcop.Globals.ID;
import jcop.Globals.Msg;
import static jcop.Globals.Types.*;
import jcop.compiler.JCopTypes.JCopAccess;
import jcop.generation.RunTimeLoggingGenerator;
import AST.Access;
import AST.Annotation;
import AST.Block;
import AST.CatchClause;
import AST.ClassInstanceExpr;
import AST.Dot;
import AST.ElementValuePair;
import AST.Expr;
import AST.ExprStmt;
import AST.List;
import AST.MethodAccess;
import AST.MethodDecl;
import AST.Modifiers;
import AST.Opt;
import AST.ReturnStmt;
import AST.Stmt;
import AST.StringLiteral;
import AST.SuperAccess;
import AST.ThisAccess;
import AST.ThrowStmt;
import AST.TryStmt;
import AST.TypeAccess;
import AST.TypeDecl;
import AST.VarAccess;
import AST.VariableDeclaration;

/**
 * Documented by wander,
 * 
 * {@link jcop.transformation.PartialMethodSourceTransformer
 * PartialMethodSourceTransformer} helper generator for base method
 * 
 * <pre>
 * {@link jcop.generation.RunTimeLoggingGenerator RunTimeLoggingGenerator} is used for generating runtime logging constructs.
 * </pre>
 * 
 * <pre>
 * jcop specification for base method definition.
 * </pre>
 */
public class BaseMethodGenerator extends LayeredMethodGenerator {
	private RunTimeLoggingGenerator logGenerator;

	public BaseMethodGenerator(MethodDecl baseMethod) {
		super(null, baseMethod);
		logGenerator = RunTimeLoggingGenerator.getInstance();
	}

	public BaseMethodGenerator(MethodDecl partialMethod, MethodDecl baseMethod) {
		super(partialMethod, baseMethod);
		logGenerator = RunTimeLoggingGenerator.getInstance();
	}

	/**
	 * generate {@link AST.Block Block} for layer activation
	 * 
	 * <pre>
	 * <code>
	 *   {@code <possibleLogging>}
	 *   jcop.lang.Composition __composition__ = jcop.lang.JCop.current();
	 *   try{
	 *     __composition__.firstLayer().get().{@code <delegationMethodName>}
	 *   (<targetArgs>, __composition__.firstLayer(), __composition__, {@code <oldArgs>});
	 *   }
	 *   finally {
	 *     jcop.lang.JCop.setComposition(__composition__);
	 *   }
	 * </code>
	 * </pre>
	 * 
	 * @return
	 */
	public Block generateLayerActivationBlock() {
		VariableDeclaration current = generateVarForCurrentCompostion(); // generateVarForOldCompostion();
		// generateVarForOldCompostion();
		// VariableDeclaration current = generateVarForCurrentCompostion();
		// ExprStmt activateContexts = activateContexts();
		Block block = createStmtBlock(current,
				createTryBlock(createFirstLayerAccess()));
		return logGenerator.genLayeredMethodBlock(block,
				baseMethod.getFullQualifiedName());
		// return block;
	}

	/**
	 * generate {@link AST.MethodDecl MethodDecl} for base method
	 * 
	 * @param requiresSuperCall
	 * @return
	 */
	public MethodDecl generateBaseMethod(boolean requiresSuperCall) {
		Stmt exception = generateBaseMethodBodyStatement(requiresSuperCall);
		MethodDecl defaultMethod = new MethodDecl(partialMethod.getModifiers()
				.fullCopy(),
				(Access) (partialMethod.getTypeAccess().fullCopy()),
				// baseMethod.type().createBoundAccess(),
				partialMethod.getID(),
				partialMethod.getParameters().fullCopy(), partialMethod
						.getExceptionList().fullCopy(), genOptBlock(exception));
		return defaultMethod;
	}

	/**
	 * generate {@link AST.Stmt Stmt} for super call in base method body. if
	 * requiresSuperCall is true, generate 'return' stmt. otherwise, generate
	 * {@link AST.Stmt Stmt} for {@link jcop.lang.InvalidMethodAccessException
	 * InvalidMethodAccessException}
	 * 
	 * @param requiresSuperCall
	 * @return
	 */
	private Stmt generateBaseMethodBodyStatement(boolean requiresSuperCall) {
		if (requiresSuperCall)
			return maybeGenerateReturnStmt(baseMethod, generateSuperCall());
		else
			return generateThrowsException(new TypeAccess(Globals.jcopPackage,
					InvalidMethodAccessException));

	}

	/**
	 * generate {@link AST.Expr Expr} for super call
	 * 
	 * <pre>
	 * <code>
	 *   super.{@code <baseMethodId>}({@code <partialMethodArgs>});
	 * </code>
	 * </pre>
	 * 
	 * @return
	 */
	private Expr generateSuperCall() {
		return new SuperAccess()
				.qualifiesAccess(createMethodAccess(baseMethod.getID(),
						generateArgs(partialMethod.getParameters())));
	}

	/**
	 * generate {@link AST.MethodDecl MethodDecl} for wrapper method(base
	 * method). block of logging is inserted into the method body depending on
	 * option '-rtl'
	 * 
	 * <pre>
	 * <code>
	 *   public {@code @}jcop.lang.BaseMethod __wrap__{@code <hostClassId>}$$${@code baseMethodId>}({@code <args>}) 
	 *   {@code <excetionList>} {
	 *     {@code <loggedBlock>}(optional)
	 *   }
	 * </code>
	 * </pre>
	 * 
	 * @return
	 */
	public MethodDecl generateWrapper() {
		Block loggedBlock = logGenerator.genBaseMethodBlock(
				baseMethod.getBlock(), baseMethod.getFullQualifiedName());
		Modifiers modif = createPublicModifierFor(baseMethod);
		modif.addModifier(genAnnotation(BASE_METHOD_ANNOTATION));
		MethodDecl newMethod = new MethodDecl(modif,
				// .createBoundAccess won't work due to side effects
				(Access) baseMethod.getTypeAccess().fullCopy(),
				// baseMethod.type().createBoundAccess(),
				genWrapperIdentifier(), baseMethod.getParameters().fullCopy(),
				baseMethod.getExceptionList().fullCopy(), new Opt<Block>(
						loggedBlock));
		// System.err.println(baseMethod.getBlock());
		return newMethod;
	}

	/**
	 * generate {@link AST.Block Block} for {@link AST.TryStmt TryStmt}
	 * enclosing toBeWrapped.
	 * 
	 * @param toBeWrapped
	 * @return
	 */
	private Stmt createTryBlock(Expr toBeWrapped) {
		Block tryBlock = new Block();
		tryBlock.addStmt(baseMethod.isVoid() ? new ExprStmt(toBeWrapped)
				: new ReturnStmt(toBeWrapped));
		TryStmt tryStmt = new TryStmt(tryBlock, new List<CatchClause>(),
				createFinallyBlockForForwardingMethod());
		return tryStmt;
	}

	/**
	 * generate {@link AST.Expr Expr} for first layer access in the composition.
	 * 
	 * <pre>
	 * <code>
	 * __composition__.firstLayer().get().{@code <delegationMethodName>}
	 * (<targetArgs>, __composition__.firstLayer(), __composition__, {@code <oldArgs>});
	 * </code>
	 * </pre>
	 * replaced with
	 * <pre>
	 * <code>
	 * __composition__.firstLayer().get(this).{@code <delegationMethodName>}
	 * (<targetArgs>, __composition__.firstLayer(), __composition__, {@code <oldArgs>});
	 * </code>
	 * </pre>
	 * @return
	 */
	private Expr createFirstLayerAccess() {
		String delegationMethodName = generateDelegationMethodName(partialMethod);
		List<Expr> args = createArgsForCallToLayeredMethod();
		Dot concreteLayerWithParams = new VarAccess(ID.composition)
				.qualifiesAccess(createMethodAccess(ID.firstLayer)
						.qualifiesAccess(
								createMethodAccess("get", new ThisAccess()).qualifiesAccess(
										createMethodAccess(
												delegationMethodName, args))));
		return concreteLayerWithParams;
	}

	/**
	 * generate {@link AST.Opt {@code Opt<Block>} for {@link AST.TryStmt
	 * TryStmt}, that is finally block
	 * 
	 * <pre>
	 * <code>
	 *   finally{
	 *     jcop.lang.JCop.setComposition(__composition__);
	 *   }
	 * </code>
	 * </pre>
	 * 
	 * @return
	 */
	private Opt<Block> createFinallyBlockForForwardingMethod() {
		Access setComposition = JCopAccess.get(JCOP).qualifiesAccess(
				createMethodAccess(ID.setComposition, new VarAccess(
						ID.composition)));
		return genOptBlock(new ExprStmt(setComposition));
	}

	// private VariableDeclaration generateVarForOldCompostion() {
	// Expr lhs =
	// createCurrentCompositionAccess().qualifiesAccess(createAddLayerAccess());
	// return new VariableDeclaration( JCopAccess.get(COMPOSITION),
	// ID.oldComposition, lhs);
	// }

	// private Access createAddLayerAccess() {
	// return createMethodAccess(ID.addLayer, createImplicitActivationAccess());
	// }

	// private Expr createImplicitActivationAccess() {
	// String baseMethodFQN = createFullQualifiedSignature(baseMethod);
	// return JCopAccess.get(COMPOSITION).qualifiesAccess(
	// createMethodAccess(
	// ID.implicitlyActivatedLayers,
	// new StringLiteral(baseMethodFQN),
	// createTargetArgsForCallToLayeredMethod(baseMethod)));
	// }
	/**
	 * generate {@link AST.VariableDeclaration VariableDeclaration} for current
	 * composition.
	 * 
	 * <pre>
	 * <code>
	 * jcop.lang.Composition __composition__ = jcop.lang.JCop.current();
	 * </code>
	 * </pre>
	 * 
	 * @return
	 */
	private VariableDeclaration generateVarForCurrentCompostion() {
		return new VariableDeclaration(JCopAccess.get(COMPOSITION),
				ID.composition, createCurrentCompositionAccess());
	}

	/**
	 * generate list of {@link AST.Expr Expr} for args of layered method
	 * 
	 * <pre>
	 * <code>
	 * [{@code <target>}, __composition__.firstLayer(), __composition__, {@code <oldArgs>}]
	 * </code>
	 * <target> can be {@link AST.ClassInstanceExpr ClassInstanceExpr} or {@link AST.ThisAccess ThisAccess}
	 * depending on method is static or not
	 * </pre>
	 * 
	 * @return
	 */
	private List<Expr> createArgsForCallToLayeredMethod() {
		List<Expr> args = generateArgs(baseMethod.getParameterList());
		args.insertChild(createTargetArgsForCallToLayeredMethod(baseMethod), 0);
		args.insertChild(new VarAccess(ID.composition)
				.qualifiesAccess(createMethodAccess(ID.firstLayer)), 1);
		args.insertChild(new VarAccess(ID.composition), 2);
		return args;
	}

	/**
	 * generate {@link AST.Expr Expr} for first arg of call layered method
	 * 
	 * <pre>
	 * jcop specification for layered method.
	 * </pre>
	 * 
	 * @param method
	 * @return
	 */
	private Expr createTargetArgsForCallToLayeredMethod(MethodDecl method) {
		if (method.isStatic())
			return new ClassInstanceExpr(createHostTypeAccessFor(method),
					new List<Expr>());
		else
			return new ThisAccess();
	}

	private Access createHostTypeAccessFor(MethodDecl method) {
		TypeDecl type = method.hostType();
		return new TypeAccess(type.packageName(), type.getID());
	}

	/**
	 * return base method name
	 * 
	 * <pre>
	 * <code>
	 * __wrap__{@code<baseMethodId>}$$${@code <baseMethodName>}
	 * </code>
	 * </pre>
	 * 
	 * @return
	 */
	public String genWrapperIdentifier() {
		return genWrapperIdentifierPrefix() + baseMethod.name();
	}

	/**
	 * return base method signature
	 * 
	 * @return
	 */
	public String genWrapperSignature() {
		return genWrapperIdentifierPrefix() + baseMethod.signature();
	}

	/**
	 * return the following string:
	 * 
	 * <pre>
	 * <code>
	 * __wrap__{@code<hostClassId>}$$$
	 * </code>
	 * </pre>
	 * 
	 * @return
	 */
	private String genWrapperIdentifierPrefix() {
		return ID.wrappedMethodPrefix + baseMethod.hostType().getID() + "$$$";
	}

}
