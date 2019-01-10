package org.sadun.util.pool2;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.StringTokenizer;

import org.sadun.util.IndentedPrintWriter;
import org.sadun.util.codegen.VariableNameGenerator;

import com.deltax.util.SignatureAnalyzer;

/**
 * This class generates a stub for a class C in the same package as C, extending the given
 * base class. The base class must implement the PooledObject interface.
 * 
 * @author Cristiano Sadun
 */
class StubGenerator {

	private static String lineSep = System.getProperty("line.separator");
	private File codeDir;
	private PrintStream logStream;
	private String classPath;
	private Class clsToExtend;

	public StubGenerator(Class clsToExtend) throws IOException {
		this(clsToExtend, new File(File.separator + "temp"));
	}

	public StubGenerator(Class clsToExtend, File codeDir) throws IOException {
		this(clsToExtend, codeDir, System.getProperty("java.class.path"));
	}

	public StubGenerator(Class clsToExtend, File codeDir, String classPath)
		throws IOException {
		this.codeDir = codeDir;
		this.classPath = classPath;
		this.clsToExtend = clsToExtend;
		if (!PooledObject.class.isAssignableFrom(clsToExtend))
			throw new IllegalArgumentException(
				"The given base class "
					+ clsToExtend.getName()
					+ " does not implement the PooledObject interface");
		if (!codeDir.exists())
			if (!codeDir.mkdirs())
				throw new IOException("Cannot create directory " + codeDir);
	}

	public String generateCode(Class cls, StringWriter unqualifiedClsName) {

		if (logStream != null) {
			logStream.println("Generating code..");
		}

		/*
		 * Compute implemented interfaces
		 */
		Set implementedInterfaces = new HashSet();
		Class[] declaredClasses = cls.getInterfaces();
		for (int i = 0; i < declaredClasses.length; i++) {
			//if (declaredClasses[i].isInterface())
				implementedInterfaces.add(declaredClasses[i]);
		}

		StringWriter sw = new StringWriter();
		IndentedPrintWriter pw = new IndentedPrintWriter(sw);

		/*
		 * Package declaration
		 */
		pw.print("package ");
		pw.print(cls.getPackage().getName());
		pw.println(";");
		pw.println();

		/*
		 * Imports
		 */

		pw.println("import org.sadun.util.pool2.ActivationException;");
		pw.println();

		/*
		 * Class name
		 */

		pw.print("public class ");

		unqualifiedClsName.write("Pooled");
		unqualifiedClsName.write(getUnqualifedName(cls));
		pw.print(unqualifiedClsName.toString());
		pw.print(" extends " + BasePooledObject.class.getName());

		/*
		 * Implemented interfaces
		 */
		if (implementedInterfaces.size() != 0) {
			pw.print(" implements ");
			for (Iterator i = implementedInterfaces.iterator(); i.hasNext();) {
				Class interfaceCls=(Class)i.next();
				pw.print(interfaceCls.getName());
				if (i.hasNext())
					pw.print(", ");
			}
		}

		pw.println(" {");
		pw.println();

		/*
		 * Constructor
		 */

		pw.incIndentation(3);

		pw.print("public ");
		pw.print(unqualifiedClsName.toString());
		pw.print("(org.sadun.util.pool2.ObjectPool objectPool, ");
		pw.print(cls.getName());
		pw.println(" obj) {");
		pw.println("   super(objectPool, obj);");
		pw.println("}");
		pw.println();

		/*
		 * Methods
		 */

		Method[] methods = cls.getMethods();
		for (int i = 0; i < methods.length; i++) {
			Method method = methods[i];

			// Skip "Object" methods
			if (method.getDeclaringClass() == Object.class)
				continue;

			int modifiers = method.getModifiers();

			if (Modifier.isPublic(modifiers))
				pw.print("public ");
			else if (Modifier.isPrivate(modifiers))
				pw.print("private ");
			else if (Modifier.isProtected(modifiers))
				pw.print("protected ");

			// Synchronized and native are ignored on purpose
			if (Modifier.isStatic(modifiers))
				pw.print("static ");
			if (Modifier.isFinal(modifiers))
				pw.print("final ");
			if (Modifier.isStrict(modifiers))
				pw.print("strictfp ");

			pw.print(SignatureAnalyzer.getJavaTypeName(method.getReturnType()));
			pw.print(" ");

			pw.print(method.getName());
			pw.print("(");
			Class[] paramTypes = method.getParameterTypes();

			VariableNameGenerator vng = new VariableNameGenerator();
			String[][] pList = new String[paramTypes.length][2];

			for (int j = 0; j < paramTypes.length; j++) {
				pw.print(
					pList[j][0] =
						SignatureAnalyzer.getJavaTypeName(paramTypes[j]));
				pw.print(" ");
				pw.print(
					pList[j][1] =
						vng.generateNext(method.getName(), paramTypes[j]));
				if (j < paramTypes.length - 1)
					pw.print(", ");
			}

			pw.print(") ");

			/*
			 * Exceptions
			 */
			Class[] excTypes = method.getExceptionTypes();
			if (excTypes.length > 0) {
				pw.print("throws ");
				for (int j = 0; j < excTypes.length; j++) {
					pw.print(excTypes[j].getName());
					if (j < excTypes.length - 1)
						pw.print(", ");
				}
				pw.print(" ");
			}

			pw.println("{");

			/*
			 * Method body
			 */

			addMethodBody(pw, pList, cls, method);

			pw.println("}");
			pw.println();
		}

		pw.decIndentation(3);

		pw.print("}");

		return sw.toString();
	}

	public byte[] generateStub(Class cls) throws IOException {

		// Create the file
		StringWriter clsName = new StringWriter();
		StringReader code = new StringReader(generateCode(cls, clsName));

		File sourceCodeFile =
			getFileForClassSource(cls.getPackage(), clsName.toString());
		if (logStream != null) {
			logStream.println(
				"Writing " + sourceCodeFile.getAbsolutePath() + "..");
		}

		BufferedWriter bw = new BufferedWriter(new FileWriter(sourceCodeFile));
		int c;
		while ((c = code.read()) != -1)
			bw.write(c);
		bw.close();

		// Delete existing class file if any
		File compiledFile =
			getFileForCompiledClass(cls.getPackage(), clsName.toString());
		if (compiledFile.exists()) {
			if (logStream != null) {
				logStream.println(
					"Deleting existing "
						+ compiledFile.getAbsolutePath()
						+ "..");
			}
			if (!compiledFile.delete())
				throw new IOException(
					"Cannot delete existing class file " + compiledFile);
		}

		// Create command line options
		String[] args = new String[5];

		args[0] = "-classpath";
		args[1] = codeDir.getCanonicalPath();
		if (classPath != null)
			args[1] += ";" + classPath;
		args[2] = "-d";
		args[3] = codeDir.getCanonicalPath();
		args[4] = sourceCodeFile.getAbsolutePath();

		if (logStream != null) {
			synchronized (logStream) {
				logStream.print("Invoking javac ");
				for (int i = 0; i < args.length; i++) {
					logStream.print(args[i]);
					if (i < args.length - 1)
						logStream.print(" ");
				}
				logStream.println();
			}
		}

		// Compile it

		com.sun.tools.javac.Main compiler = new com.sun.tools.javac.Main();

		compiler.compile(args);

		if (logStream != null) {
			logStream.println("Reading " + compiledFile.getAbsolutePath());
		}

		ByteArrayOutputStream bis = new ByteArrayOutputStream();
		BufferedInputStream is =
			new BufferedInputStream(new FileInputStream(compiledFile));
		while ((c = is.read()) != -1)
			bis.write(c);
		is.close();

		if (logStream != null) {
			logStream.println(
				"Class bytecode for " + clsName.toString() + " generated");
		}

		return bis.toByteArray();

	}

	private File getFileForClassSource(Package pkg, String unqualifiedClsName)
		throws IOException {
		return getFileForClass0(pkg, unqualifiedClsName, "java");
	}

	private File getFileForCompiledClass(
		Package pkg,
		String unqualifiedClsName)
		throws IOException {
		return getFileForClass0(pkg, unqualifiedClsName, "class");
	}

	/**
	 * Method getFileForClass0.
	 * @param string
	 */
	private File getFileForClass0(
		Package pkg,
		String unqualifiedClsName,
		String ext)
		throws IOException {
		StringWriter sw = new StringWriter();
		StringTokenizer st = new StringTokenizer(pkg.getName(), ".");
		while (st.hasMoreTokens()) {
			sw.write(st.nextToken());
			sw.write(File.separator);
		}

		File dir = new File(codeDir, sw.toString());
		if (!dir.exists())
			if (!dir.mkdirs())
				throw new IOException(
					"Cannot create directory " + dir.getAbsolutePath());

		sw.write(unqualifiedClsName);
		File f = new File(codeDir, sw.toString() + "." + ext);
		return f;
	}

	/**
	 * Method addMethodBody.
	 * @param method
	 */
	private void addMethodBody(
		IndentedPrintWriter pw,
		String[][] pList,
		Class originalClass,
		Method method) {
		pw.incIndentation(3);

		if (!Modifier.isStatic(method.getModifiers())) {
			pw.println("_getOriginal();");

			if (method.getReturnType() != Void.TYPE)
				printResultVariable(method.getReturnType(), pw);

			pw.print("((");
			pw.print(originalClass.getName());
			pw.print(")");
			pw.print("getOriginal()).");
		} else {

			if (method.getReturnType() != Void.TYPE)
				printResultVariable(method.getReturnType(), pw);

			pw.print(originalClass.getName());
			pw.print(".");
		}
		pw.print(method.getName());
		pw.print("(");
		for (int i = 0; i < pList.length; i++) {
			pw.print(pList[i][1]);
			if (i < pList.length - 1)
				pw.print(",");
		}
		pw.println(");");
		if (!Modifier.isStatic(method.getModifiers()))
			pw.println("_releaseOriginal();");

		if (method.getReturnType() != Void.TYPE)
			pw.println("return res;");

		pw.decIndentation(3);

	}

	private void printResultVariable(
		Class returnType,
		IndentedPrintWriter pw) {
		pw.print(SignatureAnalyzer.getJavaTypeName(returnType));
		pw.print(" res = ");
	}

	/**
	 * Method getUnqualifedName.
	 * @param class
	 * @return String
	 */
	private String getUnqualifedName(Class cls) {
		int i = cls.getName().lastIndexOf(".");
		if (i == -1)
			return cls.getName();
		return cls.getName().substring(i + 1);
	}

	/*public static void main(String[] args) throws Exception {
		//String code = new StubGenerator().generateCode(StubGenerator.class, new StringWriter());
		byte[] classBytes =
			new StubGenerator(BasePooledObject.class).generateStub(
				StubGenerator.class);
	}
	*/
	
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
		this.logStream = logStream;
	}

}
