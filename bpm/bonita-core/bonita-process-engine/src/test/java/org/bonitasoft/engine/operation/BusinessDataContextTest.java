/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 **/

package org.bonitasoft.engine.operation;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.commons.Container;
import org.junit.Test;

public class BusinessDataContextTest {

    @Test
    public void should_create_a_consistent_context() throws Exception {
        //when
        BusinessDataContext context = new BusinessDataContext("address", new Container(2L, "process"));

        //then
        assertThat(context.getName()).isEqualTo("address");
        assertThat(context.getContainer().getId()).isEqualTo(2L);
        assertThat(context.getContainer().getType()).isEqualTo("process");
    }

}
