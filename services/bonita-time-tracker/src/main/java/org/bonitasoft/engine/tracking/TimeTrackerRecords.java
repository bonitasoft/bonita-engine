package org.bonitasoft.engine.tracking;

public class TimeTrackerRecords {

    /**
     * this key is used to track the connector execution (execute method only, not in/out parameters processing) including the pool submission (that may have
     * additional impact if the pool is full).
     */
    public static final String EXECUTE_CONNECTOR_INCLUDING_POOL_SUBMIT = "EXECUTE_CONNECTOR_INCLUDING_POOL_SUBMIT";

    /**
     * this key is used to track the connector execution (execute method only, not in/out parameters processing), without potential pool submission impact
     */
    public static final String EXECUTE_CONNECTOR_CALLABLE = "EXECUTE_CONNECTOR_CALLABLE";

    /**
     * this key is used to track connector output parameters processing only (not pooling, not input, not execute)
     */
    public static final String EXECUTE_CONNECTOR_OUTPUT_OPERATIONS = "EXECUTE_CONNECTOR_OUTPUT_OPERATIONS";

    /**
     * this key is used to track connector input parameters processing only (not pooling, not execute, not output)
     */
    public static final String EXECUTE_CONNECTOR_INPUT_EXPRESSIONS = "EXECUTE_CONNECTOR_INPUT_EXPRESSIONS";

    /**
     * this key is used to track only the call to disconnect on a connector
     */
    public static final String EXECUTE_CONNECTOR_DISCONNECT = "EXECUTE_CONNECTOR_DISCONNECT";

    /**
     * this key is used to track the whole connector execution including pooling, input, execute, output and disconnect
     */
    public static final String EXECUTE_CONNECTOR_WORK = "EXECUTE_CONNECTOR_WORK";
}
