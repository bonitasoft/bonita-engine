package org.bonitasoft.engine.repair.helper.design;

import org.bonitasoft.engine.bpm.flownode.ActivityDefinition;
import org.bonitasoft.engine.bpm.flownode.GatewayDefinition;
import org.bonitasoft.engine.bpm.flownode.GatewayType;
import org.bonitasoft.engine.bpm.flownode.TransitionDefinition;
import org.bonitasoft.engine.bpm.process.DesignProcessDefinition;
import org.bonitasoft.engine.bpm.process.impl.ProcessDefinitionBuilder;
import org.bonitasoft.engine.expression.Expression;
import org.bonitasoft.engine.expression.ExpressionBuilder;
import org.bonitasoft.engine.repair.helper.designer.*;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Created by Vincent Elcrin
 * Date: 16/12/13
 * Time: 18:44
 */
public class SimpleProcessDesignerTest {

    ProcessDefinitionBuilder builder = getProcessDefinitionBuilder();

    @Test
    public void should_be_able_to_design_a_linear_process() throws Exception {
        DesignProcessDefinition design = new SimpleProcessDesigner(builder)
                .startWith(new StartEvent("A"))
                .then(new UserTask("B"))
                .then(new UserTask("C"))
                .then(new EndEvent("D"))
                .done();

        assertEquals("[B, C]", getActivities(design));
        assertEquals("[A_->_B, B_->_C, C_->_D]", getTransactions(design));
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

        assertEquals("[B, D]", getActivities(design));
        assertEquals("[C]", getGateways(design));
        assertEquals("[A_->_B, B_->_C, C_->_D, D_->_E]", getTransactions(design));
    }

    @Test
    public void should_be_able_to_design_a_process_with_parallel_activities() throws Exception {
        DesignProcessDefinition design = new SimpleProcessDesigner(builder)
                .startWith(new StartEvent("A"))
                .then(new UserTask("B1"), new UserTask("B2"))
                .then(new EndEvent("C"))
                .done();

        assertEquals("[B1, B2]", getActivities(design));
        assertEquals("[A_->_B1, A_->_B2, B1_->_C, B2_->_C]", getTransactions(design));
    }

    @Test
    public void should_be_able_to_design_a_process_with_conditions() throws Exception {
        Expression TRUE = new ExpressionBuilder().createConstantBooleanExpression(true);
        DesignProcessDefinition design = new SimpleProcessDesigner(builder)
                .startWith(new StartEvent("A"))
                .then(new UserTask("B"))
                .then(new Gateway("C", GatewayType.EXCLUSIVE))
                .then(
                        new UserTask("D1").setDefault(true),
                        new UserTask("D2").setCondition(TRUE))
                .then(new EndEvent("E"))
                .done();

        assertEquals("[B, D1, D2]", getActivities(design));
        assertEquals("[C (C_->_D1)]", getGateways(design));
        assertEquals("[A_->_B, B_->_C, C_->_D1, C_->_D2 (true), D1_->_E, D2_->_E]", getTransactions(design));
    }

    private String getGateways(DesignProcessDefinition design) {
        return stringify(design.getProcessContainer().getGatewaysList(), new Stringifier<GatewayDefinition>() {
            @Override
            public String stringify(GatewayDefinition gateway) {
                String text = gateway.getName();
                if(gateway.getDefaultTransition() != null) {
                    text += " (" + gateway.getDefaultTransition().getName() + ")";
                }
                return text;
            }
        });
    }

    private String getActivities(DesignProcessDefinition design) {
        return stringify(design.getProcessContainer().getActivities(), new Stringifier<ActivityDefinition>() {
            @Override
            public String stringify(ActivityDefinition activity) {
                return activity.getName();
            }
        });
    }

    private String getTransactions(DesignProcessDefinition design) {
        return stringify(design.getProcessContainer().getTransitions(), new Stringifier<TransitionDefinition>() {
            @Override
            public String stringify(TransitionDefinition transition) {
                String text =  transition.getName();
                if(transition.getCondition() != null) {
                    text += " (" + transition.getCondition().getName() + ")";
                }
                return text;
            }
        });
    }

    interface Stringifier<O> {
        String stringify(O object);
    }

    private <O> String stringify(Collection<O> items, Stringifier<O> stringifier) {
        List<String> strings = new ArrayList<String>(items.size());
        for (O item : items) {
            strings.add(stringifier.stringify(item));
        }
        Collections.sort(strings);
        return strings.toString();
    }

    private ProcessDefinitionBuilder getProcessDefinitionBuilder() {
        ProcessDefinitionBuilder builder = new ProcessDefinitionBuilder();
        builder.createNewInstance("Designed by designer", "1.0");
        return builder;
    }
}
