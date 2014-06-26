package jcop.transformation;

import static jcop.Globals.Types.LAYERED_METHOD_ANNOTATION;
import jcop.VisitedNodes;
import jcop.Globals.Types;
import jcop.generation.layermembers.BaseMethodGenerator;
import jcop.generation.layermembers.PartialMethodGenerator;
import jcop.generation.layermembers.PartialMethodGroupGenerator;
import jcop.generation.layers.ConcreteLayerClassGenerator;
import jcop.generation.layers.InstanceLayerClassGenerator;
import jcop.generation.layers.NewInstanceLayerClassGenerator;
import jcop.generation.layers.RootLayerClassGenerator;
import jcop.transformation.lookup.Lookup;
import AST.Block;
import AST.ImportDecl;
import AST.LayerDeclaration;
import AST.List;
import AST.MethodDecl;
import AST.Modifiers;
import AST.SimpleSet;
import AST.Stmt;
import AST.TypeDecl;

/**
 * PartialMethodGroupSourceTransformer is not a subclass of
 * PartialMethodTransformer but LayerMemberTransformer WANDER 6-9
 * 
 * @author wander
 * 
 */
public class PartialMethodGroupSourceTransformer extends LayerMemberTransformer {

	protected PartialMethodGroupGenerator partialGroupGen;
	protected BaseMethodGenerator baseGen;
	protected MethodDecl partialMethodGroup;
	protected MethodDecl baseMethod;

	public PartialMethodGroupSourceTransformer(LayerDeclaration openLayer,
			MethodDecl partialMethodGroup) {
		super(openLayer);
		this.partialMethodGroup = partialMethodGroup;
		this.baseMethod = Lookup
				.lookupMethodCorrespondingTo(partialMethodGroup);
		this.partialGroupGen = new PartialMethodGroupGenerator(openLayer,
				partialMethodGroup);
		this.baseGen = new BaseMethodGenerator(partialMethodGroup, baseMethod);
	}

	protected java.util.List transformTemp() {

		if (isLayerLocalMethod()) {
			System.out.println("partial method group cannot be layer-local");
		}

		if (baseMethodIsDeclaredInSuperClass()) {
			createBaseMethod();
		}

		if (!hasLayeredMethod()) {
			createLayeredMethod();
		}

		if (VisitedNodes.firstVisit(baseMethod)) {
			transformBaseMethod();
		}

		TypeDecl hostType = partialMethodGroup.hostType();
		if (VisitedNodes.firstVisit(hostType)) {
			transformHostTypeModifier(hostType);
			addImportsOfLayerDeclToHost(hostType);
		}

		// 这里生成的代理方法与原有partialmethod不同
		createDelegationMethods();

		java.util.List l = createPartialMethodGroup();
		layerClass.resetCache();
		return l;
	}

	@Override
	protected List transform() {
		return null;
		/*
		 * if(isLayerLocalMethod() ){
		 * System.out.println("partial method group cannot be layer-local"); }
		 * 
		 * if(baseMethodIsDeclaredInSuperClass()){ createBaseMethod(); }
		 * //无法理解这个是干什么用的 if(!hasLayeredMethod()){ createLayeredMethod(); }
		 * 
		 * if(VisitedNodes.firstVisit(baseMethod)){ transformBaseMethod(); }
		 * 
		 * TypeDecl hostType = partialMethodGroup.hostType();
		 * if(VisitedNodes.firstVisit(hostType)){
		 * transformHostTypeModifier(hostType);
		 * addImportsOfLayerDeclToHost(hostType); }
		 * 
		 * //这里生成的代理方法与原有partialmethod不同 createDelegationMethods();
		 * 
		 * List l = createPartialMethodGroup(); layerClass.resetCache(); return
		 * l;
		 */
	}

	private boolean isLayerLocalMethod() {
		return baseMethod == null;
	}

	/**
	 * check whether base method declared in supper class of host type
	 * 
	 * @return
	 */
	private boolean baseMethodIsDeclaredInSuperClass() {
		if (!isLayerLocalMethod())
			return (baseMethod.hostType() != partialMethodGroup.hostType());
		else
			return false;
	}

	protected void createBaseMethod() {
		baseMethod = baseGen
				.generateBaseMethod(baseMethodIsDeclaredInSuperClass());
		baseGen.setBaseMethod(baseMethod);
		partialGroupGen.setBaseMethod(baseMethod);
		// malte: brauche ich das?
		// setBaseForPartialMethod(baseMethod, partialMethod);
		addBodyDeclToEnclosing(baseMethod);
	}

	private boolean hasLayeredMethod() {
		MethodDecl method = baseMethod;

		String sig = baseGen.genWrapperSignature();
		SimpleSet candidates = method.hostType().methodsSignature(sig);

		return (!candidates.isEmpty());
	}

	public MethodDecl createLayeredMethod() {
		MethodDecl wrapper = baseGen.generateWrapper();
		addBodyDeclToEnclosing(wrapper);
		return wrapper;
	}

	protected void addBodyDeclToEnclosing(MethodDecl decl) {
		TypeDecl host = getHostType();
		addBodyDeclTo(decl, host);
	}

	private TypeDecl getHostType() {
		return partialMethodGroup.hostType();// .topLevelType();
	}

	/**
	 * generate base method body with {@link BaseMethodGenerator}
	 * 
	 * @see BaseMethodGenerator#generateLayerActivationBlock()
	 * @see BaseMethodGenerator#genAnnotation(String)
	 */
	public void transformBaseMethod() {
		MethodDecl baseMethod = this.baseMethod;
		Block activationBlock = baseGen.generateLayerActivationBlock();
		Modifiers m = this.baseGen.createPublicModifierFor(baseMethod);
		m.addModifier(baseGen.genAnnotation(LAYERED_METHOD_ANNOTATION));
		baseMethod.setModifiers(m);
		baseMethod.setBlock(activationBlock);
	}

	private void transformHostTypeModifier(TypeDecl hostType) {
		if (!hostType.isPublic())
			hostType.setModifiers(partialGroupGen
					.createPublicModifierFor(hostType));

	}

	/**
	 * add imports of LayerDecl to hostType
	 * 
	 * @param hostType
	 */
	private void addImportsOfLayerDeclToHost(TypeDecl hostType) {
		for (ImportDecl im : getImportsofHostLayer()) {
			if (!importOfThatType(im, hostType))
				hostType.compilationUnit().addImportDecl(im);
		}
	}

	/**
	 * check whether im({@link ImportDecl}) is in thatType({@link ImportDecl})
	 * 
	 * @param im
	 * @param thatType
	 * @return
	 */
	private boolean importOfThatType(ImportDecl im, TypeDecl thatType) {
		return im.getAccess().type() == thatType;
	}

	private List<ImportDecl> getImportsofHostLayer() {
		return openLayer.hostLayer().compilationUnit().getImportDeclList();
	}

	/**
	 * generate delegation methods with {@link PartialMethodGenerator} and
	 * {@link BaseMethodGenerator}
	 * 
	 * @see PartialMethodGenerator#genDelegationMethodName(MethodDecl);
	 * @see BaseMethodGenerator#genWrapperIdentifier();
	 */
	public void createDelegationMethods() {
		// TODO 需要仔细考虑pmg的代理方法与pm的代理方法是否一致
		String genMethodName = partialGroupGen
				.genDelegationMethodName(partialMethodGroup);
		if (firstDelegationMethodGeneration(baseMethod)) {
			generateDelegationMethodInConcreteLayer(genMethodName);
			generateDelegationMethodInLayer(genMethodName,
					baseGen.genWrapperIdentifier());
		}
		generateDelegationMethodsInInstanceLayer(genMethodName);
	}

	/**
	 * generate {@link jcop.lang.Layer Layer}'s delegation method with
	 * {@link RootLayerClassGenerator}
	 * 
	 * @param genMethodName
	 * @see RootLayerClassGenerator#genDelegationMethod(MethodDecl, String,
	 *      String)
	 * @see RootLayerClassGenerator#genDelegationMethodToSuperLayer(MethodDecl,
	 *      String)
	 */
	protected void generateDelegationMethodInLayer(String genMethodName,
			String methodToBeCalled) {
		RootLayerClassGenerator gen = new RootLayerClassGenerator(openLayer);

		MethodDecl delegatee = gen.genDelegationMethod(partialMethodGroup,
				genMethodName, methodToBeCalled);
		addDelegationMethodToLayer(delegatee, Types.LAYER);

		MethodDecl superLayerMethod = gen.genDelegationMethodToSuperLayer(
				partialMethodGroup, methodToBeCalled);
		addDelegationMethodToLayer(superLayerMethod, Types.LAYER);
	}

	/**
	 * generate {@link jcop.lang.ConcreteLayer ConcreteLayer}'s delegation
	 * method with {@link ConcreteLayerClassGenerator}
	 * 
	 * @param genMethodName
	 * 
	 * @see ConcreteLayerClassGenerator#genDelegationMethod(MethodDecl, String)
	 */
	private void generateDelegationMethodInConcreteLayer(String genMethodName) {
		ConcreteLayerClassGenerator gen = new ConcreteLayerClassGenerator(
				openLayer);
		MethodDecl method = gen.genDelegationMethod(partialMethodGroup,
				genMethodName);
		addDelegationMethodToLayer(method, Types.CONCRETE_LAYER);
	}

	/**
	 * 
	 * 
	 * @param genMethodName
	 */
	private void generateDelegationMethodsInInstanceLayer(String genMethodName) {
		NewInstanceLayerClassGenerator gen = new NewInstanceLayerClassGenerator(
				openLayer);
		MethodDecl method = gen.genDelegationMethod(partialMethodGroup,
				genMethodName);
		addBodyDeclTo(method, layerClass);
		// WANDER 6-17 这里没有想清楚superlayer的内容
		MethodDecl superLayerMethod = gen.genDelegationMethodToSuperLayer(
				partialMethodGroup, genMethodName);
		addBodyDeclTo(superLayerMethod, layerClass);

		/*
		 * WANDER 6-17 it seems to be unnecessary Stmt metaInit =
		 * partialGroupGen.genPartialMethodMetaClassInstantiation();
		 * addToInit(metaInit);
		 */
	}

	/**
	 * 
	 * @return
	 * @seee PartialMethodGroupGenerator#generatePartialMethodGroup();
	 */
	private java.util.List createPartialMethodGroup() {
		return partialGroupGen.generatePartialMethodGroup();
	}

}
