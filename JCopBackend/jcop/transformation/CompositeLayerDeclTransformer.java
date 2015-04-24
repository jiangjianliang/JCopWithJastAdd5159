package jcop.transformation;

import jcop.generation.jcopaspect.JCopAspect;
import AST.ASTNode;
import AST.CompositeLayerDecl;
import AST.ImportDecl;

public class CompositeLayerDeclTransformer extends Transformer {
	private CompositeLayerDecl compositeLayerDecl;
	private CompositeLayerDeclGenerator gen;
	
	public CompositeLayerDeclTransformer(CompositeLayerDecl compositeLayerDecl){
		this.compositeLayerDecl = compositeLayerDecl;
		this.gen = new CompositeLayerDeclGenerator(compositeLayerDecl);
		
	}
	
	@Override
	protected ASTNode transform() {
		addImportsToAspect();
		return this.gen.generateClassDecl();
	}
	
	private void addImportsToAspect() {
		for (ImportDecl im : compositeLayerDecl.compilationUnit().getImportDeclList())
			JCopAspect.getInstance().addImport(im);

	}
}
