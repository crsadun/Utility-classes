package org.sadun.util.pool2;

import java.io.IOException;
import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;

/**
 * This classloader can generate and load code for pooled object
 * to wrap existing classes, if the <tt>org.sadun.util.pool2.StubClassLoader.generate</tt>
 * property is set.
 * 
 * @author Cristiano Sadun
 *
 */
class StubClassLoader extends ClassLoader {

	private Map stubsMap = new HashMap();
	private StubGenerator sg;
	private PrintStream logStream;
	public boolean generateIfNotFound;

	public StubClassLoader(Class basePooledObject) throws IOException {
		this(basePooledObject, System.getProperty("org.sadun.util.pool2.StubClassLoader.generate") != null);
	}

	public StubClassLoader(Class basePooledObject, boolean generateIfNotFound) throws IOException {
		sg = new StubGenerator(basePooledObject);
		this.generateIfNotFound=generateIfNotFound;
	}

	public static String getPooledClassName(Class cls) {
		return getPooledClassName(cls.getName());
	}

	/**
	 * Return the qualifed class name of the pooled object for the given qualifed class name.
	 */
	public static String getPooledClassName(String qualifiedName) {

		int i=qualifiedName.lastIndexOf(".");
		String name;
		if (i==-1) name=qualifiedName;
		else name=qualifiedName.substring(0,i)+".Pooled"+qualifiedName.substring(i+1);

		return name;
	}


	/**
	 * @see java.lang.ClassLoader#findClass(String)
	 */
	protected Class findClass(String qualifiedName) throws ClassNotFoundException {

		if (!generateIfNotFound)
			throw new ClassNotFoundException(qualifiedName+" is not in class path. Please define the property \"org.sadun.util.pool2.StubClassLoader.generate\" if you want to auto-generate stub classes at runtime");

		String pooledClassName=getPooledClassName(qualifiedName);
		Class cls;
		if ((cls=(Class)stubsMap.get(pooledClassName))!=null) {
			if (logStream!=null)
				logStream.println("Bytecode for "+pooledClassName+" already generated");
			return cls;
		}

		if (logStream!=null)
			logStream.println("Generating and loading bytecode for "+pooledClassName);

		int i=qualifiedName.lastIndexOf(".");
		String name;
		if (i==-1) name=qualifiedName;
		else name=qualifiedName.substring(i+1);

		if (!name.startsWith("Pooled"))
			throw new IllegalArgumentException("This ClassLoader can be only used to load pooled objects");
		Object obj = stubsMap.get(name);
		if (obj != null) return (Class)obj;
		String name2=qualifiedName.substring(0,i)+"."+name.substring(6);
		try {
			byte [] clsBytes = sg.generateStub(this.loadClass(name2));
			cls=this.defineClass(qualifiedName, clsBytes, 0, clsBytes.length);
		} catch (IOException e) {
			throw new ClassNotFoundException("Could not generate stub", e);
		}
		stubsMap.put(name, cls);
		return cls;
	}

	public static void main(String[] args) throws Exception {
		new StubClassLoader(BasePooledObject.class).loadClass("org.sadun.util.pool2.PooledStubGenerator");
	}


	/**
	 * Returns the logStream.
	 * @return PrintStream
	 */
	public PrintStream getLogStream() {
		return logStream;
	}

	/**
	 * Sets the logStream.
	 * @param logStream The logStream to set
	 */
	public void setLogStream(PrintStream logStream) {
		this.logStream=logStream;
		this.sg.setLogStream(logStream);
	}

}
