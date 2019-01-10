/*
 * Created on Nov 25, 2003
 * 
 * To change the template for this generated file go to Window - Preferences -
 * Java - Code Generation - Code and Comments
 */
package org.sadun.util.watchdog.listeners;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.Properties;
import java.util.StringTokenizer;
import java.util.logging.Logger;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

/**
 * NOTE: this class relies on javamail. 
 * You need to download and install both the 
 * <a href="http://java.sun.com/products/javabeans/glasgow/jaf.html">JavaBeans Activation Framework</a>
 * and <a href="http://java.sun.com/products/javamail/">JavaMail</a> to compile and run it.
 * 
 * 
 * @author Cristiano Sadun
 * @version 1.1
 */
public class MailAlarmListener extends WaitAndRetryListener {

	private String emailAddresses;
	private String hostname;
	private URL mailTemplateURL;

	/**
	 *  
	 */
	public MailAlarmListener(
		String emailAddresses,
		String hostname,
		String mailTemplateURL)
		throws MalformedURLException {
		this(
			WaitAndRetryListener.DEFAULT_MAX_RETRY_COUNT,
			emailAddresses,
			hostname,
			mailTemplateURL);
	}
	
	public MailAlarmListener(
			String emailAddresses,
			String hostname)  {
	    this(WaitAndRetryListener.DEFAULT_MAX_RETRY_COUNT, emailAddresses,hostname);
	}

	/**
	 * @param emailAddresses
	 *            a comma-separated list of email addresses
	 * @param hostname
	 *            the name of the SMTP host
	 * @param maxRetryCount
	 */
	public MailAlarmListener(
		int maxRetryCount,
		String emailAddresses,
		String hostname,
		String mailTemplateURL)
		throws MalformedURLException {
		super(maxRetryCount);
		init(emailAddresses, hostname);
		if (mailTemplateURL!=null) 
		    this.mailTemplateURL = new URL(mailTemplateURL);
	}
	
	public MailAlarmListener(
			int maxRetryCount,
			String emailAddresses,
			String hostname) {
	    super(maxRetryCount);
		init(emailAddresses, hostname);
	}
	
	private void init(String emailAddresses,
			String hostname) {
		this.emailAddresses = emailAddresses;
		this.hostname = hostname;
		
	}

	public MailAlarmListener() {
		super();
	}

	private void checkState() throws IllegalStateException {
		if (emailAddresses == null)
			throw new IllegalStateException("No email addresses specified. Please set the EmailAddresses property");
		else if (hostname == null)
			throw new IllegalStateException("No SMTP host name specified. Please set the HostName property");
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.sadun.util.watchdog.WatchDog.Listener#checkFailed(java.lang.Object,
	 *      java.lang.Throwable)
	 */
	public void checkFailed(Object obj, Throwable e) {
		checkState();
		StringTokenizer st = new StringTokenizer(emailAddresses, ",;");
		while (st.hasMoreTokens()) {
			try {
				sendMail(st.nextToken(), obj, e);
			} catch (SendMailFailedException e1) {
				Logger.getAnonymousLogger().warning(e1.getMessage());
			}
		}
	}

	private Session mailSession;
	
	/**
	 * @param obj
	 * @param e
	 */
	private void sendMail(
		String toAddress,
		Object obj,
		Throwable e)
		throws SendMailFailedException {
			if (mailSession==null) {
				Properties p = new Properties();
				p.put("mail.smtp.host", hostname);
				mailSession = Session.getDefaultInstance(p, null);
			}
			String fromAddress = "nobody@locahost";

			Message msg = new MimeMessage(mailSession); 
			try {
				msg.setFrom(new InternetAddress(fromAddress));
				InternetAddress[] address = { new InternetAddress(toAddress) }; 
				msg.setRecipients(Message.RecipientType.TO, address); 
				msg.setSubject("Failure report"); 
				msg.setSentDate(new Date()); 
				msg.setText(createFailureMessage(e)); 
				Transport.send(msg);
			} catch (AddressException e1) {
				throw new SendMailFailedException(e1);
			} catch (MessagingException e1) {
				throw new SendMailFailedException(e1);
			} 
	}

	/**
	 * @return
	 */
	private String createFailureMessage(Throwable e) {
	    if (mailTemplateURL==null)
			return "Subject: Watchdog alarm\r\nFrom: watchdog@localhost\r\n\r\nAutomatic failure alarm\r\n\r\n"
				+ e.getMessage();
	    else {
	        BufferedReader br=null;
	        try {
                br = new BufferedReader(new InputStreamReader(mailTemplateURL.openStream()));
                StringWriter sw=new StringWriter();
                int c;
                while((c=br.read())!=-1) sw.write(c);
                return sw.toString();
            } catch (IOException e1) {
                return "Subject: Watchdog alarm\r\nFrom: watchdog@localhost\r\n\r\nAutomatic failure alarm\r\n\r\n"
				+ e.getMessage()+"\r\n\r\nPlease note: (Mail template failed to load for "+e1.getMessage()+").";
            } finally {
                if (br!=null)
                    try {
                        br.close();
                    } catch (IOException e2) {
                        e2.printStackTrace();
                    }
            }
	    }
	}

	/**
	 * @return Returns the mailTemplateURL.
	 */
	public String getMailTemplateURL() {
		return mailTemplateURL.toString();
	}

	/**
	 * @param mailTemplateURL
	 *            The mailTemplateURL to set.
	 */
	public void setMailTemplateURL(String mailTemplateURL)
		throws MalformedURLException {
		this.mailTemplateURL = new URL(mailTemplateURL);
	}

	/**
	 * @return Returns the emailAddresses.
	 */
	public String getEmailAddresses() {
		return emailAddresses;
	}

	/**
	 * @param emailAddresses
	 *            The emailAddresses to set.
	 */
	public void setEmailAddresses(String emailAddresses) {
		this.emailAddresses = emailAddresses;
	}

	/**
	 * @return Returns the hostname.
	 */
	public String getHostname() {
		return hostname;
	}

	/**
	 * @param hostname
	 *            The hostname to set.
	 */
	public void setHostname(String hostname) {
		this.hostname = hostname;
	}

}
