package org.sadun.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.util.Date;
import java.util.Locale;

/**
 * A simple implementation of {@link org.sadun.util.Version}.
 * 
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano Sadun</a>
 * @version 1.0
 */
public class VersionImpl implements Version {
	
	protected int major;
	protected int minor;
	protected int micro;
	protected String quality;
	protected long buildNumber;
	protected long buildTimeStamp;
	
	public VersionImpl(int major, int minor, int micro, String quality, long buildNumber, long buildTimeStamp) {
		this.major=major;
		this.minor=minor;
		this.micro=micro;
		this.quality=quality;
		this.buildNumber=buildNumber;
		this.buildTimeStamp=buildTimeStamp;
	}
	
	public VersionImpl(int major, int minor, int micro, long buildNumber, long buildTimeStamp) {
		this(major, minor, micro, null, buildNumber, buildTimeStamp);
	}
	
	public VersionImpl(int major, int minor, int micro, long buildNumber) {
		this(major, minor, micro, null, buildNumber, NOT_VALUED);
	}
	
	public VersionImpl(int major, int minor, long buildNumber) {
		this(major, minor, NOT_VALUED, null, buildNumber);
	}
	
	public VersionImpl(int major, int minor, int micro, String quality, long buildNumber) {
		this(major, minor, micro, quality, buildNumber, NOT_VALUED);
	}
	
	public VersionImpl(int major, int minor, int micro, String quality) {
		this(major, minor, micro, quality, NOT_VALUED);
	}
	
	public VersionImpl(int major, int minor, int micro) {
		this(major, minor, micro, null);
	}
	
	public VersionImpl(int major, int minor, int micro, String quality, long buildNumber, String buildTimeStamp, Locale locale) throws ParseException {
		this(major, minor, micro, quality, buildNumber, DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT, locale).parse(buildTimeStamp).getTime());
	}
	
	public VersionImpl(int major, int minor, int micro, String quality, long buildNumber, String buildTimeStamp) throws ParseException {
		this(major, minor, micro, quality, buildNumber, buildTimeStamp, Locale.getDefault());
	}

	public int getMajorNumber() {
		return major;
	}

	public int getMinorNumber() {
		return minor;
	}

	public int getMicroNumber() {
		return micro;
	}

	public String getQualityState() {
		return quality;
	}

	public long getBuildNumber() {
		return buildNumber;
	}

	public long getBuildTimestamp() {
		return buildTimeStamp;
	}

	public String getShortDescription() {
		String vn=getVersionNumber();
		if (vn.length()==0) return getFullDescription();
		else return vn;
	}
	
	// Return the version, of "" if no major number is present
	private String getVersionNumber() {
		StringBuffer sb = new StringBuffer();
		if (major!=NOT_VALUED) {
			sb.append(major);
			if (minor!=NOT_VALUED) {
				sb.append(".");
				sb.append(minor);
				if (micro!=NOT_VALUED) {
					sb.append(".");
					sb.append(micro);
				}
			}
		}
		return sb.toString();
	}

	/* (non-Javadoc)
	 * @see org.sadun.util.Version#getFullDescription()
	 */
	public String getFullDescription() {
		StringBuffer sb = new StringBuffer();
		String vn=getVersionNumber();
		sb.append(vn);
		if (quality!=null) sb.append(quality);
		if (buildNumber!=NOT_VALUED || buildTimeStamp !=NOT_VALUED) {
			if (sb.length()>0) sb.append(" ");
			
			if (buildNumber!=NOT_VALUED) { sb.append("build "); sb.append(buildNumber); } 
			if (buildTimeStamp!=NOT_VALUED) {
				if (buildNumber!=NOT_VALUED)
					sb.append(" ");
				sb.append(DateFormat.getDateTimeInstance(DateFormat.SHORT, DateFormat.SHORT).format(new Date(buildTimeStamp)));
			}
		}
		return sb.toString();
	}
	
	/**
	 * Return true if the object contains information enough to be externally meaningful.
	 * @return true if the object contains information enough to be externally  meaningful.
	 */
	public boolean hasInfoEnough() {
		if (major!=Version.NOT_VALUED) return true;
		if (quality!=null) return true;
		if (buildNumber!=Version.NOT_VALUED) return true;
		if (buildTimeStamp!=Version.NOT_VALUED) return true;
		return false;
	}


	public long getBuildTimeStamp() {
		return buildTimeStamp;
	}
	public void setBuildTimeStamp(long buildTimeStamp) {
		this.buildTimeStamp = buildTimeStamp;
	}
	public int getMajor() {
		return major;
	}
	public void setMajor(int major) {
		this.major = major;
	}
	public int getMicro() {
		return micro;
	}
	public void setMicro(int micro) {
		this.micro = micro;
	}
	public int getMinor() {
		return minor;
	}
	public void setMinor(int minor) {
		this.minor = minor;
	}
	public String getQuality() {
		return quality;
	}
	public void setQuality(String quality) {
		this.quality = quality;
	}
	public void setBuildNumber(long buildNumber) {
		this.buildNumber = buildNumber;
	}
}
