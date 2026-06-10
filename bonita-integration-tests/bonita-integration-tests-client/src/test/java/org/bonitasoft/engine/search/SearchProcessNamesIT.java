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
package org.bonitasoft.engine.search;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import java.util.ArrayList;
import java.util.List;

import org.bonitasoft.engine.TestWithTechnicalUser;
import org.bonitasoft.engine.bpm.bar.BusinessArchiveBuilder;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDefinition;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoSearchDescriptor;
import org.bonitasoft.engine.bpm.process.ProcessDeploymentInfoUpdater;
import org.bonitasoft.engine.bpm.process.ProcessNameInfo;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration tests for {@link org.bonitasoft.engine.api.ProcessManagementAPI#searchProcessNames}, which groups
 * process deployment infos by (name, displayName) and returns their versions.
 * <p>
 * Process names are prefixed with a unique marker so the search term can scope results to this test's data,
 * independently of any other process deployed in the shared engine.
 */
public class SearchProcessNamesIT extends TestWithTechnicalUser {

    private static final String MARKER = "SPNIT_";

    private static final String INVOICE = MARKER + "invoice";

    private static final String EXPENSE = MARKER + "expense";

    // display names ordered differently from the technical names, to prove ordering is on displayName
    private static final String INVOICE_DISPLAY = MARKER + "A-Invoice";

    private static final String EXPENSE_DISPLAY = MARKER + "Z-Expense";

    private final List<ProcessDefinition> processes = new ArrayList<>();

    @Override
    @Before
    public void before() throws Exception {
        super.before();
        // (INVOICE, INVOICE_DISPLAY): two enabled versions + one disabled version
        deployEnabled(INVOICE, "1.0", INVOICE_DISPLAY);
        deployEnabled(INVOICE, "2.0", INVOICE_DISPLAY);
        deployDisabled(INVOICE, "3.0", INVOICE_DISPLAY);
        // (EXPENSE, EXPENSE_DISPLAY): one enabled version
        deployEnabled(EXPENSE, "1.0", EXPENSE_DISPLAY);
    }

    @Override
    @After
    public void after() throws Exception {
        for (final ProcessDefinition process : processes) {
            disableAndDeleteProcess(process);
        }
        super.after();
    }

    @Test
    public void should_group_by_name_and_displayName_excluding_disabled_versions_ordered_by_displayName()
            throws Exception {
        // given - search the test's enabled processes, ordered by display name
        final SearchOptions options = new SearchOptionsBuilder(0, 10)
                .filter(ProcessDeploymentInfoSearchDescriptor.ACTIVATION_STATE, "ENABLED")
                .sort(ProcessDeploymentInfoSearchDescriptor.DISPLAY_NAME, Order.ASC)
                .searchTerm(MARKER).done();

        // when
        final SearchResult<ProcessNameInfo> result = getProcessAPI().searchProcessNames(options);

        // then - two distinct groups; the disabled invoice 3.0 is excluded from the version list
        assertThat(result.getCount()).isEqualTo(2);
        assertThat(result.getResult())
                .extracting(ProcessNameInfo::getName, ProcessNameInfo::getDisplayName, ProcessNameInfo::getVersions)
                .containsExactly(
                        tuple(INVOICE, INVOICE_DISPLAY, List.of("1.0", "2.0")),
                        tuple(EXPENSE, EXPENSE_DISPLAY, List.of("1.0")));
    }

    @Test
    public void should_filter_groups_by_search_term() throws Exception {
        // given
        final SearchOptions options = new SearchOptionsBuilder(0, 10)
                .filter(ProcessDeploymentInfoSearchDescriptor.ACTIVATION_STATE, "ENABLED")
                .searchTerm(INVOICE).done();

        // when
        final SearchResult<ProcessNameInfo> result = getProcessAPI().searchProcessNames(options);

        // then - only the invoice group matches the term
        assertThat(result.getCount()).isEqualTo(1);
        assertThat(result.getResult())
                .extracting(ProcessNameInfo::getName, ProcessNameInfo::getVersions)
                .containsExactly(tuple(INVOICE, List.of("1.0", "2.0")));
    }

    @Test
    public void should_paginate_over_groups() throws Exception {
        // given - one group per page, ordered by display name (A-Invoice before Z-Expense)
        final SearchResult<ProcessNameInfo> firstPage = getProcessAPI().searchProcessNames(groupsPage(0, 1,
                ProcessDeploymentInfoSearchDescriptor.DISPLAY_NAME, Order.ASC));
        final SearchResult<ProcessNameInfo> secondPage = getProcessAPI().searchProcessNames(groupsPage(1, 1,
                ProcessDeploymentInfoSearchDescriptor.DISPLAY_NAME, Order.ASC));

        // then - count is the total number of groups on every page; each page returns its own group
        assertThat(firstPage.getCount()).isEqualTo(2);
        assertThat(firstPage.getResult()).extracting(ProcessNameInfo::getName).containsExactly(INVOICE);
        assertThat(secondPage.getCount()).isEqualTo(2);
        assertThat(secondPage.getResult()).extracting(ProcessNameInfo::getName).containsExactly(EXPENSE);
    }

    @Test
    public void should_order_groups_by_requested_field_and_direction() throws Exception {
        // displayName: A-Invoice < Z-Expense ; name: SPNIT_expense < SPNIT_invoice
        assertThat(orderedNames(ProcessDeploymentInfoSearchDescriptor.DISPLAY_NAME, Order.ASC))
                .containsExactly(INVOICE, EXPENSE);
        assertThat(orderedNames(ProcessDeploymentInfoSearchDescriptor.DISPLAY_NAME, Order.DESC))
                .containsExactly(EXPENSE, INVOICE);
        assertThat(orderedNames(ProcessDeploymentInfoSearchDescriptor.NAME, Order.ASC))
                .containsExactly(EXPENSE, INVOICE);
        assertThat(orderedNames(ProcessDeploymentInfoSearchDescriptor.NAME, Order.DESC))
                .containsExactly(INVOICE, EXPENSE);
    }

    private List<String> orderedNames(final String sortField, final Order order) throws Exception {
        return getProcessAPI().searchProcessNames(groupsPage(0, 10, sortField, order)).getResult().stream()
                .map(ProcessNameInfo::getName).collect(java.util.stream.Collectors.toList());
    }

    private SearchOptions groupsPage(final int startIndex, final int count, final String sortField, final Order order) {
        return new SearchOptionsBuilder(startIndex, count)
                .filter(ProcessDeploymentInfoSearchDescriptor.ACTIVATION_STATE, "ENABLED")
                .sort(sortField, order)
                .searchTerm(MARKER).done();
    }

    @Test
    public void should_not_apply_activation_state_filter_when_absent() throws Exception {
        // given - no activationState filter: every version (including the disabled 3.0) is grouped
        final SearchOptions options = new SearchOptionsBuilder(0, 10)
                .searchTerm(INVOICE).done();

        // when
        final SearchResult<ProcessNameInfo> result = getProcessAPI().searchProcessNames(options);

        // then
        assertThat(result.getResult())
                .extracting(ProcessNameInfo::getName, ProcessNameInfo::getVersions)
                .containsExactly(tuple(INVOICE, List.of("1.0", "2.0", "3.0")));
    }

    private void deployEnabled(final String name, final String version, final String displayName) throws Exception {
        final DesignProcessDefinition design = new ProcessDefinitionBuilder().createNewInstance(name, version)
                .addAutomaticTask("step1").getProcess();
        final ProcessDefinition process = deployAndEnableProcess(design);
        setDisplayName(process, displayName);
        processes.add(process);
    }

    private void deployDisabled(final String name, final String version, final String displayName) throws Exception {
        final DesignProcessDefinition design = new ProcessDefinitionBuilder().createNewInstance(name, version)
                .addAutomaticTask("step1").getProcess();
        final ProcessDefinition process = deployProcess(
                new BusinessArchiveBuilder().createNewBusinessArchive().setProcessDefinition(design).done());
        setDisplayName(process, displayName);
        processes.add(process);
    }

    private void setDisplayName(final ProcessDefinition process, final String displayName) throws Exception {
        final ProcessDeploymentInfoUpdater updater = new ProcessDeploymentInfoUpdater();
        updater.setDisplayName(displayName);
        getProcessAPI().updateProcessDeploymentInfo(process.getId(), updater);
    }
}
