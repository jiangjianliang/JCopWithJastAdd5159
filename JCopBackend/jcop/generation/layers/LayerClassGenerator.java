/**
 * Copyright (c) 2009 Software Architecture Group
 *                    Hasso-Plattner-Institute, Potsdam, Germany
 *  
 * Permission is hereby granted, free of charge, to any person
 * obtaining a copy of this software and associated documentation
 * files (the "Software"), to deal in the Software without
 * restriction, including without limitation the rights to use,
 * copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the
 * Software is furnished to do so, subject to the following
 * conditions:
 * 
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 * 
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
 * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
 * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
 * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
 * OTHER DEALINGS IN THE SOFTWARE.
 */
package jcop.generation.layers;

import static jcop.Globals.Types.*;
//XXX by wander
//import com.sun.xml.internal.bind.v2.model.core.MaybeElement;

import jcop.Globals;
import jcop.Globals.ID;
import jcop.Globals.Types;
import jcop.compiler.JCopTypes.JCopAccess;
import jcop.generation.Generator;
import jcop.transformation.ASTTools.Generation;
import jcop.transformation.lookup.Lookup;
import AST.Access;
import AST.ArrayDecl;
import AST.ArrayTypeAccess;
import AST.ClassDecl;
import AST.Dot;
import AST.Expr;
import AST.ExprStmt;
import AST.FieldDeclaration;
import AST.LayerDecl;
import AST.LayerDeclaration;
import AST.List;
import AST.MemberDecl;
import AST.MethodDecl;
import AST.Modifiers;
import AST.NamedMember;
import AST.ParTypeAccess;
import AST.ParameterDeclaration;
import AST.PrimitiveType;
import AST.ReturnStmt;
import AST.Stmt;
import AST.SuperAccess;
import AST.ThisAccess;
import AST.TypeAccess;
import AST.TypeDecl;
import AST.VarAccess;

/**
 * Documented by wander
 * 
 * <pre>
 * generate constructs for {@link LayerDeclaration}
 * </pre>
 */
public class LayerClassGenerator extends LayerGenerator {
	protected LayerDeclaration layerDecl;

	private String layerName;

	public LayerClassGenerator(LayerDeclaration layerDecl) {
		super(Lookup.lookupLayerClassDecl(layerDecl));
		this.layerName = layerDecl.getID();
		this.layerDecl = layerDecl;
	}

	public String getLayerName() {
		return layerName;
	}

	public LayerDeclaration getLayerDecl() {
		return layerDecl;
	}

	public ParameterDeclaration createLayerParam() {
		return new ParameterDeclaration(createLayerTypeAccess(),
				ID.layerParameterName);
	}

	/**
	 * generate delegation method named{@code <generatedMethodName>} for
	 * {@code <baseMethodDecl>}
	 * 
	 * @param baseFieldDecl
	 * @param generatedMethodName
	 * @param delegation
	 * @return
	 */
	public MethodDecl createDelegationMethod(FieldDeclaration baseFieldDecl,
			String generatedMethodName, Expr delegation) {
		Modifiers modifiers = getTransformedModifiersFor(baseFieldDecl);
		return new MethodDecl(modifiers,
				getLayeredStateAccessFor(baseFieldDecl), generatedMethodName,
				getTransformedParamsFor(baseFieldDecl), new List<Access>(),
				genOptBlock(new ReturnStmt(delegation)));
	}

	/**
	 * generate delegation method named {@code <generatedMethodName>} for
	 * {@code <baseMethodDecl>}
	 * 
	 * @param baseMethodDecl
	 * @param generatedMethodName
	 * @param delegation
	 * @return
	 */
	public MethodDecl generateDelegationMethod(MethodDecl baseMethodDecl,
			String generatedMethodName, Expr delegation) {
		Modifiers modifiers = getTransformedModifiersFor(baseMethodDecl);
		Access typeAccess = transformToFullQualified(baseMethodDecl
				.getTypeAccess());
		List<ParameterDeclaration> params = getTransformedParamsFor(baseMethodDecl);

		MethodDecl method = new MethodDecl(
				modifiers,
				typeAccess,
				// (Access)baseMethodDecl.getTypeAccess().fullCopy(),
				generatedMethodName,
				// TODO: fqn for partial fields
				params,
				transformToFullQualifiedList(baseMethodDecl.getExceptionList()),
				genOptBlock(maybeGenerateReturnStmt(baseMethodDecl, delegation)));

		return method;
		// new Opt<Block>(new Block(new List<Stmt>().add(
		// originalMethodDeclaration.isVoid() ? new ExprStmt(delegation) : new
		// ReturnStmt(delegation)))));
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

	private Access createTargetAccess(MemberDecl baseMemberDecl) {
		// return baseMemberDecl.hostType().topLevelType().createBoundAccess();
		TypeDecl layerClass = baseMemberDecl.hostType();
		// TypeDecl layerClass = this.layerClass;
		// String pckgName = layerClass.hostPackage();
		//
		// // String layerName = layerClass.isTopLevelType()
		// // ? baseMemberDecl.hostType().getFullQualifiedName() // should be
		// fqn
		// // : layerClass.enclosingType().hostType().name();

		// String name = pckgName.replaceFirst(pckgName, layerClass.getID());

		return layerClass.createQualifiedAccess();// new
													// TypeAccess("pckg","TestClass").qualifiesAccess(new
													// member"NestedClass");

	}

	/**
	 * WANDER 这里的修改不是很确定
	 * 
	 * @param toBeExtended
	 * @return
	 */
	protected List<Expr> genLayerParams(List<Expr> toBeExtended) {
		toBeExtended.insertChild(new ThisAccess(ID.layerParameterName), 0);
		// toBeExtended.insertChild(new VarAccess(ID.layerProxyParameterName),
		// 1);
		// toBeExtended.insertChild(new VarAccess(ID.composition), 2);
		toBeExtended.insertChild(new VarAccess(ID.wander_CurrentLayerIndex), 1);
		toBeExtended.insertChild(new VarAccess(ID.wander_Composition), 2);
		return toBeExtended;
	}

	private List<ParameterDeclaration> getTransformedParamsFor(
			FieldDeclaration baseFieldDecl) {
		return getTransformedParamsFor(baseFieldDecl,
				new List<ParameterDeclaration>());
	}

	/**
	 * generate list of {@link ParameterDeclaration} for baseMethodDecl.
	 * 
	 * @param baseMethodDecl
	 * @return
	 */
	protected List<ParameterDeclaration> getTransformedParamsFor(
			MethodDecl baseMethodDecl) {
		List<ParameterDeclaration> params = baseMethodDecl.getParameterList();
		params = transformToFullQualified(params);
		return getTransformedParamsFor(baseMethodDecl, params);
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
	 * WANDER
	 * 
	 * @param baseMemberDecl
	 * @param params
	 * @return
	 */
	private List<ParameterDeclaration> getTransformedParamsFor(
			MemberDecl baseMemberDecl, List<ParameterDeclaration> params) {
		List<ParameterDeclaration> newParams = params.fullCopy();
		newParams.insertChild(new ParameterDeclaration(
				createTargetAccess(baseMemberDecl), ID.targetParameterName), 0);

		// newParams.insertChild(
		// new ParameterDeclaration(JCopAccess.get(LAYER_PROXY),
		// ID.layerProxyParameterName), 1);
		// newParams.insertChild(
		// new ParameterDeclaration(JCopAccess.get(COMPOSITION),
		// ID.composition), 2);

		newParams.insertChild(new ParameterDeclaration(
				createCurrentIndexAccess(), ID.wander_CurrentLayerIndex), 1);
		newParams.insertChild(new ParameterDeclaration(createListAccess(),
				ID.wander_Composition), 2);
		return newParams;
	}

	// begin new-feature
	private Access createCurrentIndexAccess() {
		return new TypeAccess("int");
	}

	private Access createListAccess() {
		return new ArrayTypeAccess(new TypeAccess(Globals.jcopPackage,
				Types.LAYER_PROXY));
	}

	// end new-feature

	private Modifiers getTransformedModifiersFor(NamedMember method) {
		Modifiers modifiers = createPublicModifierFor(method);
		modifiers.addModifier(genAnnotation(DELEGATION_METHOD_ANNOTATION));
		return Generation.removeStaticModifier(modifiers);
	}

	private Access getLayeredStateAccessFor(FieldDeclaration fieldDecl) {
		Access fieldAccess = (fieldDecl.type().isPrimitive()) ? ((PrimitiveType) fieldDecl
				.type()).boxed().createQualifiedAccess() : fieldDecl.type()
				.createQualifiedAccess();
		ParTypeAccess access = new ParTypeAccess(JCopAccess.get(LAYERED_STATE),
				new List<Access>().add(fieldAccess));
		return access;
	}

	public List<Expr> generateArgsFromLayeredMethod(
			List<ParameterDeclaration> parameters) {
		List<Expr> args = generateArgs(parameters);
		addSuperLayerParams(args);
		return args;
	}

	/**
	 * generate delegation method in superlayer.
	 * 
	 * <pre>
	 * <code>
	 *   __superlayer__{@code <genMethodName>}
	 * </code>
	 * </pre>
	 * 
	 * @param originalMethodDecl
	 * @param genMethodName
	 * @return
	 */
	public MethodDecl genDelegationMethodToSuperLayer(
			MethodDecl originalMethodDecl, String genMethodName) {
		List<Expr> args = generateArgsFromLayeredMethod(originalMethodDecl
				.getParameterList());
		String methodName = Globals.ID.superlayer + genMethodName;
		Expr body = new SuperAccess().qualifiesAccess(createMethodAccess(
				genMethodName, args));
		MethodDecl method = generateDelegationMethod(originalMethodDecl,
				methodName, body);
		return method;
	}

	/**
	 * WANDER 这里的修改不是很确定
	 * 
	 * @param args
	 */
	private void addSuperLayerParams(List<Expr> args) {
		args.insertChild(new VarAccess(ID.targetParameterName), 0);
		// args.insertChild(new VarAccess(ID.layerProxyParameterName), 1);
		// args.insertChild(new VarAccess(ID.composition), 2);
		args.insertChild(new VarAccess(ID.wander_CurrentLayerIndex), 1);
		args.insertChild(new VarAccess(ID.wander_Composition), 2);
	}
}
