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
package org.bonitasoft.engine.command.helper.designer;

import static org.bonitasoft.engine.command.helper.designer.Transition.fails;
import static org.bonitasoft.engine.command.helper.designer.Transition.meet;
import static org.junit.Assert.assertEquals;

import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.junit.Test;

/**
 * Created by Vincent Elcrin
 * Date: 16/12/13
 * Time: 18:44
 */
public class SimpleProcessDesignerTest {

    ProcessDefinitionBuilder builder = createProcessDefinitionBuilder();

    @Test
    public void should_be_able_to_design_a_linear_process() throws Exception {
        DesignProcessDefinition design = new SimpleProcessDesigner(builder)
                .startWith(new StartEvent("A"))
                .then(new UserTask("B"))
                .then(new UserTask("C"))
                .then(new EndEvent("D"))
                .done();

        assertEquals("[B, C]", DesignerTestUtils.getActivities(design));
        assertEquals("[A_->_B, B_->_C, C_->_D]", DesignerTestUtils.getTransactions(design));
    }

    @Test
    public void should_be_able_to_design_a_process_with_a_gate() throws Exception {
        DesignProcessDefinition design = new SimpleProcessDesigner(builder)
                .startWith(new StartEvent("A"))
                .then(new UserTask("B"))
                .then(new Gateway("C", GatewayType.EXCLUSIVE))
                .then(new UserTask("D"))
                .then(new EndEvent("E"))
                .done();

        assertEquals("[B, D]", DesignerTestUtils.getActivities(design));
        assertEquals("[C]", DesignerTestUtils.getGateways(design));
        assertEquals("[A_->_B, B_->_C, C_->_D, D_->_E]", DesignerTestUtils.getTransactions(design));
    }

    @Test
    public void should_be_able_to_design_a_process_with_parallel_activities() throws Exception {
        DesignProcessDefinition design = new SimpleProcessDesigner(builder)
                .startWith(new StartEvent("A"))
                .then(new UserTask("B1"), new UserTask("B2"))
                .then(new EndEvent("C"))
                .done();

        assertEquals("[B1, B2]", DesignerTestUtils.getActivities(design));
        assertEquals("[A_->_B1, A_->_B2, B1_->_C, B2_->_C]", DesignerTestUtils.getTransactions(design));
    }

    @Test
    public void should_be_able_to_design_a_process_multiple_incoming_conditions() throws Exception {
        Expression condition = new ExpressionBuilder().createConstantBooleanExpression(true);
        DesignProcessDefinition design = new SimpleProcessDesigner(builder)
                .startWith(new StartEvent("A"))
                .then(new UserTask("B"))
                .then(new UserTask("C1"), new UserTask("C2"))
                .then(new Gateway("D", GatewayType.EXCLUSIVE)
                        .when("C1", fails())
                        .when("C2", meet(condition)))
                .then(new EndEvent("F"))
                .done();

        assertEquals("[B, C1 (C1_->_D), C2]", DesignerTestUtils.getActivities(design));
        assertEquals("[D]", DesignerTestUtils.getGateways(design));
        assertEquals("[A_->_B, B_->_C1, B_->_C2, C1_->_D, C2_->_D (true), D_->_F]", DesignerTestUtils.getTransactions(design));
    }

    @Test
    public void should_be_able_to_design_a_process_with_multiple_outgoing_conditions() throws Exception {
        Expression condition = new ExpressionBuilder().createConstantBooleanExpression(true);
        DesignProcessDefinition design = new SimpleProcessDesigner(builder)
                .startWith(new StartEvent("A"))
                .then(new UserTask("B"))
                .then(new Gateway("C", GatewayType.EXCLUSIVE))
                .then(
                        new UserTask("D1").when("C", fails()),
                        new UserTask("D2").when("C", meet(condition)))
                .then(new EndEvent("E"))
                .done();

        assertEquals("[B, D1, D2]", DesignerTestUtils.getActivities(design));
        assertEquals("[C (C_->_D1)]", DesignerTestUtils.getGateways(design));
        assertEquals("[A_->_B, B_->_C, C_->_D1, C_->_D2 (true), D1_->_E, D2_->_E]", DesignerTestUtils.getTransactions(design));
    }

    @Test
    public void should_be_able_to_design_a_process_with_complex_branch() throws Exception {
        DesignProcessDefinition design = new SimpleProcessDesigner(builder)
                .startWith(new StartEvent("A"))
                .then(new UserTask("B"))
                .then(new Gateway("C", GatewayType.PARALLEL))
                .then(
                        new Branch().start(new UserTask("D1")).then(new UserTask("D2")),
                        new UserTask("E"))
                .then(new Gateway("F", GatewayType.PARALLEL))
                .then(new EndEvent("G"))
                .done();

        assertEquals("[B, D1, D2, E]", DesignerTestUtils.getActivities(design));
        assertEquals("[C, F]", DesignerTestUtils.getGateways(design));
        assertEquals("[A_->_B, B_->_C, C_->_D1, C_->_E, D1_->_D2, D2_->_F, E_->_F, F_->_G]", DesignerTestUtils.getTransactions(design));
    }

    @Test
    public void should_be_able_to_design_a_process_with_complex_() throws Exception {
        DesignProcessDefinition design = new SimpleProcessDesigner(builder)
                .startWith(new StartEvent("A"))
                .then(
                        new UserTask("B"),
                        new Branch()
                                .start(new Gateway("C", GatewayType.PARALLEL))
                                .then(new UserTask("D"), new UserTask("E"))
                                .then(new Gateway("F", GatewayType.PARALLEL)))
                .then(new Gateway("G", GatewayType.PARALLEL))
                .then(new UserTask("H"))
                .then(new EndEvent("I"))
                .done();

        assertEquals("[A_->_B, A_->_C, B_->_G, C_->_D, C_->_E, D_->_F, E_->_F, F_->_G, G_->_H, H_->_I]", DesignerTestUtils.getTransactions(design));
    }

    private ProcessDefinitionBuilder createProcessDefinitionBuilder() {
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("Designed by designer", "1.0");
        return builder;
    }
}
