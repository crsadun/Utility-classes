/*
 * Created on Feb 27, 2004
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package org.sadun.util;

import java.io.IOException;
import java.io.Reader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.deltax.util.FIFOQueue;
import com.deltax.util.QueueEmptyException;

/**
 * A class to efficiently parse and find anchors in an HTML stream.
 * 
 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">Cristiano
 *         Sadun</a>
 * @version 1.0
 */
public class HTMLAnchorIterator implements Iterator {

	private Reader reader;
	private boolean eof;
	private FIFOQueue anchors = new FIFOQueue();

	/**
	 * Create an anchor iterator over the given reader.
	 */
	public HTMLAnchorIterator(Reader r) {
		this.reader = r;
		this.eof = false;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#hasNext()
	 */
	public boolean hasNext() {
		if (anchors.size() == 0) {
			try {
				findNextAnchors();
			} catch (IOException e) {
				e.printStackTrace();
				return false;
			}
			if (anchors.size() == 0)
				return false;
		}
		return true;
	}

	private static final int LOOKING_FOR_MANDATORY_SPACES = 0;
	private static final int LOOKING_FOR_SPACES = 1;
	private static final int LOOKING_FOR_HREF = 2;
	private static final int LOOKING_FOR_A = 3;
	private static final int LOOKING_FOR_EQUAL = 4;
	private static final int LOOKING_FOR_OPENING_QUOTE = 5;
	private static final int IN_HREF = 6;
	private static final int URL_FOUND = 7;
	private static final int CHECK_FOR_EOF = 8;

	private String stateDescr[] =
		{
			"LOOKING_FOR_MANDATORY_SPACES",
			"LOOKING_FOR_SPACES",
			"LOOKING_FOR_HREF",
			"LOOKING_FOR_A",
			"LOOKING_FOR_EQUAL",
			"LOOKING_FOR_OPENING_QUOTE",
			"IN_HREF",
			"URL_FOUND",
			"CHECK_FOR_EOF" };

	private synchronized void findNextAnchors() throws IOException {
		if (!reader.ready())
			return;
		PushbackReader rd = new PushbackReader(reader);
		int state = LOOKING_FOR_A, onFound = -1;
		int c;
		char openingQuote = 0;
		eof = false;
		StringBuffer sb = new StringBuffer();
		do {
			//System.out.println("State: "+stateDescr[state]);
			switch (state) {
				case CHECK_FOR_EOF :
					c = rd.read();
					if (c == -1)
						eof = true;
					else
						state = onFound;
					break;
				case LOOKING_FOR_A :
					if (rd.nextEqualsIgnoreCase("<a", false)) {
						state = LOOKING_FOR_MANDATORY_SPACES;
						onFound = LOOKING_FOR_HREF;
					} else {
						state = CHECK_FOR_EOF;
						onFound = LOOKING_FOR_A;
					}
					break;
				case LOOKING_FOR_MANDATORY_SPACES :
					if (jumpSpaces(rd, 1))
						state = onFound;
					else {
						state = CHECK_FOR_EOF;
						onFound = LOOKING_FOR_A;
					} // reset
					break;
				case LOOKING_FOR_SPACES :
					if (jumpSpaces(rd, 0))
						state = onFound;
					else {
						state = CHECK_FOR_EOF;
						onFound = LOOKING_FOR_A;
					} // reset
					break;
				case LOOKING_FOR_HREF :
					if (rd.nextEqualsIgnoreCase("href", false)) {
						state = LOOKING_FOR_SPACES;
						onFound = LOOKING_FOR_EQUAL;
					} else {
						state = CHECK_FOR_EOF;
						onFound = LOOKING_FOR_A;
					}
					break;
				case LOOKING_FOR_EQUAL :
					c = rd.read();
					if (c == -1) {
						eof = true;
						break;
					}
					if (c == '=') {
						state = LOOKING_FOR_SPACES;
						onFound = LOOKING_FOR_OPENING_QUOTE;
					} else {
						state = CHECK_FOR_EOF;
						onFound = LOOKING_FOR_A;
					}
					break;
				case LOOKING_FOR_OPENING_QUOTE :
					c = rd.read();
					if (c == -1) {
						eof = true;
						break;
					}
					if (c == '\"' || c == '\'') {
						state = IN_HREF;
						openingQuote = (char) c;
					} else {
						state = CHECK_FOR_EOF;
						onFound = LOOKING_FOR_A;
					}
					break;
				case IN_HREF :
					c = rd.read();
					if (c == -1) {
						eof = true;
						break;
					}

					if (c == openingQuote) {
						state = URL_FOUND;
					} else
						sb.append((char) c);

			}
		} while (!eof && state != URL_FOUND);
		anchors.put(sb.toString());
	}

	private boolean jumpSpaces(PushbackReader rd, int minRequiredSpaces)
		throws IOException {
		int spaces = 0;
		boolean cont;
		int c;
		do {
			c = rd.read();
			cont = Character.isWhitespace((char) c);
			if (cont)
				spaces++;
			else
				rd.unread(c);
		} while (cont);
		return spaces >= minRequiredSpaces;
	}

	public Object next() {
		try {
			return anchors.get();
		} catch (QueueEmptyException e) {
			if (eof)
				throw new NoSuchElementException();
			try {
				findNextAnchors();
			} catch (IOException e1) {
				throw new RuntimeException(e1);
			}
			return next();
		}

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.util.Iterator#remove()
	 */
	public void remove() {
		throw new UnsupportedOperationException();
	}

	/**
	 * A classt to hold the results of a HTMLAnchorIterator
	 * {@link HTMLAnchorIterator#getURLs()}operation, containing both string
	 * URLs which can be successfully translated to URL objects and ones which
	 * can't.
	 * 
	 * @author <a href="mailto:cristianosadunTAKETHISAWAY@hotmail.com">
	 *         Cristiano Sadun</a>
	 * @version 1.0
	 */
	public class URLsCollection {

		private URL[] valid;
		private Map invalid = new HashMap();
		private URL context;

		private URLsCollection(URL context, HTMLAnchorIterator iterator)
			throws IOException {
			this.context = context;
			List l = new ArrayList();
			while (iterator.hasNext())
				l.add(iterator.next());
			List valid = new ArrayList();
			for (Iterator i = l.iterator(); i.hasNext();) {
				String urlString = (String) i.next();
				try {
					if (context != null)
						valid.add(new URL(context, urlString));
					else
						valid.add(new URL(urlString));
				} catch (MalformedURLException e) {
					invalid.put(urlString, e);
				}
			}
			this.valid = new URL[valid.size()];
			valid.toArray(this.valid);
		}

		public int getSize() {
			return getValidCount() + getInvalidCount();
		}
		public int getValidCount() {
			return valid.length;
		}
		public int getInvalidCount() {
			return invalid.keySet().size();
		}
		public URL getContext() {
			return context;
		}
		public URL[] getValidURLs() {
			URL[] urls = new URL[valid.length];
			System.arraycopy(valid, 0, urls, 0, valid.length);
			return urls;
		}
		public String[] getInvalidURLs() {
			String[] urls = new String[invalid.keySet().size()];
			invalid.keySet().toArray(urls);
			return urls;
		}
		public MalformedURLException getMalformedURLException(String url) {
			MalformedURLException e = (MalformedURLException) invalid.get(url);
			if (e == null)
				throw new IllegalArgumentException(
					"'" + url + "' is an unknown URL");
			return e;
		}

	}

	private URLsCollection urlsCollection = null;

	public synchronized URLsCollection getURLs(URL context)
		throws IOException {
		if (urlsCollection == null)
			urlsCollection = new URLsCollection(context, this);
		if (context != urlsCollection.context)
			throw new IllegalArgumentException(
				"Invalid context: "
					+ context
					+ ". An URLsCollection has already been requested with a different context ("
					+ urlsCollection.context
					+ ")");
		return urlsCollection;
	}

}
