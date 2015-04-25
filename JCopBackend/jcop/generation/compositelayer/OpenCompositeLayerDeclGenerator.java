package jcop.generation.compositelayer;

import java.util.Set;

import AST.ASTNode;
import AST.Access;
import AST.Block;
import AST.BooleanLiteral;
import AST.CastExpr;
import AST.CompositeRuleDecl;
import AST.Expr;
import AST.ExprStmt;
import AST.IfStmt;
import AST.InstanceInitializer;
import AST.InstanceOfExpr;
import AST.List;
import AST.MethodAccess;
import AST.MethodDecl;
import AST.Modifier;
import AST.Modifiers;
import AST.OpenCompositeLayerDecl;
import AST.Opt;
import AST.ParExpr;
import AST.ParameterDeclaration;
import AST.PrimitiveTypeAccess;
import AST.ReturnStmt;
import AST.Stmt;
import AST.TypeAccess;
import AST.TypeDecl;
import AST.VarAccess;
import jcop.generation.Generator;

public class OpenCompositeLayerDeclGenerator extends Generator {

	private OpenCompositeLayerDecl compositeLayer;
	private TypeDecl hostType;

	public OpenCompositeLayerDeclGenerator(OpenCompositeLayerDecl compositeLayer) {
		this.compositeLayer = compositeLayer;
		this.hostType = compositeLayer.hostType();
	}

	/**
	 * generate {@link ASTNode} of {@link InstanceInitializer}
	 * 
	 * @return
	 */
	public ASTNode<ASTNode> createDummyNode() {
		return new InstanceInitializer(new Block(new List<Stmt>()));
	}

	/**
	 * generate {@link MethodDecl}
	 * 
	 * <pre>
	 * <code>
	 * public boolean match(Object target){ 
	 * 	  if(target instanceof "hostType().name()"){ 
	 *        return true; 
	 *    }
	 *    return false;
	 * } 
	 * </code>
	 * </pre>
	 * 
	 * hostType().name() will be replaced with actual String
	 * 
	 * @return
	 */
	public MethodDecl generateMatchMethod() {
		// construct method-body
		Expr condition = new InstanceOfExpr(new VarAccess("target"),
				new TypeAccess(hostType.packageName(), hostType.name()));
		List<Stmt> thenList = new List<Stmt>();
		thenList.add(new ReturnStmt(new BooleanLiteral(true)));

		List<Stmt> bodyList = new List<Stmt>();
		bodyList.add(new IfStmt(condition, new Block(thenList), new Opt<Stmt>()));
		bodyList.add(new ReturnStmt(new BooleanLiteral(false)));
		Block body = new Block(bodyList);

		// construct method-modifiers
		Modifiers modifiers = new Modifiers();
		modifiers.addModifier(new Modifier("public"));// TODO 4-23 Modifier

		// construct method-param
		List<ParameterDeclaration> params = new List<ParameterDeclaration>();
		params.add(new ParameterDeclaration(new TypeAccess("java.lang",
				"Object"), "target"));// TODO 4-23 TypeAccess

		// construct method-exception
		List<Access> exceptions = new List<Access>();

		MethodDecl method = new MethodDecl(modifiers, new PrimitiveTypeAccess(
				"boolean"), "match", params, exceptions, new Opt<Block>(body));
		// System.out.println(method.toString());
		return method;
	}

	/**
	 * generate {@link MethodDecl}
	 * 
	 * <pre>
	 * <code>
	 * public void collectInfo(Object target, WeakHashMap map){
	 *    map.put((...)target.getXXX(), YYYY); 
	 *    ...
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @param rules
	 * @return
	 */
	public MethodDecl generateCollectInfoMethod(Set<CompositeRuleDecl> rules) {
		// construct method-body
		List<Stmt> bodyList = new List<Stmt>();
		for (CompositeRuleDecl compositeRule : rules) {
			bodyList.add(generateCollectInfoStmt(compositeRule));
		}
		Block body = new Block(bodyList);

		// construct method-modifier
		Modifiers modifiers = new Modifiers();
		modifiers.addModifier(new Modifier("public"));// TODO 4-23 Modifier

		// construct method-param
		List<ParameterDeclaration> params = new List<ParameterDeclaration>();
		params.add(new ParameterDeclaration(new TypeAccess("java.lang",
				"Object"), "target"));// TODO 4-23 TypeAccess
		params.add(new ParameterDeclaration(new TypeAccess("java.util",
				"WeakHashMap"), "map"));// TODO 4-23 TypeAccess

		// construct method-exception
		List<Access> exceptions = new List<Access>();

		MethodDecl method = new MethodDecl(modifiers, new PrimitiveTypeAccess(
				"void"), "collectInfo", params, exceptions,
				new Opt<Block>(body));

		return method;
	}

	/**
	 * generate {@ExprStmt}
	 * 
	 * <pre><code>
	 * map.put(target.getXXX(), yyy);
	 * </code>
	 * 
	 * <pre>
	 * @param compositeRule
	 * @return
	 */
	public Stmt generateCollectInfoStmt(CompositeRuleDecl compositeRule) {
		List<Expr> params = new List<Expr>();

		String fieldName = compositeRule.getTarget().toString();// compositeRule.getTarget().name();//TODO
																// 4-23
		String methodName = "get" + fieldName.substring(0, 1).toUpperCase()
				+ fieldName.substring(1); // getXXX()
		// Expr expr = new CastExpr();
		Expr expr = new ParExpr(new CastExpr(new TypeAccess(
				hostType.packageName(), hostType.name()), new VarAccess(
				"target"))).qualifiesAccess(new MethodAccess(methodName,
				new List<Expr>()));

		Stmt stmt = new ExprStmt(
				new VarAccess("map").qualifiesAccess(createMethodAccess("put",
						expr, compositeRule.getLayerIns())));
		return stmt;
	}
	
}
