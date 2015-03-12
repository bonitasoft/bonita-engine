/**
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 **/
package org.bonitasoft.engine.bdm;

import static org.assertj.core.api.Assertions.assertThat;

import org.bonitasoft.engine.bdm.CodeGenerator;
import org.bonitasoft.engine.bdm.RelationFieldAnnotator;
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
