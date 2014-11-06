/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/

package com.bonitasoft.engine.business.application.importer;

import static org.assertj.core.api.Assertions.*;

import com.bonitasoft.engine.business.application.ApplicationImportPolicy;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class StrategySelectorTest {

    @InjectMocks
    private StrategySelector selector;

    @Test
    public void selectStrategy_should_return_instance_of_FailOnDuplicate_when_policy_is_FailOnDuplicate() throws Exception {
        //when
        ApplicationImportStrategy strategy = selector.selectStrategy(ApplicationImportPolicy.FAIL_ON_DUPLICATES);

        //then
        assertThat(strategy).isInstanceOf(FailOnDuplicateApplicationImportStrategy.class);
    }

}