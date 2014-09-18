/*******************************************************************************
 * Copyright (C) 2014 Bonitasoft S.A.
 * Bonitasoft is a trademark of Bonitasoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * Bonitasoft, 32 rue Gustave Eiffel 38000 Grenoble
 * or Bonitasoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.model.field;

import static com.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.aBO;
import static com.bonitasoft.engine.bdm.builder.FieldBuilder.aBooleanField;
import static com.bonitasoft.engine.bdm.model.assertion.FieldAssert.assertThat;
import static org.assertj.core.api.Assertions.assertThat;

import org.junit.Ignore;
import org.junit.Test;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.field.RelationField.FetchType;
import com.bonitasoft.engine.bdm.model.field.RelationField.Type;

/**
 * @author Colin PUY
 */
public class RelationFieldTest {

    private final BusinessObject aBo = aBO("boName").withField(aBooleanField("aField")).build();

    @Test
    public void should_not_be_marshallizable_without_reference() {
        final RelationField field = new RelationField();
        field.setName("aName");
        field.setType(Type.AGGREGATION);

        assertThat(field).cannotBeMarshalled();
    }

    @Test
    @Ignore
    public void should_not_be_marshallizable_without_name() {
        final RelationField field = new RelationField();
        field.setType(Type.AGGREGATION);
        field.setReference(aBo);

        assertThat(field).cannotBeMarshalled();
    }

    @Test
    public void should_not_be_marshallizable_without_type() {
        final RelationField field = new RelationField();
        field.setReference(aBo);
        field.setName("aName");

        assertThat(field).cannotBeMarshalled();
    }

    @Test
    public void should_be_marshallizable_with_only_name_type_and_reference() {
        final RelationField field = new RelationField();
        field.setName("aName");
        field.setType(Type.AGGREGATION);
        field.setReference(aBo);

        assertThat(field).canBeMarshalled();
    }

    @Test
    public void should_not_be_marshallizable_whitout_fetchType() {
        final RelationField field = new RelationField();
        field.setName("aName");
        field.setType(Type.AGGREGATION);
        field.setReference(aBo);

        field.setFetchType(null);

        assertThat(field).cannotBeMarshalled();
    }

    @Test
    public void should_have_a_default_fetchType_to_eager() {
        final RelationField field = new RelationField();

        assertThat(field.getFetchType()).isEqualTo(FetchType.EAGER);
    }

    @Test
    public void can_be_lazy() {
        final RelationField field = new RelationField();
        field.setFetchType(FetchType.LAZY);

        assertThat(field.getFetchType()).isEqualTo(FetchType.LAZY);
    }
}
