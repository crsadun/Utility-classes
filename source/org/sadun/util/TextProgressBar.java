package org.sadun.util;

/**
 * <font color="red">NOT FUNCTIONAL YET</font>
 * To change this generated comment edit the template variable "typecomment":
 * Window>Preferences>Java>Templates.
 * To enable and disable the creation of type comments go to
 * Window>Preferences>Java>Code Generation.
 * 
 * @author Cristiano Sadun
 */
public class TextProgressBar {

	public static final String ANSI = "ansi";

	private String terminalType;
	private int total;
	private int incPercentage=10;
	private int progress;
	private int curPos;
	private int maxPos=20;
	
	public TextProgressBar(String terminalType) {
		this(terminalType, 0);
	}

	public TextProgressBar(String terminalType, int total) {
		if (ANSI.equals(terminalType)) {
			this.terminalType = terminalType;
		} else
			throw new UnsupportedOperationException(
				"Terminal type " + terminalType + " not supported");
		this.total=total;
	}
	
	public synchronized void inc(int n) {
		// If there's a total, add a point if progress+total is a proper percentage
		// of total; else, just add n points
		if (total==0) {
			printPoints(n);
		} else {
			int nPoints = ((progress+n)/total) % incPercentage;
			printPoints(nPoints);
		}
		progress+=n;
	}
	
	public void inc() {
		inc(1);
	}
	
	private void printPoints(int nPoints) {
		for(int i=0;i<nPoints;i++) {
			if (curPos>=maxPos) {
				cursorBack(curPos);
				curPos=0;
			}
			curPos++;
			System.out.print(".");
		}
	}
	
	private void cursorBack(int n) {
		if (ANSI.equals(terminalType)) 
			escape(String.valueOf(n)+"D");
	}
	
	private void escape(char [] s) {
		char [] s2 = new char[s.length+2];
		System.arraycopy(s, 0, s2, 2, s.length);
		s2[0]=(char)27; // ESC
		s2[1]='['; // bracket
		System.out.print(s2);
	}
	
	private void escape(String s) {
		escape(s.toCharArray());
	}
	
	public static void main(String[] args) throws InterruptedException {
		TextProgressBar tpb=new  TextProgressBar(TextProgressBar.ANSI);
		for(int i=0;i<50;i++) {
			tpb.inc();
			Thread.sleep(100);
		}
	}
	/**
	 * Returns the total.
	 * @return int
	 */
	public int getTotal() {
		return total;
	}

	/**
	 * Sets the total.
	 * @param total The total to set
	 */
	public void setTotal(int total) {
		this.total = total;
	}

	/**
	 * Returns the progress.
	 * @return int
	 */
	public int getProgress() {
		return progress;
	}

	/**
	 * Sets the progress.
	 * @param progress The progress to set
	 */
	public void setProgress(int progress) {
		this.progress = progress;
	}

}
