package jcop.lang;

import java.util.Enumeration;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.LinkedList;

/**
 * Documented by wander,
 * 
 * <pre>
 * helper class of mapping from {@code <Key>} to {@code LinkedList<Value>}
 * </pre>
 * 
 * @param <Key>
 * @param <Value>
 */
public class LinkedHashtable<Key extends java.lang.Object, Value extends java.lang.Object>
		extends Hashtable<Key, LinkedList<Value>> {

	/**
	 * add ({@code <key>},{@code <toBeAdded>}) at position {@code <pos>}
	 * 
	 * @param key
	 * @param toBeAdded
	 * @param pos
	 * @return
	 */
	public synchronized java.util.LinkedList<Value> addValueAt(Key key,
			Value toBeAdded, int pos) {
		init(key);
		java.util.LinkedList<Value> list = get(key);
		if (pos < 0)
			list.add(toBeAdded);
		else
			list.add(pos, toBeAdded);
		return list;
	}

	/**
	 * add ({@code <key>},{@code <toBeAdded>}) at head of list
	 * 
	 * @param key
	 * @param toBeAdded
	 * @return
	 */
	public synchronized java.util.LinkedList<Value> prependValue(Key key,
			Value toBeAdded) {
		return addValueAt(key, toBeAdded, 0);
	}

	/**
	 * add ({@code <key>},{@code <toBeAdded>}) at tail of list
	 * 
	 * @param key
	 * @param toBeAdded
	 * @return
	 */
	public synchronized java.util.LinkedList<Value> appendValue(Key key,
			Value toBeAdded) {
		return addValueAt(key, toBeAdded, -1);
	}

	/**
	 * add ({@code <current>},{@code <list>}) or merge {@code <list>} into
	 * original value of key {@code <current>}
	 * 
	 * @param current
	 * @param list
	 */
	public void appendValues(Key current, LinkedList list) {
		init(current);
		get(current).addAll(list);

	}

	public void init(Key key) {
		if (!containsKey(key))
			put(key, new LinkedList<Value>());
	}

	public synchronized LinkedList<Value> remove(Key key, Value value) {
		LinkedList<Value> set = get(key);
		set.remove(value);
		return set;
	}

	public synchronized LinkedList<Value> get(Object key) {
		if (!containsKey(key))
			return new LinkedList<Value>();
		else
			return super.get(key);
	}

	public synchronized LinkedList<Value> getint(Key key) {
		init(key);
		return super.get(key);
	}

	public LinkedHashtable() {
		super();
	}

	public void merge(LinkedHashtable toBeMerged) {
		Enumeration<Key> keys = toBeMerged.keys();
		while (keys.hasMoreElements()) {
			Key current = keys.nextElement();
			appendValues(current, toBeMerged.getint(current));

		}
	}

}