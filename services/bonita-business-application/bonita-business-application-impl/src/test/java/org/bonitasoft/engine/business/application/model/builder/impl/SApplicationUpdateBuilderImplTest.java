/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/
package org.bonitasoft.engine.business.application.model.builder.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;

import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationFields;
import org.bonitasoft.engine.business.application.model.builder.impl.SApplicationUpdateBuilderImpl;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.junit.Test;

public class SApplicationUpdateBuilderImplTest {

    @Test
    public void constructorShoulUpdateUpdatedByAndLastUpdateDateFields() throws Exception {
        final EntityUpdateDescriptor desc = new SApplicationUpdateBuilderImpl(17L).done();
        final Map<String, Object> fields = desc.getFields();
        assertThat(fields.size()).isEqualTo(2);
        assertThat(fields.get(SApplicationFields.UPDATED_BY)).isEqualTo(17L);
        assertThat(fields.get(SApplicationFields.LAST_UPDATE_DATE)).isNotNull();
    }
}
