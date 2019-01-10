package com.deltax.util;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * A class to analyze a Java method signature declaration, returning information on type names.
 * <p>
 * When returning information, the class may or may not consider primitive types, arrays, or both.
 * If primitive types are returned, the returned type names may include <tt>char</tt>, <tt>boolean</tt>, etc.
 * If arrays are considered, the returned type names may have <tt>[]</tt> qualifications.
 * 
 * @version 1.0
 * @author cris
 */
public class SignatureAnalyzer {
	
	private String signature;
	private String returnTypeName;
	private String[] paramTypeNames;
	private int count;
	private boolean excludePrimitive;
	private boolean excludeArrays;
	
	/**
	 * Create an analyzer for the given signature.
	 * @param signature the method signature to analyze, in internal form
         * @param excludePrimitive if <b>true</b>, primitive types will be discarded
         * @param excludeArrays if <b>true</b> only the component type of array types 
         * will be included in the type information	 
         */
	public SignatureAnalyzer(String signature, boolean excludePrimitive, boolean excludeArrays) {
		this.signature=signature;
		this.excludePrimitive=excludePrimitive;
		this.excludeArrays=excludeArrays;
		List params=new ArrayList();
		StringBuffer returnType = new StringBuffer();
		parse(signature, returnType, params);
		this.returnTypeName=returnType.toString();
		if ("".equals(this.returnTypeName) && excludePrimitive) this.returnTypeName=null;
		this.paramTypeNames=new String[params.size()];
		params.toArray(paramTypeNames);
		this.count=0;
	}

	/**
	 * Create an analyzer for the given signature, which returns both primitive and array type names
	 * @param signature the method signature to analyze, in internal form
	 */	
	public SignatureAnalyzer(String signature) {
		this(signature, false, false);
	}
	
	
	/**
	 * Return name of the method's return type. If primitive types are excluded at {@link 
	 * #SignatureAnalyzer(java.lang.String, boolean, boolean) construction}, and the method's return type 
	 * is primitive, will return <b>null</b>. 
	 * If only array component types are to be considered (as declared at {@link 
	 * #SignatureAnalyzer(java.lang.String, boolean, boolean) construction}), and the method's return type
	 * is an array, the array's element type will be returned (or <b>null</b> if it's primitive and
	 * primitive types are excluded).
	 * @return name of the method's return type, or <b>null</b>
	 */
	public String getReturnTypeName() {
		return returnTypeName;
	}
	
	/**
	 * Return the number of (considered) parameters in the signature
	 * @return int the number of (considered) parameters in the signature
	 */
	public int countParameters() {
		return count;
	}
	
	/**
	 * Return <b>true</b> if {@link #nextParameterTypeName() nextParameterTypeName()} has more
	 * parameters to return
	 * @return <b>true</b> if {@link #nextParameterTypeName() nextParameterTypeName()} has more
	 * parameters to return
	 */
	public boolean hasMoreParameters() {
		return count < paramTypeNames.length;
	}
	
	/**
	 * Return the type name of the next parameter in the list. Depending on
	 * {@link #SignatureAnalyzer(java.lang.String, boolean, boolean) construction settings}, primitive 
	 * types may be excluded and array types may the component type name only.
	 * @return the type name of the next parameter in the list
	 */
	public String nextParameterTypeName() {
		if (! hasMoreParameters()) throw new NoSuchElementException();
		return paramTypeNames[count++];
	}
	

	private void parse(String signature, StringBuffer returnType, List params) {
		
		// Locate the parameters
		if (signature==null) throw new IllegalArgumentException(signature+" is not a valid signature");
		if ("".equals(signature)) throw new IllegalArgumentException(signature+" is not a valid signature");
		if (! (signature.charAt(0)=='(')) throw new IllegalArgumentException(signature+" is not a valid signature");
		int msi=signature.indexOf(')');
		int pos=1;
		StringBuffer currParam=new StringBuffer();
		synchronized(currParam) {
		while(pos<msi) {
			char c=signature.charAt(pos);
			switch(c) {
				case '[': // Array
					pos++;
					if (!excludeArrays) currParam.append("[");
					break;
				case 'L': // Object
					int i=signature.indexOf(';',pos);
					if (i>=msi) throw new IllegalArgumentException(signature+" is not a valid signature");
					currParam.append(signature.substring(pos, i+1));
					pos=i+1;
					params.add(getJavaTypeName(currParam.toString()));
					currParam.delete(0, currParam.length());
					break;
				default: // Primitive
					currParam.append(c);
					if (!excludePrimitive) params.add(getJavaTypeName(currParam.toString()));	
					currParam.delete(0, currParam.length());
					pos++;
			}
		} 
		}
		
		// and the return type
		String typeDescriptor=signature.substring(msi+1);
		if (excludePrimitive || excludeArrays) {
			// Locate the non-array type
			int i;
			for(i=0;i<typeDescriptor.length();i++)
				if (typeDescriptor.charAt(i)!='[') break;
			// If it's primitive, it has length 1
			if (excludePrimitive && typeDescriptor.length()-i==1) return;
			// And consider only the element type if required
			if (excludeArrays && i > 0) typeDescriptor=typeDescriptor.substring(i);
		}
		
		returnType.append(getJavaTypeName(typeDescriptor));
	}
	
	/**
	 * Return the java name for an internal type descriptor of primitive type	 * @param primitiveTypeDescriptor the internal primitive type descriptor	 * @return the java name for an internal type descriptor of primitive type	 */
	public static String getJavaPrimitiveTypeName(char primitiveTypeDescriptor) {
		if ('B'==primitiveTypeDescriptor) return "byte";
		else if ('C'==primitiveTypeDescriptor) return "char";
		else if ('D'==primitiveTypeDescriptor) return "double";
		else if ('F'==primitiveTypeDescriptor) return "float";
		else if ('I'==primitiveTypeDescriptor) return "int";
		else if ('J'==primitiveTypeDescriptor) return "long";
		else if ('S'==primitiveTypeDescriptor) return "short";
		else if ('Z'==primitiveTypeDescriptor) return "boolean"; 
		else if ('V'==primitiveTypeDescriptor) return "void"; 		throw new IllegalArgumentException("Invalid primitive type descriptor \""+primitiveTypeDescriptor+"\"");
	}
	
	/**
	 * Return the internal type descriptor name for an java primitive type
	 * @param javaType the internal primitive type descriptor
	 * @return the java name for an internal type descriptor of primitive type
	 */
	public static char getInternalPrimitiveTypeDescriptor(String javaType) {
		if ("byte".equals(javaType)) return 'B';
		else if ("char".equals(javaType)) return 'C';
		else if ("double".equals(javaType)) return 'D';
		else if ("float".equals(javaType)) return 'F';
		else if ("int".equals(javaType)) return 'I';
		else if ("long".equals(javaType)) return 'J';
		else if ("short".equals(javaType)) return 'S';
		else if ("boolean".equals(javaType)) return 'Z'; 
		else if ("void".equals(javaType)) return 'V'; 
		throw new IllegalArgumentException("Invalid primitive type name \""+javaType+"\"");
	}
	
	/**
	 * Return the java name for an internal type descriptor of primitive, array or object type
	 *	 * @param typeDescriptor the internal type descriptor	 * @return the externalized name for the type descriptor 	 */ 
	public static String getJavaTypeName(String typeDescriptor) {
		if (typeDescriptor.length()==1) return getJavaPrimitiveTypeName(typeDescriptor.charAt(0));
		else if (typeDescriptor.startsWith("L")) {
			int i=typeDescriptor.indexOf(';');
			if (i==-1) throw new IllegalArgumentException("Invalid type descriptor \""+typeDescriptor+"\"");
			return typeDescriptor.substring(1,i).replace('/','.');
		} else if (typeDescriptor.startsWith("[")) {
			if (typeDescriptor.length()==1) throw new IllegalArgumentException("Invalid type descriptor \""+typeDescriptor+"\"");
			return getJavaTypeName(typeDescriptor.substring(1))+"[]";
		}
		throw new IllegalArgumentException("Invalid type descriptor \""+typeDescriptor+"\"");
	}
	
	public static String getJavaTypeName(Class cls) {
		if (cls.isPrimitive()) {
			if (Byte.TYPE==cls) return "byte";
			else if (Character.TYPE==cls) return "char";
			else if (Boolean.TYPE==cls) return "boolean";
			else if (Short.TYPE==cls) return "short";
			else if (Integer.TYPE==cls) return "int";
			else if (Long.TYPE==cls) return "long";
			else if (Double.TYPE==cls) return "double";
			else if (Float.TYPE==cls) return "float";
			else if (Void.TYPE==cls) return "void";
			throw new RuntimeException("Unexpected type "+cls);
		} else if (cls.isArray()) {
			return getJavaTypeName(cls.getComponentType())+"[]";
		}
		else return cls.getName();
	}
	
	public static Class getTypeClass(String javaType) throws ClassNotFoundException {
		try {
		    if (javaType.endsWith("[]")) {
		        Class componentClass=getTypeClass(javaType.substring(0, javaType.length()-2));
		        return Array.newInstance(componentClass,0).getClass();
		    }
		    else
			return getPrimitiveTypeClass(javaType);
		} catch(IllegalArgumentException e) {
			return Class.forName(javaType);
		}
	}
	
	public static Class getPrimitiveTypeClass(String javaPrimitiveTypeName) {
		if ("byte".equals(javaPrimitiveTypeName)) return Byte.TYPE;
		else if ("char".equals(javaPrimitiveTypeName)) return Character.TYPE;
		else if ("double".equals(javaPrimitiveTypeName)) return Double.TYPE;
		else if ("float".equals(javaPrimitiveTypeName)) return Float.TYPE;
		else if ("int".equals(javaPrimitiveTypeName)) return Integer.TYPE;
		else if ("long".equals(javaPrimitiveTypeName)) return Long.TYPE;
		else if ("short".equals(javaPrimitiveTypeName)) return Short.TYPE;
		else if ("boolean".equals(javaPrimitiveTypeName)) return Boolean.TYPE;
		else if ("void".equals(javaPrimitiveTypeName)) return Void.TYPE;
		throw new IllegalArgumentException("Invalid primitive type name \""+javaPrimitiveTypeName+"\"");
	}
	
	public static boolean isPrimitiveTypeName(String javaTypeName) {
		if ("byte".equals(javaTypeName)) return true;
		else if ("char".equals(javaTypeName)) return true;
		else if ("double".equals(javaTypeName)) return true;
		else if ("float".equals(javaTypeName)) return true;
		else if ("int".equals(javaTypeName)) return true;
		else if ("long".equals(javaTypeName)) return true;
		else if ("short".equals(javaTypeName)) return true;
		else if ("boolean".equals(javaTypeName)) return true;
		else if ("void".equals(javaTypeName)) return true;
		return false;
	}

    public static String toExternalParamsList(String paramsInternal) {
        StringBuffer sb = new StringBuffer();
        for(int i=0;i<paramsInternal.length();i++) {
            int array=0;
            char c=paramsInternal.charAt(i);
            while (c=='[') {
                i++;
                if (i==paramsInternal.length()) throw new RuntimeException("? Invalid descriptor "+paramsInternal);
                array++;c=paramsInternal.charAt(i);
            }
            String javaTypeName;
            try {
                javaTypeName=getJavaPrimitiveTypeName(c);
            } catch(IllegalArgumentException e) {
                String s=paramsInternal.substring(i);
                int j=s.indexOf(';');
                String typeDesc=s.substring(0,j+1);
                
                if (typeDesc.length()==0)
                    continue;
                
                javaTypeName=getJavaTypeName(typeDesc);
                i+=j;
            }
            sb.append(javaTypeName);
            for(int k=0;k<array;k++) {
                sb.append("[]");
            }
            if (i<paramsInternal.length()-1) sb.append(", ");
        }
        return sb.toString();
        
    }
}