package org.sadun.util.io;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.sadun.util.Action;
import org.sadun.util.ActionException;
import org.sadun.util.ConditionException;

/**
 * This class executes an {@link org.sadun.util.Action action}for every line in
 * a text file, possibly matching a {@link org.sadun.util.Condition condition}.
 * 
 * @author Cristiano Sadun
 */
public class ForEachLine {

    public abstract class Condition implements org.sadun.util.Condition {

        public final boolean holds(Object param) throws ConditionException {
            return holds((String) param);
        }

        public abstract boolean holds(String param) throws ConditionException;

    }

    private File processedFile;
    private org.sadun.util.Condition condition;
    private IOException closeFailure;
    private Action action;
    private boolean failOnConditionException = true;
    private boolean failOnActionException = true;
    private boolean failOnUnexistingFile = false;
    private Map conditionFailures = new HashMap();
    private Map actionFailures = new HashMap();
    private int readLinesCount;
    private int processedLinesCount;
    private boolean fileFound;

    /**
     * Executes the given {@link org.sadun.util.Action action} on the given file
     * for each line which matches the given {@link org.sadun.util.Condition condition}
     * 
     * @param file the file to process
     * @param the action to execute on each line
     * @param condition the condition that must hold on the line
     */
    public ForEachLine(File file, Action action,
            org.sadun.util.Condition condition) {
        this.processedFile = file;
        this.condition = condition;
        this.action = action;
    }

    /**
     * Executes the given {@link org.sadun.util.Action action} on the given file
     * for each line.
     * 
     * @param file the file to process
     * @param the action to execute on each line
     */
    public ForEachLine(File file, Action action) {
        this(file, action, Condition.ALWAYS_TRUE);
    }

    /**
     * Process the file. If an exception occurs, the file is automatically
     * closed.
     * 
     * @throws IOException
     * @throws ConditionException
     * @throws ActionException
     */
    public boolean process() throws IOException, ConditionException,
            ActionException {
        BufferedReader br = null;
        this.readLinesCount = 0;
        this.processedLinesCount = 0;
        this.fileFound = processedFile.exists();
        if (!processedFile.exists())
            if (!failOnUnexistingFile)
                return false;
        try {
            br = new BufferedReader(new FileReader(processedFile));
            String line;
            while ((line = br.readLine()) != null) {
                readLinesCount++;
                try {
                    if (condition.holds(line)) {
                        try {
                            action.execute(line);
                            processedLinesCount++;
                        } catch (ActionException e1) {
                            if (failOnActionException = true)
                                throw e1;
                            else
                                actionFailures.put(new Integer(readLinesCount),
                                        e1);
                        }
                    }
                } catch (ConditionException e) {
                    if (failOnConditionException)
                        throw e;
                    else
                        conditionFailures.put(new Integer(readLinesCount), e);
                }
            }
            return true;
        } finally {
            closeFailure = null;
            if (br != null)
                try {
                    br.close();
                } catch (IOException e) {
                    closeFailure = e;
                }
        }
    }

    /**
     * Return whether or not the file has been closed successfully  (after a {@link #process()} operation).
     * 
     * @return whether or not the file has been closed successfully.
     */
    public boolean isClosed() {
        return closeFailure == null;
    }

    /**
     * Returns the lines which did <i>not</i> match the condtion (after a {@link #process()} operation).
     * 
     * @return the lines which did <i>not</i> match the condtion (after a {@link #process()} operation).
     */
    public int[] getLinesWithFailedConditions() {
        int[] result = new int[conditionFailures.keySet().size()];
        int c = 0;
        for (Iterator i = conditionFailures.keySet().iterator(); i.hasNext();) {
            result[c++] = ((Integer) i.next()).intValue();
        }
        return result;
    }

    /**
     * Returns Returns the lines where the action failed (after a {@link #process()} operation).
     * 
     * @return Returns the lines where the action failed (after a {@link #process()} operation).
     */
    public int[] getLinesWithFailedActions() {
        int[] result = new int[actionFailures.keySet().size()];
        int c = 0;
        for (Iterator i = actionFailures.keySet().iterator(); i.hasNext();) {
            result[c++] = ((Integer) i.next()).intValue();
        }
        return result;
    }

    /**
     * Return the exception that was raised by evaluating the condition at the given line number,
     * or <b>null</b> if no exception exists for that line (after a {@link #process()} operation).
     * 
     * @param lineNumber the line number for which a condition exception may have occured.
     * @return the exception that was raised by evaluating the condition at the given line number,
     * or <b>null</b> if no exception exists for that line (after a {@link #process()} operation).
     */
    public ConditionException getConditionException(int lineNumber) {
        return (ConditionException) conditionFailures.get(new Integer(
                lineNumber));
    }

    /**
     * Return the exception that was raised by executing the action at the given line number,
     * or <b>null</b> if no exception exists for that line (after a {@link #process()} operation).
     * 
     * @param lineNumber the line number for which a action exception may have occured.
     * @return the exception that was raised by evaluating the action executed at the given line number,
     * or <b>null</b> if no exception exists for that line (after a {@link #process()} operation).
     */
    public ActionException getActionException(int lineNumber) {
        return (ActionException) actionFailures.get(new Integer(lineNumber));
    }

    /**
     * Return true if the processing has been successful (after a {@link #process()} operation).
     * 
     * @return true if the processing has been successful (after a {@link #process()} operation).
     */
    public boolean hasBeenSuccessful() {
        return conditionFailures.size() + actionFailures.size() == 0;
    }

    /**
     * Return the condition provided at construction.
     * 
     * @return the condition provided at construction.
     */
    public org.sadun.util.Condition getCondition() {
        return condition;
    }

    /**
     * Set a condition before processing. 
     * 
     * @param condition the condition to set.
     */
    public void setCondition(org.sadun.util.Condition condition) {
        this.condition = condition;
    }

    /**
     * @return Returns the failOnActionException.
     */
    public boolean isFailOnActionException() {
        return failOnActionException;
    }

    /**
     * @param failOnActionException
     *            The failOnActionException to set.
     */
    public void setFailOnActionException(boolean failOnActionException) {
        this.failOnActionException = failOnActionException;
    }

    /**
     * @return Returns the failOnConditionException.
     */
    public boolean isFailOnConditionException() {
        return failOnConditionException;
    }

    /**
     * @param failOnConditionException
     *            The failOnConditionException to set.
     */
    public void setFailOnConditionException(boolean failOnConditionException) {
        this.failOnConditionException = failOnConditionException;
    }

    /**
     * @return Returns the processedFile.
     */
    public File getProcessedFile() {
        return processedFile;
    }

    /**
     * @param processedFile
     *            The processedFile to set.
     */
    public void setProcessedFile(File processedFile) {
        this.processedFile = processedFile;
    }

    /**
     * @return Returns the processedLinesCount.
     */
    public int getProcessedLinesCount() {
        return processedLinesCount;
    }

    /**
     * @return Returns the readLinesCount.
     */
    public int getReadLinesCount() {
        return readLinesCount;
    }

    /**
     * @return Returns the fileFound.
     */
    public boolean isFileFound() {
        return fileFound;
    }
}
