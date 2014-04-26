package jcop.lang;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;
import java.util.List;
import java.util.logging.Logger;

public class Composition implements Cloneable {

	public ArrayList<LayerProxy> tmpActualLayers;

	public LayerToProxyMap layerToProxyMap;
	// not sure if i need this list
	// public ArrayList<Layer> deactivatedLayers;
	public String deactivated = "";

	// public ContextComposition contexts;

	protected Composition() {
		super();
		// activatedLayers = new ArrayList<LayerProxy>();
		layerToProxyMap = new LayerToProxyMap();
		// deactivatedLayers = new ArrayList<Layer>();
		// contexts = new ContextComposition(this);
	}

	/**
	 * add {@code <layerList>} to current composition.
	 * 
	 * @param layerList
	 * @return
	 */
	public Composition withLayer(Collection<Layer> layerList) {
		return this.withLayer(layerList.toArray(new Layer[0]));
	}

	public Composition withLayer(Layer... layerList) {
		Composition old = this.clone();
		addLayer(layerList);
		return old;
	}

	public static Composition current() {
		return JCop.current();
	}

	private boolean invalidated = true;

	protected void invalidate() {
		invalidated = true;
	}

	/**
	 * add list of {@link Layer} to current composition
	 * 
	 * @param layerList
	 */
	public void addLayer(Layer... layerList) {
		for (Layer _layer : layerList) {
			if (_layer != null) {
				// addLayer(_layer.getImplicitActivations());
				// for (Layer toBeDeactivated :
				// _layer.getImplicitDeActivations())
				// removeLayer(toBeDeactivated);
				// this.deactivatedLayers.remove(_layer);
				layerToProxyMap.addLayer(_layer);
				// System.out.println("before: " +activatedLayers);
				Composition c = _layer.onWith(this);

				this.layerToProxyMap = c.layerToProxyMap;
				// System.out.println("after: " +activatedLayers);
				invalidateComposition();
			}
		}
	}

	/**
	 * add {@link Collection} of {@link Layer} to current composition and log
	 * this with name {@code <loggerName>}
	 * 
	 * @param loggerName
	 * @param layerList
	 * @return old composition before adding
	 */
	public Composition addLayerWithLogging(String loggerName,
			Collection<Layer> layerList) {
		Composition old = this.withLayer(layerList);
		Collection<String> layerNames = new ArrayList<String>();
		for (Layer _layer : layerList) {
			layerNames.add(_layer.toString());
		}
		LayerLogger.logLayerActivation(loggerName,
				layerNames.toArray(new String[layerNames.size()]));
		return old;
	}

	/**
	 * add list of {@link Layer} to current composition and log this with name
	 * {@code <loggerName>}
	 * 
	 * @param loggerName
	 * @param layerList
	 * @return old composition before adding
	 */
	public Composition addLayerWithLogging(String loggerName,
			Layer... layerList) {
		Composition old = this.withLayer(layerList);
		String[] layerNames = new String[layerList.length];
		for (int i = 0; i < layerList.length; ++i) {
			layerNames[i] = layerList[i].toString();
		}

		LayerLogger.logLayerActivation(loggerName, layerNames);
		return old;
	}

	/**
	 * remove {@code <toBeRemoved>} of {@link Layer} from current composition
	 * 
	 * @param toBeRemoved
	 * @return old composition before removing
	 */
	public Composition removeLayer(Layer toBeRemoved) {
		Composition old = this.clone();
		boolean removed = this.layerToProxyMap.removeLayer(toBeRemoved);
		// while (removed)
		// removed = this.activatedLayers.remove(toBeRemoved);
		// this.deactivatedLayers.remove(_layer);
		// this.deactivatedLayers.add(0, _layer);
		// this.deactivated += _layer.toString();
		return old;
	}

	/**
	 * remove {@code <toBeRemoved>} of {@link Layer} from current composition
	 * and log this with name {@code <loggerName>}
	 * 
	 * @param loggerName
	 * @param _layer
	 * @return old composition before removing
	 */
	public Composition removeLayerWithLogging(String loggerName, Layer _layer) {
		Composition old = this.removeLayer(_layer);
		LayerLogger.logLayerDeactivation(loggerName,
				new java.lang.String[] { _layer.toString() });
		return old;
	}

	/**
	 * get first layer in current composition. default value is BaseLayer
	 * 
	 * @return
	 */
	public LayerProxy firstLayer() {
		return ((LayerProxy) getTmpLayerComposition().get(0));
	}

	public Layer[] getLayer() {
		return getTmpLayerComposition().toArray(new Layer[0]);
	}

	// public ContextComposition getContexts() {
	// return contexts;
	// }

	//
	// public void setContexts(ContextComposition comp) {
	// contexts = comp;
	// }

	public void setActiveFor(Object o) {
		for (Layer l : getLayer())
			l.setActiveFor(o);
	}

	public void setInactiveFor(Object o) {
		for (Layer l : getLayer())
			l.setInactiveFor(o);
	}

	/**
	 * get next {@link LayerProxy} of {@code <current>}
	 * 
	 * @param current
	 * @return
	 */
	public LayerProxy next(LayerProxy current) {
		ArrayList<LayerProxy> composition = getTmpLayerComposition();

		for (int i = 0; i < composition.size(); i++) {
			LayerProxy _layer = composition.get(i);
			if (current.equals(_layer))
				return composition.get(i + 1);
		}
		return composition.get(0);
	}

	@Override()
	public String toString() {
		StringBuffer buffer = new StringBuffer();
		buffer.append("[");
		for (LayerProxy _layer : getTmpLayerComposition()) {
			buffer.append(_layer);
			buffer.append(", ");
		}
		if (buffer.length() > 0)
			buffer.delete(buffer.length() - 2, buffer.length());
		buffer.append("]");
		return buffer.toString();
	}

	@Override()
	protected Composition clone() {
		Composition clone = new Composition();
		if (this.tmpActualLayers != null) {
			clone.tmpActualLayers = (ArrayList<LayerProxy>) tmpActualLayers
					.clone();
		}
		clone.deactivated = this.deactivated;
		clone.layerToProxyMap = (LayerToProxyMap) layerToProxyMap.clone();
		// clone.deactivatedLayers.addAll(this.deactivatedLayers);
		// clone.activatedContextLayers.addAll(this.activatedContextLayers);
		// clone.contexts = this.contexts;
		// clone.parent_id = this.id;
		return clone;
	}

	// public static List<Layer> getImplicitlyActivatedLayers(Object target) {
	// return getImplicitlyActivatedLayers(target,
	// Arrays.asList(Layer.getLayers()));
	// }
	/**
	 * FIXME what this used for
	 * 
	 * @param signature
	 * @param target
	 * @return
	 */
	public static List<Layer> getImplicitlyActivatedLayers(String signature,
			Object target) {
		return getImplicitlyActivatedLayers(target,
				getPartialMethodProvidersFor(signature));
	}

	public static List<Layer> getImplicitlyActivatedLayers(Object target,
			Iterable<Layer> layerList) {
		List<Layer> layers = new ArrayList<Layer>();
		for (Layer l : layerList) {
			if (l.isImplicitlyActive() || l.isActiveFor(target)) {
				layers.add(l);
			}
		}
		if (target instanceof LayerProvider)
			layers.addAll(((LayerProvider) target).getLayers());
		return layers;
	}

	// reflective API

	public static List<Layer> getPartialMethodProvidersFor(String signature) {
		List<Layer> layers = new ArrayList<Layer>();
		for (Layer l : Layer.getLayers()) {
			if (l.providesPartialMethod(signature))
				layers.add(l);
		}
		return layers;
	}

	public void invalidateComposition() {
		tmpActualLayers = null;
	}

	/**
	 * get temporary layer composition
	 * 
	 * @return
	 */
	public ArrayList<LayerProxy> getTmpLayerComposition() {
		// if (tmpActualLayers == null)
		tmpActualLayers = buildTmpLayerComposition();
		return tmpActualLayers;
	}

	/**
	 * build temporary layer composition.
	 * <ul>
	 * <li>static active layers</li>
	 * <li>current active layers</li>
	 * <li>BaseLayer</li>
	 * </ul>
	 * WANDER 修改这个，来扩展新功能
	 * @return
	 */
	public ArrayList<LayerProxy> buildTmpLayerComposition() {
		ArrayList<LayerProxy> tmpList = new ArrayList<LayerProxy>();
		tmpList.addAll(Arrays.asList(Layer.getStaticActiveLayers()));
		tmpList.addAll(this.getDirectActivatedLayers());
		// tmpList.addAll(this.getContextActivatedLayers());
		tmpList.add(new LayerProxy(BaseLayer.getInstance()));
		return (ArrayList<LayerProxy>) tmpList;
	}

	public List<LayerProxy> getDirectActivatedLayers() {
		return new ArrayList<LayerProxy>(this.layerToProxyMap.getLayers());
	}

	// public List<LayerProxy> getContextActivatedLayers() {
	// ArrayList<LayerProxy> resultList = new ArrayList<LayerProxy>();
	// //resultList.addAll(contexts.getActivatedLayers());
	// resultList.removeAll(this.activatedLayers);
	// // resultList.removeAll(this.deactivatedLayers);
	// return resultList;
	// }

	static class LayerLogger {

		public static void logLayerActivation(String loggerName,
				String... layers) {
			logLayer(loggerName, "activating layer", layers);
		}

		public static void logLayerDeactivation(String loggerName,
				String... layers) {
			logLayer(loggerName, "deactivating layer", layers);
		}

		public static void logLayer(String loggerName, String msg,
				String... layers) {
			StringBuilder strBuilder = new StringBuilder(msg);
			if (layers.length > 1)
				strBuilder.append("s");
			strBuilder.append(createLayerList(layers));
			strBuilder.append("...");
			log(loggerName, strBuilder.toString());
		}

		public static StringBuffer createLayerList(String[] layers) {
			StringBuffer buffer = new StringBuffer();
			for (int i = 0; i < layers.length; ++i) {
				if (i > 0)
					buffer.append(",");
				buffer.append(" ").append(layers[i]);
			}
			return buffer;
		}

		public static void log(String loggerName, String msg) {
			getLogger(loggerName).info(msg);
		}

		public static Logger getLogger(String loggerName) {
			return isEmptyString(loggerName) ? Logger.getAnonymousLogger()
					: Logger.getLogger(loggerName);
		}

		public static boolean isEmptyString(String str) {
			return str == null || "".equals(str);
		}
	}

	public boolean contains(Layer l) {
		return getTmpLayerComposition().contains(l);

	}

	/**
	 * contain two mappings from {@link Layer} to {@link LayerProxy}
	 * 
	 * @author wander
	 * 
	 */
	class LayerToProxyMap {

		private ArrayList<LayerProxy> activatedLayers;

		private LinkedHashtable<Layer, LayerProxy> activatedProxies;

		private LayerToProxyMap() {
			activatedLayers = new ArrayList<LayerProxy>();
			activatedProxies = new LinkedHashtable<Layer, LayerProxy>();
		}

		private LayerToProxyMap(ArrayList<LayerProxy> activatedLayers,
				LinkedHashtable<Layer, LayerProxy> activatedProxies) {
			this.activatedLayers = activatedLayers;
			this.activatedProxies = activatedProxies;
		}

		/**
		 * return current list of active {@link Layer}({@link LayerProxy},
		 * actually)
		 * 
		 * @return
		 */
		public Collection<? extends LayerProxy> getLayers() {
			return activatedLayers;
		}

		/**
		 * add {@code <toBeAdded>} of {@link Layer} to {@code <activatedLayers>}
		 * and {@code <activatedProxies>}
		 * 
		 * @param toBeAdded
		 */
		public void addLayer(Layer toBeAdded) {
			LayerProxy proxy = new LayerProxy(toBeAdded);
			activatedLayers.add(0, proxy);
			activatedProxies.appendValue(toBeAdded, proxy);
		}

		/**
		 * remove {@code <toBeRemove>} of {@link Layer} to
		 * {@code <activatedLayers>} and {@code <activatedProxies>}
		 * 
		 * @param toBeRemoved
		 * @return
		 */
		public boolean removeLayer(Layer toBeRemoved) {
			for (LayerProxy proxy : activatedProxies.get(toBeRemoved))
				activatedLayers.remove(proxy);
			activatedProxies.remove(toBeRemoved);
			return true;
		}

		@Override
		public LayerToProxyMap clone() {
			return new LayerToProxyMap(
					(ArrayList<LayerProxy>) activatedLayers.clone(),
					(LinkedHashtable<Layer, LayerProxy>) activatedProxies
							.clone());
		}

	}

}