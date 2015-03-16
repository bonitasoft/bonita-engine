/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.api.impl;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.util.HashSet;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.hazelcast.core.Cluster;
import com.hazelcast.core.HazelcastInstance;
import com.hazelcast.core.Member;

@RunWith(MockitoJUnitRunner.class)
public class NodeConfigurationExtTest {

    @Mock
    private HazelcastInstance hazelcastInstance;

    @Mock
    private Cluster cluster;

    @InjectMocks
    private NodeConfigurationExt nodeConfigurationExt;

    private HashSet<Member> two_members;

    private HashSet<Member> one_members;

    @Before
    public void before() {
        when(hazelcastInstance.getCluster()).thenReturn(cluster);
        two_members = new HashSet<Member>();
        two_members.add(mock(Member.class));
        two_members.add(mock(Member.class));
        one_members = new HashSet<Member>();
        one_members.add(mock(Member.class));
    }

    @Test
    public void testShouldResumeElementsWhenOneNode() {
        when(cluster.getMembers()).thenReturn(one_members);

        assertTrue("must return true when cluster have one element", nodeConfigurationExt.shouldResumeElements());
    }

    @Test
    public void testShouldResumeElementsWhenSeveralNode() {
        when(cluster.getMembers()).thenReturn(two_members);

        assertFalse("must return false when cluster have one element", nodeConfigurationExt.shouldResumeElements());
    }

}
