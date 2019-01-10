package org.sadun.util.codegen;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 *
 * A class to generate variable names depending on types. 
 * <p>
 * Given a method name and a Java type, the {@link #generateNext(java.lang.String, java.lang.Class) generateNext()} 
 * method produces  valid (i.e. compilable) sequences of variable names, depending on the passed type,
 * until either a different method name is passed or the {@link #reset() reset()} method is invoked.
 * <p>
 * For example, the invocations:
 * <pre>
 *  generateNext(&quot;myMethod&quot;, t1);
 *  generateNext(&quot;myMethod&quot;, t2);
 *  generateNext(&quot;myMethod&quot;, t3);
 * </pre>
 * where t1=<tt>String</tt>, t2=<tt>int</tt>, t3=<tt>String</tt> will produce 
 * <tt>string1, i1, string2</tt>.
 * <p>
 * This generator supports both JDK1.3 and JDK1.4 keywords.
 * 
 * @version 1.0
 * @author Cristiano Sadun
 */
public class VariableNameGenerator {

	/**
	 * Constant to use at construction to indicate JDK1_3 keyword compliance.
	 */	
	public static final int JDK1_3=0;
	
	/**
	 * Constant to use at construction to indicate JDK1_4 keyword compliance.
	 */	
	public static final int JDK1_4=1;
	
	private Map prefixMap;
	private String currentMethodName = null;
	private int jdkRelease;

	private static Set reservedNames;
	private static Set jdk14reservedNames;

	/**
	 * Create a variable name generator for the given target compiler (see class constants).
	 * @param jdkRelease one of the JDK_x constants.
	 */
	public VariableNameGenerator(int jdkRelease) {
		if (jdkRelease != JDK1_3 &&
		    jdkRelease != JDK1_4) throw new IllegalArgumentException("jdk release parameter incorrect - use the constants defined by this class");  
		this.jdkRelease=jdkRelease;
		this.prefixMap = new HashMap();
	}
	
	/**
	 * Create a variable name generator for the JDK1_4 target compiler (see class constants).
	 */
	public VariableNameGenerator() {
		this(JDK1_4);
	}

	/**
	 * Reset the name generator
	 */
	public void reset() {
		prefixMap = new HashMap();
	}

	public String generateNext(String methodName, Class type) {
		if (!methodName.equals(currentMethodName)) {
			currentMethodName = methodName;
			reset();
		}
		if (type.isPrimitive())
			return generateForPrimitiveType(methodName, type);
		else if (type.isArray()) {
			String s = generateNext(methodName, type.getComponentType());
			if (!s.endsWith("Array"))
				s += "Array";
			return s;
		}
		return generateForObjectType(methodName, type);
	}

	/**
	 * Method generateForObjectType.
	 * @param methodName
	 * @param type
	 * @return String
	 */
	private String generateForObjectType(String methodName, Class type) {
		int i = type.getName().lastIndexOf(".");
		if (i == -1)
			return pickNext(getCanonicalName(type.getName()));
		else
			return pickNext(getCanonicalName(type.getName().substring(i + 1)));
	}

	private String getCanonicalName(String name) {

		if (name.length() == 1)
			return name.toLowerCase();
		if (Character.isUpperCase(name.charAt(0)))
			name = Character.toLowerCase(name.charAt(0)) + name.substring(1);

		if (isReservedIdentifier(name))
			return "_" + name;
		
		name=name.replaceAll("\\$","_");
		return name;
	}

	/**
	 * Method isReservedIdentifier. To be implemented.
	 * @param name
	 * @return boolean
	 */
	private boolean isReservedIdentifier(String name) {
		if (jdkRelease==JDK1_4) {
			if (jdk14reservedNames.contains(name)) return true;
		}
		return reservedNames.contains(name);
	}

	/**
	 * Method generateForPrimitiveType.
	 * @param type
	 */
	private String generateForPrimitiveType(String methodName, Class type) {
		return pickNext(type.getName().substring(0, 1));
	}

	/**
	 * Method pickNext.
	 * @param string
	 * @return String
	 */
	private String pickNext(String s) {
		Integer next = (Integer) prefixMap.get(s);
		if (next == null) {
			next = new Integer(0);
		}
		int n = next.intValue();
		prefixMap.put(s, new Integer(n + 1));
		return s + n;
	}

	static {
		String[] reservedKeywords =
			{
				"abstract",
				"boolean",
				"break",
				"byte",
				"case",
				"catch",
				"char",
				"class",
				"continue",
				"default",
				"delegate",
				"do",
				"double",
				"else",
				"extends",
				"false",
				"final",
				"finally",
				"float",
				"for",
				"if",
				"implements",
				"import",
				"instanceof",
				"int",
				"interface",
				"long",
				"multicast",
				"native",
				"new",
				"null",
				"object",
				"package",
				"private",
				"protected",
				"public",
				"return",
				"short",
				"static",
				"super",
				"switch",
				"synchronized",
				"this",
				"throw",
				"throws",
				"transient",
				"true",
				"try",
				"void",
				"volatile",
				"while" };

		reservedNames = new HashSet();
		for (int i = 0; i < reservedKeywords.length; i++)
			reservedNames.add(reservedKeywords[i]);
		
		String[] jdk14reservedKeywords =
			{ "assert" };
			
		jdk14reservedNames = new HashSet();
		for (int i = 0; i < jdk14reservedKeywords.length; i++)
			jdk14reservedNames .add(jdk14reservedKeywords[i]);	
	}
}
