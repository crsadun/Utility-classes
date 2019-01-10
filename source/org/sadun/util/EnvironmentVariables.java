package org.sadun.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.text.CharacterIterator;
import java.text.StringCharacterIterator;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

/**
 * An helper class to handle environment variables when they're present on the
 * underlying operating system. So far, the only supported operating system is
 * Windows 2000 - but the underlying mechanism (based on running a 'set'
 * command on the system shell) is easly usable for different o/s.
 * <p>
 * In order to add support for one OS, add the identification string and the
 * associated shell command in the private <b>getGenerator()</b> method, and
 * check that the parsing code in setBasedGenerator.getEnvironment() is correct
 * for parsing the result.
 * <p>
 * A typical usage consists in just invoking
 * 
 * <pre>
 *  EnvironmentVariables.getInstance(). {@link #toSystemProperties() toSystemProperties()};
 * </pre>
 * 
 * <p>
 * and then browse the system properties for <tt>env.</tt> entries.
 * 
 * @author cris
 */
public class EnvironmentVariables {

	/**
	 * A class implementing this interface is able to retrieve environment
	 * information in some way.
	 * 
	 * @author cris
	 */
	public static interface Generator {

		/**
		 * Return an array of a 2-columns array of Strings containing variable
		 * names and values
		 * 
		 * @return String
		 */
		public String[][] getEnvironment();

	}

	/**
	 * Classes implementing this interface can transform environment names in a
	 * "canonical" form
	 * 
	 * @author cris
	 */
	public static interface NameTransformer {

		public String transform(String name);
	}

	/**
	 * A {@link Generator Generator}based on running 'set' and parsing the
	 * result
	 * 
	 * @author cris
	 */
	private class SetBasedGenerator implements Generator {

		private String cmd;

		public SetBasedGenerator(String cmd) {
			this.cmd = cmd;
		}

		/**
		 * @see org.sadun.util.EnvironmentVariables.Generator#getEnvironment()
		 */
		public String[][] getEnvironment() {
			try {
				Process p = Runtime.getRuntime().exec(cmd);
				try {
					InputStream is = p.getInputStream();
					StringWriter sw = new StringWriter();
					int c;
					while ((c = is.read()) != -1)
						sw.write(c);
					is.close();

					BufferedReader br =
						new BufferedReader(new StringReader(sw.toString()));
					String line;
					List l = new ArrayList();
					while ((line = br.readLine()) != null) {
						int i = line.indexOf("=");
						if (i == -1)
							continue;
						String[] envEntry = new String[2];
						envEntry[0] = line.substring(0, i);
						if (line.length() == i + 1)
							continue;
						envEntry[1] = line.substring(i + 1);
						l.add(envEntry);
					}
					String[][] result = new String[l.size()][2];
					l.toArray(result);
					return result;

				} catch (IOException e) {
					e.printStackTrace();
					throw new RuntimeException("Problem obtaining the result of the 'set' command");
				}
			} catch (IOException e) {
				e.printStackTrace();
				throw new RuntimeException("Impossible to run the 'set' command");
			}
		}
	}

	/**
	 * A {@link EnvironmentVariables.NameTransformer NameTransformer}which
	 * doesn't make any trasformation
	 * 
	 * @author cris
	 */
	private static class IdenticalNameTransformer implements NameTransformer {

		private static IdenticalNameTransformer instance =
			new IdenticalNameTransformer();

		private IdenticalNameTransformer() {
		}

		public static IdenticalNameTransformer getInstance() {
			return instance;
		}

		/**
		 * @see org.sadun.util.EnvironmentVariables.NameTransformer#transform(java.lang.String)
		 */
		public String transform(String name) {
			return name;
		}
	}

	/**
	 * A {@link EnvironmentVariables.NameTransformer NameTransformer}which
	 * produces names in the style of typical java property names. In detail:
	 * <ul>
	 * <li>Each property is prefixed with a given prefix (default <tt>env.</tt>)
	 * <li>Character case is lowered
	 * <li>Underscores are substituted with dots
	 * <li>If the original variable name contains both lowercase and uppercase
	 * letters, every nonconsecutive uppercase letter is considered the start
	 * of a word, and words are divided by dots.
	 * </ul>
	 * <p>
	 * For example, <tt>JAVA_HOME</tt> will become <tt>env.java.home</tt>,
	 * <tt>CommSpec</tt> will become <tt>comm.spec</tt> and <tt>My_StrangeVariableName</tt>
	 * will become my.strange.variable.name
	 * 
	 * @version 1.1
	 * @author cris
	 */
	public class SimpleNameTransformer implements NameTransformer {

		private Map cachedNames = new HashMap();
		private String prefix;

		public SimpleNameTransformer(String prefix) {
			this.prefix = prefix;
		}

		public SimpleNameTransformer() {
			this("env");
		}

		public String transform(String name) {
			if (name.toUpperCase().equals(name)) {
				return transformAllUpperCase(name);
			} else {
				// Preprocess the name
				CharacterIterator iter = new StringCharacterIterator(name);
				StringBuffer sb = new StringBuffer(prefix);
				synchronized (sb) {
					boolean lastWasUpperCase = false;
					for (char c = iter.first();
						c != CharacterIterator.DONE;
						c = iter.next()) {
						if (Character.isUpperCase(c)) {
							if (!lastWasUpperCase) {
								sb.append("_");
								lastWasUpperCase = true;
							} else {
								lastWasUpperCase = false;
							}
							sb.append(c);
						}
					}
					return transformAllUpperCase(sb.toString());
				}
			}
		}

		/**
		 * @see org.sadun.util.EnvironmentVariables.NameTransformer#transform(java.lang.String)
		 */
		public String transformAllUpperCase(String name) {
			String result;

			if ((result = (String) cachedNames.get(name)) != null)
				return result;

			CharacterIterator iter = new StringCharacterIterator(name);
			StringBuffer sb = new StringBuffer(prefix);
			synchronized (sb) {
				if (prefix.length() > 0)
					sb.append(".");
				for (char c = iter.first();
					c != CharacterIterator.DONE;
					c = iter.next()) {
					if (c == '_') {
						if (sb.length() > 0)
							sb.append(".");
					} else
						sb.append(Character.toLowerCase(c));
				}

				result = sb.toString();
				cachedNames.put(name, result);

				return result;
			}
		}
	}

	private static EnvironmentVariables instance;
	private Map env = new HashMap();
	private SimpleNameTransformer simpleNameTransformer;

	/**
	 * Constructor for EnvironmentVariables.
	 */
	public EnvironmentVariables() {
		String[][] env = getGenerator().getEnvironment();
		for (int i = 0; i < env.length; i++) {
			this.env.put(env[i][0], env[i][1]);
		}
		this.simpleNameTransformer = new SimpleNameTransformer();
	}

	/**
	 * Return the name of the given variable, after applying the given
	 * {@link NameTransformer name transformation}
	 * 
	 * @param name
	 *            the name of the required environment entry
	 * @param t
	 *            the transformation to apply
	 * @return the value of the variable with the transformed name, or <b>null
	 *         </b> if such a name is undefined
	 */
	protected String getEnv(String name, NameTransformer t) {
		return (String) env.get(t.transform(name));
	}

	/**
	 * Return the value of the environemnt variable of the given name
	 * 
	 * @param name
	 *            the environemnt variable name
	 * @return the environemnt variable value
	 */
	public String getEnv(String name) {
		return getEnv(name, IdenticalNameTransformer.getInstance());
	}

	/**
	 * Return the value of a property whose name is a transformation of an
	 * environment variable name according to a {@link SimpleNameTransformer
	 * SimpleNameTransformer}
	 * 
	 * @param name
	 * @return String
	 */
	public String getEnvProperty(String name) {
		return getEnv(name, simpleNameTransformer);
	}

	/**
	 * Return the set of environment variables names
	 * 
	 * @return the set of environment variables names
	 */
	public Set envNames() {
		return new HashSet(env.keySet());
	}

	/**
	 * Return the set of property names derived by the environment variables,
	 * as obtained by
	 * {@link #getEnvProperty(java.lang.String) getEnvProperty()}
	 * 
	 * @return the set of property names derived by the environment variables
	 */
	public Set envPropertyNames() {
		Set s = new HashSet();
		for (Iterator i = env.keySet().iterator(); i.hasNext();) {
			s.add(simpleNameTransformer.transform((String) i.next()));
		}
		return s;
	}

	/**
	 * Return a Properties object with the environment properties, whose names
	 * are names derived by the environment variables, as obtained by
	 * {@link #getEnvProperty(java.lang.String) getEnvProperty()}
	 * 
	 * @return Properties
	 */
	public Properties getAsProperties() {
		return getAsProperties(new Properties());
	}

	private Properties getAsProperties(Properties p) {

		for (Iterator i = env.keySet().iterator(); i.hasNext();) {
			String name = (String) i.next();
			p.put(simpleNameTransformer.transform(name), env.get(name));
		}
		return p;
	}

	/**
	 * Add properties obtained by {@link #getEnvProperty(java.lang.String) 
	 * getEnvProperty()} to the system properties.
	 */
	public void toSystemProperties() {
		getAsProperties(System.getProperties());
	}

	/**
	 * Return the single instance of this class
	 * 
	 * @return EnvironmentVariables
	 */
	public static EnvironmentVariables getInstance() {
		if (instance == null)
			instance = new EnvironmentVariables();
		return instance;
	}

	/**
	 * This method checks the operating system name to know and produces a
	 * suitable EnvironmentListGenerator for the platform, or throws an
	 * UnsupportedOperationException
	 * 
	 * @throws UnsupportedOperationException
	 */
	private Generator getGenerator() throws UnsupportedOperationException {
		String osName = System.getProperties().getProperty("os.name");

		if (osName.startsWith("Windows"))
			return new SetBasedGenerator("cmd /c set");
		// Else, we can't
		throw new UnsupportedOperationException("");
	}

}
