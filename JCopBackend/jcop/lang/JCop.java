package jcop.lang;

/**
 * Documented by wander,
 * 
 * <pre>
 * JCop main construct class
 * </pre>
 * 
 */
public class JCop {
	/**
	 * thread-local property holds with thread-local constructs
	 */
	public static final ThreadLocal<Compositions> thread = new ThreadLocal<Compositions>() {
		protected Compositions initialValue() {
			return new Compositions();
		}
	};

	public static Class[] getLayerClasses() {
		return Layer.BASE.getAllLayerClasses();
	}

	public static Composition current() {
		return thread.get().getComposition();
	}

	public static void setComposition(Composition comp) {
		thread.get().setComposition(comp);
	}

	/**
	 * return current contextclass containing global active contextclass
	 * 
	 * <pre>
	 * called in JCopAspect.aj
	 * </pre>
	 * 
	 * @return
	 */
	public static ContextComposition currentContexts() {
		return thread.get().getCashedContextComposition(global).clone();
	}

	public static void setContextComposition(ContextComposition comp) {
		thread.get().setContextComposition(comp);
	}

	/**
	 * used for global active (staticactive) contextclass/layer
	 */
	public static final Compositions global = new Compositions();

	public static Compositions thread() {
		return thread.get();
	}

	public static Compositions global() {
		return global;
	}

}
