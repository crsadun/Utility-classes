package org.sadun.util;

/**
 * A generic interface to a version object.
 * <p>
 * The notion of version supported includes a major, minor and micro number, a quality state 
 * (alpha, beta, etc), a build number and a build timestamp.
 * <p>
 * A typical string denotation has the format 
 * <tt>major.minor.micro[quality] build [build number, [build timestamp]]</tt>.
 * <p>
 * Specific implementation may be created manually, or usually generated at compiletime
 * and included in the versioned package.
 *
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano Sadun</a>
 * @version 1.0
 */
public interface Version {
	
	/**
	 * A constant that can be compared to any of the numeric attributes
	 * exposed by this class to check wether a valid value or not is assigned
	 * to the attribute.
	 * <p>
	 * For example, code like
	 * <tt>if (getMicroNumber()==Version.NOT_VALUED)) ...</tt>
	 * can be used as necessary to verify if an attribute is valued or not.
	 */
	public static final int NOT_VALUED = -1;
	
	/**
	 * Return the major version number, or {@link NOT_VALUED} if there is no such number.
	 * @return the major version number, or {@link NOT_VALUED} if there is no such number.
	 */
	public int getMajorNumber();
	
	/**
	 * Return the minor version number, or {@link NOT_VALUED} if there is no such number.
	 * @return the minor  version number, or {@link NOT_VALUED} if there is no such number.
	 */
	public int getMinorNumber();

	/**
	 * Return the micro version number, or {@link NOT_VALUED} if there is no such number.
	 * @return the micro version number, or {@link NOT_VALUED} if there is no such number.
	 */	
	public int getMicroNumber();
	
	/**
	 * Return the quality state string, or <tt>null</tt> if none is set.
	 * @return the quality state string, or <tt>null</tt> if none is set.
	 */	
	public String getQualityState();
	
	/**
	 * Return the build number, or {@link NOT_VALUED} if there is no such number.
	 * @return the build number, or {@link NOT_VALUED} if there is no such number.
	 */	
	public long getBuildNumber();
	
	/**
	 * Return the build timestamp, or {@link NOT_VALUED} if there is no such timestamp.
	 * @return the build timestamp, or {@link NOT_VALUED} if there is no such timestamp.
	 */		
	public long getBuildTimestamp();
	
	/**
	 * Return a short description of the version. This is typically limited to major.micro
	 * number.
	 * @return a short description of the version. This is typically limited to major.micro
	 * number.
	 */
	public String getShortDescription();
	
	/**
	 * Return a full description of the version.
	 * @return a full description of the version.
	 */
	public String getFullDescription();
	

}
