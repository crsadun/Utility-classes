package org.sadun.util;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.StringTokenizer;

/**
 * An utility class to compute normalized paths (i.e. paths that do not contain
 * neither "." nor "..")
 * <p>
 * v1.2 supports windows UNC paths (on windows)
 * 
 * @author C. Sadun (v1.1 patched by Doug.Liao@fnf.com)
 * @version 1.2
 */
public class PathNormalizer {

	/**
	 * Normalizes a file object. The returned file object will return the
	 * normalize path when File.getAbsolutePath() is invoked.
	 * 
	 * @param path
	 *            the File object to be normalized
	 * @return a File object whose absolute path name is normalized
	 */
	public static File normalize(File path) {
		return new File(normalizePath(path.getAbsolutePath()));
	}

	/**
	 * Normalizes a string path. The returned path does not contain ".." or "."
	 * references
	 * 
	 * @param path
	 *            the path to be normalized.
	 * @return a String object containing the normalized path
	 */
	public static String normalizePath(String path) {
		File f = new File(path);
		// UNC support on windows
		boolean UNCfilename=false;
		if (System.getProperty("os.name","unknown").toLowerCase().startsWith("windows")) {
		    if (f.getPath().startsWith("\\"))  {
		        UNCfilename = true;
		    }
		}
		boolean trailingFSep = path.endsWith(File.separator);
		boolean startFSep = path.startsWith(File.separator);

		path = f.getAbsolutePath();
		StringTokenizer st = new StringTokenizer(path, File.separator);
		List names = new ArrayList();
		while (st.hasMoreTokens()) {
			String step = st.nextToken();
			if (".".equals(step))
				continue;
			else if ("..".equals(step))
				names.remove(names.size() - 1);
			else
				names.add(step);
		}

		StringBuffer sb = new StringBuffer();
		synchronized (sb) {
			// dl: 6/20
		    if (UNCfilename) sb.append(File.separator);
			if (startFSep) {
				sb.append(File.separator);
			}
			for (Iterator i = names.iterator(); i.hasNext();) {
				sb.append(i.next());
				if (i.hasNext())
					sb.append(File.separator);
			}
			if (trailingFSep)
				sb.append(File.separator);
			return sb.toString();
		}
	}

}