/*******************************************************************************
 * Copyright (C) 2013-2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Before;
import org.junit.Test;

public class RelationFieldAnnotatorTest {

    private RelationFieldAnnotator annotator;

    @Before
    public void setUp() {
        annotator = new RelationFieldAnnotator(new CodeGenerator());
    }

    @Test
    public void getJoinTableName_should_truncate_names_longer_than_14_chars() {
        String joinTableName = annotator.getJoinTableName("someLongNameLongerThanFourteen", "anotherLongNameLongerThanFourteen");

        assertThat(joinTableName).isEqualTo("SOMELONGNAMELO_ANOTHERLONGNAM");
    }

    @Test
    public void getJoinColumnName_should_truncate_names_longer_thab_26_chars() {
        String joinColumnName = annotator.getJoinColumnName("someLongNameLongerThantwentySix");

        assertThat(joinColumnName).isEqualTo("SOMELONGNAMELONGERTHANTWEN_PID");
    }
}
