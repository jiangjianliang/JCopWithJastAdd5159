package jcop.lang;

import java.util.Map.Entry;
import java.util.WeakHashMap;

public class LayerProxy extends Layer {
	private Layer l;

	private WeakHashMap<Object, Layer> map;

	public LayerProxy(Layer l) {
		super();
		this.l = l;
		if (l instanceof CompositeLayer) {
			map = new WeakHashMap<Object, Layer>();
		}
	}

	public String getName() {
		return l.getName();
	}

	public Layer get() {
		System.err.println("error");
		return l;
	}

	// by wander, for CompositeLayer
	public Layer get(Object target) {
		if(! (l instanceof CompositeLayer)){
			return l;
		}
		
		//System.err.println("right");
		Layer toBeReturned = (Layer) map.get(target);
		if (toBeReturned == null) {
			//System.out.println("toBeReturned == null");
			if (l.match(target)) {// inferring l instanceof CompositeLayer ==
									// true
				//System.err.println("match!");
				((CompositeLayer) l).collectInfo(target, map);
			}
			toBeReturned = l;
		} else if (toBeReturned instanceof CompositeLayer) {
			((CompositeLayer) toBeReturned).collectInfo(target, map);
		}// else, l is not a composite layer
		for(Entry<Object, Layer> entry :map.entrySet()){
			System.out.println(entry.getKey()+":"+entry.getValue().getName());
		}
		System.out.println();
		return toBeReturned;
	}

	@Override
	public String toString() {
		return getName();
		// return String.format("%s(%s)", getName(), get().toString());

	}

}
