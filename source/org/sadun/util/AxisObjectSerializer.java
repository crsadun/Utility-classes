package org.sadun.util;

import java.io.ByteArrayOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.lang.reflect.Method;

import org.xml.sax.Attributes;

/**
 * This class serializes an AXIS object into an XML byte array.
 * <p>
 * The java objexts must have been generated with 
 * <a href="http://ws.apache.org/axis/java/user-guide.html#WSDL2JavaBuildingStubsSkeletonsAndDataTypesFromWSDL"/>wsdl2java</a>.
 * 1.2RC3.
 * <p>
 * This class uses reflection and does not refer directly to any AXIS class, so
 * it does not require Axis libraries in classpath to compile.
 * <p>
 * This class is a singleton.
 * 
 * @author Cristiano Sadun
 */
public class AxisObjectSerializer {

    private Class msgContextCls;
    private Class axisEngineCls;
    private Class axisServerCls;
    private Class serializationContextCls;
    private Class beanSerializerCls;
    private Class qNameCls;
    private Class attributesCls;

    private static AxisObjectSerializer axisObjectSerializer;
    private static Object lock = new Object();

    private AxisObjectSerializer() {

        try {
            msgContextCls = Class.forName("org.apache.axis.MessageContext");
            axisEngineCls = Class.forName("org.apache.axis.AxisEngine");
            axisServerCls = Class.forName("org.apache.axis.server.AxisServer");
            serializationContextCls = Class
                    .forName("org.apache.axis.encoding.SerializationContext");
            beanSerializerCls = Class
                    .forName("org.apache.axis.encoding.ser.BeanSerializer");
            qNameCls = Class.forName("javax.xml.namespace.QName");
        } catch (ClassNotFoundException e) {
            throw new RuntimeException(
                    "The AXIS libraries are not in classpath (missing "+e.getMessage()+")");
        }
    }

    /**
     * Retrieve the unique instance of the serializer.
     * 
     * @return the unique instance of the serializer.
     */
    public static AxisObjectSerializer getInstance() {
        synchronized (lock) {
            if (axisObjectSerializer == null)
                axisObjectSerializer = new AxisObjectSerializer();
            return axisObjectSerializer;
        }
    }

    /**
     * Serialize an Axis java object to XML.
     * @param obj the object to serialize
     * @return a byte array containing the XML.
     */
    public byte [] serializeAxisObjectToXML(Object obj) {

        Method getTypeDescMethod;
        Method getSerializerMethod;
        try {
            getTypeDescMethod = obj.getClass().getMethod("getTypeDesc",
                    new Class[0]);
            getSerializerMethod = obj
                    .getClass()
                    .getMethod(
                            "getSerializer",
                            new Class[] { String.class, Class.class, qNameCls /* xmlType.getClass() */});
        } catch (NoSuchMethodException e) {
            throw new RuntimeException(obj + " is not an AXIS object");
        }

        try {
            Object axisServer = axisServerCls.newInstance();
            Object messageContext = msgContextCls.getConstructor(
                    new Class[] { axisEngineCls }).newInstance(
                    new Object[] { axisServer });

            ByteArrayOutputStream os;
            Writer w = new OutputStreamWriter(os=new ByteArrayOutputStream());

            Object serializationContext = serializationContextCls
                    .getConstructor(new Class[] { Writer.class, msgContextCls })
                    .newInstance(new Object[] { w, messageContext });

            Object typeDesc = getTypeDescMethod.invoke(null, new Object[0]);
            Method getXMLTypeMethod = typeDesc.getClass().getMethod(
                    "getXmlType", new Class[0]);
            Object xmlType = getXMLTypeMethod.invoke(typeDesc, new Object[0]);

            Object beanSerializer = getSerializerMethod.invoke(null,
                    new Object[] { "", obj.getClass(), xmlType });

            Method serializeMethod = beanSerializer.getClass().getMethod(
                    "serialize",
                    new Class[] { qNameCls, Attributes.class, Object.class,
                            serializationContextCls });

            serializeMethod.invoke(beanSerializer, new Object[] { xmlType,
                    null, obj, serializationContext });
            w.flush();
            return os.toByteArray();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
    
    /**
     * Serialize an Axis java object to XML.
     * 
     * @param obj the object to serialize
     * @return a String containing the XML.
     */
    public String serializeAxisObjectToXMLString(Object obj) {
        
        byte [] array = serializeAxisObjectToXML(obj);
        
        return new String(array);
    }

}