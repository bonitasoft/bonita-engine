package org.bonitasoft.engine.dependency.model;

/**
 * @author Celine Souchet
 * 
 */
public enum ScopeType {
    /**
     * The dependency is map with a process instance.
     */
    PROCESS,

    /**
     * The dependency is map with a tenant.
     */
    TENANT,

    /**
     * For the platform dependency.
     */
    GLOBAL;
}
