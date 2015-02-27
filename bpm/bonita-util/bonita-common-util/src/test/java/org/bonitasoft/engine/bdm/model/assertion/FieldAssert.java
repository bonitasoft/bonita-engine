package org.bonitasoft.engine.bdm.model.assertion;

import static org.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.aBO;
import static org.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder.aBOM;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.xml.sax.SAXException;

import org.bonitasoft.engine.bdm.builder.BusinessObjectModelBuilder;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.Field;
import org.bonitasoft.engine.bdm.model.field.RelationField;

public class FieldAssert extends AbstractAssert<FieldAssert, Field> {

    protected FieldAssert(final Field actual) {
        super(actual, FieldAssert.class);
    }

    public static FieldAssert assertThat(final Field actual) {
        return new FieldAssert(actual);
    }

    public FieldAssert canBeMarshalled() {
        try {
            final BusinessObjectModel bom = marshallUnmarshall(actual);
            Assertions.assertThat(bom.getBusinessObjects().get(0)).isNotNull();
            Assertions.assertThat(bom.getBusinessObjects().get(0).getFields().get(0)).isNotNull();
            isEqualTo(bom.getBusinessObjects().get(0).getFields().get(0));
        } catch (final Exception e) {
            failWithMessage("Expected <%s> to be marshallizable but wasn't : <%s>", actual, e.getCause());
        }
        return this;
    }

    public FieldAssert cannotBeMarshalled() {
        try {
            marshallUnmarshall(actual);
            failWithMessage("Expected <%s> not to be marshallizable", actual);
        } catch (final Exception e) {
            // OK
        }
        return this;
    }

    private BusinessObjectModel marshallUnmarshall(final Field field) throws JAXBException, IOException, SAXException {
        final BusinessObjectModelBuilder bom = aBOM().withBO(aBO("someUglyNameMightNotAppear").withField(field).build());
        addReferencedBoToBom(field, bom);
        return Marshaller.marshallUnmarshall(bom.build());
    }

    private void addReferencedBoToBom(final Field field, final BusinessObjectModelBuilder bom) {
        if (field instanceof RelationField) {
            final RelationField f = (RelationField) field;
            bom.withBO(f.getReference());
        }
    }
}
