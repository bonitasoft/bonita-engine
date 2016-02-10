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

package org.bonitasoft.engine.page.impl;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;

/**
 * @author Baptiste Mesta
 */
public class SPageMappingImplTest {

    @Test
    public void testEquals() throws Exception {
        SPageMappingImpl sPageMapping1 = new SPageMappingImpl();
        SPageMappingImpl sPageMapping2 = new SPageMappingImpl();

        setValues(sPageMapping1, sPageMapping2);

        assertThat(sPageMapping1).isEqualTo(sPageMapping2);

    }

    @Test
    public void testHashCode() throws Exception {
        SPageMappingImpl sPageMapping1 = new SPageMappingImpl();
        SPageMappingImpl sPageMapping2 = new SPageMappingImpl();

        setValues(sPageMapping1, sPageMapping2);

        assertThat(sPageMapping1.hashCode()).isEqualTo(sPageMapping2.hashCode());

    }

    @Test
    public void testToString() throws Exception {
        SPageMappingImpl sPageMapping1 = new SPageMappingImpl();
        SPageMappingImpl sPageMapping2 = new SPageMappingImpl();

        setValues(sPageMapping1, sPageMapping2);

        assertThat(sPageMapping1.toString()).isEqualTo(sPageMapping2.toString());

    }

    void setValues(SPageMappingImpl sPageMapping1, SPageMappingImpl sPageMapping2) {
        sPageMapping1.setKey("myKey");
        sPageMapping1.setUrlAdapter("urlAdapter");
        sPageMapping1.setPageAuthorizRules("net.comp.Rule");
        sPageMapping1.setUrl("myUrl");
        sPageMapping1.setPageId(11l);
        sPageMapping2.setKey("myKey");
        sPageMapping2.setUrlAdapter("urlAdapter");
        sPageMapping2.setPageAuthorizRules("net.comp.Rule");
        sPageMapping2.setUrl("myUrl");
        sPageMapping2.setPageId(11l);
    }

    @Test
    public void parseRulesShouldSetListOfRules() throws Exception {
        SPageMappingImpl mapping = new SPageMappingImpl();
        mapping.setPageAuthorizRules(",,toto,titi,tutu,");
        assertThat(mapping.getPageAuthorizationRules()).containsExactly("toto", "titi", "tutu");
    }

    @Test
    public void buildRulesAsStringShouldConcatRulesWithComma() throws Exception {
        SPageMappingImpl mapping = new SPageMappingImpl();
        mapping.setPageAuthorizationRules(Arrays.asList("toto", "titi", "tata"));
        assertThat(mapping.getPageAuthorizRules()).contains("toto,titi,tata");
    }
}
