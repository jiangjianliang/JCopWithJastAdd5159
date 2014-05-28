package jcop.transformation;

import jcop.generation.layeractivation.InstanceLayerActivationGenerator;
import AST.Access;
import AST.InstanceLayerActivation;

/**
 * <p>
 * transform {@link InstanceLayerActivation} into {@link Access} with {@link InstanceLayerActivationGenerator}
 * </p>
 * @author wander
 * 
 */
public class InstanceLayerActivationTransformer extends Transformer {
	private Access transformedAccess;
	private InstanceLayerActivationGenerator gen;
	
	public InstanceLayerActivationTransformer(InstanceLayerActivation instaceLayerActivation){
		//WANDER keyword debug
		System.err.println("new instanceLayerActivationTransformer");
		this.gen = new InstanceLayerActivationGenerator(instaceLayerActivation);
	}
	
	@Override
	protected Access transform() {
		transformedAccess = gen.generateAccess();
		return transformedAccess;
	}

}
