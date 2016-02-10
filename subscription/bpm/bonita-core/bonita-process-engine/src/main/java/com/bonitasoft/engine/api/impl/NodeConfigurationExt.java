/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import org.bonitasoft.engine.api.impl.NodeConfigurationImpl;

import com.hazelcast.core.HazelcastInstance;

/**
 * @author Baptiste Mesta
 */
public class NodeConfigurationExt extends NodeConfigurationImpl {

    private final HazelcastInstance hazelcastInstance;

    public NodeConfigurationExt(HazelcastInstance hazelcastInstance) {
        this.hazelcastInstance = hazelcastInstance;
    }

    private boolean isTheOnlyNode() {
        return hazelcastInstance.getCluster().getMembers().size() == 1;
    }

    @Override
    public boolean shouldResumeElements() {
        return isTheOnlyNode();
    }

    @Override
    public boolean shouldClearSessions() {
        return isTheOnlyNode();
    }
}
