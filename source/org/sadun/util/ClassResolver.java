package org.sadun.util;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;

/**
 * This class allows to look for methods independently from inheritance
 * chains on the parameters.
 *
 * @author Cristiano Sadun
 */
public class ClassResolver {

    /**
     * 
     */
    public ClassResolver() {
        super();
    }

    /**
     * First, the inheritance hierachy of the class is traversed to 
     * look for the field; then the interfaces are examined.
     * 
     * @param class1
     * @param attempt
     * @return
     * @throws NoSuchFieldException
     * @throws NoSuchFieldException
     * @throws 
     */
    public static Field findField(Class cls, String name) throws NoSuchFieldException {
        Class currentCls=cls;
        while(currentCls!=null) {
            Field field;
            try {
                field = cls.getDeclaredField(name);
                if (field!=null) return field;
            } catch (NoSuchFieldException e) {
                // Do nothing
            }
            currentCls=currentCls.getSuperclass();
        }
        // Look into interfaces
        Class [] interfaces = cls.getInterfaces();
        for(int i=0;i<interfaces.length;i++) {
            try {
                return findField(interfaces[i], name);
            } catch(NoSuchFieldException e) {
                // Do nothing
            }
        }
        throw new NoSuchFieldException(name);
    }
    
    /**
     * Browse in  the inheritance hierarchy of the class to find if 
     * an override applies.
     * <p>
     * The exception types are verified unless they are null.
     * 
     * @param cls
     * @param name
     * @param parameterTypes
     * @param exceptionTypes
     * @return
     * @throws NoSuchMethodException
     */
    public static Method findMethod(Class cls, String name, Class [] parameterTypes, Class [] exceptionTypes) throws NoSuchMethodException {
        return (Method)findMethod(false, cls, name,parameterTypes, exceptionTypes);
    }
    
    /**
     * Browse in  the inheritance hierarchy of the class to find if 
     * an override applies.
     * <p>
     * The exception types are not considered.
     * 
     * @param cls
     * @param name
     * @param parameters
     * @param exceptionTypes
     * @return
     * @throws NoSuchMethodException
     */
    public static Method findMethod(Class cls, String name, Object [] parameters) throws NoSuchMethodException {
        Class[]parameterTypes= getTypesOfParams(parameters);
        return (Method)findMethod(false, cls, name,parameterTypes, null);
    }
    
    /**
     * Browse in  the inheritance hierarchy of the class to find if 
     * an override applies.
     * <p>
     * The exception list of the candidate method is not verified.
     * 
     * @param cls
     * @param name
     * @param parameterTypes
     * @param exceptionTypes
     * @return
     * @throws NoSuchMethodException
     */
    public static Method findMethod(Class cls, String name, Class [] parameterTypes) throws NoSuchMethodException {
        return (Method)findMethod(false, cls, name,parameterTypes, null);
    }
    
    /**
     * Browse in  the inheritance hierarchy of the class to find if 
     * an override applies.
     * <p>
     * The exception types are verified unless they are null.
     * 
     * @param cls
     * @param name
     * @param parameterTypes
     * @param exceptionTypes
     * @return
     * @throws NoSuchMethodException
     */
    public static Constructor findConstructor(Class cls, String name, Class [] parameterTypes, Class [] exceptionTypes) throws NoSuchMethodException {
        return (Constructor)findMethod(true, cls, name,parameterTypes, exceptionTypes);
    }
    
    /**
     * Browse in  the inheritance hierarchy of the class to find if 
     * an override applies.
     * <p>
     * The exception list of the candidate method is not verified.
     * 
     * @param cls
     * @param name
     * @param parameterTypes
     * @param exceptionTypes
     * @return
     * @throws NoSuchMethodException
     */
    public static Constructor findConstructor(Class cls, Class [] parameterTypes) throws NoSuchMethodException {
        return (Constructor)findMethod(true, cls, null,parameterTypes, null);
    }
    
    /**
     * Browse in  the inheritance hierarchy of the class to find if 
     * an override applies.
     * <p>
     * The exception list of the candidate method is not verified.
     * 
     * @param cls
     * @param name
     * @param parameters instances of expected parameters
     * @param exceptionTypes
     * @return
     * @throws NoSuchMethodException
     */
    public static Constructor findConstructor(Class cls, Object [] parameters) throws NoSuchMethodException {
        Class[]parameterTypes= getTypesOfParams(parameters);
        return (Constructor)findMethod(true, cls, null, parameterTypes, null);
    }
    
    private static Class [] getTypesOfParams(Object [] parameters) {
        Class [] parameterTypes = new Class[parameters.length];
        for(int i=0;i<parameterTypes.length;i++)
            parameterTypes[i]=parameters[i].getClass();
        return parameterTypes;
    }
        
   private static Object findMethod(boolean isConstructor, Class cls, String name, Class [] parameterTypes, Class [] exceptionTypes) throws NoSuchMethodException {
       assert isConstructor || (!isConstructor && name != null); 
        Class currentCls=cls;
        while(currentCls!=null) {
            
            Object [] m;
            if (isConstructor) m=currentCls.getDeclaredConstructors(); 
            else m=currentCls.getDeclaredMethods();
            for(int i=0;i<m.length;i++) {
                if (isConstructor) {
	                Constructor mt = (Constructor)m[i];
	                if (parameterTypesMatch(mt.getParameterTypes(), mt.getExceptionTypes(), parameterTypes, exceptionTypes)) return mt;
                } else {
                    Method mt = (Method)m[i];
                    if (name.equals(mt.getName())) {
                        if (parameterTypesMatch(mt.getParameterTypes(), mt.getExceptionTypes(), parameterTypes, exceptionTypes)) return mt;
                    }
                }
            }
            if (!isConstructor)
                currentCls=currentCls.getSuperclass();
            else currentCls=null; // Constructors make sense only in the leaf class 
        }
            
        throw new NoSuchMethodException(name);
    }

    /**
     * @param parameterTypes
     * @param parameterTypes2
     * @param exceptionTypes
     * @return
     */
    private static boolean parameterTypesMatch(Class[] declaredParameterTypes,
            Class[] declaredExceptionTypes,
            Class[] candidateParameterTypes, Class[] candidateExceptionTypes) {
        if (declaredParameterTypes.length != candidateParameterTypes.length) return false;
        for(int i=0;i<declaredParameterTypes.length;i++) {
            if (!declaredParameterTypes[i].isAssignableFrom(candidateParameterTypes[i])) return false;
        }
        if (candidateExceptionTypes!=null) {
            for(int i=0;i<declaredParameterTypes.length;i++) {
                if (!declaredExceptionTypes[i].isAssignableFrom(candidateExceptionTypes[i])) return false;
            }
        }
        return true;
    }

}
