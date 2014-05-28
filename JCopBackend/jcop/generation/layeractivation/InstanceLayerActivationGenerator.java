package jcop.generation.layeractivation;

import static jcop.Globals.Types.JCOP;

import java.util.Iterator;

import javax.management.InstanceAlreadyExistsException;

import AST.ASTNode;
import AST.Access;
import AST.CastExpr;
import AST.Expr;
import AST.InstanceLayerActivation;
import AST.List;
import AST.TypeAccess;
import jcop.Globals.ID;
import jcop.compiler.JCopTypes.JCopAccess;
import jcop.generation.Generator;

/**
 * <p>
 * generate constructs for {@link InstanceLayerActivation}
 * </p>
 * 
 * @author wander
 * 
 */
public class InstanceLayerActivationGenerator extends Generator {
	private InstanceLayerActivation instaceLayerActivation;

	public InstanceLayerActivationGenerator(
			InstanceLayerActivation instaceLayerActivation) {
		this.instaceLayerActivation = instaceLayerActivation;
	}

	/**
	 * <p>
	 * <code>
	 * jcop.lang.JCop.instanceWith((Object)?, (Layer)?, ...)
	 * jcop.lang.JCop.instanceWithout((Object)?, (Layer)?)
	 * </code>
	 * </p>
	 * 
	 * @return
	 */
	public Access generateAccess() {
		// WANDER
		Access access = null;
		if (instaceLayerActivation.getActivation()) {
			// instanceWith
			access = JCopAccess.get(JCOP).qualifiesAccess(
					createMethodAccess(
							ID.wander_InstanceLayerActivationMethodName,
							castArgs()));
		} else {
			// instanceWithout
			access = JCopAccess.get(JCOP).qualifiesAccess(
					createMethodAccess(
							ID.wander_InstanceLayerDeactivationMethodName,
							castArgs()));
		}
		return access;
	}

	/**
	 * <p>
	 * It is guaranteed that at least two parameters.
	 * <code>
	 * 
	 * </code>
	 * </p>
	 * 
	 * @return
	 */
	private List<Expr> castArgs() {
		Iterator<Expr> argsItr = instaceLayerActivation.getArgs().iterator();
		List<Expr> result = new List<Expr>();
		result.add(new CastExpr(new TypeAccess("java.lang", "Object"), argsItr
				.next()));
		while (argsItr.hasNext()) {
			result.add(new CastExpr(new TypeAccess("jcop.lang", "Layer"), argsItr
					.next()));
		}
		return result;
	}

}
