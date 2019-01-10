package com.deltax.util;

import java.io.Serializable;
import java.util.StringTokenizer;

/**
 * A class implementing a time interval, which parses back and forth
 * human-readable specifications.
 * <p>
 * The minimum time unit is the minute: when converting from milliseconds, any
 * part of minute is discarded, so for example a TimeInterval of 65.000ms
 * equals a TimeInterval of 60.000ms.
 * <p>
 * The maximum time unit is the week.
 * <p>
 * The format for a string denoting a time interval is [<i><b>w</b></i>
 * week[s]][+|,][ <i><b>d</b></i> day[s]][+|,][ <i><b>h</b></i>
 * hour[s]][+|,][ <i><b>m</b></i> minutes[s]], where <i>w</i>,<i>d
 * </i>,<i>h</i>,<i>m</i> are integer values, and the following
 * abbreviations may be used:
 * <p>
 * 
 * <pre>
 * <table border width=50% align=center>
 * <tr><td align="center"><b>Normal</b></td><td align="center"><b>Compact</b></td><td align="center"><b>Single char</b></td><td align="center"><b>Fixed length</b></td></tr>
 * <tr><td align="center">week</td><td align="center">wk</td><td align="center">w</td><td align="center">ww</td></tr>
 * <tr><td align="center">day</td><td align="center">-</td><td align="center">d</td><td align="center">dd</td></tr>
 * <tr><td align="center">hour</td><td align="center">hr</td><td align="center">h</td><td align="center">hh</td></tr>
 * <tr><td align="center">minute</td><td align="center">min</td><td align="center">m</td><td align="center">mm</td></tr>
 * </table>
 * </pre>
 * 
 * <p>
 * For example, <tt>1week+2days+3hrs+30min</tt> or <tt>3hrs,30min</tt> are
 * valid descriptors.
 * 
 * @author C. Sadun
 * @version 1.4
 */
public class TimeInterval implements Serializable {

	private static final long _MILLISECOND = 1L;
	private static final long _SECOND = 1000L;
	private static final long _MINUTE = 60000L;
	private static final long _HOUR = 0x36ee80L;
	private static final long _DAY = 0x5265c00L;
	private static final long _WEEK = 0x240c8400L;

	private static final long values[] =
		{ _MILLISECOND, _SECOND, _MINUTE, _HOUR, _DAY, _WEEK };

	private static final int MILLISECOND_INDEX = 0;
	private static final int SECOND_INDEX = 1;
	private static final int MINUTE_INDEX = 2;
	private static final int HOUR_INDEX = 3;
	private static final int DAY_INDEX = 4;
	private static final int WEEK_INDEX = 5;

	private static final String desc[][] = { { "millisecond", "msec", "ms" }, {
			"second", "sec", "ss" }, {
			"minute", "min", "m", "mm" }, {
			"hour", "hr", "h", "hh" }, {
			"day", "d", "dd" }, {
			"week", "wk", "ww" }
	};

	/**
	 * One millisecond
	 */
	public static final long MILLISECOND = values[MILLISECOND_INDEX];

	/**
	 * One second
	 */
	public static final long SECOND = values[SECOND_INDEX];

	/**
	 * One minute, in milliseconds
	 */
	public static final long MINUTE = values[MINUTE_INDEX];

	/**
	 * One hour, in milliseconds
	 */
	public static final long HOUR = values[HOUR_INDEX];

	/**
	 * One day, in milliseconds
	 */
	public static final long DAY = values[DAY_INDEX];

	/**
	 * One week, in milliseconds
	 */
	public static final long WEEK = values[WEEK_INDEX];

	/**
	 * Descriptions and abbreviations for millisecond
	 */
	public static final String MILLISECOND_STR[] = desc[MILLISECOND_INDEX];

	/**
	 * Descriptions and abbreviations for second
	 */
	public static final String SECOND_STR[] = desc[SECOND_INDEX];

	/**
	 * Descriptions and abbreviations for minute
	 */
	public static final String MINUTE_STR[] = desc[MINUTE_INDEX];

	/**
	 * Descriptions and abbreviations for hour
	 */
	public static final String HOUR_STR[] = desc[HOUR_INDEX];

	/**
	 * Descriptions and abbreviations for day
	 */
	public static final String DAY_STR[] = desc[DAY_INDEX];

	/**
	 * Descriptions and abbreviations for week
	 */
	public static final String WEEK_STR[] = desc[WEEK_INDEX];

	/**
	 * Milliseconds portion of the time interval
	 */
	public long milliseconds;

	/**
	 * Seconds portion of the time interval
	 */
	public long seconds;

	/**
	 * Week portion of the time interval
	 */
	public long weeks;

	/**
	 * Days portion of the time interval
	 */
	public long days;

	/**
	 * Hours portion of the time interval
	 */
	public long hours;

	/**
	 * Minute portion of the time interval
	 */
	public long minutes;

	/**
	 * Create a time interval of zero length
	 */
	public TimeInterval() {
		this(0L, 0L, 0L, 0L, 0L, 0L);
	}

	/**
	 * Create a time interval of the given length, in milliseconds
	 * 
	 * @param l
	 *            the length of the interval, in milliseconds
	 */
	public TimeInterval(long l) {
		//if (l < MINUTE) throw new IllegalArgumentException("The minimum
		// granularity for a TimeInterval is one minute");
		weeks = l / WEEK;
		days = (l % WEEK) / DAY;
		hours = (l % WEEK % DAY) / HOUR;
		minutes = (l % WEEK % DAY % HOUR) / MINUTE;
		seconds = (l % WEEK % DAY % HOUR % MINUTE) / SECOND;
		milliseconds = (l % WEEK % DAY % HOUR % MINUTE % SECOND) / MILLISECOND;
	}
	
	private static long [] splitTime(long l) {
	    long [] values = new long[6];
	    values[0] = l / WEEK;
	    values[1] = (l % WEEK) / DAY;
	    values[2] = (l % WEEK % DAY) / HOUR;
	    values[3] = (l % WEEK % DAY % HOUR) / MINUTE;
	    values[4] = (l % WEEK % DAY % HOUR % MINUTE) / SECOND;
	    values[5] = (l % WEEK % DAY % HOUR % MINUTE % SECOND) / MILLISECOND;
	    return values;
	}

	/**
	 * Create a time interval, with the specified week/day/hour/minute values
	 * 
	 * @param weeks
	 *            the week portion of the time interval
	 * @param days
	 *            the day portion of the time interval
	 * @param hours
	 *            the hour portion of the time interval
	 * @param minutes
	 *            the minute portion of the time interval
	 */
	public TimeInterval(
		long weeks,
		long days,
		long hours,
		long minutes,
		long seconds,
		long milliseconds) {
		this.weeks = weeks;
		this.days = days;
		this.hours = hours;
		this.minutes = minutes;
		this.seconds = seconds;
		this.milliseconds = milliseconds;
	}

	/**
	 * Creates a TimeInterval parsing a formatted specification. See class
	 * comment for information on the format.
	 * 
	 * @param s
	 *            the formatted string denoting the time interval
	 */
	public TimeInterval(String s) throws TimeIntervalFormatException {
		TimeInterval timeinterval = parse(s);
		weeks = timeinterval.weeks;
		days = timeinterval.days;
		hours = timeinterval.hours;
		minutes = timeinterval.minutes;
		seconds = timeinterval.seconds;
		milliseconds = timeinterval.milliseconds;
	}

	private static long addElementValue(TimeInterval timeinterval, String s)
		throws TimeIntervalFormatException {
		s.toLowerCase();
		for (int i = 0; i < desc.length; i++) {
			for (int j = 0; j < desc[i].length; j++) {
				if (s.endsWith(desc[i][j]) || s.endsWith(desc[i][j] + "s")) {
					s = s.substring(0, s.lastIndexOf(desc[i][j]));
					try {
						long l = Long.parseLong(s);
						switch (i) {
							case MILLISECOND_INDEX :
								timeinterval.milliseconds += l;
								break;

							case SECOND_INDEX :
								timeinterval.seconds += l;
								break;

							case MINUTE_INDEX :
								timeinterval.minutes += l;
								break;

							case HOUR_INDEX :
								timeinterval.hours += l;
								break;

							case DAY_INDEX :
								timeinterval.days += l;
								break;

							case WEEK_INDEX :
								timeinterval.weeks += l;
								break;
						}
						return values[i] * l;
					} catch (NumberFormatException _ex) {
						throw new TimeIntervalFormatException();
					}
				}
			}

		}

		throw new TimeIntervalFormatException(s);
	}

	/**
	 * Return the length of this timeinterval, in milliseconds
	 * 
	 * @return the length of this timeinterval, in milliseconds
	 */
	public long getTime() {
		return WEEK * weeks
			+ DAY * days
			+ HOUR * hours
			+ MINUTE * minutes
			+ SECOND * seconds
			+ MILLISECOND * milliseconds;
	}

	/**
	 * Create a formatted description of the timeinterval
	 * 
	 * @return a formatted description of the timeinterval
	 */
	private final String makeDescr() {
	    return makeDescr( weeks,
	    		 days,
	    		 hours,
	    		 minutes,
	    		 seconds,
	    		 milliseconds);
	}
	
	private static String makeDescr(long []values) {
	    return makeDescr(values[0],values[1],values[2],values[3],values[4],values[5]);
	}
	
	private static String makeDescr(long weeks,
			long days,
			long hours,
			long minutes,
			long seconds,
			long milliseconds) {    
		StringBuffer stringbuffer = new StringBuffer();
		synchronized (stringbuffer) {
			if (weeks != 0L) {
				stringbuffer.append(weeks);
				stringbuffer.append(" week");
				if (weeks > 1L)
					stringbuffer.append("s");
				if (days != 0L || hours != 0L || minutes != 0L)
					stringbuffer.append(",");
			}

			if (days != 0L) {
				if (weeks != 0L)
					stringbuffer.append(" ");
				stringbuffer.append(days);
				stringbuffer.append(" day");
				if (days > 1L)
					stringbuffer.append("s");
				if (hours != 0L || minutes != 0L)
					stringbuffer.append(",");
			}

			if (hours != 0L) {
				if (days != 0L)
					stringbuffer.append(" ");
				stringbuffer.append(hours);
				stringbuffer.append(" hour");
				if (hours > 1L)
					stringbuffer.append("s");
				if (minutes != 0L)
					stringbuffer.append(",");
			}

			if (minutes != 0L) {
				if (hours != 0L)
					stringbuffer.append(" ");
				stringbuffer.append(minutes);
				stringbuffer.append(" minute");
				if (minutes > 1L)
					stringbuffer.append("s");
			}

			if (seconds != 0L) {
				if (minutes != 0L)
					stringbuffer.append(" ");
				stringbuffer.append(seconds);
				stringbuffer.append(" second");
				if (seconds > 1L)
					stringbuffer.append("s");
			}

			if (milliseconds != 0L) {
				if (seconds != 0L)
					stringbuffer.append(" ");
				stringbuffer.append(milliseconds);
				stringbuffer.append(" millisecond");
				if (milliseconds > 1L)
					stringbuffer.append("s");
			}

			if (stringbuffer.length() == 0)
				return "0 milliseconds";

			return stringbuffer.toString();
		}
	}

	/**
	 * Parses a time interval formatted description and returns the
	 * corresponding TimeInterval object See class comment for information on
	 * the format.
	 * 
	 * @param s
	 *            the formatted string denoting the time interval
	 */
	public static TimeInterval parse(String s)
		throws TimeIntervalFormatException {
		StringTokenizer stringtokenizer = new StringTokenizer(s, " +,");
		TimeInterval timeinterval = new TimeInterval();
		String s1 = "";
		while (stringtokenizer.hasMoreElements()) {
			s1 = s1 + stringtokenizer.nextToken();
			try {
				Double.parseDouble(s1); // Skip numbers
			} catch (NumberFormatException e) {
				addElementValue(timeinterval, s1);
				s1 = "";
			}
		}
		return timeinterval;
	}

	/**
	 * Return the canonic description of the given interval (in milliseconds).
	 * 
	 * @param interval
	 *            the interval to describe, in milliseconds
	 * @return the canonic description of the interval.
	 */
	public static String describe(long interval) {
	    return TimeInterval.makeDescr(splitTime(interval));
		//return new TimeInterval(interval).toString();
	}

	/**
	 * Return <b>true</b> if the given time interval is longer than <i>this
	 * </i>
	 * 
	 * @return the length of this timeinterval, in milliseconds
	 */
	public boolean longer(TimeInterval timeinterval) {
		return getTime() > timeinterval.getTime();
	}

	/**
	 * Return <b>true</b> if the given time interval is shorter than <i>this
	 * </i>
	 * 
	 * @return the length of this timeinterval, in milliseconds
	 */
	public boolean shorter(TimeInterval timeinterval) {
		return getTime() < timeinterval.getTime();
	}

	/**
	 * Create a formatted description of the timeinterval
	 * 
	 * @return a formatted description of the timeinterval
	 */
	public String toString() {
		return makeDescr();
	}

	/**
	 * Return <b>true</b> only for a TimeInterval encapsulating the same
	 * amount of time, <b>false</b> otherwise.
	 * 
	 * @return <b>true</b> only for a TimeInterval encapsulating the same
	 *         amount of time, <b>false</b> otherwise
	 */
	public boolean equals(Object obj) {
		if (obj instanceof TimeInterval) {
			TimeInterval t = (TimeInterval) obj;
			return getTime() == t.getTime();
		} else
			return false;

	}

	/**
	 * Return the hash code for this time interval
	 * 
	 * @return the hash code for this time interval
	 */
	public int hashCode() {
		return String.valueOf(getTime()).hashCode();
	}

	/**
	 * Attempts to parse the first argument and prints out its value in
	 * milliseconds.
	 */
	public static void main(String args[]) throws Exception {
		if (args.length == 0) {
			System.out.println(
				"Please provide a timeinterval descriptor or a milliseconds value");
		} else {
			if (args[0].endsWith("ms"))
				args[0] = args[0].substring(0, args[0].length() - 2);
			try {
				System.out.println(
					args[0]
						+ "ms: \""
						+ new TimeInterval(Long.parseLong(args[0])).toString()
						+ "\"");
			} catch (NumberFormatException e) {
				System.out.println(
					TimeInterval.parse(args[0]).getTime() + "ms");
			}

		}
	}

}