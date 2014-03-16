package jcop.lang;

/**
 * Documented by wander,
 * 
 * <pre>
 * in JCop, BaseLayer is added into layer composition by default. for convenience,
 * this layer contains delegation method with a call to base method
 * </pre>
 * 
 */
public class BaseLayer extends Layer {

	private static BaseLayer instance;

	public BaseLayer() {
	}

	public static BaseLayer getInstance() {
		if (instance == null)
			instance = new BaseLayer();
		return instance;
	}

	public String getName() {
		return "Base";
	}
}