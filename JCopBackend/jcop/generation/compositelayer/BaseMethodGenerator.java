package jcop.generation.compositelayer;

import static jcop.Globals.Types.*;
import AST.Access;
import AST.Block;
import AST.CatchClause;
import AST.ClassInstanceExpr;
import AST.CompositeMethodDecl;
import AST.Dot;
import AST.Expr;
import AST.ExprStmt;
import AST.List;
import AST.MethodDecl;
import AST.Modifiers;
import AST.Opt;
import AST.ReturnStmt;
import AST.Stmt;
import AST.SuperAccess;
import AST.ThisAccess;
import AST.TryStmt;
import AST.TypeAccess;
import AST.TypeDecl;
import AST.VarAccess;
import AST.VariableDeclaration;
import jcop.Globals;
import jcop.Globals.ID;
import jcop.compiler.JCopTypes.JCopAccess;
import jcop.generation.Generator;

public class BaseMethodGenerator extends Generator {

	private MethodDecl baseMethod;
	private CompositeMethodDecl compositeMethod;

	public BaseMethodGenerator(CompositeMethodDecl compositeMethod,
			MethodDecl baseMethod) {
		this.compositeMethod = compositeMethod;
		this.baseMethod = baseMethod;
	}

	public void setBaseMethod(MethodDecl baseMethod) {
		this.baseMethod = baseMethod;
	}

	/**
	 * generate {@link AST.MethodDecl MethodDecl} for base method
	 * 
	 * @param requiresSuperCall
	 * @return
	 */
	public MethodDecl generateBaseMethod(boolean requiresSuperCall) {
		Stmt exception = generateBaseMethodBodyStatement(requiresSuperCall);
		MethodDecl defaultMethod = new MethodDecl(compositeMethod
				.getModifiers().fullCopy(),
				(Access) (compositeMethod.getTypeAccess().fullCopy()),
				compositeMethod.getID(), compositeMethod.getParameters()
						.fullCopy(), compositeMethod.getExceptionList()
						.fullCopy(), genOptBlock(exception));
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
		return new SuperAccess().qualifiesAccess(createMethodAccess(
				baseMethod.getID(),
				generateArgs(compositeMethod.getParameters())));
	}

	/**
	 * generate {@link AST.MethodDecl MethodDecl} for wrapper method(base
	 * method).
	 * 
	 * <pre>
	 * <code>
	 *   public {@code @}jcop.lang.BaseMethod __wrap__{@code <hostClassId>}$$${@code baseMethodId>}({@code <args>}) 
	 *   {@code <excetionList>} {
	 *   }
	 * </code>
	 * </pre>
	 * 
	 * @return
	 */
	public MethodDecl generateWrapper() {
		Modifiers modif = createPublicModifierFor(baseMethod);
		modif.addModifier(genAnnotation(BASE_METHOD_ANNOTATION));
		MethodDecl newMethod = new MethodDecl(modif, (Access) baseMethod
				.getTypeAccess().fullCopy(), genWrapperIdentifier(), baseMethod
				.getParameters().fullCopy(), baseMethod.getExceptionList()
				.fullCopy(), new Opt<Block>(baseMethod.getBlock()));
		return newMethod;
	}

	/**
	 * generate {@link AST.Block Block} for layer activation
	 * 
	 * <pre>
	 * <code>
	 *   {@code <possibleLogging>}
	 *   jcop.lang.Composition __composition__ = jcop.lang.JCop.current();
	 *   try{
	 *     __composition__.firstLayer().get(this).{@code <delegationMethodName>}
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
		Block block = createStmtBlock(current,
				createTryBlock(createFirstLayerAccess()));
		return block;
	}
	
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
	
	/**
	 * generate {@link AST.Expr Expr} for first layer access in the composition.
	 * 
	 * <pre>
	 * <code>
	 * __composition__.firstLayer().get(this).{@code <delegationMethodName>}
	 * (<targetArgs>, __composition__.firstLayer(), __composition__, {@code <oldArgs>});
	 * </code>
	 * </pre>
	 * @return
	 */
	private Expr createFirstLayerAccess() {
		String delegationMethodName = generateDelegationMethodName(compositeMethod);
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
	 * generate delegation method name of method
	 * 
	 * <pre>
	 *   jcop specification for delegation method
	 * </pre>
	 * 
	 * @param method
	 * @return
	 */
	public String generateDelegationMethodName(CompositeMethodDecl method) {
		TypeDecl host = method.hostType().topLevelType();
		String delimiter = ID.generatedMethodNameDelimiter;
		StringBuffer generatedMethodName = new StringBuffer()
				.append(host.packageName().replace(".", delimiter))
				.append(delimiter).append(host.getID()).append(delimiter)
				.append(method.getID());
		return generatedMethodName.toString();
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
