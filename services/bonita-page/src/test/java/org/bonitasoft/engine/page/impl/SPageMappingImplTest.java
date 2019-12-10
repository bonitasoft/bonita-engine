/**
 * Copyright (C) 2019 Bonitasoft S.A.
 * Bonitasoft, 32 rue Gustave Eiffel - 38000 Grenoble
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
package org.bonitasoft.engine.page.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.bonitasoft.engine.page.SPageMapping;
import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class SPageMappingImplTest {

    @Test
    public void testEquals() {
        SPageMapping sPageMapping1 = new SPageMapping();
        SPageMapping sPageMapping2 = new SPageMapping();

        setValues(sPageMapping1, sPageMapping2);

        assertThat(sPageMapping1).isEqualTo(sPageMapping2);
    }

    @Test
    public void testHashCode() {
        SPageMapping sPageMapping1 = new SPageMapping();
        SPageMapping sPageMapping2 = new SPageMapping();

        setValues(sPageMapping1, sPageMapping2);

        assertThat(sPageMapping1.hashCode()).isEqualTo(sPageMapping2.hashCode());

    }

    @Test
    public void testToString() {
        SPageMapping sPageMapping1 = new SPageMapping();
        SPageMapping sPageMapping2 = new SPageMapping();

        setValues(sPageMapping1, sPageMapping2);

        assertThat(sPageMapping1.toString()).isEqualTo(sPageMapping2.toString());
    }

    void setValues(SPageMapping sPageMapping1, SPageMapping sPageMapping2) {
        sPageMapping1.setKey("myKey");
        sPageMapping1.setUrlAdapter("urlAdapter");
        sPageMapping1.setPageAuthorizRules("net.comp.Rule");
        sPageMapping1.setUrl("myUrl");
        sPageMapping1.setPageId(11L);
        sPageMapping2.setKey("myKey");
        sPageMapping2.setUrlAdapter("urlAdapter");
        sPageMapping2.setPageAuthorizRules("net.comp.Rule");
        sPageMapping2.setUrl("myUrl");
        sPageMapping2.setPageId(11L);
    }

    @Test
    public void parseRulesShouldSetListOfRules() {
        SPageMapping mapping = new SPageMapping();
        mapping.setPageAuthorizRules(",,toto,titi,tutu,");
        assertThat(mapping.getPageAuthorizationRules()).containsExactly("toto", "titi", "tutu");
    }

    @Test
    public void buildRulesAsStringShouldConcatRulesWithComma() {
        SPageMapping mapping = new SPageMapping();
        mapping.setPageAuthorizationRules(Arrays.asList("toto", "titi", "tata"));
        assertThat(mapping.getPageAuthorizRules()).contains("toto,titi,tata");
    }
}
