package jcop.generation.layeractivation;

import static jcop.Globals.Types.JCOP;

import java.util.Iterator;

import AST.Access;
import AST.ArrayTypeAccess;
import AST.CastExpr;
import AST.Expr;
import AST.InstanceLayerActivation;
import AST.List;
import AST.TypeAccess;
import jcop.Globals.ID;
import jcop.Globals.Types;
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
		Access access = null;
		if (instaceLayerActivation.getActivation()) {
			// instanceWith
			access = JCopAccess.get(JCOP).qualifiesAccess(
					createMethodAccess(
							ID.wander_InstanceLayerActivationMethodName,
							castArgs(true)));
		} else {
			// instanceWithout
			access = JCopAccess.get(JCOP).qualifiesAccess(
					createMethodAccess(
							ID.wander_InstanceLayerDeactivationMethodName,
							castArgs(false)));
		}
		return access;
	}

	/**
	 * <p>
	 * <code>
	 * (jcop.lang.Layer),(Object[]), int[]
	 * (jcop.lang.Layer),(Object[])
	 * </code>
	 * </p>
	 * 
	 * @param activation
	 * @return
	 */
	private List<Expr> castArgs(boolean activation) {
		Iterator<Expr> argsItr = instaceLayerActivation.getArgs().iterator();
		List<Expr> result = new List<Expr>();
		result.add(new CastExpr(JCopAccess.get(Types.LAYER), argsItr.next()));
		result.add(new CastExpr(new ArrayTypeAccess(new TypeAccess("java.lang",
				"Object")), argsItr.next()));
		if (activation) {
			result.add(argsItr.next());
		}
		/*
		 * result.add(new CastExpr(new TypeAccess("java.lang", "Object"),
		 * argsItr .next())); while (argsItr.hasNext()) { result.add(new
		 * CastExpr(new TypeAccess("jcop.lang", "Layer"), argsItr .next())); }
		 */
		return result;
	}

}
