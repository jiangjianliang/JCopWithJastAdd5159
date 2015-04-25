package jcop.generation;

import static jcop.Globals.Modifiers.*;
import static jcop.Globals.Modifiers.PRIVATE;
import static jcop.Globals.Modifiers.PROTECTED;
import static jcop.Globals.Modifiers.PUBLIC;
import static jcop.Globals.Types.*;
import jcop.Globals;
import jcop.Globals.ID;
import jcop.compiler.JCopTypes;
import jcop.compiler.JCopTypes.JCopAccess;
import AST.ASTNode;
import AST.Access;
import AST.Annotation;
import AST.Block;
import AST.ClassInstanceExpr;
import AST.CompositeMethodDecl;
import AST.Expr;
import AST.ExprStmt;
import AST.FieldDeclaration;
import AST.List;
import AST.MethodAccess;
import AST.MethodDecl;
import AST.Modifier;
import AST.Modifiers;
import AST.NamedMember;
import AST.Opt;
import AST.ParameterDeclaration;
import AST.ReturnStmt;
import AST.Stmt;
import AST.StringLiteral;
import AST.ThrowStmt;
import AST.TypeAccess;
import AST.TypeDecl;
import AST.VarAccess;

/**
 * Documented by wander,
 * 
 */
public class Generator {
	public Annotation genAnnotation(String name) {
		return new Annotation(name, JCopTypes.JCopAccess.get(name), new List());
	}

	public Modifiers genModifiers(String... modifiers) {
		Modifiers m = new Modifiers();
		for (String mod : modifiers)
			m.addModifier(new Modifier(mod));
		return m;
	}

	public Opt<Block> genOptBlock(List<Stmt> stmts) {
		return new Opt<Block>(new Block(stmts));
	}

	public Opt<Block> genOptBlock(Expr expr) {
		return new Opt<Block>(createStmtBlock(new ExprStmt(expr)));
	}

	public Stmt generateThrowsException(TypeAccess exceptionType) {
		return new ThrowStmt(new ClassInstanceExpr(exceptionType,
				new List<Expr>()));
	}

	/**
	 * generate {@link AST.Stmt Stmt} for {@link AST.MethodDecl MethodDecl}. if
	 * return value of {@code <decl>} is void, generate {@link AST.ExprStmt
	 * ExprStmt}, otherwise, generate {@link AST.ReturnStmt ReturnStmt}
	 * 
	 * @param decl
	 * @param expr
	 * @return
	 */
	public Stmt maybeGenerateReturnStmt(MethodDecl decl, Expr expr) {
		if (decl.isVoid())
			return new ExprStmt(expr);
		else
			return new ReturnStmt(expr);
	}

	/**
	 * generate {@link AST.Stmt Stmt} for {@link AST.CompositeMethodDecl
	 * CompositeMethodDecl}. if return value of {@code <decl>} is void, generate
	 * {@link AST.ExprStmt ExprStmt}, otherwise, generate {@link AST.ReturnStmt
	 * ReturnStmt}
	 * 
	 * @param decl
	 * @param expr
	 * @return
	 */
	public Stmt maybeGenerateReturnStmt(CompositeMethodDecl decl, Expr expr) {
		if (decl.isVoid())
			return new ExprStmt(expr);
		else
			return new ReturnStmt(expr);
	}

	public Opt<Block> genOptBlock(Stmt... stmts) {
		return new Opt<Block>(createStmtBlock(stmts));
	}

	// public List<BodyDecl> genList(BodyDecl... elements) {
	// List<BodyDecl> list = new List<BodyDecl>();
	// return addToList(list, elements);
	// }

	public <T extends ASTNode> List<T> genList(T... elements) {
		List<T> list = new List<T>();
		for (T element : elements)
			list.add(element);
		return list;
	}

	public List<Expr> genList(Expr... elements) {
		List<Expr> list = new List<Expr>();
		for (Expr element : elements)
			list.add(element);
		return list;
	}

	// public List<ParameterDeclaration> genList(ParameterDeclaration...
	// elements) {
	// List<ParameterDeclaration> list = new List<ParameterDeclaration>();
	// for (ParameterDeclaration element : elements)
	// list.add(element);
	// return list;
	// }
	//
	// public List<Expr> genList(Expr... elements) {
	// List<Expr> list = new List<Expr>();
	// for (Expr element : elements)
	// list.add(element);
	// return list;
	// }
	//
	// public List<Stmt> createList(Stmt... elements) {
	// List<Stmt> list = new List<Stmt>();
	// for (Stmt element : elements)
	// list.add(element);
	// return list;
	// }
	/**
	 * {@link AST.Block Block} helper method. generate a block for list of stmts
	 * 
	 * @param stmts
	 * @return
	 */
	public Block createStmtBlock(Stmt... stmts) {
		return new Block(genList(stmts));
	}

	public MethodAccess createMethodAccess(String methodName, List<Expr> args) {
		return new MethodAccess(methodName, args);
	}

	/**
	 * generate {@link AST.MethodAccess MethodAccess} for {@code methodName}
	 * parameterized by {@code <args>}
	 * 
	 * <pre>
	 * <code>{@code <methodName>(<args>)}</code>
	 * </pre>
	 * 
	 * @param methodName
	 * @param args
	 * @return
	 */
	public MethodAccess createMethodAccess(String methodName, Expr... args) {
		return new MethodAccess(methodName, genList(args));
	}

	public Stmt createSysout(String string) {
		TypeAccess system = new TypeAccess("java.lang", "System");
		Access out = system.qualifiesAccess(new VarAccess("out"));
		Access println = out.qualifiesAccess(createCompositionMethodAccess(
				"println", new StringLiteral(string)));
		return new ExprStmt(println);
	}

	/**
	 * generate {@link AST.Access Access}
	 * 
	 * <pre>
	 * <code>
	 * jcop.lang.JCop.current()
	 * </code>
	 * </pre>
	 * 
	 * @return
	 */
	public Access createCurrentCompositionAccess() {
		return JCopAccess.get(JCOP).qualifiesAccess(
				createMethodAccess(ID.current));
	}

	public Access createCompositionMethodAccess(String methodname, Expr... args) {
		return createCurrentCompositionAccess().qualifiesAccess(
				createMethodAccess(methodname, args));
	}

	private static boolean arrayContains(String[] arr, String str) {
		for (String elem : arr) {
			if (elem.equals(str))
				return true;
		}
		return false;
	}

	public Modifiers createPublicModifierFor(TypeDecl decl) {
		return createPublicModifier(decl.getModifiers());
	}

	/**
	 * 
	 * 
	 * @param decl
	 * @return
	 * @see #createPublicModifier(Modifiers)
	 */
	public Modifiers createPublicModifierFor(NamedMember decl) {
		return createPublicModifier(decl.getModifiers());
	}

	public Modifiers createPublicModifierFor(CompositeMethodDecl decl) {
		return createPublicModifier(decl.getModifiers());
	}

	/**
	 * remove all modifiers({@link jcop.Globals.Modifiers#PRIVATE PRIVATE},
	 * {@link jcop.Globals.Modifiers#PROTECTED},
	 * {@link jcop.Globals.Modifiers#FINAL},
	 * {@link jcop.Globals.Modifiers#BEFORE},
	 * {@link jcop.Globals.Modifiers#AFTER} ) except
	 * {@link jcop.Globals.Modifiers.PUBLIC PUBLIC} from oldModifier
	 * 
	 * @param oldModifier
	 * @return
	 */
	public Modifiers createPublicModifier(Modifiers oldModifier) {
		String[] toBeRemoved = { PRIVATE, PROTECTED, FINAL, BEFORE, AFTER };
		Modifiers newModifier = removeModifiers(oldModifier, toBeRemoved);
		if (!newModifier.contains(PUBLIC))
			newModifier.addModifier(new Modifier(PUBLIC));
		return newModifier;
	}

	public Modifiers removeModifiers(Modifiers oldModifier,
			String... toBeRemoved) {
		Modifiers m = new Modifiers();
		for (Modifier modifier : oldModifier.getModifierList()) {
			if (!arrayContains(toBeRemoved, modifier.getID()))
				m.addModifier(modifier);
		}
		return m;
	}

	// public Modifiers createPublicModifierFor(MethodDecl methodDecl) {
	// Modifiers modifiers = methodDecl.getModifiersNoTransform().fullCopy();
	// List<Modifier> l = modifiers.getModifierList();
	//
	// for (int j = 0; j < l.getNumChild(); j++) {
	// Modifier modifier = (Modifier) (l.getChild(j));
	// if (modifier.getID().equals(PRIVATE) ||
	// modifier.getID().equals(PROTECTED)) {
	// l.removeChild(j);
	// l.setChild(new Modifier(PUBLIC), j);
	// }
	// }
	// if( !methodDecl.isPrivate() &&
	// !methodDecl.isPublic() &&
	// !methodDecl.isProtected()) {
	// l.setChild(new Modifier(PUBLIC), 0);
	// }
	// return modifiers;
	// }
	/**
	 * generate list of {@link Expr} for params
	 * 
	 * @param params
	 * @return
	 */
	public List<Expr> generateArgs(List<ParameterDeclaration> params) {
		List<Expr> args = new List<Expr>();
		for (int y = 0; y < params.getNumChild(); y++)
			args.add(new VarAccess(params.getChild(y).getID()));
		return args;
	}

	public String createFullQualifiedSignature(MethodDecl method) {
		StringBuffer buffer = new StringBuffer();
		buffer.append(method.getModifiers()).append(' ');
		getFullQualifiedNameInt(buffer, method.type());
		buffer.append(' ');
		getFullQualifiedNameInt(buffer, method.hostType());
		buffer.append('.');
		buffer.append(method.signature());
		return buffer.toString();
	}

	private void getFullQualifiedNameInt(StringBuffer buffer, TypeDecl type) {
		if (!type.isPrimitiveType() && !type.isVoid())
			buffer.append(type.packageName()).append('.');
		buffer.append(type.name());
	}

	public void createFullQualifiedSignature(StringBuffer buffer,
			FieldDeclaration field) {
		// ma: removed modifiers since they require an adaptation of the
		// get/set advice otherwise
		// buffer.append(field.getModifiers()).append(' ');
		getFullQualifiedNameInt(buffer, field.type());
		buffer.append(' ');
		getFullQualifiedNameInt(buffer, field.hostType());
		buffer.append('.');
		buffer.append(field.getID());
	}

	// public static String getFullQualifiedName(TypeDecl decl) {
	// return ASTTools.Generation.getFullQualifiedNameInt(decl).toString();
	// }
	// public static String getFullQualifiedName(Access access) {
	// return ASTTools.Generation.getFullQualifiedName(access.type());
	// //access.packageName() + '.' + access.typeName();
	// }

}
