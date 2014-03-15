package jcop.generation;

import static jcop.Globals.Modifiers.FINAL;
import static jcop.Globals.Modifiers.PUBLIC;
import static jcop.Globals.Modifiers.STATIC;
import AST.Access;
import AST.CompilationUnit;
import AST.FieldDeclaration;
import AST.ImportDecl;
import AST.LayerImportDecl;
import AST.TypeAccess;
import AST.VarAccess;

/**
 * Documented by wander,
 * 
 * <pre>
 * generate constructs for {@link LayerImportDecl}
 * </pre>
 * 
 */
public class LayerImportDeclGenerator extends Generator {
	private LayerImportDecl importDecl;
	/**
	 * not used
	 */
	private TypeAccess layerTypeAccess;

	public LayerImportDeclGenerator(LayerImportDecl importDecl) {
		this.importDecl = importDecl;
	}

	/**
	 * generate {@link FieldDeclaration}
	 * 
	 * <pre>
	 * <code>
	 *   public static final {@code <packageName>}.{@code <layerType>} {@code <layerName>} = 
	 *   FIXME wander: something is missing here 
	 * </code>
	 * </pre>
	 * 
	 * @return
	 */
	public FieldDeclaration genStaticLayerReference() {
		TypeAccess type = genLayerTypeAccess();
		return new FieldDeclaration(genModifiers(PUBLIC, STATIC, FINAL),
				(Access) type, type.name(),

				genLayerTypeAccess()
						.qualifiesAccess(new VarAccess(type.name())));

	}

	/**
	 * generate {@link TypeAccess} from {@link LayerImportDecl}
	 * 
	 * @return
	 */
	public TypeAccess genLayerTypeAccess() {
		String layerName = parseLayerName(importDecl.getAccess());
		String packageName = parsePackageName(importDecl.getAccess());
		return new TypeAccess(packageName, layerName);
	}

	private CompilationUnit getCompilationUnit() {
		return (CompilationUnit) importDecl.getParent().getParent();
	}

	/**
	 * get package name from {@link CompilationUnit} enclosing {@code <access>}
	 * 
	 * @param access
	 * @return
	 */
	private String parsePackageName(Access access) {
		String packageName = access.packageName();
		if (packageName.equals(""))
			packageName = ((CompilationUnit) importDecl.getParent().getParent())
					.packageName();
		return packageName;
	}

	// malte: this is quite ugly
	/**
	 * get layer name from {@link Access}
	 * 
	 * @param a
	 * @return
	 */
	private String parseLayerName(Access a) {
		String str = a.toString();
		return str.substring(str.lastIndexOf(".") + 1);
	}

}
