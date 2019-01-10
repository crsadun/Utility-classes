package org.sadun.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

import org.sadun.util.codegen.ObjectWrapperGenerator;
import org.sadun.util.codegen.ObjectWrapperGeneratorException;

/**
 * This cache automatically generates code which intercepts method calls and
 * marks an object as used when a method call occurs on the object itself.
 * <p>
 * The cached object must not be final and must have a public default constructor.
 * 
 * @author Cristiano Sadun
 */
public class ExtendedCache extends Cache {

    private static final boolean debug = false;
    private Cache extensionsClassCache;

    public static interface ObjectWithCache {
        public void setCache(ExtendedCache cache);
    }

    public ExtendedCache(int max) {
        this(max, 100);
    }
    
    public ExtendedCache(int max, int classCacheMax) {
        super(max);
        extensionsClassCache=new Cache(classCacheMax);
    }

    public Object put(Object key, Object value) {
        Object obj;
        try {
            obj = createExtensionObject(value);
            return super.put(key, obj);
        } catch (ObjectWrapperGeneratorException e) {
            throw new RuntimeException(e);
        }

    }

    private Object createExtensionObject(Object value)
            throws ObjectWrapperGeneratorException {
        
        Class cls = (Class)extensionsClassCache.get(value.getClass());
        Object obj;
        if (cls==null) {

        String valueClassName = value.getClass().getName();
        String extensionClassName = valueClassName + "CachedObjectWrapper";
        // Create and compile the class on the fly
        ObjectWrapperGenerator gen = new ObjectWrapperGenerator();
        gen.beginWrap(value.getClass(), true, true);
        gen.setEpilogue(value.getClass().getMethods(), "cache.used(this); "
                + (debug ? "System.out.println(\"Accessed\");" : ""));
        gen.addField("cache", ExtendedCache.class, Modifier.PRIVATE);
        gen.addMethod("setCache", new Class[] { ExtendedCache.class },
                Void.TYPE, "this.cache=$(paramNames[0]);");
        gen.endWrap();
        obj = gen.wrap(value);
        extensionsClassCache.put(value.getClass(), obj.getClass());
        } else {
            try {
                Constructor ctor = cls.getConstructor(new Class [] { value.getClass() });
                obj=ctor.newInstance(new Object[] { value });
            } catch (IllegalArgumentException e1) {
                throw new RuntimeException("Could not instantiate wrapper class "+cls,e1);
            } catch (InstantiationException e1) {
                throw new RuntimeException("Could not instantiate wrapper class "+cls,e1);
            } catch (IllegalAccessException e1) {
                throw new RuntimeException("Could not instantiate wrapper class "+cls,e1);
            } catch (InvocationTargetException e1) {
                throw new RuntimeException("Could not instantiate wrapper class "+cls,e1);
            } catch (SecurityException e) {
                throw new RuntimeException("Could not instantiate wrapper class "+cls,e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("Could not instantiate wrapper class "+cls,e);
            }
        }
        try {
            // Set the cache
            Method m = obj.getClass().getMethod("setCache",
                    new Class[] { ExtendedCache.class });
            m.invoke(obj, new Object[] { this });
        } catch (SecurityException e) {
            throw new ObjectWrapperGeneratorException(
                    "Unexpected: can't access method created on purpose by the extended cache",
                    e);
        } catch (NoSuchMethodException e) {
            throw new ObjectWrapperGeneratorException(
                    "Unexpected: method created on purpose by the extended cache is not found",
                    e);
        } catch (IllegalArgumentException e) {
            throw new ObjectWrapperGeneratorException(
                    "Unexpected: can't set the cache in wrapper object", e);
        } catch (IllegalAccessException e) {
            throw new ObjectWrapperGeneratorException(
                    "Unexpected: can't set the cache in wrapper object", e);
        } catch (InvocationTargetException e) {
            throw new ObjectWrapperGeneratorException(
                    "Unexpected: can't set the cache in wrapper object", e);
        }

        return obj;
    }

}
