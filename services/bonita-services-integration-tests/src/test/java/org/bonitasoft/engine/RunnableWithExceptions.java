package org.bonitasoft.engine;

/**
 * @author Baptiste Mesta
 */
public interface RunnableWithExceptions<T extends Exception, U> {

    void run(U argument) throws T;

}
