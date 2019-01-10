package org.sadun.util;

import java.io.File;
import java.io.FileFilter;

/**
 * A FileFilter to filter in or out directories.
 * 
 * @author Cristiano Sadun
 */
public class DirectoryFileFilter implements FileFilter {
	
	private boolean doAccept;

	/**
	 * Constructor for DirectoryFileFilter.
	 * 
	 * @param doAccept if <b>true</b> directories are accepted,
	 *         otherwise refused.
	 */
	public DirectoryFileFilter(boolean doAccept) {
		this.doAccept=doAccept;
	}
	
	/**
	 * Create a DirectoryFileFilter which accepts only directories.
	 * 
	 * @param doAccept if <b>true</b> directories are accepted,
	 *         otherwise refused.
	 */
	public DirectoryFileFilter() {
		this(true);
	}

	/**
	 * Accepts or refuses directores depending on the {@link 
	 * #DirectoryFileFilter(boolean) construction parameters}.
	 * 
	 * @see java.io.FileFilter#accept(File)
	 */
	public boolean accept(File pathname) {
		return doAccept == pathname.isDirectory();
	}

}
