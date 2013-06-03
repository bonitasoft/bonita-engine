/**
 * Copyright (C) 2011-2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.exception;

import java.io.PrintStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author Matthieu Chaffotte
 * @author Baptiste Mesta
 */
public class BonitaException extends Exception {

    private static final long serialVersionUID = -5413586694735909486L;

    private int[] causesPositions;

    public BonitaException(final String message) {
        super(message);
    }

    public BonitaException(final String message, final Throwable cause) {
        super(message);
        mergeStacks(cause);
    }

    public BonitaException(final Throwable cause) {
        super(cause.getMessage());
        mergeStacks(cause);
    }

    protected int[] getCausesPositions() {
        return causesPositions;
    }

    private void mergeStacks(final Throwable cause) {
        final StackTraceElement[] currentStack = getStackTrace();
        final List<StackTraceElement[]> causesStacks = new ArrayList<StackTraceElement[]>();
        final List<Throwable> exceptions = new ArrayList<Throwable>();
        Throwable subCause = cause;
        int causeslength = 0;
        do {
            final StackTraceElement[] stackTrace = subCause.getStackTrace();
            causesStacks.add(stackTrace);
            exceptions.add(subCause);
            causeslength += stackTrace.length + 1;
        } while ((subCause = subCause.getCause()) != null);
        final StackTraceElement[] mergedStackTrace = new StackTraceElement[currentStack.length + causeslength + 1];
        System.arraycopy(currentStack, 0, mergedStackTrace, 0, currentStack.length);
        causesPositions = new int[exceptions.size() + 1];
        causesPositions[0] = currentStack.length;
        mergedStackTrace[currentStack.length] = new StackTraceElement("══════════════════════════", "<server stack trace>", "", -3);
        int current = currentStack.length + 1;
        int i = 0;
        for (final StackTraceElement[] stackTraceElements : causesStacks) {
            System.arraycopy(stackTraceElements, 0, mergedStackTrace, current, stackTraceElements.length);
            current += stackTraceElements.length;
            causesPositions[i + 1] = current;
            mergedStackTrace[current] = new StackTraceElement("", "Caused By:", exceptions.get(i).getMessage(), -3);
            current += 1;
            i++;
        }
        setStackTrace(mergedStackTrace);
    }

    @Override
    public void printStackTrace(final PrintStream s) {
        if (causesPositions != null && causesPositions.length > 0) {
            printCauses(s);
        } else {
            super.printStackTrace(s);
        }
    }

    /**
     * Print our stack trace as a cause for the specified stack trace.
     */
    private void printCauses(final PrintStream s) {
        boolean printSuper = false;
        synchronized (s) {
            int startStack1, endStack1, startStack2, endStack2;
            final StackTraceElement[] stackTraceElements = getStackTrace();
            if (printSuper = stackTraceElements != null && stackTraceElements.length > 0) {
                for (int j = 0; j <= causesPositions[0] - 1; j++) {
                    s.println("\tat " + stackTraceElements[j]);
                }
                for (int i = 0; i < causesPositions.length - 1; i++) {
                    if (i == 0) {
                        startStack1 = 0;
                    } else {
                        startStack1 = causesPositions[i - 1] + 1;
                    }
                    endStack1 = causesPositions[i] - 1;
                    startStack2 = causesPositions[i] + 1;
                    endStack2 = causesPositions[i + 1] - 1;
                    // Compute number of frames in common between this and caused
                    int m = endStack1;
                    int n = endStack2;
                    while (m >= startStack1 && n >= startStack2 && stackTraceElements[m].equals(stackTraceElements[n])) {
                        m--;
                        n--;
                    }
                    final int framesInCommon = endStack1 - m;

                    if (i == 0) {
                        s.println("Server Exceptions: ");
                    } else {
                        s.println("Caused by: " + this);
                    }
                    for (int j = startStack2; j <= n; j++) {
                        s.println("\tat " + stackTraceElements[j]);
                    }
                    if (framesInCommon != 0) {
                        s.println("\t... " + framesInCommon + " more");
                    }
                }
            }
        }
        if (printSuper) {
            super.printStackTrace(s);
        }
    }

    /**
     * Prints this throwable and its backtrace to the specified
     * print writer.
     * 
     * @param s
     *            <code>PrintWriter</code> to use for output
     * @since JDK1.1
     */
    @Override
    public void printStackTrace(final PrintWriter s) {
        if (causesPositions != null && causesPositions.length > 0) {
            printCauses(s);
        } else {
            super.printStackTrace(s);
        }
    }

    /**
     * Print our stack trace as a cause for the specified stack trace.
     */
    private void printCauses(final PrintWriter s) {
        boolean printSuper = false;
        synchronized (s) {
            int startStack1, endStack1, startStack2, endStack2;
            final StackTraceElement[] stackTraceElements = getStackTrace();
            if (printSuper = stackTraceElements != null && stackTraceElements.length > 0) {
                for (int j = 0; j <= causesPositions[0] - 1; j++) {
                    s.println("\tat " + stackTraceElements[j]);
                }
                for (int i = 0; i < causesPositions.length - 1; i++) {
                    if (i == 0) {
                        startStack1 = 0;
                    } else {
                        startStack1 = causesPositions[i - 1] + 1;
                    }
                    endStack1 = causesPositions[i] - 1;
                    startStack2 = causesPositions[i] + 1;
                    endStack2 = causesPositions[i + 1] - 1;
                    // Compute number of frames in common between this and caused
                    int m = endStack1;
                    int n = endStack2;
                    while (m >= startStack1 && n >= startStack2 && stackTraceElements[m].equals(stackTraceElements[n])) {
                        m--;
                        n--;
                    }
                    final int framesInCommon = endStack1 - m;

                    s.println("Caused by: " + stackTraceElements[causesPositions[i + 1]]);
                    for (int j = startStack2; j <= n; j++) {
                        s.println("\tat " + stackTraceElements[j]);
                    }
                    if (framesInCommon != 0) {
                        s.println("\t... " + framesInCommon + " more");
                    }
                }
            }
        }
        if (printSuper) {
            super.printStackTrace(s);
        }
    }

    public StackTraceElement[] getStackTrace(final int index) {
        final StackTraceElement[] stackTrace = getStackTrace();
        int startIndex;
        if (index == 0) {
            startIndex = 0;
        } else {
            startIndex = causesPositions[index - 1] + 1;
        }
        int endIndex;
        if (index == causesPositions.length - 1) {
            endIndex = stackTrace.length - 1;
        } else {
            endIndex = causesPositions[index + 1] - 1;
        }
        return Arrays.copyOfRange(stackTrace, startIndex, endIndex);
    }

    /**
     * Override this method to define a default message.<br />
     * Default message will be the getMessage() result if no other message is defined.
     * 
     * @return This method returns the default message of the exception.
     */
    protected String getDefaultMessage() {
        return "";
    }

    @Override
    public String getMessage() {
        String message = super.getMessage();
        if (message != null && message.isEmpty() && getCause() != null) {
            message = getCause().getMessage();
        }
        if (message != null && message.isEmpty()) {
            message = getDefaultMessage();
        }
        return message;
    }

}
