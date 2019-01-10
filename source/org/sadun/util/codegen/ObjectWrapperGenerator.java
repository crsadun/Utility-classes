/*
 * Created on Aug 29, 2003
 *
 * To change the template for this generated file go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */
package org.sadun.util.codegen;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.sadun.util.IndentedPrintWriter;
import org.sadun.util.ObjectLister;
import org.sadun.util.SymbolTable;
import org.sadun.util.Wrapper;

import com.sun.tools.javac.v8.JavaCompiler;
import com.sun.tools.javac.v8.util.Context;

/**
 * This class generates wrapper classes for existing classes, in both source and compiled form. 
 * <tt>tools.jar</tt> must be in classpath if Class compilation is required.
 * <p>
 * The wrapper class source code is produced in a working directory (by default, 
 * the temporary directory) and compiled on the fly. The resulting class will implement
 * the same interfaces as the wrapped object.
 * <p>
 * Wrapping is performed in two phases: preparation and code/class generation.
 * <p>
 * The preparation phase starts by invoking {@link #beginWrap(Class, String) beginWrap()} (or overloads)
 * which declares which class to wrap. The wrapper class will have a method for each <tt>public</tt>
 * method exposed by the original wrapped class. <tt>static</tt> methods will simply invoke
 * the corresponding method of the wrapped class.
 * <p>
 * Further methods can be added programmatically by {@link #addMethod(String, Class[], String[], Class, String, int) 
 * addMethod()} (or overloads), defining signature and body. An incorrect body will generate compilation
 * errors if an attempt to produce a Class object is executed later.
 * <p>
 * The standard implementation for the wrapper object methods consists in simply invoking corresponding
 * methods in the wrapped object. However, prologue or epilogue code (occurring before or after the wrapped object
 * method invocation) can be added by using the {@link #setPrologue(Method, String, Class[])
 * setPrologue()} and {@link #setEpilogue(Method[], String, Class[]) setEpilogue()} methods (in various overloads).
 * Prologue/epilogue code may have a set of thrown exceptions attached.
 * <p>
 * Prologue and epilogue code can contain references to the some values depending on the current
 * method, as of the following table, expressed by the syntax $(<i>symbol name</i>).
 * <p>
 * Unless declared otherwise when invoking {@link #beginWrap(Class, String, boolean) beginWrap()}, the generated 
 * wrappers implement the {@link org.sadun.util.Wrapper Wrapper} interface.
 * <p>
 * <table align=center width=70% border>
 * <tr align=center><td><b>Symbol</b></td><td><b>Description</b></td></tr>
 * <tr align=center><td><tt>$(methodName)</tt></td><td>the name of the method</td></tr>
 * <tr align=center><td><tt>$(returnType)</tt></td><td>the return type of the method</td></tr>
 * <tr align=center><td><tt>$(paramNames)</tt></td><td>the parameter types of the method</td></tr>
 * <tr align=center><td><tt>$(paramTypes)</tt></td><td>the parameter types of the method</td></tr>
 * </table>
 * <p>
 * For example, the following code is valid in prologue/epilogue: 
 * <tt>System.out.println("Invoking wrapped $(methodName)");</tt> 
 * 
 * <p>
 * {@link #endWrap() endWrap()} terminates the preparation phase.
 * <p>
 * At this point, either {@link #createCode() createCode()}, {@link #createClass() createClass()}
 * or {@link #wrap(Object) wrap()} may be invoked to produce either the source code, a compiled <b>Class</b> object
 * or a wrapper object instance. 
 * <p>
 * Here's an example of typical inteface-based usage:
 * <p>
 * <pre>
 *  <font color=green>// Create a generator</font>
 *  ObjectWrapperGenerator gen = new ObjectWrapperGenerator();
 *  <font color=green>// Define the class to wrap</font>
 *  Class cls = ArrayList.class;
 *  gen.beginWrap(cls, "ListWrapper");
 *  <font color=green>// Add a prologue to all methods</font>
 *  gen.setPrologue(
 *	  cls.getMethods(),
 *	  "System.out.println(\"Wrapped call to \\\"$(methodName)\\\"\");",
 *	  new Class[] { RuntimeException.class });
 *  <font color=green>// End the preparation phase</font>
 *  gen.endWrap();
 *
 *  <font color=green>// Create a wrapped object instance and cast to the List interface</font>
 *  List l = (List) gen.wrap(new ArrayList());
 *  <font color=green>// Use the wrapped object</font>
 *  l.add(new String("Hello"));
 *  <font color=green>// Use the {@link org.sadun.util.Wrapper Wrapper} interface</font>
 *  System.out.println("Wrapping "+((Wrapper)l).getWrappedObject().getClass());
 * </pre> 
 * 
 * @version 1.1
 * @author Cristiano Sadun
 */
public class ObjectWrapperGenerator {

	private static final int BODY_INDENTATION = 3;

	private final class OWGClassLoader extends ClassLoader {
		/* (non-Javadoc)
		 * @see java.lang.ClassLoader#findClass(java.lang.String)
		 */
		protected Class findClass(String name) throws ClassNotFoundException {

			log("Loading class " + name);

			File classfilePath =
				new File(
					workingDirectory
						+ File.separator
						+ name.replace('.', File.separatorChar)
						+ ".class");
			BufferedInputStream is = null;
			try {
				is =
					new BufferedInputStream(new FileInputStream(classfilePath));
				ByteArrayOutputStream os = new ByteArrayOutputStream();
				int c;
				while ((c = is.read()) != -1)
					os.write(c);
				os.close();
				byte[] classDef = os.toByteArray();
				return defineClass(name, classDef, 0, classDef.length);
			} catch (FileNotFoundException e) {
				throw new ClassNotFoundException(
					"Class file " + classfilePath + " not found");
			} catch (IOException e) {
				throw new ClassNotFoundException(
					"Class file " + classfilePath + " not readable",
					e);
			} finally {
				if (is != null)
					try {
						is.close();
					} catch (IOException e1) {
						System.err.println(
							"Warning: cannot close input stream for "
								+ classfilePath);
						e1.printStackTrace(System.err);
					}
			}
		}

	}

	public static String[] makeParamNames(
		String methodName,
		Class[] paramTypes) {
		String[] paramNames = new String[paramTypes.length];
		VariableNameGenerator vgn = new VariableNameGenerator();
		for (int i = 0; i < paramTypes.length; i++) {
			paramNames[i] = vgn.generateNext(methodName, paramTypes[i]);
		}
		return paramNames;
	}
	
	private class FieldSpec {
	    
        private String name;
        private Class type;
        private int modifiers;
	    
        public FieldSpec(String name, Class type, int modifiers) {
            this.name=name;
            this.type=type;
            this.modifiers=modifiers;
        }
        
        public boolean equals(Object obj) {
            if (obj instanceof FieldSpec) {
                FieldSpec fs = (FieldSpec)obj;
                return fs.name.equals(name) && fs.type==type;
            } else return false;
        }
        
        public int hashCode() { return name.hashCode(); }

        /**
         * @return
         */
        public String getFieldCode() {
            StringBuffer sb = new StringBuffer();
            sb.append(Modifier.toString(modifiers));
            sb.append(' ');
            sb.append(ensureCorrectTypeName(type));
            sb.append(' ');
            sb.append(name);
            sb.append(';');
            return sb.toString();
            
        }
	}

	private class MethodSpec {

		private String name;
		private Class[] paramTypes;
		private Class returnType;
		private String bodyCode;
		private int modifiers;
		private String[] paramNames;

		private MethodSpec(String name, Class[] paramTypes) {
			this.name = name;
			this.paramTypes = paramTypes;
		}

		public MethodSpec(
			String name,
			Class[] paramTypes,
			Class returnType,
			String bodyCode,
			int modifiers) {
			this(name, paramTypes, null, returnType, bodyCode, modifiers);
		}

		public MethodSpec(
			String name,
			Class[] paramTypes,
			Class returnType,
			String bodyCode) {
			this(name, paramTypes, null, returnType, bodyCode);
		}

		public MethodSpec(
			String name,
			Class[] paramTypes,
			String[] paramNames,
			Class returnType,
			String bodyCode,
			int modifiers) {
			this.name = name;
			this.paramTypes = paramTypes;
			this.returnType = returnType;
			this.bodyCode = bodyCode;
			this.modifiers = modifiers;
			if (paramNames == null) {
				paramNames = makeParamNames(name, paramTypes);
			}
			assert paramNames.length == paramTypes.length;
			this.paramNames = paramNames;
		}

		public MethodSpec(
			String name,
			Class[] paramTypes,
			String[] paramNames,
			Class returnType,
			String bodyCode) {
			this(
				name,
				paramTypes,
				paramNames,
				returnType,
				bodyCode,
				Modifier.PUBLIC);
		}

		public String getMethodDeclaration(SymbolTable st) {

			StringWriter sw = new StringWriter();
			PrintWriter pw = new PrintWriter(sw);

			pw.print(Modifier.toString(modifiers));
			pw.print(" ");
			pw.print(TypeNameGenerator.getName(returnType));
			pw.print(" ");
			pw.print(name);
			pw.print("(");

			for (int i = 0; i < paramTypes.length; i++) {
				pw.print(TypeNameGenerator.getName(paramTypes[i]));
				pw.print(" ");
				pw.print(paramNames[i]);
				st.defineSymbol("paramNames["+i+"]", paramNames[i]);
				if (i < paramTypes.length - 1)
					pw.print(", ");
			}
			pw.print(")");

			return sw.toString();
		}

		public String getMethodCode(SymbolTable st) {
			StringWriter sw = new StringWriter();
			IndentedPrintWriter pw = new IndentedPrintWriter(sw);

			pw.print(getMethodDeclaration(st));
			pw.println(" {");
			pw.incIndentation(BODY_INDENTATION);
			pw.println(st.evaluate(bodyCode));
			pw.decIndentation(BODY_INDENTATION);
			pw.println("}");

			return sw.toString();
		}

		public boolean equals(Object obj) {
			if (obj instanceof MethodSpec) {
				MethodSpec ms = (MethodSpec) obj;
				return ms.name.equals(name) && ms.paramTypes.equals(paramTypes);
			}
			return false;
		}

		public int hashCode() {
			return name.hashCode();
		}
		
		public String toString() {
		    return getMethodDeclaration(new SymbolTable());
		}

	}

	private String workingDirectory = System.getProperty("java.io.tmpdir");
	private OWGClassLoader owgClassLoader = new OWGClassLoader();

	private static final int UNDETERMINED = 0;
	private static final int PREPARATION = 1;
	private static final int READY = 2;

	private Class classToWrap;
	private String wrapperClassName;
	private String wrapperClassPackageName;
	private String wrapperClassBaseName;

	private String currentCode;
	private Class currentWrapperClass;

	private List methodsList;
	private List additionalMethodsList;
	private List additionalFieldsList;
	private Map additionalCodeByMethod;
	private boolean implementWrapperInterface;
	private int phase = UNDETERMINED;
	private SymbolTable symbolTable;

	private Set additionalInterfaces;
	private Map additionalInterfacesImplementorsByMethod = new HashMap();

	private boolean verbose = false;
	private boolean extendsWrappedClass = false;
	

	/**
	 * Begin the preparation phase, setting which class is to be wrapped.
	 * The wrapper class will live in the same package as the wrapped class 
	 * and have the same name concatenated with the <tt>Wrapped</tt> postfix.
	 * <p>
	 * For example, the wrapper for a class <tt>MyClass</tt> will be named
	 * <tt>MyClassWrapper</tt>.
	 * 
	 * @param cls the class to be wrapped
	 * @param implementWrapperInterface if <b>true</b>, the wrapper object will implement the 
	 *         {@link org.sadun.util.Wrapper Wrapper} interface
	 */
	public void beginWrap(Class cls, boolean implementWrapperInterface) {
		beginWrap(
			cls,
			mkDefaultWrapperClassName(cls),
			implementWrapperInterface);
	}
	
	/**
	 * Begin the preparation phase, setting which class is to be wrapped.
	 * The wrapper class will live in the same package as the wrapped class 
	 * and have the same name concatenated with the <tt>Wrapped</tt> postfix.
	 * <p>
	 * For example, the wrapper for a class <tt>MyClass</tt> will be named
	 * <tt>MyClassWrapper</tt>.
	 * 
	 * @param cls the class to be wrapped
	 * @param implementWrapperInterface if <b>true</b>, the wrapper object will implement the 
	 *         {@link org.sadun.util.Wrapper Wrapper} interface
	 */
	public void beginWrap(Class cls, boolean implementWrapperInterface, boolean extendWrappedClass) {
		beginWrap(
			cls,
			mkDefaultWrapperClassName(cls),
			extendWrappedClass,
			implementWrapperInterface);
	}

	/**
	 * Begin the preparation phase, setting which class is to be wrapped.
	 * The wrapper class will live in the same package as the wrapped class 
	 * and have the same name concatenated with the <tt>Wrapped</tt> postfix.
	 * <p>
	 * For example, the wrapper for a class <tt>MyClass</tt> will be named
	 * <tt>MyClassWrapper</tt>.
	 * 
	 * @param cls the class to be wrapped
     * @param extendWrappedClass makes the wrapper type-compatible with the wrapped class. 
     *         The wrapped class must have a public default constructor.
	 */
	public void beginWrap(Class cls) {
		beginWrap(cls, mkDefaultWrapperClassName(cls),false);
	}

	/**
	 * Begin the preparation phase, setting which class is to be wrapped
	 * and the fully qualified name of the wrapper class. A getter for the wrapped object
	 * is automatically added and the wrapper object will implement the
	 * Wrapper interface.
	 * 
	 * @param cls the class to be wrapped
	 * @param wrapperClassName the fully qualified name of the wrapper class
     * @param extendWrappedClass makes the wrapper type-compatible with the wrapped class. 
     *         The wrapped class must have a public default constructor.
	 */
	public void beginWrap(Class cls, String wrapperClassName, boolean extendWrappedClass) {
		beginWrap(cls, wrapperClassName, extendWrappedClass, true);
	}

	/**
	 * Begin the preparation phase, setting which class is to be wrapped
	 * and the name of the wrapper class. 
	 * 
	 * @param cls the class to be wrapped
	 * @param wrapperClassName the fully qualified name of the wrapper class
	 * @param implementWrapperInterface if <b>true</b>, the wrapper object will implement the 
	 *         {@link org.sadun.util.Wrapper Wrapper} interface
	 * @param extendWrappedClass makes the wrapper type-compatible with the wrapped class. 
     *         The wrapped class must have a public default constructor.
	 */
	public void beginWrap(
		Class cls,
		String wrapperClassName,
		boolean implementWrapperInterface,
		boolean extendWrappedClass) {

		log(
			"Beginning wrap of "
				+ cls
				+ " to "
				+ wrapperClassName
				+ ", "
				+ (implementWrapperInterface ? "also" : "not ")
				+ " implementing Wrapper interface");

		this.classToWrap = cls;
		this.methodsList = new ArrayList();
		this.additionalMethodsList = new ArrayList();
		this.additionalFieldsList = new ArrayList();
		this.additionalCodeByMethod = new HashMap();
		this.wrapperClassName = wrapperClassName;
		this.phase = PREPARATION;
		this.symbolTable = new SymbolTable();
		this.extendsWrappedClass=extendWrappedClass;
		this.implementWrapperInterface = implementWrapperInterface;
		this.additionalInterfaces = new HashSet();
		this.currentCode = null;
		this.currentWrapperClass = null;

		Method[] methods = cls.getMethods();
		for (int i = 0; i < methods.length; i++) {
			methodsList.add(methods[i]);
		}

		int p = wrapperClassName.lastIndexOf(".");
		if (p != -1) {
			wrapperClassPackageName = wrapperClassName.substring(0, p);
			wrapperClassBaseName = wrapperClassName.substring(p + 1);
		} else {
			wrapperClassPackageName = "";
			wrapperClassBaseName = wrapperClassName;
		}

		// add the getter for the wrapped object
		if (implementWrapperInterface) {
			addMethod(
				"getWrappedObject",
				new Class[0],
				Object.class,
				"return wrappedObject;");

			addMethod(
				"getWrappedObjectClass",
				new Class[0],
				Class.class,
				"return wrappedObject.getClass();");
		}
	}

	/**
	 * @param cls
	 * @return
	 */
	private static String mkDefaultWrapperClassName(Class cls) {
		return ensureCorrectClassName(cls)+"Wrapper";
		/*
		String clsName = cls.getName();
		int i = clsName.lastIndexOf('.');
		if (i != -1)
			clsName = clsName.substring(i + 1);
		return clsName + "Wrapper";
		*/
	}

	/**
	 * Add an interface and a corresponding implementing object to 
	 * the wrapped object.
	 * 
	 * @param interfaceCls
	 * @param implementor
	 */
	public synchronized void addInterface(Class interfaceCls) {

		checkInterface(interfaceCls);

		if (additionalInterfaces.contains(interfaceCls))
			throw new IllegalArgumentException(
				interfaceCls + " has already been added");

		log(
			"Adding interface "
				+ interfaceCls.getName()
				+ " and its methods via a delegate object");

		additionalInterfaces.add(interfaceCls);

		Method[] interfaceMethods = interfaceCls.getMethods();
		for (int i = 0; i < interfaceMethods.length; i++) {
			Method method = interfaceMethods[i];
			methodsList.add(method);
			if (additionalInterfacesImplementorsByMethod.containsKey(method))
				throw new IllegalArgumentException(
					"Collision detected: the method "
						+ method.getName()
						+ " is already defined by the interface "
						+ ((Class) additionalInterfacesImplementorsByMethod
							.get(method))
							.getName());
			additionalInterfacesImplementorsByMethod.put(method, interfaceCls);
		}
	}

	public synchronized void addInterface(
		Class interfaceCls,
		String name,
		Class[] paramTypes,
		String[] paramNames,
		Class returnType,
		String code) {
		checkInterface(interfaceCls);

		log(
			"Adding interface "
				+ interfaceCls
				+ " via specific method declaration (\""
				+ name
				+ "\")");

		// Add the interface to the list of interfaces implemented
		additionalInterfaces.add(interfaceCls);
		// But add only the given method
		addMethod(name, paramTypes, paramNames, returnType, code);
	}

	public synchronized void addInterface(
		Class interfaceCls,
		Method method,
		String code) {
		addInterface(
			interfaceCls,
			method.getName(),
			method.getParameterTypes(),
			makeParamNames(method.getName(), method.getParameterTypes()),
			method.getReturnType(),
			code);
	}

	private void checkInterface(Class interfaceCls) {
		if (phase != PREPARATION)
			throw new IllegalStateException("Please invoke beginWrap() before adding epilogue/prologue code");
		if (!interfaceCls.isInterface())
			throw new IllegalArgumentException(
				interfaceCls + " is not an interface");
		for (int i = 0; i < classToWrap.getInterfaces().length; i++) {
			if (interfaceCls == classToWrap.getInterfaces()[i])
				throw new IllegalArgumentException(
					"The "
						+ interfaceCls.getName()
						+ " interface is already implemented by "
						+ classToWrap);
		}
	}

	private static final int BEFORE = 0;
	private static final int AFTER = 1;
    
    

	/**
	 * Set prologue code for a wrapped method, defined by name and parameter types.
	 * <p>
	 * The prloogue code is executed before the invocation of the wrapped object method occurs.
	 * The prologue code can contain refereneces to symbols (see class description).
	 * <p>
	 * If the code throws any checked exception, use the {@link #setPrologue(Method, String, Class[])
	 * setPrologue() with exceptions} overload. 
	 *  
	 * @param m the wrapped method
	 * @param prologueCode the code
	 */
	public void setPrologue(Method m, String prologueCode) {
		setPrologue(m, prologueCode, null);
	}

	/**
	 * Set prologue code for a wrapped method, defined by name and parameter types.
	 * <p>
	 * The prloogue code is executed before the invocation of the wrapped object method occurs.
	 * The prologue code can contain refereneces to symbols (see class description).
	 * 
	 * @param m
	 * @param prologueCode
	 * @param throwedExceptions
	 */
	public void setPrologue(
		Method m,
		String prologueCode,
		Class[] throwedExceptions) {
		if (m.getDeclaringClass() == Object.class)
			return;
		log("Setting prologue code to method " + m.getName());
		addCode(m, prologueCode, throwedExceptions, BEFORE);
	}

	/**
	 * Set prologue code for a set of methods.
	 * <p>
	 * The prologue code is executed before the invocation of the wrapped object method occurs.
	 * The prologue code can contain refereneces to symbols (see class description).
	 *  
	 * @param methods the set of wrapped method with the same prologue
	 * @param prologueCode the code
	 * @param throwedExceptions the exception thrown by the code.
	 */
	public void setPrologue(
		Method[] methods,
		String prologueCode,
		Class[] throwedExceptions) {
		for (int i = 0; i < methods.length; i++)
			setPrologue(methods[i], prologueCode, throwedExceptions);
	}

	/**
	 * Set prologue code for a set of methods.
	 * <p>
	 * The prloogue code is executed before the invocation of the wrapped object method occurs.
	 * The prologue code can contain refereneces to symbols (see class description).
	 * <p>
	 * If the code throws any checked exception, use the {@link #setPrologue(Method[], String, Class[])
	 * setPrologue() with exceptions} overload.
	 *  
	 * @param methods the set of wrapped method with the same prologue
	 * @param prologueCode the code
	 */
	public void setPrologue(Method[] methods, String prologueCode) {
		setPrologue(methods, prologueCode, null);
	}

	/**
	 * Set prologue code for a wrapped method, defined by name and parameter types.
	 * <p>
	 * The prloogue code is executed before the invocation of the wrapped object method occurs.
	 * The prologue code can contain refereneces to symbols (see class description).
	 *  
	 * @param methodName the name of the wrapped method
	 * @param paramTypes the parameter types of the wrapped method
	 * @param prologueCode the code
	 */
	public void setPrologue(
		String methodName,
		Class[] paramTypes,
		String prologueCode) {
		Method m;
		try {
			m = classToWrap.getMethod(methodName, paramTypes);
			setPrologue(m, prologueCode);
		} catch (SecurityException e) {
			throw new IllegalArgumentException(
				"Cannot access " + methodName + " in " + classToWrap);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(
				"The method "
					+ methodName
					+ " does not exist in "
					+ classToWrap);
		}
	}

	/**
	 * Set epilogue code for a wrapped method, defined by name and parameter types.
	 * <p>
	 * The epilogue code is executed after the invocation of the wrapped object method occurs.
	 * The epilogue code can contain refereneces to symbols (see class description).
	 * <p>
	 * If the code throws any checked exception, use the {@link #setEpilogue(Method, String, Class[])
	 * setEpilogue() with exceptions} overload.
	 * 
	 * @param methodName the name of the wrapped method
	 * @param paramTypes the parameters of the wrapped method
	 * @param prologueCode the code
	 */
	public void setEpilogue(
		String methodName,
		Class[] paramTypes,
		String prologueCode) {
		Method m;
		try {
			m = classToWrap.getMethod(methodName, paramTypes);
			setEpilogue(m, prologueCode);
		} catch (SecurityException e) {
			throw new IllegalArgumentException(
				"Cannot access " + methodName + " in " + classToWrap);
		} catch (NoSuchMethodException e) {
			throw new IllegalArgumentException(
				"The method "
					+ methodName
					+ " does not exist in "
					+ classToWrap);
		}
	}

	/**
	 * Set epilogue code for a wrapped method.
	 * <p>
	 * The epilogue code is executed after the invocation of the wrapped object method occurs.
	 * The epilogue code can contain refereneces to symbols (see class description).
	 * <p>
	 * If the code throws any checked exception, use the {@link #setEpilogue(Method, String, Class[])
	 * setEpilogue() with exceptions} overload. 
	 *  
	 * @param m the wrapped method
	 * @param epilogueCode the code
	 */
	public void setEpilogue(Method m, String epilogueCode) {
		setEpilogue(m, epilogueCode, null);
	}

	/**
	 * Set epilogue code for a wrapped method.
	 * <p>
	 * The epilogue code is executed after the invocation of the wrapped object method occurs.
	 * The epilogue code can contain refereneces to symbols (see class description).
	 *  
	 * @param m the wrapped method
	 * @param epilogueCode the code
	 * @param throwedExceptions the exceptions thrown by the code
	 */
	public void setEpilogue(
		Method m,
		String epilogueCode,
		Class[] throwedExceptions) {
		if (m.getDeclaringClass() == Object.class)
			return;
		log("Setting epilogue code to method " + m.getName());
		addCode(m, epilogueCode, throwedExceptions, AFTER);
	}

	/**
	 * Set epilogue code for a set of wrapped method.
	 * <p>
	 * The epilogue code is executed after the invocation of the wrapped object method occurs.
	 * The epilogue code can contain refereneces to symbols (see class description).
	 *  
	 * @param methods the methods to wrap
	 * @param epilogueCode the code
	 * @param throwedExceptions the exceptions thrown by the code
	 */
	public void setEpilogue(
		Method[] methods,
		String prologueCode,
		Class[] throwedExceptions) {
		for (int i = 0; i < methods.length; i++)
			setEpilogue(methods[i], prologueCode, throwedExceptions);
	}

	/**
	 * Set epilogue code for a set of wrapped method.
	 * <p>
	 * The epilogue code is executed after the invocation of the wrapped object method occurs.
	 * The epilogue code can contain refereneces to symbols (see class description).
	 * <p>
	 * If the code throws any checked exception, use the {@link #setEpilogue(Method[], String, Class[])
	 * setEpilogue() with exceptions} overload.
	 * 
	 * @param methods the methods to wrap
	 * @param epilogueCode the code
	 */
	public void setEpilogue(Method[] methods, String epilogueCode) {
		setPrologue(methods, epilogueCode, null);
	}

	private void addCode(
		Method m,
		String code,
		Class[] throwedExceptions,
		int position) {
		assert position == BEFORE || position == AFTER;

		if (phase != PREPARATION)
			throw new IllegalStateException("Please invoke beginWrap() before adding epilogue/prologue code");

		if (m.getDeclaringClass() == Object.class)
			return; // Ignore Object methods
		if (classToWrap.isAssignableFrom(m.getClass()))
			throw new IllegalArgumentException(
				"The method " + m + " does not exist in " + classToWrap);
		Object[] additionalCode = (Object[]) additionalCodeByMethod.get(m);
		if (additionalCode == null) {
			additionalCode = new Object[2];
			additionalCode[BEFORE] = new ArrayList();
			additionalCode[AFTER] = new ArrayList();
			additionalCodeByMethod.put(m, additionalCode);
		}

		Object[] codeSpec = new Object[2];
		codeSpec[0] = code;
		codeSpec[1] = throwedExceptions;

		((ArrayList) additionalCode[position]).add(codeSpec);

	}

	/**
	 * Add a method specification to the wrapper class. The method must be either new or a valid overload
	 * not already existing. The method body source code is specified directly in a String object.
	 * 
	 * @param name the method name
	 * @param paramTypes the types of the parameters
	 * @param paramNames the names of the parameters
	 * @param returnType the return type 
	 * @param bodyCode the Java source code of the method body
	 * @param modifiers the modifiers for the method
	 */
	public void addMethod(
		String name,
		Class[] paramTypes,
		String[] paramNames,
		Class returnType,
		String bodyCode,
		int modifiers) {
		checkAdditionalMethod(name, paramTypes, returnType);
		additionalMethodsList.add(
			new MethodSpec(
				name,
				paramTypes,
				paramNames,
				returnType,
				bodyCode,
				modifiers));
	}
	
	/**
	 * Add a field specification to the wrapper class. The field must not exist already in
	 * the class. The field is not initialized.
	 * 
	 * @param name
	 * @param type
	 * @param modifiers
	 */
	public void addField(String name, Class type, int modifiers) {
	   additionalFieldsList.add(new FieldSpec(name, type, modifiers)); 
	}

	/**
	 * Add a method specification to the wrapper class. The method must be either new or a valid overload
	 * not already existing. The method body source code is specified directly in a String object.
	 * <p>
	 * The names of the parameters are automatically generated by using a {@link VariableNameGenerator 
	 * VariableNameGenerator} object and following the rules defined therein.
	 * 
	 * @param name the method name
	 * @param paramTypes the types of the parameters
	 * @param returnType the return type 
	 * @param bodyCode the Java source code of the method body
	 * @param modifiers the modifiers for the method
	 */
	public void addMethod(
		String name,
		Class[] paramTypes,
		Class returnType,
		String bodyCode,
		int modifiers) {
		checkAdditionalMethod(name, paramTypes, returnType);
		additionalMethodsList.add(
			new MethodSpec(name, paramTypes, returnType, bodyCode, modifiers));
	}

	/**
	 * Add a <b>public</b> method specification to the wrapper class. The method must be either new or a valid 
	 * overload not already existing. The method body source code is specified directly in a String object.
	 * 
	 * @param name the method name
	 * @param paramTypes the types of the parameters
	 * @param paramNames the names of the parameters
	 * @param returnType the return type 
	 * @param bodyCode the Java source code of the method body
	 */
	public void addMethod(
		String name,
		Class[] paramTypes,
		String[] paramNames,
		Class returnType,
		String bodyCode) {
		checkAdditionalMethod(name, paramTypes, returnType);
		additionalMethodsList.add(
			new MethodSpec(name, paramTypes, paramNames, returnType, bodyCode));
	}

	/**
	 * Add a <b>public</b> method specification to the wrapper class. The method must be either new or a valid 
	 * overload not already existing. The method body source code is specified directly in a String object.
	 * <p>
	 * The names of the parameters are automatically generated by using a {@link VariableNameGenerator 
	 * VariableNameGenerator} object and following the rules defined therein.
	 * 
	 * @param name the method name
	 * @param paramTypes the types of the parameters
	 * @param returnType the return type 
	 * @param bodyCode the Java source code of the method body
	 */
	public void addMethod(
		String name,
		Class[] paramTypes,
		Class returnType,
		String bodyCode) {
		checkAdditionalMethod(name, paramTypes, returnType);
		additionalMethodsList.add(
			new MethodSpec(name, paramTypes, returnType, bodyCode));
	}

	private void checkAdditionalMethod(
		String name,
		Class[] paramTypes,
		Class returnType) {

		log("Adding method " + name);

		if (phase != PREPARATION)
			throw new IllegalStateException("Please invoke beginWrap() first");

		for (Iterator i = additionalMethodsList.iterator(); i.hasNext();) {
			MethodSpec ms = (MethodSpec) i.next();
			if (ms.name.equals(name)) {
				if (ms.paramTypes.equals(paramTypes))
					throw new IllegalArgumentException(
						"Cannot add the same method '"
							+ name
							+ "' twice with identical parameter list "
							+ ObjectLister.getInstance().list(paramTypes));
				if (ms.returnType != returnType)
					throw new IllegalArgumentException(
						"Cannot overload method '"
							+ name
							+ "' with different return types ("
							+ TypeNameGenerator.getName(ms.returnType)
							+ ", "
							+ TypeNameGenerator.getName(returnType));
			}

		}
	}

	/**
	 * Terminate the preparation phase. As a result, the generator is ready for
	 * generating either source or compiled code.
	 */
	public void endWrap() {

		log("Ending preparation phase");

		if (phase != PREPARATION)
			throw new IllegalStateException("Please invoke beginWrap() first");
		phase = READY;
	}

	/**
	 * Create the source code for the class. No syntax check is performed.
	 * 
	 * @return the source code for the wrapper object
	 */
	public synchronized String createCode() {
		if (currentCode != null)
			return currentCode;

		if (phase != READY)
			throw new IllegalStateException("Please invoke beginWrap() followed by endWrap() first");

		log("Creating code for " + wrapperClassName);

		StringWriter sw = new StringWriter();
		IndentedPrintWriter pw = new IndentedPrintWriter(sw);
		ObjectLister objectLister = new ObjectLister((char) 0);

		// Package declaration
		if (wrapperClassPackageName.length() != 0) {
			pw.print("package ");
			pw.print(wrapperClassPackageName);
			pw.println(";");
			pw.println();
			pw.println();
		}

		// Class name declaration
		pw.print("public class ");
		pw.print(wrapperClassBaseName);
		
		// should it extend the original class, add the extends
		if (extendsWrappedClass) {
		    pw.print(" extends ");
		    pw.print(ensureCorrectTypeName(this.classToWrap));
		}

		// implemented interfaces
		List interfaces = new ArrayList();
		for (int c = 0; c < classToWrap.getInterfaces().length; c++) {
			interfaces.add(classToWrap.getInterfaces()[c]);
		}

		for (Iterator c = additionalInterfaces.iterator(); c.hasNext();)
			interfaces.add(c.next());

		if (interfaces.size() > 0 || implementWrapperInterface) {
			pw.print(" implements ");
			for(Iterator c=interfaces.iterator();c.hasNext();) {
			    pw.print(ensureCorrectTypeName( (Class)c.next() ));
			}
			//pw.print(objectLister.list(interfaces, "getName"));
		}

		if (implementWrapperInterface) {
			if (interfaces.size() > 0)
				pw.print(", ");
			pw.print(Wrapper.class.getName());
		}

		pw.println(" {");
		pw.println();
		pw.incIndentation(3);

		// Wrapped object declaration
		pw.print("private ");
		pw.print(ensureCorrectTypeName(classToWrap));
		pw.println(" wrappedObject;");

		// Additional interface implementors declaration and names
		VariableNameGenerator vng = new VariableNameGenerator();
		String[] implementorNames = new String[additionalInterfaces.size()];
		Map implementorNamesByInterface = new HashMap();

		int count = 0;
		for (Iterator it = additionalInterfaces.iterator(); it.hasNext();) {
			Class cls = (Class) it.next();
			pw.print("private ");
			pw.print(ensureCorrectTypeName(cls));
			pw.print(" ");
			pw.print(
				implementorNames[count] = vng.generateNext("__internal", cls));
			implementorNamesByInterface.put(cls, implementorNames[count++]);
			pw.println(";");
		}

		pw.println();
		
		// Fields
		for(Iterator it = additionalFieldsList.iterator(); it.hasNext();) {
		    pw.println(((FieldSpec)it.next()).getFieldCode());
		}
		
		pw.println();

		// Constructor 
		if (extendsWrappedClass) {
		    try {
                // Check the base class has a default constructor
                classToWrap.getConstructor(new Class[0]);
            } catch (SecurityException e) {
                throw new RuntimeException("The wrapped class "+ensureCorrectTypeName(classToWrap)+" must have a public default constructor if the wrapper must also extend it",e);
            } catch (NoSuchMethodException e) {
                throw new RuntimeException("The wrapped class "+ensureCorrectTypeName(classToWrap)+" must have a public default constructor if the wrapper must also extend it",e);
            }
		} 
	
		pw.print("public ");
		pw.print(wrapperClassBaseName);
		pw.print("(");
		pw.print(ensureCorrectTypeName(classToWrap));
		pw.print(" wrappedObject");
	

		// Additional implementors
		count = 0;
		for (Iterator it = additionalInterfaces.iterator(); it.hasNext();) {
			Class cls = (Class) it.next();
			pw.print(", ");
			pw.print(ensureCorrectTypeName(cls));
			pw.print(" ");
			pw.print(implementorNames[count++]);
		}

		pw.println(") {");
		pw.incIndentation(2);
		pw.println(
			"if (wrappedObject==null) throw new IllegalArgumentException(\"Cannot wrap null\");");
		pw.println("this.wrappedObject=wrappedObject;");

		// Additional implementors initialization 
		count = 0;
		for (Iterator it = additionalInterfaces.iterator(); it.hasNext();) {
			Class cls = (Class) it.next();
			pw.print("this.");
			pw.print(implementorNames[count]);
			pw.print(" = ");
			pw.print(implementorNames[count++]);
			pw.println(";");
		}

		pw.decIndentation(2);
		pw.println("}");

		// Methods

		for (Iterator i = methodsList.iterator(); i.hasNext();) {
			Method method = (Method) i.next();
			//VariableNameGenerator vng = new VariableNameGenerator();
			if (method.getDeclaringClass() == Object.class)
				continue;

			Object[] additionalCode =
				(Object[]) additionalCodeByMethod.get(method);

			Class[] thrownExceptions = method.getExceptionTypes();

			if (additionalCode != null) {
				// Add required exception types
				Set s = new HashSet();
				for (int z = 0; z < thrownExceptions.length; z++)
					s.add(thrownExceptions[z]);
				s.addAll(getExceptionSet((ArrayList) additionalCode[BEFORE]));
				s.addAll(getExceptionSet((ArrayList) additionalCode[AFTER]));
				thrownExceptions = new Class[s.size()];
				s.toArray(thrownExceptions);
			}

			//	Method declaration

			pw.print("public ");

			int mod = method.getModifiers();
			if (Modifier.isStatic(mod))
				pw.print("static ");
			if (Modifier.isStrict(mod))
				pw.print("strictfp");

			pw.print(TypeNameGenerator.getName(method.getReturnType()));
			pw.print(" ");
			pw.print(method.getName());
			pw.print("(");

			Class[] pTypes = method.getParameterTypes();
			String[] pNames = new String[pTypes.length];
			for (int j = 0; j < pTypes.length; j++) {
				pw.print(TypeNameGenerator.getName(pTypes[j]));
				pw.print(" ");
				pw.print(
					pNames[j] = vng.generateNext(method.getName(), pTypes[j]));
				if (j < pTypes.length - 1)
					pw.print(", ");
			}

			pw.print(")");

			// thrown exception 
			if (thrownExceptions.length > 0) {
				pw.print(" throws ");
				for (int k = 0; k < thrownExceptions.length; k++) {
					pw.print(thrownExceptions[k].getName());
					if (k < thrownExceptions.length - 1)
						pw.print(", ");
				}
			}
			pw.println(" {");
			pw.incIndentation(BODY_INDENTATION);

			// Method body

			symbolTable.defineSymbol("methodName", method.getName());
			symbolTable.defineSymbol(
				"returnType",
				TypeNameGenerator.getName(method.getReturnType()));

			List pTypeNames = new ArrayList();
			for (int k0 = 0; k0 < method.getParameterTypes().length; k0++) {
				pTypeNames.add(
					TypeNameGenerator.getName(method.getParameterTypes()[k0]));
			}

			symbolTable.defineSymbol(
				"paramTypes",
				objectLister.list(pTypeNames));
				
			symbolTable.defineSymbol(
				"paramNames",
				objectLister.list(pNames)
			);
			
			for(int c=0;c<pNames.length;c++) {
			symbolTable.defineSymbol(
					"paramNames["+c+"]",
					pNames[c]
				);
			}

			// Prologues
			if (additionalCode != null)
				printCode(pw, (List) additionalCode[BEFORE]);

			if (method.getReturnType() != Void.TYPE)
				pw.print(
					TypeNameGenerator.getName(method.getReturnType())
						+ " __result = ");

			if (Modifier.isStatic(method.getModifiers())) {
				pw.println(classToWrap.getName());
				pw.println(".");
			} else {
				// Check which implementor to use
				if (additionalInterfaces
					.contains(method.getDeclaringClass())) {
					pw.print(
						implementorNamesByInterface.get(
							method.getDeclaringClass()));
					pw.print(".");
				} else
					pw.print("wrappedObject.");
			}
			pw.print(method.getName());
			pw.print("(");

			for (int j = 0; j < pTypes.length; j++) {
				pw.print(pNames[j]);
				if (j < pTypes.length - 1)
					pw.print(", ");
			}
			pw.println(");");

			// Epilogues
			if (additionalCode != null)
				printCode(pw, (List) additionalCode[AFTER]);

			if (method.getReturnType() != Void.TYPE)
				pw.println("return __result;");

			pw.decIndentation(BODY_INDENTATION);
			pw.println("}");
			pw.println();
		}

		// Additional methods
		for (Iterator i = additionalMethodsList.iterator(); i.hasNext();) {

			MethodSpec ms = (MethodSpec) i.next();
			pw.println(ms.getMethodCode(symbolTable));
			pw.println();

		}

		pw.decIndentation(3);
		pw.print("}"); // End class

		return currentCode = sw.toString();
	}

	/**
     * @param class1
     * @return
     */
    private static String ensureCorrectTypeName(Class classToWrap) {
        String s=classToWrap.getName().replaceAll("\\$",".");
        return s;
   }

    /**
     * @param classToWrap2
     * @return
     */
    private static String ensureCorrectClassName(Class classToWrap) {
        String s=classToWrap.getName().replaceAll("\\$","_");
        return s;
    }

    /**
	 * @param object
	 * @return
	 */
	private Set getExceptionSet(List l) {
		Set s = new HashSet();
		for (Iterator i = l.iterator(); i.hasNext();) {
			Object[] additionalCode = (Object[]) i.next();
			Class[] exc = (Class[]) additionalCode[1];
			if (exc != null)
				for (int j = 0; j < exc.length; j++)
					s.add(exc[j]);
		}
		return s;
	}

	/**
	 * @param pw
	 * @param list
	 */
	private void printCode(IndentedPrintWriter pw, List list) {
		if (list == null)
			return;
		if (list.size() == 0)
			return;
		for (Iterator i = list.iterator(); i.hasNext();) {
			Object[] codeSpec = (Object[]) i.next();
			String code = (String) codeSpec[0];
			code = symbolTable.evaluate(code);
			pw.println(code);
		}
	}

	/**
	 * Return a wrapper class as a <b>Class</b> Object.
	 * @return
	 * @throws IOException
	 */
	public synchronized Class createClass()
		throws ObjectWrapperGeneratorException {

		if (currentWrapperClass != null)
			return currentWrapperClass;

		File sourcePath =
			new File(
				workingDirectory
					+ File.separator
					+ wrapperClassName.replace('.', File.separatorChar)
					+ ".java");

		// Create the code
		StringReader rs = new StringReader(createCode());
		
		log("Compiling code of " + wrapperClassName+" from "+sourcePath.getAbsolutePath());

		// Write it
		sourcePath.getParentFile().mkdirs();
		BufferedWriter br = null;
		try {
			br = new BufferedWriter(new FileWriter(sourcePath));
			int c;
			while ((c = rs.read()) != -1) {
				br.write(c);
			}
		} catch (IOException e2) {
			throw new ObjectWrapperGeneratorException(
				"Could not write wrapper code",
				e2);
		} finally {
			if (br != null)
				try {
					br.close();
				} catch (IOException e3) {
					e3.printStackTrace(System.err);
					System.err.println(
						"Warning: could not close wrapper code file");
				}
		}

		// Compile - tools.jar must be in classpath
		com.sun.tools.javac.v8.util.List list =
			com.sun.tools.javac.v8.util.List.make(sourcePath.getAbsolutePath());
		Context ctx = new Context();
		JavaCompiler jc = new JavaCompiler(ctx);
		try {
			//jc.verbose = true;
			jc.classOutput = true;
			com.sun.tools.javac.v8.util.List l = jc.compile(list);
			jc.close();
		} catch (Throwable e) {
			throw new ObjectWrapperGeneratorException(
				"Code compilation has failed",
				e);
		}

		if (jc.errorCount() != 0)
			throw new ObjectWrapperGeneratorException("Code compilation has failed");

		try {
			return currentWrapperClass =
				owgClassLoader.loadClass(wrapperClassName);
		} catch (ClassNotFoundException e1) {
			throw new ObjectWrapperGeneratorException(
				"Code loading has failed",
				e1);
		}
	}

	/**
	 * Return an instance of the wrapper object for the given object
	 * (when no other implementors are necessary).
	 * 
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	public Object wrap(Object obj) throws ObjectWrapperGeneratorException {
		if (!classToWrap.isAssignableFrom(obj.getClass()))
			throw new IllegalArgumentException(
				"The object "
					+ obj
					+ " is not assignment-compatible with "
					+ classToWrap);
		return wrap(new Object[] { obj });
	}

	/**
	 * Return an instance of the wrapper object for the given object.
	 * All the objects in the array are used.
	 * 
	 * @param obj
	 * @return
	 * @throws IOException
	 */
	public synchronized Object wrap(Object[] obj)
		throws ObjectWrapperGeneratorException {
		Class wrapperClass = createClass();

		log("Wrapping " + obj + " in " + wrapperClassName);

		Constructor ctor;

		// Determine the construction parameters (wrapped object + additional interfaces implementors)
		List ctorParams = new ArrayList();
		ctorParams.add(classToWrap);
		for (Iterator i = additionalInterfaces.iterator(); i.hasNext();) {
			ctorParams.add(i.next());
		}
		Class[] ctorParamsArray = new Class[ctorParams.size()];
		ctorParams.toArray(ctorParamsArray);

		try {
			ctor = wrapperClass.getConstructor(ctorParamsArray);
			return ctor.newInstance(obj);
		} catch (SecurityException e) {
			throw new ObjectWrapperGeneratorException(e);
		} catch (NoSuchMethodException e) {
			throw new ObjectWrapperGeneratorException(e);
		} catch (InstantiationException e) {
			throw new ObjectWrapperGeneratorException(e);
		} catch (IllegalAccessException e) {
			throw new ObjectWrapperGeneratorException(e);
		} catch (InvocationTargetException e) {
			throw new ObjectWrapperGeneratorException(e);
		}
	}

	/**
	 * Get the working directory, where source and class files are stored.
	 * 
	 * @return the working directory, where source and class files are stored.
	 */
	public String getWorkingDirectory() {
		return workingDirectory;
	}

	/**
	 * Set the working directory, where source and class files are stored.
	 * 
	 * @param workingDirectory the directory where source and class files are stored.
	 */
	public void setWorkingDirectory(String workingDirectory) {
		this.workingDirectory = workingDirectory;
	}

	/*
	public static void main(String args[]) throws IOException {
		ObjectWrapperGenerator gen = new ObjectWrapperGenerator();
	
		Class cls = ArrayList.class;
	
		gen.beginWrap(cls, "test.ListWrapper");
		gen.setPrologue(
			cls.getMethods(),
			"System.out.println(\"Wrapped call to \\\"$(methodName)\\\"\");",
			new Class[] { RuntimeException.class });
		gen.addInterface(Terminable.class);
		gen.endWrap();
		System.out.println(gen.createCode());
		//List l1 = (List) gen.wrap(new Object[] { new ArrayList()});
		//List l2 = (List) gen.wrap(new Object[] { new ArrayList()});
	
		//l.add(new String("Hello"));
		//System.out.println("Wrapping " + ((Wrapper) l).getWrappedObject().getClass());
	}
	
	private static class TestTerminable implements Terminable {
		public boolean isShuttingDown() {
			return false;
		}
		public void shutdown() {
		}
		public void run() {
		}
	
	}
	*/

	/**
	 * @param string
	 */
	private void log(String msg) {
		if (verbose)
			System.err.println(msg);
	}

	/**
	 * Return true if the generator is in verbose mode, otherwise false.
	 * @return true if the generator is in verbose mode, otherwise false.
	 */
	public boolean isVerbose() {
		return verbose;
	}

	/**
	 * Set the verbose mode, which provides information on the generator's actions on standard error
	 * @param verbose true or false to enable/disable the verbose mode
	 */
	public void setVerbose(boolean verbose) {
		this.verbose = verbose;
	}

}
