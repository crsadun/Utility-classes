package com.deltax.util.listener;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.sadun.util.ClassResolver;

/**
 * This class dispatches signals to method calls via reflection looking for
 * methods called <tt>on<i>&lt;signal class unqualified name&gt;</i>Signal</tt>.
 * <p>
 * For example, upon receiving a signal object whose class name is
 * <tt>org.sadun.signals.<b>Init</b></tt> the dispatcher will attempt to
 * invoke a method called <tt>on<b>Init</b>Signal</tt>.
 * <p>
 * If signal class name already ends with <tt>-Signal</tt>, the suffix is not
 * repeated, so if the signal above is called
 * <tt>org.sadun.signals.<b>InitSignal</b></tt>, the associated method will
 * be the same <tt>on<b>Init</b>Signal</tt>.
 * 
 * 
 * @author Cristiano Sadun
 */
public class Dispatcher implements Listener {

    private Object target;
    private Map clsToMethod = new HashMap();
    private boolean debug = true;
    private final static Pattern methodPattern = Pattern
            .compile("on(.*)Signal");

    /**
     * Create a signal dispatcher for the given target object
     * 
     * @param target
     *            the object which will receive method calls
     */
    public Dispatcher(Object target) {
        this(target, target.getClass().getPackage().getName());
    }

    /**
     * 
     * @param target
     * @param autoSearch
     * @param autoSearchPackages
     */
    public Dispatcher(Object target, String autoSearchPackages) {
        this.target = target;
        if (autoSearchPackages != null) {

            Method[] methods = target.getClass().getDeclaredMethods();
            for (int i = 0; i < methods.length; i++) {
                if (!Modifier.isPublic(methods[i].getModifiers()))
                    continue;
                String mName = methods[i].getName();
                Matcher matcher = methodPattern.matcher(mName);
                if (!matcher.matches())
                    continue;
                
                // Check that the argument are of right type
                Class [] params = methods[i].getParameterTypes();
                if (params.length!=1) {
                    if (debug)
                       System.err.println(mName+" has more than 1 parameter");
                    continue;
                }
                if (! Signal.class.isAssignableFrom(params[0])) {
                    if (debug)
                        System.err.println(mName+" parameter is not derived from Signal class");
                     continue;
                 }
                
                String unqualifiedSignalName = matcher.group(1) + "Signal";
                StringTokenizer st = new StringTokenizer(autoSearchPackages,
                        ",; ");
                while (st.hasMoreTokens()) {
                    String pkgName = st.nextToken();
                    String clsName;
                    if (pkgName.endsWith("."))
                        clsName = pkgName + unqualifiedSignalName;
                    else
                        clsName = pkgName + "." + unqualifiedSignalName;

                    try {
                        Class signalClass = target.getClass().getClassLoader()
                                .loadClass(clsName);
                        
                        // Ensure the signal class name is the same as the parameter
                        if (!params[0].isAssignableFrom(signalClass)) {
                            if (debug)
                                System.err.println("Method parameter type "+params[0].getName()+" is not assignable from "+signalClass.getName());
                        }
                            
                        
                        addKnownSignal(signalClass);
                    } catch (ClassNotFoundException e) {
                        // Not found, ignore
                        if (debug)
                            System.err.println(clsName + " not found");
                    }
                }

            }
        }
    }

    /**
     * Add a signal class to the ones known by the dispatcher.
     * 
     * @param signalClass
     */
    public void addKnownSignal(Class signalClass) {
        if (!Signal.class.isAssignableFrom(signalClass))
            throw new IllegalArgumentException("The class " + signalClass
                    + " does not extend " + Signal.class.getName());

        String methodName = baseName(signalClass.getName());
        if (methodName.length() > 1) {
            methodName = Character.toUpperCase(methodName.charAt(0))
                    + methodName.substring(1);
        }

        if (methodName.endsWith("Signal"))
            methodName = "on" + methodName;
        else
            methodName = "on" + methodName + "Signal";
        try {
            Method m = ClassResolver.findMethod(target.getClass(), methodName,
                    new Class[] { signalClass });
            if (m.getReturnType() != Void.TYPE)
                throw new IllegalArgumentException("The target object's "
                        + methodName + "(" + signalClass.getName()
                        + ") method must have void type.");
            clsToMethod.put(signalClass, m);
            if (debug)
                System.err.println("Associated known signal " + signalClass
                        + " to method " + m.getName());
        } catch (SecurityException e) {
            throw new RuntimeException(e);
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException("The target object (of class "
                    + target.getClass().getName()
                    + " does not have a public void " + methodName + "("
                    + signalClass.getName() + ") method.");
        }
    }

    public void receive(Signal signal) {
        Method m = (Method) clsToMethod.get(signal.getClass());
        try {
            m.invoke(target, new Object[] { signal });
        } catch (Exception e) {
            e.printStackTrace();
        }

    }

    private String baseName(String name) {
        int i = name.lastIndexOf('.');
        if (i == -1)
            return name;
        else
            return name.substring(i + 1);
    };
    
    static class TestSignal extends Signal {
        
        TestSignal(Object source) {
            super(source);
        }
    }
    
    static class TestSignal2Signal extends TestSignal {
        
        TestSignal2Signal(Object source) {
            super(source);
        }
    }
}
