package org.sadun.util;

import java.lang.reflect.Field;

import org.sadun.util.xml.configuration.AutoConfigurator;

/**
 * Finds a field thru the inheritance hierarchy of the class
 * <p>
 * This class exists only for backwards compatibility. See
 * {@link org.sadun.util.ClassResolver} for a collection of similar methods.
 *
 * @author Cristiano Sadun
 */
public class FieldResolver extends ClassResolver {

    public static void main(String[] args) throws NoSuchFieldException {
        System.out.println(FieldResolver.findField(AutoConfigurator.class, "buffer"));
    }

    /**
     * @param class1
     * @param string
     * @return
     * @throws NoSuchFieldException
     */
    public static Field findField(Class cls, String s) throws NoSuchFieldException {
        return ClassResolver.findField(cls,s);
        
    }

}
