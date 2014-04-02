package jcop.lang;

import java.util.WeakHashMap;

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

	// wander: begin modification
	/**
	 * each thread has their own copy of threadgroup composition
	 */
	public static final ThreadLocal<Composition> threadGroup = new ThreadLocal<Composition>() {
		protected Composition initialValue() {
			return new Composition();
		}
	};

	/**
	 * WANDER: WeakHashMap
	 */
	public static final WeakHashMap<Thread, Composition> threadGroupMap = new WeakHashMap<Thread, Composition>();

	/**
	 * later it should throw Exception
	 * 
	 * @param from
	 * @param to
	 * @return
	 */
	public static boolean bindTo(Thread from, Thread to) {
		synchronized (threadGroupMap) {
			Composition fromComp = threadGroupMap.get(from);
			Composition toComp = threadGroupMap.get(to);
			if (fromComp == null) {
				fromComp = new Composition();
				threadGroupMap.put(from, fromComp);
			}
			if (toComp != null) {
				return false;
			} else {
				threadGroupMap.put(to, fromComp);
			}
		}
		return true;
	}

	public static Composition threadGroupComposition() {
		Composition comp;
		synchronized (threadGroupMap) {
			comp = threadGroupMap.get(Thread.currentThread());
			if (comp == null) {
				// TODO
				comp = addComposition();
				System.err
						.println("threadGroupWith: current thread group composition is null");
			}
		}
		return comp;
	}

	/**
	 * 
	 * @param toBeAdded
	 */
	public static void threadGroupWith(Layer toBeAdded) {
		synchronized (threadGroupMap) {
			Composition comp = threadGroupMap.get(Thread.currentThread());
			if (comp == null) {
				comp = addComposition();
				System.err
						.println("threadGroupWith: current thread group composition is null");
			}
			comp.addLayer(toBeAdded);
		}
	}

	/**
	 * 
	 * @param toBeRemoved
	 */
	public static void threadGroupWithout(Layer toBeRemoved) {
		synchronized (threadGroupMap) {
			Composition comp = threadGroupMap.get(Thread.currentThread());
			if (comp == null) {
				comp = addComposition();
				System.err
						.println("threadGroupWithout: current thread group composition is null");
			}
			comp.removeLayer(toBeRemoved);
		}
	}

	/**
	 * 
	 * @param toBeRemove
	 * @param toBeAdded
	 */
	public static void threadGroupReplace(Layer toBeRemoved, Layer toBeAdded) {
		synchronized (threadGroupMap) {
			Composition comp = threadGroupMap.get(Thread.currentThread());
			if (comp == null) {
				comp = addComposition();
				System.err
						.println("threadGroupWithout: current thread group composition is null");
			}
			comp.removeLayer(toBeRemoved);
			comp.addLayer(toBeAdded);
		}
	}

	private static Composition addComposition() {
		Composition comp = new Composition();
		threadGroupMap.put(Thread.currentThread(), comp);
		return comp;
	}

	// wander: end modification
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
