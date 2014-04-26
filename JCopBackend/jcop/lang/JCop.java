package jcop.lang;

import java.util.ArrayList;
import java.util.List;
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

	// begin new-feature
	private static WeakHashMap<Object, Composition> objectMap = new WeakHashMap<Object, Composition>();

	/**
	 * WANDER 这里也是一个临界区，需要保持多线程下的一些特性
	 * 
	 * @param obj
	 * @param toBeAdded
	 */
	public synchronized static void instanceWith(Object obj, Layer toBeAdded) {
		Composition objComposition = null;
		objComposition = objectMap.get(obj);
		if (objComposition == null) {
			objComposition = new Composition();
			objectMap.put(obj, objComposition);
		}
		objComposition.addLayer(toBeAdded);
	}

	public synchronized static void instanceWithout(Object obj, Layer toBeRemoved) {
		Composition objComposition = null;
		objComposition = objectMap.get(obj);
		if (objComposition == null) {
			objComposition = new Composition();
			objectMap.put(obj, objComposition);
		}
		objComposition.removeLayer(toBeRemoved);
	}

	/**
	 * WANDER 这里是个临界区，需要保持多线程下的一些特性
	 * 
	 * @param obj
	 * @return
	 */
	public synchronized static LayerProxy[] construct(Object obj) {
		List<LayerProxy> result = new ArrayList<LayerProxy>();

		Composition objComposition = objectMap.get(obj);
		if (objComposition == null) {
			//System.err.println("null");
			objComposition = new Composition();
			objectMap.put(obj, objComposition);
		}
		List<LayerProxy> list = objComposition.getTmpLayerComposition();
		//System.err.println(list.size());
		if(list.size() == 1+Layer.getStaticActiveLayers().length){
			//System.err.println("no instance layer composition");
			List<LayerProxy> cur = JCop.current().buildTmpLayerComposition();
			result.addAll(cur);
			//System.out.println(cur.size());
		}
		else {
			//System.err.println("having instance layer composition "+list.size());
			result.addAll(list);
			//result.add(new LayerProxy(Layer.BASE));
		}
		return result.toArray(new LayerProxy[0]);
	}
	// end new-feature
}
