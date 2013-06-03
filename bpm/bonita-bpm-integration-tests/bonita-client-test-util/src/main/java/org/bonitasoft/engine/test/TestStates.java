package org.bonitasoft.engine.test;

import org.bonitasoft.engine.bpm.process.ActivationState;
import org.bonitasoft.engine.bpm.process.ConfigurationState;
import org.bonitasoft.engine.bpm.process.ProcessInstance;

/**
 * @author Baptiste Mesta
 */
public class TestStates {

    public static String getInitialState(final Object activity) {
        return "initializing";
    }

    public static String getNormalFinalState(final Object activity) {
        return "completed";
    }

    public static String getNormalFinalState(final ProcessInstance processInstance) {
        return "completed";
    }

    public static String getReadyState(final Object step1) {
        return "ready";
    }

    public static String getSkippedState(final Object step1) {
        return "skipped";
    }

    public static ConfigurationState getProcessDepInfoUnresolvedState() {
        return ConfigurationState.UNRESOLVED;
    }

    public static ConfigurationState getProcessDepInfoResolvedState() {
        return ConfigurationState.RESOLVED;
    }

    public static ActivationState getProcessDepInfoEnabledState() {
        return ActivationState.ENABLED;
    }

    public static ActivationState getProcessDepInfoDisabledState() {
        return ActivationState.DISABLED;
    }

    public static String getWaitingState() {
        return "waiting";
    }

    public static String getFailedState() {
        return "failed";
    }

    public static String getAbortedState() {
        return "aborted";
    }

    public static String getExecutingState() {
        return "executing";
    }

    public static String getCancelledState() {
        return "cancelled";
    }

    public static String getInterruptingState() {
        return "interrupted";
    }

}
