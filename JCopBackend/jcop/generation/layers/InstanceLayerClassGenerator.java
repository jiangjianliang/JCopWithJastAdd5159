/**
 * 
 */
package jcop.generation.layers;

import static jcop.Globals.Modifiers.*;
import static jcop.Globals.Types.CONCRETE_LAYER;
import static jcop.Globals.Types.LAYER;
import static jcop.Globals.Types.LAYER_PROXY;
import jcop.Globals;
import jcop.Globals.ID;
import jcop.Globals.Types;
import jcop.compiler.JCopTypes.JCopAccess;
import AST.Access;
import AST.BodyDecl;
import AST.ClassDecl;
import AST.ClassInstanceExpr;
import AST.ConstructorDecl;
import AST.Dot;
import AST.Expr;
import AST.ExprStmt;
import AST.FieldDeclaration;
import AST.LayerDecl;
import AST.LayerDeclaration;
import AST.List;
import AST.MethodAccess;
import AST.MethodDecl;
import AST.Modifiers;
import AST.Opt;
import AST.ParameterDeclaration;
import AST.PartialMethodDecl;
import AST.ReturnStmt;
import AST.Stmt;
import AST.StringLiteral;
import AST.SuperAccess;
import AST.SuperConstructorAccess;
import AST.ThisAccess;
import AST.TypeAccess;
import AST.VarAccess;

/**
 * Documented by wander,
 * 
 * generate constructs for top level {@link AST.LayerDecl LayerDecl}
 * 
 * 
 */
public class InstanceLayerClassGenerator extends LayerClassGenerator {
	private LayerDecl topLevelLayer;

	public InstanceLayerClassGenerator(LayerDecl layerDecl) {
		super(layerDecl);
		topLevelLayer = layerDecl;
	}

	public InstanceLayerClassGenerator(LayerDeclaration layerDeclaration) {
		super(layerDeclaration);
	}
	/**
	 * generate {@link AST.Method Method} from baseFieldDecl,
	 * 
	 * <pre>
	 * <code>
	 *   _target.{@code <transformedMethodName>}({@code <params>})
	 * </code>
	 * </pre>
	 * 
	 * @param baseFieldDecl
	 * @param generatedMethodName
	 * @param wrapperFieldID
	 * @return
	 */
	public MethodDecl generateDelegationMethod(FieldDeclaration baseFieldDecl,
			String generatedMethodName, String wrapperFieldID) {
		// method body: _target.<transformedMethodName>(<params>)
		Dot methodBody = new VarAccess(ID.targetParameterName)
				.qualifiesAccess(createWrapperFieldAccess(baseFieldDecl,
						wrapperFieldID));
		MethodDecl method = super.createDelegationMethod(baseFieldDecl,
				generatedMethodName, methodBody);
		return method;
	}

	/**
	 * generate {@link AST.MethodDecl MethodDecl}
	 * 
	 * <pre>
	 * <code>
	 * public static xxx.xxxx.xxx getInstance(){
	 *   return xxx;
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @return
	 */
	private MethodDecl generateSingletonGetter() {
		return new MethodDecl(genModifiers(PUBLIC, STATIC), new TypeAccess(
				topLevelLayer.packageName(), topLevelLayer.getID()),
				ID.getLayerInstance, new List(), new List(),
				genOptBlock(new ReturnStmt(new VarAccess(getLayerName()))));
	}

	// private MethodDecl generateSingletonGetter() {
	// ClassDecl layer = this.layerClass;
	// return new MethodDecl (
	// genModifiers(PUBLIC ),
	// JCopAccess.get(LAYER),
	// "getSingleton",
	// new List(),
	// new List(),
	// genOptBlock(
	// new ReturnStmt(new VarAccess(layer.getID()))
	// ));
	// }
	/**
	 * generate {@link AST.ClassDecl ClassDecl}
	 * 
	 * @return
	 */
	public ClassDecl generateClassDecl() {
		LayerDecl layerDecl = this.topLevelLayer;
		String id = layerDecl.getID();
		Opt<Access> supertype = generateSuperType(layerDecl);
		List<Access> interfaces = layerDecl.getImplementsList().fullCopy();
		List<BodyDecl> body = generateBody(layerDecl.isStaticActive());
		Modifiers modifiers = generateModifiers();
		ClassDecl c = new ClassDecl(modifiers, id, supertype, interfaces, body);
		return c;
	}

	/**
	 * generate {@link AST.Opt<Access> {@code Opt<Access>} ,
	 * 
	 * <pre>
	 * <code>extends xxxLayer</code> or
	 * <code>extends jcop.lang.ConcreteLayer</code>
	 * </pre>
	 * 
	 * @param layerDecl
	 * @return
	 */
	private Opt<Access> generateSuperType(LayerDecl layerDecl) {
		if (layerDecl.hasSuperClassAccess()) {
			// System.err.println("layer superclass:" +
			// layerDecl.getSuperClassAccess().t);
			return layerDecl.getSuperClassAccessOpt().fullCopy();
		} else {
			// System.err.println("no superclass");
			return new Opt<Access>(JCopAccess.get(CONCRETE_LAYER));
		}
	}

	/**
	 * get all modifier except 'staticactive'
	 * 
	 * @return
	 */
	private Modifiers generateModifiers() {
		Modifiers m = this.topLevelLayer.getModifiers().fullCopy();
		if (m.isStaticActive())
			m = removeModifiers(m, jcop.Globals.Modifiers.STATIC_ACTIVE);
		return m;
	}

	/**
	 * generate {@link AST.ConstructorDecl ConstructorDecl},
	 * 
	 * <pre>
	 * <code>
	 * public xxxx(){
	 *  super();
	 * } 
	 * </code>
	 * </pre>
	 * 
	 * @return
	 */
	private ConstructorDecl generateConstructor() {
		ClassDecl layerClass = getLayerClass();
		return new ConstructorDecl(genModifiers(PUBLIC), layerClass.getID(),
				new List<ParameterDeclaration>(), new List<Access>(),
				createSuperCallOpt(), createStmtBlock());
	}

	// private Stmt generateLayerNameSetter() {
	// String id = getLayerClass().getID();
	// return new ExprStmt(
	// createMethodAccess(ID.setName, new StringLiteral(id)));
	// }
	/**
	 * generate {@link AST.Opt<Stmt> Opt<Stmt>},
	 * 
	 * <pre>
	 * <code>
	 *   super();
	 * </code>
	 * </pre>
	 * 
	 * @return
	 */
	private Opt<Stmt> createSuperCallOpt() {
		return new Opt<Stmt>(new ExprStmt(new SuperConstructorAccess("super",
				new List())));
	}

	/**
	 * generate {@link AST.FieldDeclaration FieldDeclaration},
	 * 
	 * <pre>
	 * <code>
	 *   public static final 
	 *   FIXME can not find method in layer
	 * </code>
	 * </pre>
	 * 
	 * @return
	 */
	public FieldDeclaration generateSingletonReference() {
		return new FieldDeclaration(genModifiers(PUBLIC, STATIC, FINAL),
				createLayerTypeAccess(), topLevelLayer.getID(), new Opt<Expr>(
						new ClassInstanceExpr(new TypeAccess(
								topLevelLayer.packageName(),
								topLevelLayer.getID()), new List<Expr>())));
	}

	private Access createWrapperFieldAccess(FieldDeclaration baseFieldDecl,
			String generatedMethodName) {
		// List<Expr> args = createList(new ThisAccess());
		return createMethodAccess(generatedMethodName);
	}

	/**
	 * generate list of {@link AST.BodyDecl BodyDecl},
	 * 
	 * <pre>
	 * <code>
	 *  generateConstructor;
	 *  generateThisAccess;
	 *  generateGetNameMethod;
	 *  
	 *  -----------only valid for 'staticactive' modifier
	 *    generateSingletonGetter;
	 *    generateSingletonReference;
	 *  -----------
	 * </code>
	 * at last, clean partial method declaration in layer
	 * </pre>
	 * 
	 * @param staticactive
	 * @return
	 */
	private List<BodyDecl> generateBody(boolean staticactive) {
		List<BodyDecl> body = topLevelLayer.getBodyDeclList().fullCopy();
		// body.add(generateSingletonReference());
		body.add(generateConstructor());
		body.add(generateThisAccess(topLevelLayer));
		body.add(generateGetNameMethod(topLevelLayer));
		if (staticactive) {
			body.add(generateSingletonGetter());
			body.add(generateSingletonReference());
		}

		// body.add(generateSingletonGetter());
		// body.add(generateMethodForImplicitActivation(layerDecl));
		// body.add(generateMethodForImplicitDeactivation(layerDecl));
		removePartilalMehods(body);
		return body;
	}

	/**
	 * generate {@link AST.BodyDecl AST.BodyDecl},
	 * 
	 * <pre>
	 * <code>
	 * public java.lang.String getName(){
	 *   return xxxLayer;
	 * }
	 * </code>
	 * </pre>
	 * 
	 * @param layerDecl
	 * @return
	 */
	private BodyDecl generateGetNameMethod(LayerDecl layerDecl) {
		return new MethodDecl(
				genModifiers(PUBLIC),
				new TypeAccess("java.lang", "String"),
				"getName",
				new List(),
				new List(),
				genOptBlock(new ReturnStmt(new StringLiteral(layerDecl.getID()))));

	}

	/**
	 * generate {@link AST.BodyDecl},
	 * 
	 * <pre>
	 * <code>
	 *   public {@code @}jcop.lang.Generated() xxxLayer _thislayer = this;
	 * </code>
	 * </pre>
	 * 
	 * @param layerDecl
	 * @return
	 */
	private BodyDecl generateThisAccess(LayerDecl layerDecl) {
		Modifiers modifs = genModifiers(PUBLIC);
		modifs.addModifier(genAnnotation(Types.GENERATED));
		return new FieldDeclaration(modifs, new TypeAccess(layerDecl.getID()),
				ID.thislayer, new ThisAccess());
	}

	// private BodyDecl generateSuperAccess(LayerDecl layerDecl) {
	// Access a = layerDecl.hasSuperClassAccess()
	// ? generateSuperType(layerDecl).getChildNoTransform(0)
	// : JCopAccess.get(LAYER);
	//
	//
	//
	// return new FieldDeclaration(genModifiers(PUBLIC),
	// a,
	// ID.superlayer,
	// new SuperAccess("super").qualifiesAccess(new VarAccess(ID.thislayer)) );
	// }
	/**
	 * remove all partial methods in bodyDecls
	 * 
	 * @param bodyDecls
	 */
	private void removePartilalMehods(AST.List<BodyDecl> bodyDecls) {
		for (int i = bodyDecls.getNumChild() - 1; i >= 0; i--) {
			BodyDecl bodyDecl = bodyDecls.getChild(i);
			// TODO: ugly!
			if (bodyDecl instanceof PartialMethodDecl)
				bodyDecls.removeChild(i);
		}
	}

	// private ArrayTypeAccess genLayerArrayAccess() {
	// return new ArrayTypeAccess(JCopAccess.get(LAYER));
	// }
	//
	// private BodyDecl generateMethodForImplicitDeactivation(LayerDecl
	// layerDecl) {
	// return generateMethodForImplicitDeActivation(
	// Globals.Identifiers.implicitDeactivationList,
	// layerDecl.getImplicitDeactivationList());
	// }
	//
	// private BodyDecl generateMethodForImplicitActivation(LayerDecl layerDecl)
	// {
	// return generateMethodForImplicitDeActivation(
	// Globals.Identifiers.implicitActivationList,
	// layerDecl.getImplicitActivationList());
	// }
	//
	// private BodyDecl generateMethodForImplicitDeActivation(String id,
	// List<Access> layers) {
	// return new MethodDecl(
	// genModifiers("public"),
	// genLayerArrayAccess(),
	// id,
	// new List<ParameterDeclaration>(),
	// new List<Access>(),
	// genOptBlock(
	// new ReturnStmt(
	// new ArrayCreationExpr(
	// genLayerArrayAccess(),
	// new Opt<ArrayInit>(
	// new ArrayInit(createIdentifiers(layers))
	// )
	// )
	// )
	// )
	// );
	// }
	//
	// private List<Expr> createIdentifiers(List<Access> layers) {
	// List<Expr>exprs = new List<Expr>();
	//
	// // *NoTransform methods required here, otherwise
	// // 'can not reclassify' error
	// for (int i = 0; i < layers.getNumChildNoTransform(); i++) {
	// ParseName layerAccess = (ParseName)layers.getChildNoTransform(i);
	// exprs.add(new VarAccess(layerAccess.getID() ));
	// }
	// return exprs;
	// }

	// private void addDelegationMethodToHostClass(MethodDecl delegationMethod)
	// {
	// this.layerClassGenerator.layerClass.resetCache();
	// this.layerClassGenerator.layerClass.addMemberMethod(delegationMethod);
	// this.layerClassGenerator.layerClass.resetCache();
	// }

	// private void addPartialMethodToList(FieldDeclaration field, String
	// generatedMethodName, TypeDecl/* ClassDecl */decl) {
	// String signature = Generation.createFullQualifiedSignature(field);
	// Expr metaClassInit = createPartialFieldMetaClassInstantiation(field,
	// generatedMethodName);
	// addPartialDeclToList(field, generatedMethodName, decl, signature,
	// metaClassInit);
	// }

	// private void addPartialDeclToList(MemberDecl member, String
	// generatedMethodName, TypeDecl/* ClassDecl */decl, String signature, Expr
	// metaClass) {
	// decl.resetCache();
	// InstanceInitializer init = getInitializer(decl);
	// init.getBlock().addStmt(new ExprStmt(new
	// VarAccess("partialMethodSignatures").qualifiesAccess(new
	// MethodAccess("put", new List<Expr>().add(new
	// StringLiteral(signature)).add(metaClass)))));
	// decl.resetCache();
	// }
	/**
	 * generate {@link AST.MethodDecl MethodDecl} from originalMethodDecl,
	 * 
	 * @param originalMethodDecl
	 * @param generatedMethodName
	 * @return
	 */
	public MethodDecl genDelegationMethod(MethodDecl originalMethodDecl,
			String generatedMethodName) {
		// method body:
		// _target.<transformedMethodName>(<params>)
		List<Expr> args = generateArgs(originalMethodDecl.getParameterList());
		genLayerParams(args);
		Dot methodBody = new VarAccess(ID.targetParameterName)
				.qualifiesAccess(new MethodAccess(originalMethodDecl.getID(),
						args));
		MethodDecl method = super.generateDelegationMethod(originalMethodDecl,
				generatedMethodName, methodBody);
		return method;
	}

	/**
	 * generate {@link AST.Expr Expr},
	 * 
	 * {@code
	 *   FIXME static layer declaration
	 * }
	 * 
	 * @return
	 */
	public Expr createLayerInit() {
		Expr layerInit = createLayerTypeAccess().qualifiesAccess(
				new VarAccess(getLayerName()));// new
												// ClassInstanceExpr(createLayerTypeAccess(),
												// new List());
		ClassInstanceExpr layerProxyInit = new ClassInstanceExpr(
				JCopAccess.get(LAYER_PROXY), genList(layerInit));
		return layerProxyInit;
	}

}