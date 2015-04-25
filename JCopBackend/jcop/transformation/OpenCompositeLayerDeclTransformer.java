package jcop.transformation;

import static jcop.Globals.Types.LAYERED_METHOD_ANNOTATION;

import java.util.HashSet;
import java.util.Set;

import AST.ASTNode;
import AST.Block;
import AST.BodyDecl;
import AST.CompositeMemberDecl;
import AST.CompositeMethodDecl;
import AST.CompositeRuleDecl;
import AST.ImportDecl;
import AST.List;
import AST.MemberDecl;
import AST.MethodDecl;
import AST.Modifier;
import AST.Modifiers;
import AST.OpenCompositeLayerDecl;
import AST.Program;
import AST.SimpleSet;
import AST.TypeDecl;
import jcop.Globals;
import jcop.VisitedNodes;
import jcop.Globals.Types;
import jcop.generation.compositelayer.BaseMethodGenerator;
import jcop.generation.compositelayer.ConcreteLayerClassGenerator;
import jcop.generation.compositelayer.OpenCompositeLayerDeclGenerator;
import jcop.generation.compositelayer.RootLayerClassGenerator;
import jcop.generation.jcopaspect.JCopAspect;
import jcop.transformation.lookup.Lookup;

public class OpenCompositeLayerDeclTransformer extends Transformer {
	private OpenCompositeLayerDecl compositeLayer;
	private OpenCompositeLayerDeclGenerator gen;
	private TypeDecl hostType;
	
	//to check delegation method
	private static HashSet<Object> delegationMethodGeneration = new HashSet<Object>();
	
	public OpenCompositeLayerDeclTransformer(OpenCompositeLayerDecl compositeLayer){
		this.compositeLayer = compositeLayer;
		this.gen = new OpenCompositeLayerDeclGenerator(compositeLayer);
		this.hostType = compositeLayer.hostType();
	}
	
	@Override
	protected ASTNode transform() {
		
		CompositeLayerMemberSets members = collectRuleAndMethod();
		
		transformCompositeMethod(members.getMethods());
		
		Modifiers modifiers = new Modifiers();
		modifiers.addModifier(new Modifier("public"));
		
		
		List<BodyDecl> bodyList = new List<BodyDecl>();
		bodyList.add(gen.generateMatchMethod());
		bodyList.add(gen.generateCollectInfoMethod(members.getRules()));
		
		compositeLayer.hostLayer().setBodyDeclList(bodyList);
		
		//先假设CompositeRule中都是采用package+name的方式new出行为层，否则import不好处理
		//以后再或者委托给生成collectInfo方法体内容的方法来完成，再议
		
		return gen.createDummyNode();
	}
	
	
	/**
	 * collect all the composite rules and composite method
	 * @return
	 */
	private CompositeLayerMemberSets collectRuleAndMethod(){
		Set<CompositeRuleDecl> rules = new HashSet<CompositeRuleDecl>();
		Set<CompositeMethodDecl> methods = new HashSet<CompositeMethodDecl>();
		for(CompositeMemberDecl member : compositeLayer.getCompositeMemberDeclList()){
			if(member instanceof CompositeRuleDecl){
				rules.add((CompositeRuleDecl) member);
			}else if(member instanceof CompositeMethodDecl){
				methods.add((CompositeMethodDecl) member);
			}
		}
		
		return new CompositeLayerMemberSets(rules, methods);
	}
	
	/**
	 * 
	 * @param methods
	 */
	private void transformCompositeMethod(Set<CompositeMethodDecl> methods){
		for(CompositeMethodDecl compositeMethod: methods){
			MethodDecl baseMethod = Lookup.lookupMethodCorrespondingTo(compositeMethod);
			if(baseMethod == null){
				System.err.println("cannot find base method for composite method "+compositeMethod.signature());
			}else{
				BaseMethodGenerator baseGen = new BaseMethodGenerator(compositeMethod, baseMethod);
				if(baseDeclaredInSuper(baseMethod, compositeMethod)){
					baseMethod = createBaseMethod(baseGen, true);//baseMethod以后有用吗？
				}
				if(!hasLayeredMethod(baseGen, baseMethod)){
					createLayeredMethod(baseGen);
				}
				if (VisitedNodes.firstVisit(baseMethod)){
					transformBaseMethod(baseGen, baseMethod);				
				}
				if(VisitedNodes.firstVisit(hostType)){
					addImportsOfLayerDeclToHost(hostType);
				}
				createDelegationMethods(baseGen, compositeMethod, baseMethod);
				
			}
		}
	}
	//
	/**
	 * 
	 */
	private boolean baseDeclaredInSuper(MethodDecl baseMethod, CompositeMethodDecl compositeMethod){
		return baseMethod.hostType() != compositeMethod.hostType();
	}
	
	protected MethodDecl createBaseMethod(BaseMethodGenerator baseGen, boolean baseDeclaredInSuper) {
		MethodDecl baseMethod = baseGen
				.generateBaseMethod(baseDeclaredInSuper);
		baseGen.setBaseMethod(baseMethod);
		
		addBodyDeclTo(baseMethod, hostType);
		return baseMethod;
	}
	//
	private boolean hasLayeredMethod(BaseMethodGenerator baseGen, MethodDecl baseMethod) {
		String sig = baseGen.genWrapperSignature();
		SimpleSet candidates = baseMethod.hostType().methodsSignature(sig);

		return (!candidates.isEmpty());
	}
	
	/**
	 * generate layered method in hostType
	 * 
	 * @return
	 * @see #addBodyDeclToEnclosing(MethodDecl)
	 */
	public MethodDecl createLayeredMethod(BaseMethodGenerator baseGen) {
		MethodDecl wrapper = baseGen.generateWrapper();
		addBodyDeclTo(wrapper, hostType);
		return wrapper;
	}
	
	//
	/**
	 * generate base method body with {@link BaseMethodGenerator}
	 * 
	 * @see BaseMethodGenerator#generateLayerActivationBlock()
	 * @see BaseMethodGenerator#genAnnotation(String)
	 */
	public void transformBaseMethod(BaseMethodGenerator baseGen, MethodDecl baseMethod) {
		Block activationBlock = baseGen.generateLayerActivationBlock();
		Modifiers m = baseGen.createPublicModifierFor(baseMethod);
		m.addModifier(baseGen.genAnnotation(LAYERED_METHOD_ANNOTATION));
		baseMethod.setModifiers(m);
		baseMethod.setBlock(activationBlock);
	}
	
	//
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
	
	private List<ImportDecl> getImportsofHostLayer() {
		return compositeLayer.hostLayer().compilationUnit().getImportDeclList();
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

	//
	/**
	 * generate delegation methods with {@link PartialMethodGenerator} and
	 * {@link BaseMethodGenerator}
	 * 
	 * @see PartialMethodGenerator#genDelegationMethodName(MethodDecl);
	 * @see BaseMethodGenerator#genWrapperIdentifier();
	 */
	public void createDelegationMethods(BaseMethodGenerator baseGen, CompositeMethodDecl compositeMethod, MethodDecl baseMethod) {
		String genMethodName = baseGen
				.generateDelegationMethodName(compositeMethod);
		
		if (firstDelegationMethodGeneration(baseMethod)) {
			generateDelegationMethodInConcreteLayer(compositeMethod, genMethodName);
			generateDelegationMethodInLayer(compositeMethod, genMethodName,
					baseGen.genWrapperIdentifier());
		}
		//TODO generateDelegationMethodsInInstanceLayer(genMethodName);
	}
	
	/**
	 * put member into {@link jcop.generation.jcopaspect.JCopAspect JCopAspect}
	 * 's visitedMemberForDelegation, and check whether member is first visited
	 * by delegationMethodGeneration
	 * 
	 * @param member
	 * @return
	 * 
	 * @see jcop.generation.jcopaspect.JCopAspect#addVisitedMemberForDelegation(MemberDecl)
	 * @see #delegationMethodGeneration
	 */
	protected boolean firstDelegationMethodGeneration(MemberDecl member) {
		JCopAspect.getInstance().addVisitedMemberForDelegation(member);
		return (firstVisit(delegationMethodGeneration, member));
	}
	
	/**
	 * check whether set contains field, if not, put field into set and return
	 * false;
	 * 
	 * @param set
	 * @param field
	 * @return
	 */
	private boolean firstVisit(HashSet<Object> set, MemberDecl field) {
		if (set.contains(field))
			return false;
		else {
			set.add(field);
			return true;
		}
	}
	
	
	/**
	 * generate {@link jcop.lang.Layer Layer}'s delegation method with
	 * {@link RootLayerClassGenerator}
	 * 
	 * @param compositeMethod
	 * @param genMethodName
	 * @param methodToBeCalled
	 * 
	 * @see RootLayerClassGenerator#genDelegationMethod(MethodDecl, String,
	 *      String)
	 * @see RootLayerClassGenerator#genDelegationMethodToSuperLayer(MethodDecl,
	 *      String)
	 */
	protected void generateDelegationMethodInLayer(CompositeMethodDecl compositeMethod, String genMethodName,
			String methodToBeCalled) {
		RootLayerClassGenerator gen = new RootLayerClassGenerator(compositeLayer);

		MethodDecl delegatee = gen.genDelegationMethod(compositeMethod,
				genMethodName, methodToBeCalled);
		
		addDelegationMethodToLayer(delegatee, Types.LAYER);

		MethodDecl superLayerMethod = gen.genDelegationMethodToSuperLayer(
				compositeMethod, methodToBeCalled);
		addDelegationMethodToLayer(superLayerMethod, Types.LAYER);
	}
	
	/**
	 * add delegationMethod to layer
	 * 
	 * @param delegationMethod
	 * @param layername
	 */
	protected void addDelegationMethodToLayer(MethodDecl delegationMethod,
			String layername) {
		TypeDecl decl = hostType.lookupType(Globals.jcopPackage,
				layername);
		addBodyDeclTo(delegationMethod, decl);
	}
	
	/**
	 * generate {@link jcop.lang.ConcreteLayer ConcreteLayer}'s delegation
	 * method with {@link ConcreteLayerClassGenerator}
	 * 
	 * @param compositeMethod
	 * @param genMethodName
	 * 
	 * @see ConcreteLayerClassGenerator#genDelegationMethod(MethodDecl, String)
	 */
	private void generateDelegationMethodInConcreteLayer(CompositeMethodDecl compositeMethod, String genMethodName) {
		ConcreteLayerClassGenerator gen = new ConcreteLayerClassGenerator(
				compositeLayer);
		MethodDecl method = gen.genDelegationMethod(compositeMethod,
				genMethodName);
		addDelegationMethodToLayer(method, Types.CONCRETE_LAYER);
	}
	
	
	
	class CompositeLayerMemberSets {

		private Set<CompositeRuleDecl> rules;
		private Set<CompositeMethodDecl> methods;

		public CompositeLayerMemberSets(Set<CompositeRuleDecl> rules, Set<CompositeMethodDecl> methods){
			this.rules = rules;
			this.methods = methods;
		}

		public Set<CompositeRuleDecl> getRules() {
			return rules;
		}

		public Set<CompositeMethodDecl> getMethods() {
			return methods;
		}
		
	}
}
