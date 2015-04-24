package jcop.transformation;

import AST.ASTNode;
import AST.Access;
import AST.BodyDecl;
import AST.ClassDecl;
import AST.CompilationUnit;
import AST.List;
import AST.MethodDecl;
import AST.Modifier;
import AST.Modifiers;
import AST.OpenCompositeLayerDecl;
import AST.Opt;
import AST.Program;
import AST.TypeAccess;
import AST.TypeDecl;
import jcop.generation.jcopaspect.JCopAspect;
import jcop.generation.layers.OpenCompositeLayerDeclGenerator;
import jcop.lang.JCop;

public class OpenCompositeLayerDeclTransformer extends Transformer {
	private OpenCompositeLayerDecl compositeLayer;
	private OpenCompositeLayerDeclGenerator gen;
	
	public OpenCompositeLayerDeclTransformer(OpenCompositeLayerDecl compositeLayer){
		this.compositeLayer = compositeLayer;
		this.gen = new OpenCompositeLayerDeclGenerator(compositeLayer);
	}
	
	@Override
	protected ASTNode transform() {
		
		Modifiers modifiers = new Modifiers();
		modifiers.addModifier(new Modifier("public"));
		
		
		List<BodyDecl> bodyList = new List<BodyDecl>();
		bodyList.add(gen.generateMatchMethod());
		bodyList.add(gen.generateCollectInfoMethod());
		compositeLayer.hostLayer().setBodyDeclList(bodyList);
		/*
		TypeDecl classDecl = new ClassDecl(
				modifiers,
				compositeLayer.getID(),
				new Opt<Access>(inh),//extends CompositeLayer
				new List<Access>(),
				bodyList
				);
		*/
		//先假设CompositeRule中都是采用package+name的方式new出行为层，否则import不好处理
		//又或者委托给生成collectInfo方法体内容的方法来完成，再议
		
		ASTNode program = compositeLayer.hostType();
		while(!(program instanceof Program)){
			program = program.getParent();
		}
		
		return gen.createDummyNode();
	}

}
