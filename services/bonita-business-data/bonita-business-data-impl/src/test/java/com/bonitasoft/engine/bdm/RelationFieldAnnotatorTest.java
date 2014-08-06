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
    public void getJoinTableName_should_truncate_names_longer_than_14_chars() throws Exception {
        String joinTableName = annotator.getJoinTableName("someLongNameLongerThanFourteen", "anotherLongNameLongerThanFourteen");

        assertThat(joinTableName).isEqualTo("SOMELONGNAMELO_ANOTHERLONGNAM");
    }

    @Test
    public void getJoinColumnName_should_truncate_names_longer_thab_26_chars() throws Exception {
        String joinColumnName = annotator.getJoinColumnName("someLongNameLongerThantwentySix");

        assertThat(joinColumnName).isEqualTo("SOMELONGNAMELONGERTHANTWEN_PID");
    }
}
