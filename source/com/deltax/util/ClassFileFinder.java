/************************************************************************************
* Copyright (C) 1999 Cristiano Sadun crsadun@tin.it
*
* This program is free software; you can redistribute it and/or
* modify it under the terms of the GNU General Public License
* as published by the Free Software Foundation; either version 2
* of the License, or any later version.
*
* This program is distributed in the hope that it will be useful,
* but WITHOUT ANY WARRANTY; without even the implied warranty of
* MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
* GNU General Public License for more details.
*
* You should have received a copy of the GNU General Public License
* along with this program; if not, write to the Free Software
* Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
************************************************************************************/

package com.deltax.util;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

/**
 * This interface defines services for publicly load class bytecode.
 * The {@link JDK12ClassFileFinder JDK12ClassFileFinder} class implements this interface emulating the
 * Java 2 system class loader behaviour.
 *
 * @version 1.0
 * @author Cristiano Sadun
 */
public interface ClassFileFinder {

   /**
    * Returns the supported loading scheme
    * @return a description string
    */
   public String getSupportedLoadingScheme();

  /**
   * Open class data. The input stream reads exactly and only the
   * class byte data.
   *
   * @param className the name of the class to find
   * @exception ClassNotFoundException if the class is not found
   * @exception IOException if an I/O Exception occurs
   */
   public InputStream openClass(String className) throws IOException, ClassNotFoundException;
   
   /**
    * Returns a byte array with the bytecode for the class
    *     * @param className the name of the class to find    * @return byte[] the bytecode for the class    * @throws IOException if a problem arises while loading the bytecode    * @throws ClassNotFoundException if the class definition cannot be found    */
   public byte [] getClassBytes(String className) throws IOException, ClassNotFoundException;

   /**
	 * This method finds the class file - in a way depending on the particular
	 * implementation
	 * @param className the name of the class to find
	 * @return the File object for the class file; this can be a .class
	           file, or a JAR file containing the class or other
	 * @exception ClassNotFoundException if the class is not found
	 * @exception IOException if an I/O Exception occurs
	 */
   public File findClassFile(String className) throws IOException, ClassNotFoundException;
}