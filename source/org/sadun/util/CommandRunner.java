package org.sadun.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintStream;
import java.lang.reflect.Method;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import com.deltax.util.JPanelPrintStream;
import com.deltax.util.TimeInterval;

/**
 * A class to run a command in a separate thread, monitor its progress via a
 * monitor window and being notified of its completion, exit status, and
 * elasped time.
 * 
 * @author cris
 * @version 1.0
 */
public class CommandRunner extends Thread {

	/**
	 * Classes implementing this interface may receive
	 * {@link CommandRunner CommandRunner}events.
	 */
	public static interface Listener {
		public void starting();
		public void failed(Exception e);
		public void started();
		public void terminated(int exitValue, long elapsed);
	}

	/**
	 * An helper class which implements all the methods in the
	 * {@link CommandRunner.Listener CommandRunner.Listener}interface in an
	 * empty way.
	 */
	public static class Adapter implements Listener {
		public void starting() {
		}
		public void failed(Exception e) {
		}
		public void started() {
		}
		public void terminated(int exitValue, long elapsed) {
		}
	}

	public static class StdOutAdapter implements Listener {
		public void starting() {
			System.out.println("Starting");
		}
		public void failed(Exception e) {
			e.printStackTrace();
		}
		public void started() {
			System.out.println("Started");
		}
		public void terminated(int exitValue, long elapsed) {
			System.out.println(
				"Terminated with exit value "
					+ exitValue
					+ " in "
					+ elapsed
					+ "ms");
		}
	}

	private String command;
	private Class cls;
	private String[] args;
	private Component parent;
	private boolean openWindowOnStartup = true;
	private boolean outputOnSystemOut = false;
	private boolean closeWindowOnTermination = false;
	private boolean captureOutput = false;
	private boolean outputElapsedTime = true;
	private String lastOutput;
	private JFrame jf;

	private Set listeners = new HashSet();

	/**
	 * Create a command runner with given parent window, running a system
	 * command with the given arguments. If used as a thread, the command
	 * runner has the given daemon status.
	 * 
	 * @param parent
	 *            the parent window for the CommandRunner monitor
	 * @param command
	 *            the executable to run
	 * @param args
	 *            the arguments to pass to the executable
	 * @param daemon
	 *            the daemon status of this {@link java.lang.Thread Thread}
	 */
	public CommandRunner(
		Component parent,
		String command,
		String[] args,
		boolean daemon) {
		setDaemon(daemon);
		this.parent = parent;
		this.command = command;
		this.args = unquote(args);
	}

	/**
	 * Create a command runner with given parent window, running a class'
	 * main() method with the given arguments. If used as a thread, the command
	 * runner has the given daemon status.
	 * 
	 * @param parent
	 *            the parent window for the CommandRunner monitor
	 * @param cls
	 *            the class whose main method is to be run
	 * @param args
	 *            the arguments to pass to the executable
	 * @param daemon
	 *            the daemon status of this {@link java.lang.Thread Thread}
	 */
	public CommandRunner(
		Component parent,
		Class cls,
		String[] args,
		boolean daemon) {
		setDaemon(daemon);
		this.parent = parent;
		this.cls = cls;
		this.args = unquote(args);
	}

	/**
	 * Create a command runner with no parent window, running a class' main()
	 * method with the given arguments. If used as a thread, the command runner
	 * has the given daemon status.
	 * 
	 * @param cls
	 *            the class whose main method is to be run
	 * @param args
	 *            the arguments to pass to the executable
	 * @param daemon
	 *            the daemon status of this {@link java.lang.Thread Thread}
	 */
	public CommandRunner(Class cls, String[] args, boolean daemon) {
		this(null, cls, args, daemon);
	}

	/**
	 * Create a command runner with the given parent window, running a class'
	 * main() method with the given arguments. If used as a thread, the command
	 * runner has daemon status (it terminates if it is the only one thread
	 * running).
	 * 
	 * @param parent
	 *            the parent window for the CommandRunner monitor
	 * @param cls
	 *            the class whose main method is to be run
	 * @param args
	 *            the arguments to pass to the executable
	 */
	public CommandRunner(Component parent, Class cls, String[] args) {
		this(parent, cls, args, true);
	}

	/**
	 * Create a command runner with no parent window, running a class' main()
	 * method with the given arguments. If used as a thread, the command runner
	 * has daemon status (it terminates if it is the only one thread running).
	 * 
	 * @param cls
	 *            the class whose main method is to be run
	 * @param args
	 *            the arguments to pass to the executable
	 */
	public CommandRunner(Class cls, String[] args) {
		this(cls, args, true);
	}

	/**
	 * Create a command runner with no parent window, running a system command
	 * with the given arguments. If used as a thread, the command runner has
	 * the given daemon status.
	 * 
	 * @param command
	 *            the executable to run
	 * @param args
	 *            the arguments to pass to the executable
	 * @param daemon
	 *            the daemon status of this {@link java.lang.Thread Thread}
	 */
	public CommandRunner(String command, String[] args, boolean daemon) {
		this(null, command, args, daemon);
	}

	/**
	 * Create a command runner with given parent window, running a system
	 * command with the given arguments. If used as a thread, the command
	 * runner has daemon status (it terminates if it is the only one thread
	 * running).
	 * 
	 * @param parent
	 *            the parent window for the CommandRunner monitor
	 * @param command
	 *            the executable to run
	 * @param args
	 *            the arguments to pass to the executable
	 */
	public CommandRunner(Component parent, String command, String[] args) {
		this(parent, command, args, true);
	}

	/**
	 * Create a command runner with no parent window, running a system command
	 * with the given arguments. If used as a thread, the command runner has
	 * daemon status (it terminates if it is the only one thread running).
	 * 
	 * @param parent
	 *            the parent window for the CommandRunner monitor
	 * @param command
	 *            the executable to run
	 * @param args
	 *            the arguments to pass to the executable
	 */
	public CommandRunner(String command, String[] args) {
		this(command, args, true);
	}

	/**
	 * Add a listener to the listener set. On {@link #run() run()}, each
	 * listener is notifyed of events concerning the run via its
	 * {@link CommandRunner.Listener CommandRunner.Listener}interface, in
	 * arbitrary order.
	 * 
	 * @param listener
	 *            the {@link CommandRunner.Listener CommandRunner.Listener}
	 *            object to add.
	 */
	public void addListener(CommandRunner.Listener listener) {
		listeners.add(listener);
	}

	/**
	 * Remove a listener to the listener set. If the given listener is not in
	 * the listeners set, this method does nothing.
	 * 
	 * @param listener
	 *            the {@link CommandRunner.Listener CommandRunner.Listener}
	 *            object to remove.
	 */
	public void removeListener(CommandRunner.Listener listener) {
		listeners.remove(listener);
	}

	private String[] unquote(String[] s) {
		for (int i = 0; i < s.length; i++)
			s[i] = unquote(s[i]);
		return s;
	}

	private String unquote(String s) {
		if (s == null)
			return null;
		if (s.length() < 2)
			return s;
		if ((s.charAt(0) == '\'' && s.charAt(s.length() - 1) == '\'')
			|| (s.charAt(0) == '\"' && s.charAt(s.length() - 1) == '\"'))
			return s.substring(1, s.length() - 1);
		return s;
	}

	private static final int STARTING = 0;
	private static final int FAILED = 1;
	private static final int STARTED = 2;
	private static final int TERMINATED = 3;

	private void notify(int eventCode) {
		notify(eventCode, null);
	}

	private void notify(int eventCode, Object arg) {
		for (Iterator i = listeners.iterator(); i.hasNext();) {
			Listener listener = (Listener) i.next();
			switch (eventCode) {
				case STARTING :
					listener.starting();
					break;
				case FAILED :
					listener.failed((Exception) arg);
					break;
				case STARTED :
					listener.started();
					break;
				case TERMINATED :
					Object[] v = (Object[]) arg;
					listener.terminated(
						((Integer) v[0]).intValue(),
						((Long) v[1]).longValue());
					break;
			}
		}
	}

	/**
	 * Run the command. Depending on the various flag, command output goes to a
	 * window or standard output, or both. Each listener registered via
	 * {@link #addListener(org.sadun.util.CommandRunner.Listener) addListener}
	 * is notifyed of events concerning the run via its
	 * {@link CommandRunner.Listener CommandRunner.Listener}interface, in
	 * arbitrary order.
	 */
	public synchronized void run() {
		// Open a window
		String s;
		if (command != null)
			s = command + " " + argsList(args);
		else
			s = cls + " " + argsList(args);
		closeWindow();
		JPanelPrintStream jpos = new JPanelPrintStream();
		JLabel label = new JLabel(" Running " + s);
		if (openWindowOnStartup) {
			jf = new JFrame("Running \"" + command + "\"");
			jf.getContentPane().setLayout(new BorderLayout());
			jf.getContentPane().add(label, BorderLayout.NORTH);
			jf.getContentPane().add(jpos.getPanel(), BorderLayout.CENTER);
			jf.pack();
			jf.setSize(600, 400);
			if (parent == null) {
				Dimension screenDim =
					Toolkit.getDefaultToolkit().getScreenSize();
				jf.setLocation(
					(screenDim.width - jf.getSize().width) / 2,
					(screenDim.height - jf.getSize().height) / 2);
			} else {
				jf.setLocation(
					parent.getLocation().x + 30,
					parent.getLocation().y + 30);
			}
			jf.setVisible(true);
		}

		if (command != null)
			executeCommand(s, jpos, label);
		else
			executeClass(s, jpos, label);

		if (closeWindowOnTermination)
			closeWindow();
	}

	private void executeClass(
		String classString,
		JPanelPrintStream jpos,
		JLabel label) {
		PrintStream stdout = System.out;
		try {
			Method ms = cls.getMethod("main", new Class[] { String[].class });

			if (openWindowOnStartup)
				System.setOut(jpos);
			notify(STARTING);
			long startTime = System.currentTimeMillis(), elapsed = 0;
			ms.invoke(null, new Object[] { args });
			elapsed = System.currentTimeMillis() - startTime;
			notify(STARTED);
			Object[] arg = new Object[2];
			arg[0] = new Integer(0);
			arg[1] = new Long(elapsed);
			if (openWindowOnStartup) {
				System.setOut(stdout);
				jpos.flush();
			}
			notify(TERMINATED, arg);

			if (outputElapsedTime) {
				String ti = new TimeInterval(elapsed).toString();
				if (this.openWindowOnStartup) {
					jf.setTitle("Run \"" + cls + "\" in " + ti);
					label.setText(" " + cls + " - terminated");
					jpos.flush();
				}
				if (this.outputOnSystemOut)
					System.out.println("Elapsed " + ti);
			} else {
				if (this.openWindowOnStartup) {
					jf.setTitle(" \"" + cls + "\" terminated");
					label.setText(" " + cls + " - terminated");
					jpos.flush();
				}
			}

		} catch (Exception e) {
			notify(FAILED, e);
			if (this.openWindowOnStartup) {
				ByteArrayOutputStream bs = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(bs);
				ps.println("Exception occurred");
				ps.println();
				e.printStackTrace(ps);
				jpos.println(bs.toString());
				jpos.flush();
				JOptionPane.showMessageDialog(
					jf,
					bs.toString(),
					"Exception Occurred",
					JOptionPane.ERROR_MESSAGE);
				jf.setTitle(" \"" + classString + "\" failed");
				label.setText(" " + classString + " - failed");
			}
			if (this.outputOnSystemOut)
				e.printStackTrace();
		}
		System.setOut(stdout);
	}

	private void executeCommand(
		String cmdString,
		JPanelPrintStream jpos,
		JLabel label) {

		String[] cmdLine = new String[args.length + 1];
		cmdLine[0] = command;
		System.arraycopy(args, 0, cmdLine, 1, args.length);

		for (int i = 0; i < cmdLine.length; i++) {
			System.out.print(cmdLine[i]);
			System.out.print(" ");
		}
		System.out.println();

		try {

			notify(STARTING);
			long startTime = System.currentTimeMillis(), elapsed = 0;

			Process p = Runtime.getRuntime().exec(cmdLine);
			notify(STARTED);
			InputStream is = p.getInputStream();
			int c;
			boolean cont;
			do {
				cont = false;
				while ((c = is.read()) != -1) {
					if (openWindowOnStartup)
						jpos.write(c);
					if (outputOnSystemOut)
						System.out.write(c);
				}
				elapsed = System.currentTimeMillis() - startTime;
				try {
					int v = p.exitValue();
					jpos.flush();
					Object[] arg = new Object[2];
					arg[0] = new Integer(v);
					arg[1] = new Long(elapsed);
					notify(TERMINATED, arg);
					p.destroy();
					if (captureOutput)
						lastOutput = jpos.getContents();
				} catch (IllegalThreadStateException e) {
					cont = true;
				}
			}
			while (cont);
			if (outputElapsedTime) {
				String ti = new TimeInterval(elapsed).toString();
				if (this.openWindowOnStartup) {
					jf.setTitle("Run \"" + command + "\" in " + ti);
					label.setText(" " + cmdString + " - terminated");
				}
				if (this.outputOnSystemOut)
					System.out.println("Elapsed " + ti);
			} else {
				if (this.openWindowOnStartup) {
					jf.setTitle(" \"" + cmdString + "\" terminated");
					label.setText(" " + cmdString + " - terminated");
				}
			}
		} catch (IOException e) {
			notify(FAILED, e);
			if (this.openWindowOnStartup) {
				ByteArrayOutputStream bs = new ByteArrayOutputStream();
				PrintStream ps = new PrintStream(bs);
				ps.println("Exception occurred");
				ps.println();
				e.printStackTrace(ps);
				JOptionPane.showMessageDialog(
					jf,
					bs.toString(),
					"Exception Occurred",
					JOptionPane.ERROR_MESSAGE);
				jf.setTitle(" \"" + command + "\" failed");
				label.setText(" " + cmdString + " - failed");
			}
			if (this.outputOnSystemOut)
				e.printStackTrace();
		}
	}

	private void closeWindow() {
		if (jf == null)
			return;
		jf.setVisible(false);
		jf.dispose();
		jf = null;
		lastOutput = null;
	}

	private String argsList(String[] args) {
		StringBuffer sb = new StringBuffer();
		synchronized (sb) {
			for (int i = 0; i < args.length; i++) {
				sb.append(args[i]);
				if (i < args.length - 1)
					sb.append(" ");
			}
			return sb.toString();
		}
	}

	/**
	 * Returns the closeWindowOnTermination.
	 * 
	 * @return boolean
	 */
	public boolean isCloseWindowOnTermination() {
		return closeWindowOnTermination;
	}

	/**
	 * Returns the openWindowOnStartup.
	 * 
	 * @return boolean
	 */
	public boolean isOpenWindowOnStartup() {
		return openWindowOnStartup;
	}

	/**
	 * Returns the outputOnSystemOut.
	 * 
	 * @return boolean
	 */
	public boolean isOutputOnSystemOut() {
		return outputOnSystemOut;
	}

	/**
	 * Sets the closeWindowOnTermination.
	 * 
	 * @param closeWindowOnTermination
	 *            The closeWindowOnTermination to set
	 */
	public synchronized void setCloseWindowOnTermination(boolean closeWindowOnTermination) {
		this.closeWindowOnTermination = closeWindowOnTermination;
	}

	/**
	 * Sets the openWindowOnStartup.
	 * 
	 * @param openWindowOnStartup
	 *            The openWindowOnStartup to set
	 */
	public synchronized void setOpenWindowOnStartup(boolean openWindowOnStartup) {
		this.openWindowOnStartup = openWindowOnStartup;
	}

	/**
	 * Set the output-on-system-out property. If <b>true</b>, the runner
	 * will (also) send the execution output to standard output.
	 * 
	 * @param outputOnSystemOut
	 *            The outputOnSystemOut to set
	 */
	public synchronized void setOutputOnSystemOut(boolean outputOnSystemOut) {
		this.outputOnSystemOut = outputOnSystemOut;
	}

	/**
	 * Return <b>true</b> if the runner is set to capture and store the
	 * output of the program run.
	 * 
	 * @see #getLastOutput()
	 * @return boolean
	 */
	public boolean isCaptureOutput() {
		return captureOutput;
	}

	/**
	 * Returns the output of the last execution, or <b>null</b> if either no
	 * execution has occurred yet, or
	 * {@link #isCaptureOutput() isCaptureOutput()}is <b>false</b>.
	 * 
	 * @return String
	 */
	public String getLastOutput() {
		return lastOutput;
	}

	/**
	 * Set the capture-output property. If <b>true</b> the output of the last
	 * run of the command is stored in memory, and can be retrieved with
	 * {@link #getLastOutput() getLastOutput()}.
	 * 
	 * @param captureOutput
	 *            The value to set.
	 */
	public void setCaptureOutput(boolean captureOutput) {
		this.captureOutput = captureOutput;
	}

	/**
	 * Returns the outputElapsedTime.
	 * 
	 * @return boolean
	 */
	public boolean isOutputElapsedTime() {
		return outputElapsedTime;
	}

	/**
	 * Sets the outputElapsedTime.
	 * 
	 * @param outputElapsedTime
	 *            The outputElapsedTime to set
	 */
	public void setOutputElapsedTime(boolean outputElapsedTime) {
		this.outputElapsedTime = outputElapsedTime;
	}

	/*
	 * Test method
	 */
	public static void main(String[] args) {
		new Thread() {
			public void run() {
				CommandRunner cmd =
					new CommandRunner(
						ListMapIterator.class,
						new String[] { "-X" },
						false);
				//new CommandRunner("java", new String[0], false);
				//cmd.setOpenWindowOnStartup(false);
				//cmd.setOutputOnSystemOut(true);
				//cmd.addListener(new CommandRunner.StdOutAdapter());
				cmd.start();
				while (true) {
					System.out.println("busy");
					Thread.yield();
				}
			}
		}
		.start();
	}

}
