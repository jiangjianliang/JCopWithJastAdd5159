package jcop.generation.layermembers;

import static jcop.Globals.Modifiers.AFTER;
import static jcop.Globals.Modifiers.AFTER_ANNOT;
import static jcop.Globals.Modifiers.BEFORE;
import static jcop.Globals.Modifiers.BEFORE_ANNOT;
import static jcop.Globals.Types.PARTIAL_METHOD_ANNOTATION;
import jcop.Globals;
import jcop.Globals.ID;
import jcop.Globals.Types;
import jcop.compiler.JCopTypes.JCopAccess;
import jcop.generation.RunTimeLoggingGenerator;
import AST.Access;
import AST.ArrayTypeAccess;
import AST.Block;
import AST.CatchClause;
import AST.Expr;
import AST.ExprStmt;
import AST.LayerDeclaration;
import AST.List;
import AST.MethodDecl;
import AST.Modifiers;
import AST.Opt;
import AST.ParameterDeclaration;
import AST.PartialMethodGroupDecl;
import AST.ProceedExpr;
import AST.ReturnStmt;
import AST.Stmt;
import AST.TryStmt;
import AST.TypeAccess;
import AST.VarAccess;
import AST.VariableDeclaration;

/**
 * <pre>
 * generate constructs for partial method group of
 * {@link AST.MethodDecl}
 * 
 * <pre>
 * @author wander
 * 
 */
public class PartialMethodGroupGenerator extends LayeredMethodGenerator {

	public PartialMethodGroupGenerator(LayerDeclaration openLayer,
			MethodDecl partialMethodGroup) {
		// TODO openLayer is not used
		super(partialMethodGroup);
	}

	public String genDelegationMethodName(MethodDecl method) {
		return generateDelegationMethodName(method);
	}

	/**
	 * AAA()
	 * AAA1()
	 * AAA2()
	 * @return
	 */
	public java.util.List generatePartialMethodGroup() {
		java.util.List<MethodDecl> methodList = new java.util.ArrayList<MethodDecl>();
		int numOfPartialMethod = ((PartialMethodGroupDecl) this.partialMethod)
				.getNumBodyGroup();
		for (int i = 0; i < numOfPartialMethod; i++) {
			// WANDER 6-17 这里未必正确
			MethodDecl partialMethod = this.partialMethod;
			Modifiers modif = createPublicModifierFor(partialMethod);
			modif.addModifier(genAnnotation(PARTIAL_METHOD_ANNOTATION));
			MethodDecl newMethod;
			if (i == 0) {
				newMethod = new MethodDecl(modif, (Access) partialMethod
						.getTypeAccess().fullCopy(), partialMethod.getID(),
						genLayerParams(partialMethod), partialMethod
								.getExceptionList().fullCopy(),
						genPartialMethodBlock(i));
			} else {
				newMethod = new MethodDecl(modif, (Access) partialMethod
						.getTypeAccess().fullCopy(), partialMethod.getID()
						+ String.valueOf(i), genLayerParams(partialMethod),
						partialMethod.getExceptionList().fullCopy(),
						genPartialMethodBlock(i));
			}

			methodList.add(newMethod);
		}
		return methodList;
	}

	/**
	 * <p>
	 * generate list of {@link ParameterDeclaration} for decl
	 * </p>
	 * WANDER
	 * 
	 * @param decl
	 * @return
	 */
	private List<ParameterDeclaration> genLayerParams(MethodDecl decl) {
		TypeAccess concreteLayerAccess = JCopAccess.getLayerType(getLayer());
		List<ParameterDeclaration> params = decl.getParameterList().fullCopy();
		params.insertChild(new ParameterDeclaration(concreteLayerAccess,
				ID.layerParameterName), 0);
		params.insertChild(new ParameterDeclaration(createCurrentIndexAccess(),
				ID.wander_CurrentLayerIndex), 1);
		params.insertChild(new ParameterDeclaration(createListAccess(),
				ID.wander_Composition), 2);
		return params;
	}

	// begin new-feature
	private Access createCurrentIndexAccess() {
		return new TypeAccess("int");
	}

	private Access createListAccess() {
		return new ArrayTypeAccess(new TypeAccess(Globals.jcopPackage,
				Types.LAYER_PROXY));
	}

	// end new-feature

	/**
	 * generate {@link Opt} for partial method, logging block may be included
	 * 
	 * @param i
	 * @return
	 */
	public Opt<Block> genPartialMethodBlock(int i) {
		RunTimeLoggingGenerator log = RunTimeLoggingGenerator.getInstance();
		Block block = log.createBlockWithLoggingMessage(partialMethod,
				getLayer().getID());
		return new Opt<Block>(genBlockWithBeforeAfterSemantics(partialMethod,
				block, i));
	}

	/**
	 * generate {@code Block} for before/after semantics
	 * 
	 * @param partialMethod
	 * @param block
	 * @param i
	 * @return
	 */
	private Block genBlockWithBeforeAfterSemantics(MethodDecl partialMethod,
			Block block, int i) {
		Modifiers m = partialMethod.getModifiers();
		if (containsBeforeModifier(m))
			block = generateBlockForBeforeMethod(i);
		else if (containsAfterModifier(m))
			block = generateBlockForAfterMethod(i);

		return block;
	}

	/**
	 * check modifier contains 'before'
	 * 
	 * @param m
	 * @return
	 */
	private boolean containsBeforeModifier(Modifiers m) {
		return m.contains(BEFORE) || m.contains(BEFORE_ANNOT);
	}

	private Block generateBlockForBeforeMethod(int i) {
		Block block = ((PartialMethodGroupDecl) partialMethod).getBodyGroup(i)
				.fullCopy();
		// Block block = partialMethod.getBlock().fullCopy();
		TryStmt tryStmt = new TryStmt(block, new List<CatchClause>(),
				genOptBlock(genProceedExprStmt()));
		return createStmtBlock(tryStmt);
	}

	/**
	 * check modifier contains 'after'
	 * 
	 * @param m
	 * @return
	 */
	private boolean containsAfterModifier(Modifiers m) {
		return m.contains(AFTER) || m.contains(AFTER_ANNOT);
	}

	private Block generateBlockForAfterMethod(int i) {
		Block block = ((PartialMethodGroupDecl) partialMethod).getBodyGroup(i)
				.fullCopy();
		// Block block = partialMethod.getBlock().fullCopy();
		List<Stmt> stmts = block.getStmtList();

		if (!partialMethod.isVoid()) {
			String var = "return$type";
			Stmt s1 = genProceedVar(var);
			Stmt s2 = new ReturnStmt(new VarAccess(var));
			stmts.insertChild(s1, 0);
			stmts.addChild(s2);
		} else {
			stmts.insertChild(genProceedExprStmt(), 0);
		}
		return block;
	}

	/**
	 * generate proceed Stmt
	 * 
	 * @param var
	 * @return
	 */
	private Stmt genProceedVar(String var) {
		Access type = partialMethod.getTypeAccess();
		return new VariableDeclaration(type, var, genProceedExpr());
	}

	private Stmt genProceedExprStmt() {
		return maybeSurroundWithReturn(genProceedExpr());
	}

	/**
	 * generate {@link ReturnStmt} if return value of partialMethod is void,
	 * {@link ExprStmt} otherwise.
	 * 
	 * @param expr
	 * @return
	 */
	private Stmt maybeSurroundWithReturn(Expr expr) {
		if (!partialMethod.isVoid())
			return new ReturnStmt(expr);
		return new ExprStmt(expr);
	}

	private Expr genProceedExpr() {
		List<Expr> args = generateArgs(partialMethod.getParameterList());
		return new ProceedExpr(args);
	}
}
