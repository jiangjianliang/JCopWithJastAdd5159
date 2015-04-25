package jcop.generation.compositelayer;

import static jcop.Globals.Types.*;
import AST.Access;
import AST.ArrayDecl;
import AST.CompositeLayerDeclaration;
import AST.CompositeMethodDecl;
import AST.Expr;
import AST.List;
import AST.MethodDecl;
import AST.Modifiers;
import AST.ParameterDeclaration;
import AST.TypeAccess;
import AST.TypeDecl;
import jcop.Globals.ID;
import jcop.compiler.JCopTypes.JCopAccess;
import jcop.generation.Generator;
import jcop.transformation.ASTTools.Generation;

public class LayerClassGenerator extends Generator {
	protected CompositeLayerDeclaration compositeLayer;
	
	public LayerClassGenerator(CompositeLayerDeclaration compositeLayer){
		this.compositeLayer = compositeLayer;
	}
	/**
	 * generate delegation method named {@code <generatedMethodName>} for {@code <baseMethodDecl>}
	 * 
	 * @param compositeMethod
	 * @param generatedMethodName
	 * @param delegation
	 * @return
	 */
	public MethodDecl generateDelegationMethod(CompositeMethodDecl compositeMethod,
			String generatedMethodName, Expr delegation) {
		Modifiers modifiers = getTransformedModifiersFor(compositeMethod);
		Access typeAccess = transformToFullQualified(compositeMethod
				.getTypeAccess());
		List<ParameterDeclaration> params = getTransformedParamsFor(compositeMethod);

		MethodDecl method = new MethodDecl(
				modifiers,
				typeAccess,
				generatedMethodName,
				params,
				transformToFullQualifiedList(compositeMethod.getExceptionList()),
				genOptBlock(maybeGenerateReturnStmt(compositeMethod, delegation)));

		return method;
	}
	
	private Modifiers getTransformedModifiersFor(CompositeMethodDecl method) {
		Modifiers modifiers = createPublicModifierFor(method);
		modifiers.addModifier(genAnnotation(DELEGATION_METHOD_ANNOTATION));
		return Generation.removeStaticModifier(modifiers);
	}
	
	/**
	 * transform {@link Access} into full-qualified {@link Access}
	 * 
	 * @param access
	 * @return
	 */
	public Access transformToFullQualified(Access access) {
		TypeDecl type = access.type();

		if (type.isPrimitive())
			new TypeAccess(type.name());
		if (access.isQualified())
			return (Access) access.fullCopy();// type.createQualifiedAccess();
		if (type.isArrayDecl())
			return ((ArrayDecl) type).createBoundAccess();
		else
			return new TypeAccess(type.packageName(), type.name());
	}
	
	/**
	 * generate list of {@link ParameterDeclaration} for baseMethodDecl.
	 * 
	 * @param compositeMethod
	 * @return
	 */
	protected List<ParameterDeclaration> getTransformedParamsFor(
			CompositeMethodDecl compositeMethod) {
		List<ParameterDeclaration> params = compositeMethod.getParameterList();
		params = transformToFullQualified(params);
		return getTransformedParamsFor(compositeMethod, params);
	}
	
	/**
	 * generate list of full-qualified {@link ParameterDeclaration} for list of
	 * {@link ParameterDeclaration}
	 * 
	 * @param params
	 * @return
	 */
	public List<ParameterDeclaration> transformToFullQualified(
			List<ParameterDeclaration> params) {
		List<ParameterDeclaration> newParams = new List<ParameterDeclaration>();
		List<ParameterDeclaration> copyOfParams = params.fullCopy();
		for (int i = 0; i < copyOfParams.getNumChildNoTransform(); i++) {
			ParameterDeclaration oldParam = copyOfParams.getChild(i);
			// String packageName = oldParam.type().packageName();
			// String typeName = oldParam.getTypeAccess().type().name();
			//
			ParameterDeclaration newParam = new ParameterDeclaration(
					transformToFullQualified(oldParam.getTypeAccess()),
					// new TypeAccess(packageName, typeName),
					oldParam.getID());
			newParams.add(newParam);
			// System.out.println(newParam);
		}
		return newParams;
	}
	
	/**
	 * 
	 * @param baseMemberDecl
	 * @param params
	 * @return
	 */
	private List<ParameterDeclaration> getTransformedParamsFor(
			CompositeMethodDecl compositeMethod, List<ParameterDeclaration> params) {
		List<ParameterDeclaration> newParams = params.fullCopy();
		newParams.insertChild(new ParameterDeclaration(
				createTargetAccess(compositeMethod), ID.targetParameterName), 0);
		newParams.insertChild(
				new ParameterDeclaration(JCopAccess.get(LAYER_PROXY),
						ID.layerProxyParameterName), 1);
		newParams.insertChild(
				new ParameterDeclaration(JCopAccess.get(COMPOSITION),
						ID.composition), 2);
		return newParams;
	}
	
	private Access createTargetAccess(CompositeMethodDecl compositeMethod) {
		TypeDecl layerClass = compositeMethod.hostType();

		return layerClass.createQualifiedAccess();

	}
	
	
	/**
	 * transform list of {@link Access} into list of {@link Access}, but latter
	 * is full-qualified
	 * 
	 * @param accessList
	 * @return
	 */
	private List<Access> transformToFullQualifiedList(List<Access> accessList) {
		List<Access> fqAccessList = new List<Access>();
		for (Access access : accessList)
			fqAccessList.add(transformToFullQualified(access));
		return fqAccessList;
	}
}
