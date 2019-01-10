package org.sadun.util;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

/**
 * An iterator to go thru a Map of Lists, using a specific key list for deciding
 * how to access the sublists.
 * <p>
 * Given a {@link java.util.Map java.util.Map} binding each key K<sub>i</sub> to a
 * {@link java.util.List java.util.List} L<sub>i</sub>, this Iterator allows to 
 * browse the set { L<sub>1</sub>, L<sub>2</sub>, ... , L<sub>n</sub>} , by 
 * specifying the order in which the lists should be picked.
 * <p>
 * Such order is specified by providing a key list at construction time; if no list 
 * is given, the order of iteration is the arbitrary order in which the keys are 
 * stored in the {@link java.util.Map java.util.Map}, as given by the iterator
 * provided by the {@link java.util.Set#iterator() iterator()} method of the
 * {@link java.util.Map java.util.Map}'s key set.
 * <p>
 * For example, if a Map is built by
 * <pre>
 *  list1.add("Elem 1.1");
 *  list1.add("Elem 1.2");
 *  list2.add("Elem 2.1");
 *  list3.add("Elem 3.1");
 *  list3.add("Elem 3.2");
 *  list3.add("Elem 3.3");
 *  map.add("1", list1);
 *  map.add("2", list2);
 *  map.add("3", list3);
 * </pre>
 * a <code>ListMapIterator</code> built by
 * <pre>
 * ListMapIterator i = new ListMapIterator(map, new Arrays.asList(new String[] { "1", "2", "3")));
 * </pre>
 * will produce the iteration <code>Elem1.1, Elem1.2, Elem2.1, Elem3.1, Elem3.2, Elem3.3</code>.
 * 
 * @version 1.0
 * @author Cristiano Sadun
 */
public class ListMapIterator implements Iterator {

	Map map;
	Iterator currentKeyIterator;
	Iterator currentListIterator;
	Object currentKey;

	boolean failOnUnknownKeys;
	boolean requireUniqueKeys;
	
	private static List getDefaultOrderingList(Map map) {
		Set s=map.keySet();
		List l = new ArrayList();
		for(Iterator i = s.iterator(); i.hasNext();) {
			l.add(i.next());
		}
		return l;
	}

	/**
	 * Create a ListMapIterator which uses default ordering
	 * (as provided by the set of keys in the map).
	 * 	 * @param map the map of Lists to iterate	 */
	public  ListMapIterator(Map map) {
		this(map, getDefaultOrderingList(map));
	}
	
	/**
	 * Create a ListMapIterator which uses the key ordering
	 * defined in the given list.
	 *  	 * @param map the map of Lists to iterate.	 * @param keyorder the order with which the map Lists must be iterated.	 */	
	public ListMapIterator(Map map, List keyorder) {
		this(map, keyorder, true, false);
	}

	/**
	 * Create a ListMapIterator which uses the key ordering defined in the given list.
	 * 	 * @param map the map of Lists to iterate.
	 * @param keyorder the order with which the map Lists must be iterated.	 * @param failOnUnknownKeys if <b>true</b>, the iterator will fail with 
	 *                           an IllegalArgumentException if one key in the
	 * 						     ordering list is not defined in the map. 
	 * 							 Else, it will ignore the unknown key.	 * @param requireUniqueKeys if <b>true</b>, the iterator will fail with 
	 *                           an IllegalArgumentException if the keys in the
	 * 						     ordering list are not unique.
	 */
	public ListMapIterator(
		Map map,
		List keyorder,
		boolean failOnUnknownKeys,
		boolean requireUniqueKeys) {
		this.map = map;
		this.failOnUnknownKeys = failOnUnknownKeys;
		
		if (requireUniqueKeys) {
			Set s = new HashSet();
			for (Iterator i = keyorder.iterator(); i.hasNext();) {
				s.add(i.next());
			}
			if (s.size() != keyorder.size())
				throw new IllegalArgumentException("ordering list must contain distinct keys");
		}
		
		this.currentKeyIterator = keyorder.iterator();
		goToNextKey();
	}

	private boolean goToNextKey() {
		if (currentKeyIterator.hasNext()) {
			try {
				currentKey = currentKeyIterator.next();
				List l = ((List) map.get(currentKey));

				// Ignore 
				if (l == null) {
					if (failOnUnknownKeys)
						throw new IllegalArgumentException(
							"The key <"
								+ currentKey.toString()
								+ "> is not found in the map");
					else
						return goToNextKey();
				}
				currentListIterator = l.iterator();
				return true;
			} catch (ClassCastException e) {
				throw new IllegalArgumentException("The passed map does not contain a List");
			}
		}
		return false;
	}

	/**
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if (currentListIterator != null) {
			if (currentListIterator.hasNext())
				return true;
			else {
				if (!goToNextKey())
					return false;
				return hasNext();
			}
		}
		return false;
	}

	/**
	 * @see java.util.Iterator#next()
	 */
	public Object next() {
		if (currentListIterator == null)
			throw new NoSuchElementException();
		return currentListIterator.next();
	}

	/**
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException("remove not supported");
	}

	public static void main(String args[]) throws Exception {
		Map map = new HashMap();
		List l1 = new ArrayList();
		l1.add("hello");
		List l2 = new ArrayList();
		List l3 = new ArrayList();
		l3.add("world");
		l3.add(",");
		l3.add("how");
		List l4 = new ArrayList();
		l4.add("is it?");
		map.put("step1", l1);
		map.put("step2", l2);
		map.put("step3", l3);
		map.put("step4", l4);
		List order = new ArrayList();
		order.add("step1");
		order.add("step2");
		order.add("step3");
		order.add("step4");
		ListMapIterator i = new ListMapIterator(map, order);
		while (i.hasNext()) {
			System.out.print(i.next());
			System.out.print(" ");

		}
	}
	
	public Object getCurrentKey() {
		return currentKey;
	}

}