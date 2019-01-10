package org.sadun.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

/**
 * A map where each key is associated to a collection.
 * <p>
 * The map automatically creates collections for each key and adds the values to
 * the collection rather than the map itself.
 * 
 * 
 * @author Cristiano Sadun
 */
public class CollectionMap implements Map {

    private Map map = new HashMap();
    private Class collectionClass;
    private boolean returnPreviousItemOnPutEnabled = true;
    private boolean removeEmptyCollections = false;

    public CollectionMap(Class collectionClass) {
        this(collectionClass, true);
    }

    public CollectionMap(Class collectionClass,
            boolean returnPreviousItemOnPutEnabled) {
        if (!Collection.class.isAssignableFrom(collectionClass))
            throw new IllegalArgumentException(collectionClass.getName()
                    + " does not implement the collection interface");

        if (collectionClass.isInterface())
            throw new IllegalArgumentException(
                    "Do not pass a Collection sub-interface, but an implementation class");

        try {
            Constructor ctor = collectionClass.getConstructor(new Class[0]);
            if (!Modifier.isPublic(ctor.getModifiers()))
                throw new IllegalArgumentException(
                        "The constructor with no parameters for the class "
                                + collectionClass + " is not public");
        } catch (SecurityException e) {
            throw new IllegalArgumentException(
                    "The constructor with no parameters for the class "
                            + collectionClass
                            + " cannot be accessed for security reasons ("
                            + e.getMessage() + ")");
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("The class " + collectionClass
                    + " does not have a constructor with no parameters");
        }
        this.collectionClass = collectionClass;
        this.returnPreviousItemOnPutEnabled = returnPreviousItemOnPutEnabled;
    }

    public CollectionMap() {
        this(HashSet.class);
    }

    public Set entrySet() {
        return map.entrySet();
    }

    /**
     * This method respects the general contract of Map, but if the
     * {@link #isReturnPreviousItemOnPutEnabled() returnPreviousItemOnPutEnabled}
     * property is {@link #setReturnPreviousItemOnPutEnabled(boolean) disabled}
     * it will always return <b>null</b>, and not the previous collection
     * object.
     * <p>
     * This is so since in this Map the contained values are collections, and
     * returning an image of the previous collection (i.e. null if there was no
     * collection, and the collection *without* the new value) can reduce
     * performance.
     */
    public Object put(Object key, Object value) {
        Collection collection = (Collection) map.get(key);

        Collection result = null;
        if (collection == null) {
            map.put(key, collection = instantiateCollection());
        } else {
            if (returnPreviousItemOnPutEnabled) {
                result = instantiateCollection();
                result.addAll(collection);
            }
        }
        collection.add(value);
        return result;
    }

    public Object remove(Object key) {
        return map.remove(key);
    }

    public Object remove(Object key, Object value) {
        Collection collection = (Collection) map.get(key);
        if (collection == null)
            return null;
        Collection result = null;
        if (returnPreviousItemOnPutEnabled) {
            result = instantiateCollection();
            result.addAll(collection);
        }
        boolean changed = collection.remove(value);
        if (!changed)
            return result;
        if (collection.size() == 0 && removeEmptyCollections) {
            remove(key);
        }
        return result;
    }

    private Collection instantiateCollection() {
        try {
            return (Collection) (collectionClass.newInstance());
        } catch (Exception e) {
            throw new RuntimeException("Could not create instance of "
                    + collectionClass, e);
        }
    }

    public boolean isReturnPreviousItemOnPutEnabled() {
        return returnPreviousItemOnPutEnabled;
    }

    public void setReturnPreviousItemOnPutEnabled(
            boolean returnPreviousItemOnPutEnabled) {
        this.returnPreviousItemOnPutEnabled = returnPreviousItemOnPutEnabled;
    }

    public boolean isRemoveEmptyCollections() {
        return removeEmptyCollections;
    }

    public void setRemoveEmptyCollections(boolean removeEmptyCollections) {
        this.removeEmptyCollections = removeEmptyCollections;
    }

    public void clear() {
        map.clear();
    }

    public boolean containsKey(Object key) {
        return map.containsKey(key);

    }

    public boolean containsValue(Object value) {
        return map.containsValue(value);
    }

    public Object get(Object key) {
        return map.get(key);
    }
    
    public Set keySet() {
        return map.keySet();
    }
    
    public boolean isEmpty() {
        return map.isEmpty();
    }
    
    public void putAll(Map t) {
        Iterator i = t.keySet().iterator();
        while(i.hasNext()) {
            Object key = i.next();
            Object value = t.get(key);
            put(key, value);
        }
    }
    
    public Class getCollectionClass() {
        return collectionClass;
    }
    
    public Collection values() {
        return map.values();
    }
    
    public int size() {
        return map.size();
    }
    
    public int size(Object key) {
        Collection collection = (Collection) map.get(key);
        if (collection == null) return 0;
        return collection.size();
    }

    public static void main(String[] args) {
        Map cmap = new CollectionMap(ArrayList.class);
        cmap.put("test", "hello");
        cmap.put("test", "world");
        Object obj = cmap.get("test");
        Collection c = (Collection) obj;
        Iterator i = c.iterator();
        while (i.hasNext()) {
            System.out.print(i.next());
            System.out.print(" ");
        }
    }

    

}
