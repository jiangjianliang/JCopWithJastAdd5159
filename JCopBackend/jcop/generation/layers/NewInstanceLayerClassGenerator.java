package jcop.generation.layers;

import static jcop.Globals.Types.*;

import jcop.Globals.ID;
import jcop.compiler.JCopTypes.JCopAccess;
import jcop.transformation.ASTTools.Generation;
import AST.Access;
import AST.Block;
import AST.BreakStmt;
import AST.CastExpr;
import AST.ConstCase;
import AST.DefaultCase;
import AST.Dot;
import AST.Expr;
import AST.ExprStmt;
import AST.IntegerLiteral;
import AST.LayerDecl;
import AST.LayerDeclaration;
import AST.List;
import AST.MethodAccess;
import AST.MethodDecl;
import AST.Modifiers;
import AST.NamedMember;
import AST.Opt;
import AST.ParameterDeclaration;
import AST.PartialMethodGroupDecl;
import AST.PrimitiveTypeAccess;
import AST.ReturnStmt;
import AST.Stmt;
import AST.SwitchStmt;
import AST.ThisAccess;
import AST.TypeAccess;
import AST.VarAccess;
import AST.VariableDeclaration;

public class NewInstanceLayerClassGenerator extends LayerClassGenerator {
	private LayerDecl topLevelLayer;

	public NewInstanceLayerClassGenerator(LayerDecl layerDecl) {
		super(layerDecl);
		topLevelLayer = layerDecl;
	}

	public NewInstanceLayerClassGenerator(LayerDeclaration layerDeclaration) {
		super(layerDeclaration);
	}

	/**
	 * 
	 * @param partialMethodGroup
	 * @param wrapperFieldID
	 * @return
	 */
	public MethodDecl genDelegationMethod(MethodDecl partialMethodGroup,
			String generatedMethodName) {

		Block methodBody = generateDelegationMethodBody(partialMethodGroup);

		MethodDecl method = generateDelegationMethod(partialMethodGroup,
				generatedMethodName, methodBody);

		return method;
	}

	/**
	 * <code>
	 * int code = JCop.getNo(this, __target__);
	 * switch(code){
	 * 	case 1: AAA
	 * 	default:AAA'
	 * }
	 * <p>
	 * AAA can be return __target__.{@code<}methodName{@code>}1(this,{@code<}args{@code>});
	 * or __target__.{@code<}methodName{@code>}1(this,{@code<}args{@code>}); break;
	 * </p>
	 * <p>
	 * AAA' is return __target__.{@code<}methodName{@code>}(this,{@code<}args{@code>});
	 * or __target__.{@code<}methodName{@code>}(this,{@code<}args{@code>});
	 * </p>
	 * </code>
	 * 
	 * @param partialMethodGroup
	 * @return
	 */
	private Block generateDelegationMethodBody(MethodDecl partialMethodGroup) {
		Block resultBlock = new Block();
		List<Expr> args = generateArgs(partialMethodGroup.getParameterList());
		genLayerParams(args);

		// int code = JCop.getNo((Layer)this, (Object)__target__);
		List<Expr> varArgs = generateVarArgs();
		VariableDeclaration varDecl = new VariableDeclaration(
				new PrimitiveTypeAccess("int"),
				ID.wander_InstanceLayerMappingVar,
				JCopAccess.get(JCOP).qualifiesAccess(
						createMethodAccess(ID.wander_InstanceLayerMapping,
								varArgs)));
		resultBlock.addStmt(varDecl);
		// switch-case
		SwitchStmt switchStmt = new SwitchStmt();
		switchStmt.setExpr(new VarAccess(ID.wander_InstanceLayerMappingVar));
		// Block caseBlock = new Block();
		List caseList = new List();
		boolean needReturn = !partialMethodGroup.isVoid();
		int num = ((PartialMethodGroupDecl) partialMethodGroup)
				.getNumBodyGroup();
		for (int i = 1; i < num; i++) {
			ConstCase constCase = new ConstCase();
			constCase.setValue(new IntegerLiteral(i));
			caseList.add(constCase);
			Dot caseDot = new VarAccess(ID.targetParameterName)
					.qualifiesAccess(new MethodAccess(partialMethodGroup
							.getID() + i, args));
			if (needReturn == true) {
				caseList.add(new ReturnStmt(caseDot));
			} else {
				caseList.add(new ExprStmt(caseDot));
				caseList.add(new BreakStmt());
			}
		}
		caseList.add(new DefaultCase());
		Dot last = new VarAccess(ID.targetParameterName)
				.qualifiesAccess(new MethodAccess(partialMethodGroup.getID(),
						args));
		if (needReturn == true) {
			caseList.add(new ReturnStmt(last));
		} else {
			caseList.add(new ExprStmt(last));
		}

		switchStmt.setBlock(new Block(caseList));
		resultBlock.addStmt(switchStmt);

		return resultBlock;
	}

	/**
	 * (jcop.lang.Layer)this, (Object)__target__
	 * 
	 * @return
	 */
	private List<Expr> generateVarArgs() {
		List<Expr> args = new List<Expr>();
		args.insertChild(new VarAccess(new CastExpr(JCopAccess.get(LAYER),
				new ThisAccess("this"))), 0);
		args.insertChild(new VarAccess(new CastExpr(new TypeAccess("java.lang",
				"Object"), new VarAccess(ID.targetParameterName))), 1);
		return args;
	}

	/**
	 * 如果有返回值，需要在语句中直接return，不能采用return block的方式
	 * 
	 * @param baseMethodDecl
	 * @param generatedMethodName
	 * @param delegation
	 * @return
	 */
	private MethodDecl generateDelegationMethod(MethodDecl baseMethodDecl,
			String generatedMethodName, Block delegation) {
		// WANDER 6-17
		Modifiers modifiers = getTransformedModifiersFor(baseMethodDecl);
		Access typeAccess = transformToFullQualified(baseMethodDecl
				.getTypeAccess());
		List<ParameterDeclaration> params = getTransformedParamsFor(baseMethodDecl);

		MethodDecl method = new MethodDecl(
				modifiers,
				typeAccess,
				generatedMethodName,
				params,
				transformToFullQualifiedList(baseMethodDecl.getExceptionList()),
				new Opt<Block>(delegation));

		return method;

	}

	private Modifiers getTransformedModifiersFor(NamedMember method) {
		Modifiers modifiers = createPublicModifierFor(method);
		modifiers.addModifier(genAnnotation(DELEGATION_METHOD_ANNOTATION));
		return Generation.removeStaticModifier(modifiers);
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
