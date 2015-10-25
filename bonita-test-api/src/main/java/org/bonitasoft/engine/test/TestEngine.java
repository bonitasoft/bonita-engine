package org.bonitasoft.engine.test;

/**
 * @author Baptiste Mesta
 */
public interface TestEngine {
    String TECHNICAL_USER_NAME = "install";
    String TECHNICAL_USER_PASSWORD = "install";

    boolean start() throws Exception;

    void stop() throws Exception;
}
