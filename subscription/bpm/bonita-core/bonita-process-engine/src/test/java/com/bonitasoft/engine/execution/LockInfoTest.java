/**
 * Copyright (C) 2015 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package com.bonitasoft.engine.execution;

import static com.bonitasoft.engine.execution.LockInfoAssert.assertThat;

import org.junit.Test;

public class LockInfoTest {

    @Test
    public void should_build_LockIdentifier_with_given_information() throws Exception {
       assertThat(new LockInfo(10L, "any", 15L)).hasId(10L).hasType("any").hasTenantId(15L);
    }



}