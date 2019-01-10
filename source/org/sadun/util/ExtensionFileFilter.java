package org.sadun.util;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.StringTokenizer;

/**
 * A simple FileFilter based on file extensions.
 *
 * @author Cris Sadun
 * @version 1.0
 */
public class ExtensionFileFilter implements FileFilter {

	private String [] ext;
    private String matchingExt;
    private boolean doAccept=true;

    /**
     * Create a filter which will accept the given extension
     * (denoted by a comma-separated string)
     * @param extensions a comma-separated string with the extensions
     */
    public ExtensionFileFilter(String extensions) {
        this(extensions,true);
    }

    /**
     * Create a filter which will accept or refuse the given extension
     * (denoted by a comma-separated string)
     * @param extensions a comma-separated string with the extensions
     * @param doAccept if <b>true</b>, the filter will accept the files
     *                 with the given extensions, refuse otherwise.
     */
    public ExtensionFileFilter(String extensions, boolean doAccept) {
        this(mkExtArray(extensions));
    }

    /**
     * Create a filter which will filter the given extension
     * (denoted by a string array)
     * @param extensions a string array with the extensions
     */
    public ExtensionFileFilter(String []ext) {
        this(ext, true);
    }


    /**
     * Create a filter which will filter the given extension
     * (denoted by a string array)
     * @param extensions a string array with the extensions
     * @param doAccept if <b>true</b>, the filter will accept the files
     *                 with the given extensions, refuse otherwise.
     */
    public ExtensionFileFilter(String []ext, boolean doAccept) {
        this.ext=ext;
        this.doAccept=doAccept;
    }

    private static String [] mkExtArray(String extensions) {
        StringTokenizer st = new StringTokenizer(extensions,";,:");
        List l = new ArrayList();
        while(st.hasMoreTokens()) {
            l.add(st.nextToken());
        }
        String res [] = new String[l.size()];
        l.toArray(res);
        return res;
    }

    /**
     * Accept or refuse a file basing on its extension and the
     * current working mode (see {@link ExtensionFileFilter#getAcceptsExtensions()
     * getAcceptsExtensions()}).
     */
    public boolean accept(File path) {
        for(int i=0;i<ext.length;i++) {
            if (path.getName().endsWith(ext[i])) {
                matchingExt=ext[i];
                return doAccept;
            }
        }
        matchingExt=null;
        return ! doAccept;
    }

    /**
     * Invert the filter - after this call, files that were accepted
     * will be refused, and vice versa (see {@link
     * ExtensionFileFilter#getAcceptsExtensions() getAcceptsExtensions()}).
     */
    public void invert() { doAccept=!doAccept; }

    /**
     * Return <b>true</b> if the filter is currently set to accept
     * files whose extension matches the extensions provided at construction.
     * @return <b>true</b> if the filter is currently set to accept
     * files whose extension matches the extensions provided at construction.
     */
    public boolean getAcceptsExtensions() { return doAccept; }


    /**
     * Returns the extension that has been last matched in a call
     * of {@link ExtensionFileFilter#accept(java.io.File)
     * accept()}, or <b>null</b> if accept has never been invoked or
     * no extension has been matched.
     * @return the last matched exception
     */
    public String lastMatchingExtension() { return matchingExt; }
}
