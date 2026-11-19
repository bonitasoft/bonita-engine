/**
 * Copyright (C) 2026 Bonitasoft S.A.
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
package org.bonitasoft.engine.core.process.definition.model;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.List;

import org.bonitasoft.engine.test.persistence.repository.ProcessDeploymentInfoRepository;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringRunner;
import org.springframework.transaction.annotation.Transactional;

@RunWith(SpringRunner.class)
@ContextConfiguration(locations = { "/testContext.xml" })
@Transactional
public class ProcessNameGroupQueriesTest {

    private static final String MATCH_ALL = "%";

    // "No activation-state filter": a null state selects the base (non-WithActivationState) query.
    private static final String NO_STATE_FILTER = null;

    @Autowired
    private ProcessDeploymentInfoRepository repository;

    @Before
    public void before() {
        // (invoice, Invoice): two enabled versions + one disabled version -> a single group
        repository.add(deployInfo(1L, 101L, "invoice", "Invoice", "1.0", "ENABLED"));
        repository.add(deployInfo(2L, 102L, "invoice", "Invoice", "2.0", "ENABLED"));
        repository.add(deployInfo(3L, 103L, "invoice", "Invoice", "3.0", "DISABLED"));
        // (report, Report): a single enabled version
        repository.add(deployInfo(4L, 104L, "report", "Report", "1.0", "ENABLED"));
    }

    @Test
    public void getNumberOfProcessNameGroups_counts_distinct_name_displayName_combinations() {
        assertThat(repository.getNumberOfProcessNameGroups(NO_STATE_FILTER, MATCH_ALL)).isEqualTo(2);
    }

    @Test
    public void getNumberOfProcessNameGroups_honors_the_activationState_filter() {
        // both groups still have at least one ENABLED version
        assertThat(repository.getNumberOfProcessNameGroups("ENABLED", MATCH_ALL)).isEqualTo(2);
        // only the invoice group has a DISABLED version
        assertThat(repository.getNumberOfProcessNameGroups("DISABLED", MATCH_ALL)).isEqualTo(1);
    }

    @Test
    public void getNumberOfProcessNameGroups_honors_the_search_term() {
        assertThat(repository.getNumberOfProcessNameGroups(NO_STATE_FILTER, "%invoice%")).isEqualTo(1);
    }

    @Test
    public void getNumberOfProcessNameGroups_does_not_match_the_search_term_against_version() {
        // the term matches name and displayName only: "2.0" is a version, not a name/displayName, so it selects nothing
        assertThat(repository.getNumberOfProcessNameGroups(NO_STATE_FILTER, "%2.0%")).isZero();
    }

    @Test
    public void searchProcessNameGroups_does_not_match_the_search_term_against_version() {
        // the term matches name and displayName only: "2.0" is a version, not a name/displayName, so it selects nothing
        assertThat(repository.searchProcessNameGroups(NO_STATE_FILTER, "%2.0%",
                /* sortByName */ false, /* ascending */ true)).isEmpty();
    }

    @Test
    public void getNumberOfProcessNameGroups_distinguishes_groups_that_would_collide_under_a_naive_separator() {
        // given two distinct groups whose name+displayName concatenations would collide with a '|' separator:
        // ("a|b","c") -> "a|b|c" and ("a","b|c") -> "a|b|c"
        repository.add(deployInfo(7L, 107L, "a|b", "c", "1.0", "ENABLED"));
        repository.add(deployInfo(8L, 108L, "a", "b|c", "1.0", "ENABLED"));

        // when - then: the control-character separator keeps them distinct (counts 2, not 1)
        assertThat(repository.getNumberOfProcessNameGroups(NO_STATE_FILTER, "%a%")).isEqualTo(2);
    }

    @Test
    public void searchProcessNameGroups_returns_one_entry_per_group_ordered_by_displayName_asc() {
        final List<ProcessNameKey> groups = repository.searchProcessNameGroups(NO_STATE_FILTER, MATCH_ALL,
                /* sortByName */ false, /* ascending */ true);

        assertThat(groups).extracting(ProcessNameKey::getName, ProcessNameKey::getDisplayName)
                .containsExactly(tuple("invoice", "Invoice"), tuple("report", "Report"));
    }

    @Test
    public void searchProcessNameGroups_orders_by_displayName_desc() {
        final List<ProcessNameKey> groups = repository.searchProcessNameGroups(NO_STATE_FILTER, MATCH_ALL,
                /* sortByName */ false, /* ascending */ false);

        assertThat(groups).extracting(ProcessNameKey::getDisplayName).containsExactly("Report", "Invoice");
    }

    @Test
    public void searchProcessNameGroups_orders_by_name_asc() {
        final List<ProcessNameKey> groups = repository.searchProcessNameGroups(NO_STATE_FILTER, MATCH_ALL,
                /* sortByName */ true, /* ascending */ true);

        assertThat(groups).extracting(ProcessNameKey::getName).containsExactly("invoice", "report");
    }

    @Test
    public void searchProcessNameGroups_orders_by_name_desc() {
        final List<ProcessNameKey> groups = repository.searchProcessNameGroups(NO_STATE_FILTER, MATCH_ALL,
                /* sortByName */ true, /* ascending */ false);

        assertThat(groups).extracting(ProcessNameKey::getName).containsExactly("report", "invoice");
    }

    @Test
    public void searchProcessNameGroups_honors_the_activationState_filter() {
        // only the invoice group has a DISABLED version
        final List<ProcessNameKey> groups = repository.searchProcessNameGroups("DISABLED", MATCH_ALL,
                /* sortByName */ true, /* ascending */ true);

        assertThat(groups).extracting(ProcessNameKey::getName).containsExactly("invoice");
    }

    @Test
    public void searchProcessNameGroups_breaks_displayName_ties_by_name_asc() {
        // given two groups sharing the same displayName but with different names
        repository.add(deployInfo(9L, 109L, "zzz", "Shared", "1.0", "ENABLED"));
        repository.add(deployInfo(10L, 110L, "aaa", "Shared", "1.0", "ENABLED"));

        // when sorting by displayName, the shared-displayName groups tie on the primary key
        final List<ProcessNameKey> asc = repository.searchProcessNameGroups(NO_STATE_FILTER, "%Shared%",
                /* sortByName */ false, /* ascending */ true);
        final List<ProcessNameKey> desc = repository.searchProcessNameGroups(NO_STATE_FILTER, "%Shared%",
                /* sortByName */ false, /* ascending */ false);

        // then the secondary tiebreaker stays name ASC regardless of the primary direction
        assertThat(asc).extracting(ProcessNameKey::getName).containsExactly("aaa", "zzz");
        assertThat(desc).extracting(ProcessNameKey::getName).containsExactly("aaa", "zzz");
    }

    @Test
    public void searchProcessNameGroups_breaks_name_ties_by_displayName_asc() {
        // given two groups sharing the same name but with different display names (distinct versions satisfy the
        // unique (name, version) constraint)
        repository.add(deployInfo(11L, 111L, "dup", "Beta", "1.0", "ENABLED"));
        repository.add(deployInfo(12L, 112L, "dup", "Alpha", "2.0", "ENABLED"));

        // when sorting by name, the shared-name groups tie on the primary key
        final List<ProcessNameKey> asc = repository.searchProcessNameGroups(NO_STATE_FILTER, "%dup%",
                /* sortByName */ true, /* ascending */ true);
        final List<ProcessNameKey> desc = repository.searchProcessNameGroups(NO_STATE_FILTER, "%dup%",
                /* sortByName */ true, /* ascending */ false);

        // then the secondary tiebreaker stays displayName ASC regardless of the primary direction
        assertThat(asc).extracting(ProcessNameKey::getDisplayName).containsExactly("Alpha", "Beta");
        assertThat(desc).extracting(ProcessNameKey::getDisplayName).containsExactly("Alpha", "Beta");
    }

    @Test
    public void getNumberOfProcessNameGroups_treats_escaped_underscore_as_a_literal() {
        // given a process whose name literally contains '_', plus a near-miss that '_' would wildcard-match
        repository.add(deployInfo(5L, 105L, "a_b", "A_B", "1.0", "ENABLED"));
        repository.add(deployInfo(6L, 106L, "axb", "Axb", "1.0", "ENABLED"));

        // when searching for the literal "a_b" (underscore escaped with '#', matching the ESCAPE '#' clause)
        final long count = repository.getNumberOfProcessNameGroups(NO_STATE_FILTER, "%a#_b%");

        // then only the literal match is counted ("axb" is not matched: '_' is not treated as a wildcard)
        assertThat(count).isEqualTo(1);
    }

    @Test
    public void getVersionsForProcessNames_excludes_versions_filtered_out_by_activationState() {
        assertThat(repository.getVersionsForProcessNames(List.of("invoice"), "ENABLED"))
                .extracting(ProcessNameVersion::getVersion)
                .containsExactlyInAnyOrder("1.0", "2.0");
    }

    @Test
    public void getVersionsForProcessNames_returns_all_versions_when_not_filtered() {
        assertThat(repository.getVersionsForProcessNames(List.of("invoice"), NO_STATE_FILTER))
                .extracting(ProcessNameVersion::getVersion)
                .containsExactlyInAnyOrder("1.0", "2.0", "3.0");
    }

    private SProcessDefinitionDeployInfo deployInfo(final long id, final long processId, final String name,
            final String displayName, final String version, final String activationState) {
        return SProcessDefinitionDeployInfo.builder()
                .id(id)
                .processId(processId)
                .name(name)
                .displayName(displayName)
                .version(version)
                .activationState(activationState)
                .configurationState("RESOLVED")
                .build();
    }
}
