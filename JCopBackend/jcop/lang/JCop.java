package jcop.lang;

import java.util.ArrayList;
import java.util.HashMap;
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
	private static volatile WeakHashMap<Object, Composition> objectMap = new WeakHashMap<Object, Composition>();
	/**
	 * used for shielding some layer instance
	 */
	// private static volatile WeakHashMap<Object, ArrayList<Class<Layer>>>
	// excludeMap = new WeakHashMap<Object, ArrayList<Class<Layer>>>();

	private static volatile WeakHashMap<Object, WeakHashMap<Layer, Integer>> objectMap2 = new WeakHashMap<Object, WeakHashMap<Layer, Integer>>();

	/**
	 * WANDER 这里也是一个临界区，需要保持多线程下的一些特性
	 * 
	 * @param obj
	 * @param toBeAdded
	 */
	public synchronized static void instanceWith(Object obj, Layer... toBeAdded) {
		Composition objComposition = null;
		objComposition = objectMap.get(obj);
		if (objComposition == null) {
			objComposition = new Composition();
			objectMap.put(obj, objComposition);
		}
		objComposition.addLayer(toBeAdded);
	}

	/**
	 * 
	 * @param obj
	 * @param toBeRemoved
	 */
	public synchronized static void instanceWithout(Object obj,
			Layer toBeRemoved) {
		Composition objComposition = null;
		objComposition = objectMap.get(obj);
		if (objComposition == null) {
			objComposition = new Composition();
			objectMap.put(obj, objComposition);
		}
		objComposition.removeLayer(toBeRemoved);
	}

	public synchronized static void instanceWith(Layer toBeAdded,
			Object[] instanceList, int[] noList) {
		for (int i = 0; i < instanceList.length; i++) {
			Object instance = instanceList[i];
			Composition instanceComposition = objectMap.get(instance);
			if (instanceComposition == null) {
				instanceComposition = new Composition();
				objectMap.put(instance, instanceComposition);
			}
			instanceComposition.addLayer(toBeAdded);

			WeakHashMap<Layer, Integer> weakHashMap = objectMap2.get(instance);
			if (weakHashMap == null) {
				weakHashMap = new WeakHashMap<Layer, Integer>();
				objectMap2.put(instance, weakHashMap);
			}
			weakHashMap.put(toBeAdded, noList[i]);

		}
	}

	public synchronized static void instanceWithout(Layer toBeRemoved,
			Object[] instanceList) {
		for (int i = 0; i < instanceList.length; i++) {
			Object instance = instanceList[i];
			Composition instanceComposition = objectMap.get(instance);
			if (instanceComposition != null) {
				instanceComposition.removeLayer(toBeRemoved);
			}

			WeakHashMap<Layer, Integer> weakHashMap = objectMap2.get(instance);
			if (weakHashMap != null) {
				weakHashMap.remove(toBeRemoved);
			}

		}
	}

	public synchronized static int getNo(Layer layer, Object target) {
		int result = 0;
		WeakHashMap<Layer, Integer> weakHashMap = objectMap2.get(target);
		if (weakHashMap != null) {
			Integer temp = weakHashMap.get(layer);
			if (temp != null) {
				result = temp;
			}
		}
		return result;
	}

	/*
	 * public synchronized static void instanceResetExclude(Object obj,
	 * Class<Layer> toBeRestore) { ArrayList<Class<Layer>> result =
	 * excludeMap.get(obj); if (obj == null) { result = new
	 * ArrayList<Class<Layer>>(); excludeMap.put(obj, result); }
	 * result.remove(toBeRestore); }
	 * 
	 * public synchronized static void instanceExclude(Object obj, Class<Layer>
	 * toBeExclude) { ArrayList<Class<Layer>> result = excludeMap.get(obj); if
	 * (obj == null) { result = new ArrayList<Class<Layer>>();
	 * excludeMap.put(obj, result); } result.add(toBeExclude); }
	 */
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
			// System.err.println("null");
			objComposition = new Composition();
			objectMap.put(obj, objComposition);
		}
		List<LayerProxy> list = objComposition.getTmpLayerComposition();
		// System.err.println(list.size());
		if (list.size() == 1 + Layer.getStaticActiveLayers().length) {
			// System.err.println("no instance layer composition");
			List<LayerProxy> cur = JCop.current().buildTmpLayerComposition();
			result.addAll(cur);
			// System.out.println(cur.size());
		} else {
			// System.err.println("having instance layer composition "+list.size());
			result.addAll(list);
			result.remove(result.size() - 1);// remove BASELayer
			List<Class<Layer>> temp = getBlockList(obj);
			result.addAll(filter(JCop.current().buildTmpLayerComposition(),
					temp));
		}
		return result.toArray(new LayerProxy[0]);
	}

	/**
	 * get blocked Layer list for Object
	 * 
	 * @param obj
	 * @return
	 */
	private static List<Class<Layer>> getBlockList(Object obj) {
		/*
		 * ArrayList<Class<Layer>> result = excludeMap.get(obj); if (result ==
		 * null) { result = new ArrayList<Class<Layer>>(); excludeMap.put(obj,
		 * result); } return result;
		 */
		return new ArrayList<Class<Layer>>();
	}

	/**
	 * <p>
	 * remove all instances in list whose class is in toBeBlocked. It is
	 * guaranteed that BASELayer will always be the last element of return value
	 * </p>
	 * <p>
	 * <b>NOTICE</b>: when {@code toBeBlocked} contains no element, it means
	 * that all layers except {@code Layer.Base} are blocked
	 * </p>
	 * 
	 * @param list
	 * @param toBeBlocked
	 * @return
	 */
	private static List<LayerProxy> filter(List<LayerProxy> list,
			List<Class<Layer>> toBeBlocked) {
		List<LayerProxy> result = new ArrayList<LayerProxy>();
		if (toBeBlocked.size() == 0) {
			// Caution! this contradicts sematics of toBeBlocked
			result.add(new LayerProxy(Layer.BASE));
			return result;
		}
		for (LayerProxy proxy : list) {
			if (toBeBlocked.contains(proxy.get().getClass()) == false) {
				result.add(proxy);
			}
		}
		return result;
	}
	// end new-feature
}
