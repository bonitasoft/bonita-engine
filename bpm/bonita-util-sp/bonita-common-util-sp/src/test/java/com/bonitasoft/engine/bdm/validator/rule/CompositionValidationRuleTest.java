package com.bonitasoft.engine.bdm.validator.rule;

import static com.bonitasoft.engine.bdm.model.builder.BusinessObjectBuilder.aBO;
import static com.bonitasoft.engine.bdm.model.builder.BusinessObjectModelBuilder.aBOM;
import static com.bonitasoft.engine.bdm.model.builder.FieldBuilder.aCompositionField;
import static com.bonitasoft.engine.bdm.model.builder.FieldBuilder.anAggregationField;
import static com.bonitasoft.engine.bdm.validator.assertion.ValidationStatusAssert.assertThat;
import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.builder.FieldBuilder;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Colin PUY
 */
public class CompositionValidationRuleTest {

    private CompositionValidationRule rule;

    @Before
    public void setUp() {
        rule = new CompositionValidationRule();
    }

    @Test
    @Ignore("Not yet implemented")
    public void should_validate_that_a_composite_object_cannot_have_one_of_its_ancestor_as_a_child() throws Exception {
        BusinessObject daughter = aBO("daughter").build();
        BusinessObject mother = aBO("mother").withField(aCompositionField("daughter", daughter)).build();
        BusinessObject grandMother = aBO("grandMother").withField(aCompositionField("mother", mother)).build();

        daughter.addField(aCompositionField("forbiddenChild", grandMother));
        BusinessObjectModel bom = aBOM().withBOs(grandMother, mother, daughter).build();

        ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void should_validate_that_a_composite_object_can_be_composed_only_once() throws Exception {
        BusinessObject composite = aBO("composite").build();
        BusinessObject composedOne = aBO("composedOne").withField(aCompositionField("compositeNameOne", composite)).build();
        BusinessObject composedTwo = aBO("composedTwo").withField(aCompositionField("compositeNameTwo", composite)).build();
        BusinessObject aggregated = aBO("aggregated").withField(anAggregationField("aggregation", composite)).build();
        BusinessObjectModel bom = aBOM().withBOs(composite, composedOne, composedTwo, aggregated).build();

        ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus).isNotOk();
    }

    @Test
    public void a_bom_with_no_relation_fields_is_valid() throws Exception {
        BusinessObjectModel bom = aBOM().withBO(aBO("aBo").build()).build();

        ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus).isOk();
    }

    @Test
    public void a_bom_with_no_composition_is_valid() throws Exception {
        BusinessObject aggregated = aBO("aggregated").build();
        BusinessObject bo = aBO("aBo").withField(anAggregationField("aggreg", aggregated)).build();
        BusinessObjectModel bom = aBOM().withBOs(bo, aggregated).build();

        ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus).isOk();
    }

    @Test
    public void a_bom_with_composed_bo_in_only_one_bo_is_valid() throws Exception {
        BusinessObject composite = aBO("composite").build();
        BusinessObject bo = aBO("aBo").withField(aCompositionField("composite", composite)).build();
        BusinessObject composite2 = aBO("composite2").build();
        BusinessObject bo2 = aBO("aBo2").withField(aCompositionField("composite2", composite2)).build();
        BusinessObjectModel bom = aBOM().withBOs(bo, composite, bo2, composite2).build();

        ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus).isOk();
    }
    
    @Test
    public void a_bom_with_a_bo_composed_in_two_bos_is_invalid() throws Exception {
        BusinessObject composite = aBO("composite").build();
        BusinessObject bo = aBO("aBo").withField(aCompositionField("boOneComposite", composite)).build();
        BusinessObject bo2 = aBO("aBo2").withField(aCompositionField("boTwoComposite", composite)).build();
        BusinessObjectModel bom = aBOM().withBOs(bo, bo2, composite).build();

        ValidationStatus validationStatus = rule.validate(bom);

        assertThat(validationStatus).isNotOk();

    }
}
