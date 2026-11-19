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
package org.bonitasoft.engine.business.data;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.bonitasoft.engine.CommonAPIIT;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import org.bonitasoft.engine.bpm.process.ProcessInstance;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.business.data.model.SDataRetentionBdmTracking;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.operation.LeftOperandBuilder;
import org.bonitasoft.engine.operation.OperatorType;
import org.bonitasoft.engine.service.ServiceAccessor;
import org.bonitasoft.engine.service.ServiceAccessorSingleton;
import org.bonitasoft.engine.transaction.UserTransactionService;
import org.junit.Before;
import org.junit.Test;

/**
 * Integration test verifying that a tracking record is inserted into
 * {@code data_retention_bdm_tracking} when a BDM object is created via process execution.
 */
public class DataRetentionBdmTrackingIT extends CommonAPIIT {

    private static final String INVOICE_QUALIFIED_NAME = "com.company.model.Invoice";
    private static final String BIZ_INVOICE = "bizInvoice";

    private UserTransactionService userTransactionService;
    private DataRetentionBdmTrackingRepository trackingRepository;

    @Before
    public void setUp() throws Exception {
        ServiceAccessor serviceAccessor = ServiceAccessorSingleton.getInstance();
        userTransactionService = serviceAccessor.getUserTransactionService();
        trackingRepository = serviceAccessor.lookup(DataRetentionBdmTrackingRepository.class);

        loginWithTechnicalUser();
        createUser("testUser", "bpm");

        installBusinessDataModel(buildBom());
    }

    // No @After needed: CommonAPIIT.clean() handles process definitions, users, BDM, trackings, and logout

    @Test
    public void should_insert_tracking_record_when_bdm_object_is_created_via_process() throws Exception {
        // given — a process that creates an Invoice BDM object
        var groovyScript = "import " + INVOICE_QUALIFIED_NAME
                + "; Invoice invoice = new Invoice(); invoice.reference = 'INV-001'; return invoice;";
        var initExpression = new ExpressionBuilder().createGroovyScriptExpression(
                "createInvoice", groovyScript, INVOICE_QUALIFIED_NAME);

        var builder = new ProcessDefinitionBuilder().createNewInstance("TrackingTestProcess", "1.0");
        builder.addActor("actor");
        builder.addBusinessData(BIZ_INVOICE, INVOICE_QUALIFIED_NAME, null);
        builder.addAutomaticTask("createInvoice")
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand(BIZ_INVOICE),
                        OperatorType.ASSIGNMENT, null, null, initExpression);
        builder.addUserTask("wait", "actor");
        builder.addTransition("createInvoice", "wait");

        var definition = deployAndEnableProcessWithActor(builder.done(), "actor",
                getIdentityAPI().getUserByUserName("testUser"));

        // when — start the process (BDM object is created in the automatic task)
        ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        waitForUserTaskAndGetIt(processInstance, "wait");

        // then — verify a tracking record was inserted via the repository
        List<SDataRetentionBdmTracking> trackingRecords = userTransactionService.executeInTransaction(
                () -> trackingRepository.getByClassname(INVOICE_QUALIFIED_NAME));

        assertThat(trackingRecords).hasSize(1);
        SDataRetentionBdmTracking tracking = trackingRecords.get(0);
        assertThat(tracking.getDataId()).isGreaterThan(0);
        assertThat(tracking.getDataClassname()).isEqualTo(INVOICE_QUALIFIED_NAME);
        assertThat(tracking.getCreatedAt()).isPositive();
        assertThat(tracking.getLastModifiedAt()).isEqualTo(tracking.getCreatedAt());
    }

    @Test
    public void should_not_insert_duplicate_tracking_on_update() throws Exception {
        // given — a process that creates then updates an Invoice BDM object
        var createScript = "import " + INVOICE_QUALIFIED_NAME
                + "; Invoice invoice = new Invoice(); invoice.reference = 'INV-002'; return invoice;";
        var createExpression = new ExpressionBuilder().createGroovyScriptExpression(
                "createInvoice", createScript, INVOICE_QUALIFIED_NAME);

        var builder = new ProcessDefinitionBuilder().createNewInstance("TrackingUpdateProcess", "1.0");
        builder.addActor("actor");
        builder.addBusinessData(BIZ_INVOICE, INVOICE_QUALIFIED_NAME, null);

        // step 1: create the BDM object
        builder.addAutomaticTask("createInvoice")
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand(BIZ_INVOICE),
                        OperatorType.ASSIGNMENT, null, null, createExpression);

        // step 2: update the same BDM object (set a new reference)
        var updateScript = BIZ_INVOICE + ".reference = 'INV-002-UPDATED'; return " + BIZ_INVOICE + ";";
        var updateExpression = new ExpressionBuilder().createGroovyScriptExpression(
                "updateInvoice", updateScript, INVOICE_QUALIFIED_NAME,
                new ExpressionBuilder().createBusinessDataExpression(BIZ_INVOICE, INVOICE_QUALIFIED_NAME));
        builder.addAutomaticTask("updateInvoice")
                .addOperation(new LeftOperandBuilder().createBusinessDataLeftOperand(BIZ_INVOICE),
                        OperatorType.ASSIGNMENT, null, null, updateExpression);

        builder.addUserTask("wait", "actor");
        builder.addTransition("createInvoice", "updateInvoice");
        builder.addTransition("updateInvoice", "wait");

        var definition = deployAndEnableProcessWithActor(builder.done(), "actor",
                getIdentityAPI().getUserByUserName("testUser"));

        // when
        ProcessInstance processInstance = getProcessAPI().startProcess(definition.getId());
        waitForUserTaskAndGetIt(processInstance, "wait");

        // then — still only one tracking record (update does not create a second one)
        List<SDataRetentionBdmTracking> trackingRecords = userTransactionService.executeInTransaction(
                () -> trackingRepository.getByClassname(INVOICE_QUALIFIED_NAME));

        assertThat(trackingRecords).hasSize(1);
    }

    private static BusinessObjectModel buildBom() {
        var reference = new SimpleField();
        reference.setName("reference");
        reference.setType(FieldType.STRING);

        var invoiceBO = new BusinessObject();
        invoiceBO.setQualifiedName(INVOICE_QUALIFIED_NAME);
        invoiceBO.addField(reference);

        var bom = new BusinessObjectModel();
        bom.addBusinessObject(invoiceBO);
        return bom;
    }
}
