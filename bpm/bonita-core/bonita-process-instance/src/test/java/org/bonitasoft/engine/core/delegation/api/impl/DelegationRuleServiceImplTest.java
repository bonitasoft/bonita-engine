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
package org.bonitasoft.engine.core.delegation.api.impl;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.bonitasoft.engine.core.delegation.api.SDelegationRuleCreationException;
import org.bonitasoft.engine.core.delegation.api.SDelegationRuleNotFoundException;
import org.bonitasoft.engine.core.delegation.api.SDelegationRuleUpdateException;
import org.bonitasoft.engine.core.delegation.model.SDelegatedHumanTask;
import org.bonitasoft.engine.core.delegation.model.SDelegationRule;
import org.bonitasoft.engine.core.delegation.model.SDelegationRuleProcess;
import org.bonitasoft.engine.delegation.DelegationRuleFilterKeys;
import org.bonitasoft.engine.persistence.FilterOption;
import org.bonitasoft.engine.persistence.OrderByOption;
import org.bonitasoft.engine.persistence.OrderByType;
import org.bonitasoft.engine.persistence.QueryOptions;
import org.bonitasoft.engine.persistence.ReadPersistenceService;
import org.bonitasoft.engine.persistence.SBonitaReadException;
import org.bonitasoft.engine.persistence.SelectByIdDescriptor;
import org.bonitasoft.engine.persistence.SelectListDescriptor;
import org.bonitasoft.engine.persistence.SelectOneDescriptor;
import org.bonitasoft.engine.persistence.search.FilterOperationType;
import org.bonitasoft.engine.recorder.Recorder;
import org.bonitasoft.engine.recorder.model.DeleteAllRecord;
import org.bonitasoft.engine.recorder.model.DeleteRecord;
import org.bonitasoft.engine.recorder.model.EntityUpdateDescriptor;
import org.bonitasoft.engine.recorder.model.InsertRecord;
import org.bonitasoft.engine.recorder.model.UpdateRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DelegationRuleServiceImplTest {

    /** Fixed reference timestamp used by the date-window tests (STATUS-rewrite and active-rules-for-delegate). */
    private static final long FIXED_NOW = 1_700_000_000_000L;

    @Mock
    private Recorder recorder;
    @Mock
    private ReadPersistenceService persistenceService;

    @Spy
    @InjectMocks
    private DelegationRuleServiceImpl service;

    @BeforeEach
    void setUp() {
        // Date-window tests pin "now" to FIXED_NOW; lenient() because most tests in this class
        // never reach the rewrite path and would otherwise trip Mockito's strict-stubs check.
        lenient().doReturn(FIXED_NOW).when(service).currentTimeMillis();
    }

    @Test
    void getRule_returns_entity_when_present() throws Exception {
        //given
        final SDelegationRule rule = SDelegationRule.builder().id(42L).delegatorId(1L).delegateId(2L).build();
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(rule);

        //when
        final SDelegationRule actual = service.getRule(42L);

        //then
        assertThat(actual).isSameAs(rule);
    }

    @Test
    void getRule_throws_not_found_when_missing() throws Exception {
        //given
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(null);

        //when-then
        assertThatThrownBy(() -> service.getRule(99L))
                .isInstanceOf(SDelegationRuleNotFoundException.class)
                .hasMessageContaining("99");
    }

    @Test
    void getRuleForDelegator_returns_optional_with_rule_when_present() throws Exception {
        //given
        final SDelegationRule rule = SDelegationRule.builder().id(7L).delegatorId(11L).delegateId(12L).build();
        when(persistenceService.selectOne(argThat(this::queryTargetsRuleByDelegatorId))).thenReturn(rule);

        //when
        final Optional<SDelegationRule> actual = service.getRuleForDelegator(11L);

        //then
        assertThat(actual).containsSame(rule);
    }

    @Test
    void getRuleForDelegator_returns_empty_optional_when_none() throws Exception {
        //given
        when(persistenceService.selectOne(argThat(this::queryTargetsRuleByDelegatorId))).thenReturn(null);

        //when
        final Optional<SDelegationRule> actual = service.getRuleForDelegator(11L);

        //then
        assertThat(actual).isEmpty();
    }

    @Test
    void createOrUpdateRule_inserts_new_rule_and_whitelist_when_delegator_has_no_existing_rule()
            throws Exception {
        //given
        when(persistenceService.selectOne(argThat(this::queryTargetsRuleByDelegatorId))).thenReturn(null);
        final SDelegationRule rule = SDelegationRule.builder()
                .delegatorId(10L).delegateId(20L).startDate(100L).endDate(200L)
                .lastUpdatedBy(10L).lastUpdatedAt(150L)
                .build();
        // Recorder is expected to set the id during insert (sequence manager side-effect in production)
        doAnswer(inv -> {
            ((InsertRecord) inv.getArgument(0)).getEntity().setId(777L);
            return null;
        }).when(recorder).recordInsert(argThat((InsertRecord r) -> r.getEntity() instanceof SDelegationRule),
                eq(DelegationRuleServiceImpl.RECORD_TYPE_DELEGATION_RULE));

        //when
        final SDelegationRule persisted = service.createOrUpdateRule(rule, Arrays.asList("ProcessA", "ProcessB"));

        //then
        assertThat(persisted.getId()).isEqualTo(777L);
        verify(recorder).recordInsert(argThat((InsertRecord r) -> r.getEntity() == rule),
                eq(DelegationRuleServiceImpl.RECORD_TYPE_DELEGATION_RULE));

        final ArgumentCaptor<InsertRecord> whitelistInserts = ArgumentCaptor.forClass(InsertRecord.class);
        verify(recorder, times(2)).recordInsert(whitelistInserts.capture(),
                eq(DelegationRuleServiceImpl.RECORD_TYPE_DELEGATION_RULE_PROCESS));
        final List<InsertRecord> captured = whitelistInserts.getAllValues();
        assertThat(captured).extracting(r -> (SDelegationRuleProcess) r.getEntity())
                .extracting(SDelegationRuleProcess::getDelegationRuleId, SDelegationRuleProcess::getProcessName)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(777L, "ProcessA"),
                        org.assertj.core.groups.Tuple.tuple(777L, "ProcessB"));
        verify(recorder, never()).recordUpdate(any(UpdateRecord.class),
                eq(DelegationRuleServiceImpl.RECORD_TYPE_DELEGATION_RULE));
        verify(recorder, never()).recordDeleteAll(any(DeleteAllRecord.class));
    }

    @Test
    void createOrUpdateRule_replaces_existing_rule_and_whitelist_when_delegator_already_has_one() throws Exception {
        //given
        final SDelegationRule existing = SDelegationRule.builder()
                .id(555L).delegatorId(10L).delegateId(99L).startDate(50L).endDate(150L)
                .lastUpdatedBy(10L).lastUpdatedAt(80L)
                .build();
        when(persistenceService.selectOne(argThat(this::queryTargetsRuleByDelegatorId))).thenReturn(existing);
        final SDelegationRule incoming = SDelegationRule.builder()
                .delegatorId(10L).delegateId(20L).startDate(100L).endDate(200L)
                .lastUpdatedBy(10L).lastUpdatedAt(170L)
                .build();

        //when
        final SDelegationRule persisted = service.createOrUpdateRule(incoming, Arrays.asList("ProcessA"));

        //then
        // existing rule id is preserved
        assertThat(persisted.getId()).isEqualTo(555L);

        // rule fields are pushed through an UpdateRecord on the existing entity
        final ArgumentCaptor<UpdateRecord> updateCaptor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder).recordUpdate(updateCaptor.capture(),
                eq(DelegationRuleServiceImpl.RECORD_TYPE_DELEGATION_RULE));
        final UpdateRecord update = updateCaptor.getValue();
        assertThat(update.getEntity()).isSameAs(existing);
        assertThat(update.getFields())
                .containsEntry(SDelegationRule.DELEGATE_ID_KEY, 20L)
                .containsEntry(SDelegationRule.START_DATE_KEY, 100L)
                .containsEntry(SDelegationRule.END_DATE_KEY, 200L)
                .containsEntry(SDelegationRule.LAST_UPDATED_BY_KEY, 10L)
                .containsEntry(SDelegationRule.LAST_UPDATED_AT_KEY, 170L);

        // whitelist is wholesale-replaced: delete-all by ruleId, then re-insert
        final ArgumentCaptor<DeleteAllRecord> deleteAllCaptor = ArgumentCaptor.forClass(DeleteAllRecord.class);
        verify(recorder).recordDeleteAll(deleteAllCaptor.capture());
        final DeleteAllRecord deleteAll = deleteAllCaptor.getValue();
        assertThat(deleteAll.entityClass()).isEqualTo(SDelegationRuleProcess.class);
        assertThat(deleteAll.filters()).hasSize(1);
        final FilterOption filter = deleteAll.filters().get(0);
        assertThat(filter.getPersistentClass()).isEqualTo(SDelegationRuleProcess.class);
        assertThat(filter.getFieldName()).isEqualTo(SDelegationRuleProcess.DELEGATION_RULE_ID_KEY);
        assertThat(filter.getValue()).isEqualTo(555L);

        verify(recorder).recordInsert(argThat((InsertRecord r) -> r.getEntity() instanceof SDelegationRuleProcess p
                && p.getDelegationRuleId() == 555L && "ProcessA".equals(p.getProcessName())),
                eq(DelegationRuleServiceImpl.RECORD_TYPE_DELEGATION_RULE_PROCESS));

        // no top-level rule insert on the update path
        verify(recorder, never()).recordInsert(argThat((InsertRecord r) -> r.getEntity() instanceof SDelegationRule),
                eq(DelegationRuleServiceImpl.RECORD_TYPE_DELEGATION_RULE));
    }

    @Test
    void createOrUpdateRule_throws_when_delegate_equals_delegator() throws Exception {
        //given
        final SDelegationRule rule = SDelegationRule.builder()
                .delegatorId(10L).delegateId(10L).startDate(100L).endDate(200L).build();

        //when-then
        assertThatExceptionOfType(SDelegationRuleCreationException.class)
                .isThrownBy(() -> service.createOrUpdateRule(rule, Arrays.asList("ProcessA")))
                .withMessageContaining("delegator and delegate must be different");
        verify(recorder, never()).recordInsert(any(InsertRecord.class), any());
        verify(recorder, never()).recordUpdate(any(UpdateRecord.class), any());
    }

    @Test
    void createOrUpdateRule_throws_when_startDate_not_before_endDate() throws Exception {
        //given - equal bounds → rejected (zero-duration window can never apply)
        final SDelegationRule rule = SDelegationRule.builder()
                .delegatorId(10L).delegateId(20L).startDate(150L).endDate(150L).build();

        //when-then
        assertThatExceptionOfType(SDelegationRuleCreationException.class)
                .isThrownBy(() -> service.createOrUpdateRule(rule, Arrays.asList("ProcessA")))
                .withMessageContaining("startDate must be strictly before endDate");
        verify(recorder, never()).recordInsert(any(InsertRecord.class), any());
        verify(recorder, never()).recordUpdate(any(UpdateRecord.class), any());
    }

    @Test
    void updateRule_rejects_inverted_window_when_both_bounds_set_in_descriptor() throws Exception {
        //given
        final SDelegationRule existing = SDelegationRule.builder()
                .id(1L).delegatorId(10L).delegateId(20L).startDate(100L).endDate(150L).build();
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(existing);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(SDelegationRule.START_DATE_KEY, 500L);
        descriptor.addField(SDelegationRule.END_DATE_KEY, 400L); // < new startDate → inverted

        //when-then
        assertThatThrownBy(() -> service.updateRule(1L, descriptor, null))
                .isInstanceOf(SDelegationRuleUpdateException.class)
                .hasMessageContaining("startDate")
                .hasMessageContaining("endDate");
        verify(recorder, never()).recordUpdate(any(UpdateRecord.class), any());
    }

    @Test
    void updateRule_throws_not_found_when_rule_id_missing() throws Exception {
        //given
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(null);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(SDelegationRule.DELEGATE_ID_KEY, 99L);

        //when-then
        assertThatThrownBy(() -> service.updateRule(404L, descriptor, null))
                .isInstanceOf(SDelegationRuleNotFoundException.class)
                .hasMessageContaining("404");
        verify(recorder, never()).recordUpdate(any(UpdateRecord.class), any());
    }

    @Test
    void updateRule_rejects_inverted_partial_window_when_only_start_provided() throws Exception {
        //given
        final SDelegationRule existing = SDelegationRule.builder()
                .id(1L).delegatorId(10L).delegateId(20L).startDate(100L).endDate(150L).build();
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(existing);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(SDelegationRule.START_DATE_KEY, 200L); // > existing endDate=150 → inverted

        //when-then
        assertThatThrownBy(() -> service.updateRule(1L, descriptor, null))
                .isInstanceOf(SDelegationRuleUpdateException.class)
                .hasMessageContaining("startDate")
                .hasMessageContaining("endDate");
        verify(recorder, never()).recordUpdate(any(UpdateRecord.class), any());
    }

    @Test
    void updateRule_rejects_inverted_partial_window_when_only_end_provided() throws Exception {
        //given
        final SDelegationRule existing = SDelegationRule.builder()
                .id(1L).delegatorId(10L).delegateId(20L).startDate(100L).endDate(150L).build();
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(existing);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(SDelegationRule.END_DATE_KEY, 50L); // < existing startDate=100 → inverted

        //when-then
        assertThatThrownBy(() -> service.updateRule(1L, descriptor, null))
                .isInstanceOf(SDelegationRuleUpdateException.class)
                .hasMessageContaining("startDate")
                .hasMessageContaining("endDate");
        verify(recorder, never()).recordUpdate(any(UpdateRecord.class), any());
    }

    @Test
    void updateRule_accepts_partial_window_when_resulting_range_is_valid() throws Exception {
        //given
        final SDelegationRule existing = SDelegationRule.builder()
                .id(1L).delegatorId(10L).delegateId(20L).startDate(100L).endDate(150L).build();
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(existing);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(SDelegationRule.END_DATE_KEY, 200L); // > existing startDate=100 → valid

        //when
        final SDelegationRule updated = service.updateRule(1L, descriptor, null);

        //then
        assertThat(updated).isSameAs(existing);
        verify(recorder).recordUpdate(argThat((UpdateRecord r) -> r.getEntity() == existing),
                eq(DelegationRuleServiceImpl.RECORD_TYPE_DELEGATION_RULE));
    }

    @Test
    void updateRule_rejects_when_descriptor_makes_delegate_equal_to_existing_delegator() throws Exception {
        //given
        final SDelegationRule existing = SDelegationRule.builder()
                .id(1L).delegatorId(10L).delegateId(20L).startDate(100L).endDate(150L).build();
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(existing);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(SDelegationRule.DELEGATE_ID_KEY, 10L); // == existing.delegatorId → reject

        //when-then
        assertThatThrownBy(() -> service.updateRule(1L, descriptor, null))
                .isInstanceOf(SDelegationRuleUpdateException.class)
                .hasMessageContaining("delegator");
        verify(recorder, never()).recordUpdate(any(UpdateRecord.class), any());
    }

    @Test
    void updateRule_accepts_when_descriptor_changes_delegate_to_distinct_user() throws Exception {
        //given
        final SDelegationRule existing = SDelegationRule.builder()
                .id(1L).delegatorId(10L).delegateId(20L).startDate(100L).endDate(150L).build();
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(existing);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(SDelegationRule.DELEGATE_ID_KEY, 30L); // != delegatorId → ok

        //when
        service.updateRule(1L, descriptor, null);

        //then
        verify(recorder).recordUpdate(argThat((UpdateRecord r) -> r.getEntity() == existing
                && r.getFields().get(SDelegationRule.DELEGATE_ID_KEY).equals(30L)),
                eq(DelegationRuleServiceImpl.RECORD_TYPE_DELEGATION_RULE));
    }

    @Test
    void updateRule_leaves_whitelist_untouched_when_newProcesses_is_null() throws Exception {
        //given
        final SDelegationRule existing = SDelegationRule.builder()
                .id(1L).delegatorId(10L).delegateId(20L).startDate(100L).endDate(150L).build();
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(existing);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(SDelegationRule.LAST_UPDATED_AT_KEY, 999L);

        //when
        service.updateRule(1L, descriptor, null);

        //then
        verify(recorder, never()).recordDeleteAll(any(DeleteAllRecord.class));
        verify(recorder, never()).recordInsert(any(InsertRecord.class),
                eq(DelegationRuleServiceImpl.RECORD_TYPE_DELEGATION_RULE_PROCESS));
    }

    @Test
    void updateRule_replaces_whitelist_when_newProcesses_provided() throws Exception {
        //given
        final SDelegationRule existing = SDelegationRule.builder()
                .id(1L).delegatorId(10L).delegateId(20L).startDate(100L).endDate(150L).build();
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(existing);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(SDelegationRule.LAST_UPDATED_AT_KEY, 999L);

        //when
        service.updateRule(1L, descriptor, Arrays.asList("ProcessX", "ProcessY"));

        //then
        verify(recorder).recordDeleteAll(argThat((DeleteAllRecord r) -> r.entityClass() == SDelegationRuleProcess.class
                && r.filters().size() == 1
                && r.filters().get(0).getValue().equals(1L)));
        final ArgumentCaptor<InsertRecord> whitelistInserts = ArgumentCaptor.forClass(InsertRecord.class);
        verify(recorder, times(2)).recordInsert(whitelistInserts.capture(),
                eq(DelegationRuleServiceImpl.RECORD_TYPE_DELEGATION_RULE_PROCESS));
        assertThat(whitelistInserts.getAllValues())
                .extracting(r -> (SDelegationRuleProcess) r.getEntity())
                .extracting(SDelegationRuleProcess::getDelegationRuleId, SDelegationRuleProcess::getProcessName)
                .containsExactly(
                        org.assertj.core.groups.Tuple.tuple(1L, "ProcessX"),
                        org.assertj.core.groups.Tuple.tuple(1L, "ProcessY"));
    }

    @Test
    void updateRule_passes_only_descriptor_fields_to_recorder() throws Exception {
        //given
        final SDelegationRule existing = SDelegationRule.builder()
                .id(1L).delegatorId(10L).delegateId(20L).startDate(100L).endDate(150L).build();
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(existing);
        final EntityUpdateDescriptor descriptor = new EntityUpdateDescriptor();
        descriptor.addField(SDelegationRule.DELEGATE_ID_KEY, 30L);
        descriptor.addField(SDelegationRule.LAST_UPDATED_AT_KEY, 999L);

        //when
        service.updateRule(1L, descriptor, null);

        //then
        final ArgumentCaptor<UpdateRecord> updateCaptor = ArgumentCaptor.forClass(UpdateRecord.class);
        verify(recorder).recordUpdate(updateCaptor.capture(),
                eq(DelegationRuleServiceImpl.RECORD_TYPE_DELEGATION_RULE));
        assertThat(updateCaptor.getValue().getFields())
                .containsOnlyKeys(SDelegationRule.DELEGATE_ID_KEY, SDelegationRule.LAST_UPDATED_AT_KEY);
    }

    @Test
    void deleteRule_deletes_existing_rule_via_recorder() throws Exception {
        //given
        final SDelegationRule existing = SDelegationRule.builder()
                .id(42L).delegatorId(10L).delegateId(20L).startDate(100L).endDate(150L).build();
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(existing);

        //when
        service.deleteRule(42L);

        //then
        verify(recorder).recordDelete(argThat((DeleteRecord r) -> r.getEntity() == existing),
                eq(DelegationRuleServiceImpl.RECORD_TYPE_DELEGATION_RULE));
    }

    @Test
    void deleteRule_throws_not_found_when_rule_does_not_exist() throws Exception {
        //given
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(null);

        //when-then
        assertThatThrownBy(() -> service.deleteRule(404L))
                .isInstanceOf(SDelegationRuleNotFoundException.class)
                .hasMessageContaining("404");
        verify(recorder, never()).recordDelete(any(DeleteRecord.class), any());
    }

    @Test
    void deleteRule_does_not_explicitly_delete_whitelist_rows() throws Exception {
        //given — DB ON DELETE CASCADE handles the whitelist; the service must not issue an extra delete
        final SDelegationRule existing = SDelegationRule.builder()
                .id(42L).delegatorId(10L).delegateId(20L).build();
        when(persistenceService.selectById(any(SelectByIdDescriptor.class))).thenReturn(existing);

        //when
        service.deleteRule(42L);

        //then
        verify(recorder, never()).recordDeleteAll(any(DeleteAllRecord.class));
    }

    @Test
    void getProcessNamesByRuleId_returns_process_names_in_insertion_order() throws Exception {
        //given
        when(persistenceService.selectList(argThat(this::queryTargetsProcessNamesByRuleId)))
                .thenReturn(Arrays.asList("alpha", "beta", "gamma"));

        //when
        final List<String> processes = service.getProcessNamesByRuleId(42L);

        //then
        assertThat(processes).containsExactly("alpha", "beta", "gamma");
    }

    @Test
    void getProcessNamesByRuleId_returns_empty_list_when_no_whitelist_rows_match() throws Exception {
        //given — covers both "rule has no whitelist" and "rule does not exist":
        // the persistence layer cannot distinguish them at this projection.
        when(persistenceService.selectList(argThat(this::queryTargetsProcessNamesByRuleId)))
                .thenReturn(Collections.emptyList());

        //when-then
        assertThat(service.getProcessNamesByRuleId(42L)).isEmpty();
    }

    @Test
    void getProcessNamesByRuleIds_returns_empty_map_for_empty_input() throws Exception {
        //when
        final Map<Long, List<String>> result = service.getProcessNamesByRuleIds(Collections.emptyList());

        //then
        assertThat(result).isEmpty();
        verifyNoInteractions(persistenceService);
    }

    @Test
    void getProcessNamesByRuleIds_groups_rows_by_rule_id_preserving_insertion_order() throws Exception {
        //given
        when(persistenceService.selectList(argThat(this::queryTargetsProcessNamesByRuleIds)))
                .thenReturn(Arrays.asList(rowFor(1L, "a"), rowFor(1L, "b"), rowFor(2L, "x")));

        //when
        final Map<Long, List<String>> result = service.getProcessNamesByRuleIds(Arrays.asList(1L, 2L));

        //then
        assertThat(result).containsOnlyKeys(1L, 2L);
        assertThat(result.get(1L)).containsExactly("a", "b");
        assertThat(result.get(2L)).containsExactly("x");
    }

    @Test
    void getProcessNamesByRuleIds_returns_empty_list_for_rule_with_no_whitelist_rows() throws Exception {
        //given — rows only for rule 1; rule 2 has no whitelist
        when(persistenceService.selectList(argThat(this::queryTargetsProcessNamesByRuleIds)))
                .thenReturn(Arrays.asList(rowFor(1L, "a")));

        //when
        final Map<Long, List<String>> result = service.getProcessNamesByRuleIds(Arrays.asList(1L, 2L));

        //then
        assertThat(result.get(1L)).containsExactly("a");
        assertThat(result.get(2L)).isEmpty();
    }

    @Test
    void getProcessNamesByRuleIds_issues_a_single_persistence_call() throws Exception {
        //given
        when(persistenceService.selectList(argThat(this::queryTargetsProcessNamesByRuleIds)))
                .thenReturn(Collections.emptyList());

        //when
        service.getProcessNamesByRuleIds(Arrays.asList(1L, 2L, 3L));

        //then
        verify(persistenceService, times(1)).selectList(any(SelectListDescriptor.class));
    }

    @Test
    void searchRules_delegates_to_persistence_with_unchanged_options_when_no_status_filter() throws Exception {
        //given — filters present but no STATUS among them
        final FilterOption delegatorFilter = new FilterOption(SDelegationRule.class,
                SDelegationRule.DELEGATOR_ID_KEY, 42L);
        final QueryOptions options = new QueryOptions(0, 10, null,
                Collections.singletonList(delegatorFilter), null);

        //when
        service.searchRules(options);

        //then — same reference forwarded; no defensive copy
        final ArgumentCaptor<QueryOptions> captor = ArgumentCaptor.forClass(QueryOptions.class);
        verify(persistenceService).searchEntity(eq(SDelegationRule.class), captor.capture(), isNull());
        assertThat(captor.getValue()).isSameAs(options);
    }

    @Test
    void searchRules_rewrites_active_status_to_two_date_predicates() throws Exception {
        //given
        final QueryOptions options = statusFilterOnlyOptions("active", FilterOperationType.EQUALS);

        //when
        service.searchRules(options);

        //then
        final List<FilterOption> rewritten = captureRewrittenSearchFilters();
        assertThat(rewritten)
                .extracting(FilterOption::getFieldName, FilterOption::getFilterOperationType,
                        FilterOption::getValue)
                .containsExactlyInAnyOrder(
                        org.assertj.core.groups.Tuple.tuple(SDelegationRule.START_DATE_KEY,
                                FilterOperationType.LESS_OR_EQUALS, FIXED_NOW),
                        org.assertj.core.groups.Tuple.tuple(SDelegationRule.END_DATE_KEY,
                                FilterOperationType.GREATER_OR_EQUALS, FIXED_NOW));
    }

    @Test
    void searchRules_rewrites_scheduled_status_to_start_date_after_now() throws Exception {
        //given
        final QueryOptions options = statusFilterOnlyOptions("scheduled", FilterOperationType.EQUALS);

        //when
        service.searchRules(options);

        //then
        final List<FilterOption> rewritten = captureRewrittenSearchFilters();
        assertThat(rewritten)
                .extracting(FilterOption::getFieldName, FilterOption::getFilterOperationType,
                        FilterOption::getValue)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(SDelegationRule.START_DATE_KEY,
                        FilterOperationType.GREATER, FIXED_NOW));
    }

    @Test
    void searchRules_rewrites_expired_status_to_end_date_before_now() throws Exception {
        //given
        final QueryOptions options = statusFilterOnlyOptions("expired", FilterOperationType.EQUALS);

        //when
        service.searchRules(options);

        //then
        final List<FilterOption> rewritten = captureRewrittenSearchFilters();
        assertThat(rewritten)
                .extracting(FilterOption::getFieldName, FilterOption::getFilterOperationType,
                        FilterOption::getValue)
                .containsExactly(org.assertj.core.groups.Tuple.tuple(SDelegationRule.END_DATE_KEY,
                        FilterOperationType.LESS, FIXED_NOW));
    }

    @Test
    void searchRules_rejects_unknown_status_value() {
        //given
        final QueryOptions options = statusFilterOnlyOptions("completed", FilterOperationType.EQUALS);

        //when-then
        assertThatThrownBy(() -> service.searchRules(options))
                .isInstanceOf(SBonitaReadException.class)
                .hasMessageContaining("completed")
                .hasMessageContaining("scheduled")
                .hasMessageContaining("active")
                .hasMessageContaining("expired");
    }

    @Test
    void searchRules_rejects_status_filter_with_non_equals_operation() {
        //given
        final QueryOptions options = statusFilterOnlyOptions("active", FilterOperationType.DIFFERENT);

        //when-then
        assertThatThrownBy(() -> service.searchRules(options))
                .isInstanceOf(SBonitaReadException.class)
                .hasMessageContaining("EQUALS");
    }

    @Test
    void searchRules_rejects_duplicate_status_filter() {
        //given
        final QueryOptions options = new QueryOptions(0, 10, null,
                Arrays.asList(
                        new FilterOption(SDelegationRule.class, DelegationRuleFilterKeys.STATUS, "active",
                                FilterOperationType.EQUALS),
                        new FilterOption(SDelegationRule.class, DelegationRuleFilterKeys.STATUS,
                                "expired",
                                FilterOperationType.EQUALS)),
                null);

        //when-then
        assertThatThrownBy(() -> service.searchRules(options))
                .isInstanceOf(SBonitaReadException.class)
                .hasMessageContaining("at most once");
    }

    @Test
    void searchRules_normalises_status_value_case_so_DelegationStatus_name_form_works() throws Exception {
        //given — DelegationStatus.ACTIVE.toString() returns "ACTIVE" (Enum.name()). The rewrite
        // lowercases so Java callers passing the enum directly converge with REST callers
        // passing the lowercase JSON form.
        final QueryOptions options = statusFilterOnlyOptions("ACTIVE", FilterOperationType.EQUALS);

        //when
        service.searchRules(options);

        //then
        assertThat(captureRewrittenSearchFilters())
                .extracting(FilterOption::getFieldName)
                .containsExactlyInAnyOrder(SDelegationRule.START_DATE_KEY, SDelegationRule.END_DATE_KEY);
    }

    @Test
    void searchRules_rejects_status_filter_with_null_value() {
        //given
        final QueryOptions options = statusFilterOnlyOptions(null, FilterOperationType.EQUALS);

        //when-then
        assertThatThrownBy(() -> service.searchRules(options))
                .isInstanceOf(SBonitaReadException.class)
                .hasMessageContaining("STATUS")
                .hasMessageContaining("null");
    }

    @Test
    void getNumberOfRules_applies_same_status_rewrite() throws Exception {
        //given
        final QueryOptions options = statusFilterOnlyOptions("expired", FilterOperationType.EQUALS);
        when(persistenceService.getNumberOfEntities(eq(SDelegationRule.class), any(QueryOptions.class),
                isNull())).thenReturn(7L);

        //when
        final long count = service.getNumberOfRules(options);

        //then
        assertThat(count).isEqualTo(7L);
        final ArgumentCaptor<QueryOptions> captor = ArgumentCaptor.forClass(QueryOptions.class);
        verify(persistenceService).getNumberOfEntities(eq(SDelegationRule.class), captor.capture(), isNull());
        assertThat(captor.getValue().getFilters())
                .extracting(FilterOption::getFieldName)
                .doesNotContain(DelegationRuleFilterKeys.STATUS)
                .contains(SDelegationRule.END_DATE_KEY);
    }

    @Test
    void searchRules_forwards_pagination_order_and_other_filters_through_status_rewrite() throws Exception {
        //given
        final OrderByOption order = new OrderByOption(SDelegationRule.class, SDelegationRule.ID_KEY,
                OrderByType.DESC);
        final FilterOption otherFilter = new FilterOption(SDelegationRule.class,
                SDelegationRule.DELEGATOR_ID_KEY, 42L);
        final QueryOptions options = new QueryOptions(5, 20, Collections.singletonList(order),
                Arrays.asList(otherFilter,
                        new FilterOption(SDelegationRule.class, DelegationRuleFilterKeys.STATUS,
                                "scheduled",
                                FilterOperationType.EQUALS)),
                null);

        //when
        service.searchRules(options);

        //then
        final ArgumentCaptor<QueryOptions> captor = ArgumentCaptor.forClass(QueryOptions.class);
        verify(persistenceService).searchEntity(eq(SDelegationRule.class), captor.capture(), isNull());
        final QueryOptions rewritten = captor.getValue();
        assertThat(rewritten.getFromIndex()).isEqualTo(5);
        assertThat(rewritten.getNumberOfResults()).isEqualTo(20);
        assertThat(rewritten.getOrderByOptions()).containsExactly(order);
        assertThat(rewritten.getFilters())
                .extracting(FilterOption::getFieldName)
                .doesNotContain(DelegationRuleFilterKeys.STATUS)
                .contains(SDelegationRule.DELEGATOR_ID_KEY, SDelegationRule.START_DATE_KEY);
    }

    private static QueryOptions statusFilterOnlyOptions(final String value, final FilterOperationType op) {
        return new QueryOptions(0, 10, null,
                Collections.singletonList(new FilterOption(SDelegationRule.class,
                        DelegationRuleFilterKeys.STATUS, value, op)),
                null);
    }

    private List<FilterOption> captureRewrittenSearchFilters() throws SBonitaReadException {
        final ArgumentCaptor<QueryOptions> captor = ArgumentCaptor.forClass(QueryOptions.class);
        verify(persistenceService).searchEntity(eq(SDelegationRule.class), captor.capture(), isNull());
        return captor.getValue().getFilters();
    }

    private static Map<String, Object> rowFor(final Long ruleId, final String processName) {
        final Map<String, Object> row = new HashMap<>();
        row.put("ruleId", ruleId);
        row.put("processName", processName);
        return row;
    }

    @Test
    void getActiveDelegationRulesForDelegate_returns_rules_returned_by_persistence() throws Exception {
        //given — date-window filtering is enforced DB-side by :now; the unit test asserts forwarding only
        final SDelegationRule active = SDelegationRule.builder()
                .id(1L).delegatorId(10L).delegateId(20L)
                .startDate(FIXED_NOW - 1000L).endDate(FIXED_NOW + 1000L).build();
        when(persistenceService.selectList(argThat(this::queryTargetsActiveRulesForDelegate20)))
                .thenReturn(Collections.singletonList(active));

        //when
        final List<SDelegationRule> actual = service.getActiveDelegationRulesForDelegate(20L);

        //then
        assertThat(actual).containsExactly(active);
    }

    @Test
    void getActiveDelegationRulesForDelegate_returns_empty_list_when_persistence_returns_nothing() throws Exception {
        //given
        when(persistenceService.selectList(argThat(this::queryTargetsActiveRulesForDelegate20)))
                .thenReturn(Collections.emptyList());

        //when
        final List<SDelegationRule> actual = service.getActiveDelegationRulesForDelegate(20L);

        //then
        assertThat(actual).isEmpty();
    }

    @Test
    void getActiveDelegationRulesForDelegate_builds_descriptor_with_delegateId_and_now_parameters()
            throws Exception {
        //given
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final ArgumentCaptor<SelectListDescriptor<SDelegationRule>> captor = ArgumentCaptor
                .forClass((Class) SelectListDescriptor.class);
        when(persistenceService.selectList(captor.capture())).thenReturn(Collections.emptyList());

        //when
        service.getActiveDelegationRulesForDelegate(20L);

        //then
        final SelectListDescriptor<SDelegationRule> descriptor = captor.getValue();
        assertThat(descriptor.getQueryName()).isEqualTo(DelegationRuleServiceImpl.QUERY_ACTIVE_RULES_FOR_DELEGATE);
        assertThat(descriptor.getInputParameters())
                .containsEntry("delegateId", 20L)
                .containsEntry("now", FIXED_NOW);
        assertThat(descriptor.getReturnType()).isEqualTo(SDelegationRule.class);
    }

    @Test
    void isActiveDelegate_returns_false_when_persistence_count_is_zero() throws Exception {
        //given — date-window, whitelist match, and root-process resolution are enforced DB-side
        //       by the HQL; this unit test asserts only the count → boolean translation.
        //       The EXISTS pre-check is stubbed positive so we reach the multi-join.
        when(persistenceService.selectOne(argThat(this::queryTargetsExistsActiveRuleFor20))).thenReturn(1L);
        when(persistenceService.selectOne(argThat(this::queryTargetsIsActiveDelegateFor20And100))).thenReturn(0L);

        //when
        final boolean actual = service.isActiveDelegate(20L, 100L);

        //then
        assertThat(actual).isFalse();
    }

    @Test
    void isActiveDelegate_returns_true_when_persistence_count_is_positive() throws Exception {
        //given
        when(persistenceService.selectOne(argThat(this::queryTargetsExistsActiveRuleFor20))).thenReturn(1L);
        when(persistenceService.selectOne(argThat(this::queryTargetsIsActiveDelegateFor20And100))).thenReturn(1L);

        //when
        final boolean actual = service.isActiveDelegate(20L, 100L);

        //then
        assertThat(actual).isTrue();
    }

    @Test
    void isActiveDelegate_returns_false_when_persistence_returns_null() throws Exception {
        //given — selectOne can return null on certain DB/dialect combinations; the impl
        //guards with `count != null && count > 0L` to keep the boolean translation safe.
        when(persistenceService.selectOne(argThat(this::queryTargetsExistsActiveRuleFor20))).thenReturn(1L);
        when(persistenceService.selectOne(argThat(this::queryTargetsIsActiveDelegateFor20And100))).thenReturn(null);

        //when
        final boolean actual = service.isActiveDelegate(20L, 100L);

        //then
        assertThat(actual).isFalse();
    }

    @Test
    void isActiveDelegate_builds_join_descriptor_with_delegateId_taskId_and_now_parameters() throws Exception {
        //given — capture both selectOne calls in order: first the EXISTS pre-check (1L so we
        //reach the join), then the multi-join we want to assert on.
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final ArgumentCaptor<SelectOneDescriptor<Long>> captor = ArgumentCaptor
                .forClass((Class) SelectOneDescriptor.class);
        when(persistenceService.selectOne(captor.capture())).thenReturn(1L, 0L);

        //when
        service.isActiveDelegate(20L, 100L);

        //then
        final SelectOneDescriptor<Long> joinDescriptor = captor.getAllValues().stream()
                .filter(d -> DelegationRuleServiceImpl.QUERY_IS_ACTIVE_DELEGATE_FOR_TASK.equals(d.getQueryName()))
                .findFirst()
                .orElseThrow();
        assertThat(joinDescriptor.getInputParameters())
                .containsEntry("delegateId", 20L)
                .containsEntry("taskId", 100L)
                .containsEntry("now", FIXED_NOW);
        assertThat(joinDescriptor.getReturnType()).isEqualTo(Long.class);
    }

    @Test
    void isActiveDelegate_fast_exits_to_false_without_running_join_when_delegate_has_no_active_rule()
            throws Exception {
        //given — docstring promise: "non-delegate callers must not pay the cost of the full check"
        when(persistenceService.selectOne(argThat(this::queryTargetsExistsActiveRuleFor20))).thenReturn(0L);

        //when
        final boolean actual = service.isActiveDelegate(20L, 100L);

        //then
        assertThat(actual).isFalse();
        verify(persistenceService, never()).selectOne(argThat(this::queryTargetsIsActiveDelegateFor20And100));
    }

    @Test
    void isActiveDelegate_fast_exits_to_false_when_exists_pre_check_returns_null() throws Exception {
        //given — same null-safety guard as the multi-join branch, applied to the fast-exit count
        when(persistenceService.selectOne(argThat(this::queryTargetsExistsActiveRuleFor20))).thenReturn(null);

        //when
        final boolean actual = service.isActiveDelegate(20L, 100L);

        //then
        assertThat(actual).isFalse();
        verify(persistenceService, never()).selectOne(argThat(this::queryTargetsIsActiveDelegateFor20And100));
    }

    @Test
    void isActiveDelegate_builds_exists_descriptor_with_delegateId_and_now_parameters() throws Exception {
        //given — fast-exit returns 0 so only the EXISTS descriptor reaches persistenceService
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final ArgumentCaptor<SelectOneDescriptor<Long>> captor = ArgumentCaptor
                .forClass((Class) SelectOneDescriptor.class);
        when(persistenceService.selectOne(captor.capture())).thenReturn(0L);

        //when
        service.isActiveDelegate(20L, 100L);

        //then
        final SelectOneDescriptor<Long> descriptor = captor.getValue();
        assertThat(descriptor.getQueryName())
                .isEqualTo(DelegationRuleServiceImpl.QUERY_EXISTS_ACTIVE_RULE_FOR_DELEGATE);
        assertThat(descriptor.getInputParameters())
                .containsEntry("delegateId", 20L)
                .containsEntry("now", FIXED_NOW)
                .doesNotContainKey("taskId");
        assertThat(descriptor.getReturnType()).isEqualTo(Long.class);
    }

    @Test
    void isActiveDelegateForProcessInstance_returns_false_when_persistence_count_is_zero() throws Exception {
        //given — case-scoped counterpart of isActiveDelegate; date-window, whitelist match, and
        //       root-process resolution (via logicalGroup2) are enforced DB-side by the HQL.
        //       The EXISTS pre-check is stubbed positive so we reach the multi-join.
        when(persistenceService.selectOne(argThat(this::queryTargetsExistsActiveRuleFor20))).thenReturn(1L);
        when(persistenceService.selectOne(argThat(this::queryTargetsIsActiveDelegateForProcessInstance20And200)))
                .thenReturn(0L);

        //when
        final boolean actual = service.isActiveDelegateForProcessInstance(20L, 200L);

        //then
        assertThat(actual).isFalse();
    }

    @Test
    void isActiveDelegateForProcessInstance_returns_true_when_persistence_count_is_positive() throws Exception {
        //given
        when(persistenceService.selectOne(argThat(this::queryTargetsExistsActiveRuleFor20))).thenReturn(1L);
        when(persistenceService.selectOne(argThat(this::queryTargetsIsActiveDelegateForProcessInstance20And200)))
                .thenReturn(1L);

        //when
        final boolean actual = service.isActiveDelegateForProcessInstance(20L, 200L);

        //then
        assertThat(actual).isTrue();
    }

    @Test
    void isActiveDelegateForProcessInstance_returns_false_when_persistence_returns_null() throws Exception {
        //given — selectOne can return null on certain DB/dialect combinations; the impl
        //guards with `count != null && count > 0L` to keep the boolean translation safe.
        when(persistenceService.selectOne(argThat(this::queryTargetsExistsActiveRuleFor20))).thenReturn(1L);
        when(persistenceService.selectOne(argThat(this::queryTargetsIsActiveDelegateForProcessInstance20And200)))
                .thenReturn(null);

        //when
        final boolean actual = service.isActiveDelegateForProcessInstance(20L, 200L);

        //then
        assertThat(actual).isFalse();
    }

    @Test
    void isActiveDelegateForProcessInstance_builds_join_descriptor_with_delegateId_processInstanceId_and_now_parameters()
            throws Exception {
        //given — capture both selectOne calls in order: first the EXISTS pre-check (1L so we
        //reach the join), then the multi-join we want to assert on.
        @SuppressWarnings({ "unchecked", "rawtypes" })
        final ArgumentCaptor<SelectOneDescriptor<Long>> captor = ArgumentCaptor
                .forClass((Class) SelectOneDescriptor.class);
        when(persistenceService.selectOne(captor.capture())).thenReturn(1L, 0L);

        //when
        service.isActiveDelegateForProcessInstance(20L, 200L);

        //then
        final SelectOneDescriptor<Long> joinDescriptor = captor.getAllValues().stream()
                .filter(d -> DelegationRuleServiceImpl.QUERY_IS_ACTIVE_DELEGATE_FOR_PROCESS_INSTANCE
                        .equals(d.getQueryName()))
                .findFirst()
                .orElseThrow();
        assertThat(joinDescriptor.getInputParameters())
                .containsEntry("delegateId", 20L)
                .containsEntry("processInstanceId", 200L)
                .containsEntry("now", FIXED_NOW);
        assertThat(joinDescriptor.getReturnType()).isEqualTo(Long.class);
    }

    @Test
    void isActiveDelegateForProcessInstance_fast_exits_to_false_without_running_join_when_delegate_has_no_active_rule()
            throws Exception {
        //given — docstring promise: "non-delegate callers must not pay the cost of the full check"
        when(persistenceService.selectOne(argThat(this::queryTargetsExistsActiveRuleFor20))).thenReturn(0L);

        //when
        final boolean actual = service.isActiveDelegateForProcessInstance(20L, 200L);

        //then
        assertThat(actual).isFalse();
        verify(persistenceService, never())
                .selectOne(argThat(this::queryTargetsIsActiveDelegateForProcessInstance20And200));
    }

    @Test
    void isActiveDelegateForProcessInstance_fast_exits_to_false_when_exists_pre_check_returns_null() throws Exception {
        //given — same null-safety guard as the multi-join branch, applied to the fast-exit count
        when(persistenceService.selectOne(argThat(this::queryTargetsExistsActiveRuleFor20))).thenReturn(null);

        //when
        final boolean actual = service.isActiveDelegateForProcessInstance(20L, 200L);

        //then
        assertThat(actual).isFalse();
        verify(persistenceService, never())
                .selectOne(argThat(this::queryTargetsIsActiveDelegateForProcessInstance20And200));
    }

    @Test
    void searchDelegatedTasks_forwards_queryOptions_with_now_and_returns_persistence_result() throws Exception {
        //given
        final QueryOptions options = new QueryOptions(5, 20);
        final SDelegatedHumanTask task = SDelegatedHumanTask.builder().delegatorId(10L).delegateId(20L).build();
        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        when(persistenceService.searchEntity(eq(SDelegatedHumanTask.class), eq(options), paramsCaptor.capture()))
                .thenReturn(Collections.singletonList(task));

        //when
        final List<SDelegatedHumanTask> actual = service.searchDelegatedTasks(options);

        //then
        assertThat(actual).containsExactly(task);
        assertThat(paramsCaptor.getValue()).containsEntry("now", FIXED_NOW);
    }

    @Test
    void searchDelegatedTasks_returns_empty_list_when_persistence_returns_nothing() throws Exception {
        //given
        final QueryOptions options = new QueryOptions(0, 20);
        when(persistenceService.searchEntity(eq(SDelegatedHumanTask.class), eq(options), anyMap()))
                .thenReturn(Collections.emptyList());

        //when
        final List<SDelegatedHumanTask> actual = service.searchDelegatedTasks(options);

        //then
        assertThat(actual).isEmpty();
    }

    @Test
    void getNumberOfDelegatedTasks_forwards_queryOptions_with_now_and_returns_persistence_count() throws Exception {
        //given
        final QueryOptions options = new QueryOptions(0, 20);
        @SuppressWarnings("unchecked")
        final ArgumentCaptor<Map<String, Object>> paramsCaptor = ArgumentCaptor.forClass(Map.class);
        when(persistenceService.getNumberOfEntities(eq(SDelegatedHumanTask.class), eq(options),
                paramsCaptor.capture())).thenReturn(7L);

        //when
        final long actual = service.getNumberOfDelegatedTasks(options);

        //then
        assertThat(actual).isEqualTo(7L);
        assertThat(paramsCaptor.getValue()).containsEntry("now", FIXED_NOW);
    }

    private boolean queryTargetsRuleByDelegatorId(final SelectOneDescriptor<?> descriptor) {
        return descriptor != null
                && DelegationRuleServiceImpl.QUERY_RULE_BY_DELEGATOR_ID.equals(descriptor.getQueryName())
                && descriptor.getReturnType().equals(SDelegationRule.class);
    }

    private boolean queryTargetsProcessNamesByRuleId(final SelectListDescriptor<?> descriptor) {
        return descriptor != null
                && DelegationRuleServiceImpl.QUERY_PROCESS_NAMES_BY_RULE_ID.equals(descriptor.getQueryName());
    }

    private boolean queryTargetsProcessNamesByRuleIds(final SelectListDescriptor<?> descriptor) {
        return descriptor != null
                && DelegationRuleServiceImpl.QUERY_PROCESS_NAMES_BY_RULE_IDS.equals(descriptor.getQueryName());
    }

    private boolean queryTargetsActiveRulesForDelegate20(final SelectListDescriptor<?> descriptor) {
        return descriptor != null
                && DelegationRuleServiceImpl.QUERY_ACTIVE_RULES_FOR_DELEGATE.equals(descriptor.getQueryName())
                && descriptor.getReturnType().equals(SDelegationRule.class)
                && Long.valueOf(20L).equals(descriptor.getInputParameters().get("delegateId"))
                && Long.valueOf(FIXED_NOW).equals(descriptor.getInputParameters().get("now"));
    }

    private boolean queryTargetsIsActiveDelegateFor20And100(final SelectOneDescriptor<?> descriptor) {
        return descriptor != null
                && DelegationRuleServiceImpl.QUERY_IS_ACTIVE_DELEGATE_FOR_TASK.equals(descriptor.getQueryName())
                && descriptor.getReturnType().equals(Long.class)
                && Long.valueOf(20L).equals(descriptor.getInputParameters().get("delegateId"))
                && Long.valueOf(100L).equals(descriptor.getInputParameters().get("taskId"))
                && Long.valueOf(FIXED_NOW).equals(descriptor.getInputParameters().get("now"));
    }

    private boolean queryTargetsExistsActiveRuleFor20(final SelectOneDescriptor<?> descriptor) {
        return descriptor != null
                && DelegationRuleServiceImpl.QUERY_EXISTS_ACTIVE_RULE_FOR_DELEGATE.equals(descriptor.getQueryName())
                && descriptor.getReturnType().equals(Long.class)
                && Long.valueOf(20L).equals(descriptor.getInputParameters().get("delegateId"))
                && Long.valueOf(FIXED_NOW).equals(descriptor.getInputParameters().get("now"));
    }

    private boolean queryTargetsIsActiveDelegateForProcessInstance20And200(final SelectOneDescriptor<?> descriptor) {
        return descriptor != null
                && DelegationRuleServiceImpl.QUERY_IS_ACTIVE_DELEGATE_FOR_PROCESS_INSTANCE
                        .equals(descriptor.getQueryName())
                && descriptor.getReturnType().equals(Long.class)
                && Long.valueOf(20L).equals(descriptor.getInputParameters().get("delegateId"))
                && Long.valueOf(200L).equals(descriptor.getInputParameters().get("processInstanceId"))
                && Long.valueOf(FIXED_NOW).equals(descriptor.getInputParameters().get("now"));
    }
}
