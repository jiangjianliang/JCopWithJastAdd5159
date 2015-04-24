package jcop.transformation;

import static jcop.Globals.Types.CONCRETE_LAYER;
import AST.Access;
import AST.BodyDecl;
import AST.ClassDecl;
import AST.CompositeLayerDecl;
import AST.LayerDecl;
import AST.List;
import AST.Modifiers;
import AST.Opt;
import jcop.compiler.JCopTypes.JCopAccess;
import jcop.generation.Generator;

public class CompositeLayerDeclGenerator extends Generator {
	
	private CompositeLayerDecl topLevelCompositeLayer;
	
	public CompositeLayerDeclGenerator(CompositeLayerDecl compositeLayer){
		
		this.topLevelCompositeLayer = compositeLayer;
	}
	
	/**
	 * generate {@link AST.ClassDecl ClassDecl}
	 * 
	 * @return
	 */
	public ClassDecl generateClassDecl() {
		CompositeLayerDecl compositelayerDecl = this.topLevelCompositeLayer;
		String id = compositelayerDecl.getID();
		Opt<Access> supertype = generateSuperType(compositelayerDecl);//TODO
		List<Access> interfaces = compositelayerDecl.getImplementsList().fullCopy();
		List<BodyDecl> body = generateBody();
		Modifiers modifiers = this.topLevelCompositeLayer.getModifiers().fullCopy();
		ClassDecl c = new ClassDecl(modifiers, id, supertype, interfaces, body);
		return c;
	}
	
	/**
	 * generate {@link AST.Opt<Access> {@code Opt<Access>} ,
	 * 
	 * <pre>
	 * <code>extends xxxLayer</code> or
	 * <code>extends jcop.lang.CompositeLayer</code>
	 * </pre>
	 * 
	 * @param layerDecl
	 * @return
	 */
	private Opt<Access> generateSuperType(CompositeLayerDecl compositelayerDecl) {
		if (compositelayerDecl.hasSuperClassAccess()) {
			System.err.println("composite layer cannot inherit any layer!!!");
			//at present should not happen!!!
			return compositelayerDecl.getSuperClassAccessOpt().fullCopy();
		} else {
			return new Opt<Access>(JCopAccess.get("CompositeLayer"));
		}
	}
	
	/**
	 * generate list of {@link AST.BodyDecl BodyDecl},
	 * 
	 * @return
	 */
	private List<BodyDecl> generateBody() {
		return new List<BodyDecl>();
	}
}
