package com.bonitasoft.engine.bdm.model.assertion;

import static com.bonitasoft.engine.bdm.BOMBuilder.aBOM;
import static com.bonitasoft.engine.bdm.model.builder.BusinessObjectBuilder.aBO;

import java.io.IOException;

import javax.xml.bind.JAXBException;

import org.assertj.core.api.AbstractAssert;
import org.assertj.core.api.Assertions;
import org.xml.sax.SAXException;

import com.bonitasoft.engine.bdm.BOMBuilder;
import com.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.field.RelationField;
import com.bonitasoft.engine.bdm.model.field.Field;

public class FieldAssert extends AbstractAssert<FieldAssert, Field> {

    protected FieldAssert(Field actual) {
        super(actual, FieldAssert.class);
    }

    public static FieldAssert assertThat(Field actual) {
        return new FieldAssert(actual);
    }

    public FieldAssert canBeMarshalled() {
        try {
            BusinessObjectModel bom = marshallUnmarshall(actual);
            Assertions.assertThat(bom.getBusinessObjects().get(0)).isNotNull();
            Assertions.assertThat(bom.getBusinessObjects().get(0).getFields().get(0)).isNotNull();
            isEqualTo(bom.getBusinessObjects().get(0).getFields().get(0));
        } catch (Exception e) {
            failWithMessage("Expected <%s> to be marshallizable but wasn't : <%s>", actual, e.getCause());
        }
        return this;
    }

    public FieldAssert cannotBeMarshalled() {
        try {
            marshallUnmarshall(actual);
            failWithMessage("Expected <%s> to not be marshallizable", actual);
        } catch (Exception e) {
        }
        return this;
    }

    private BusinessObjectModel marshallUnmarshall(Field field) throws JAXBException, IOException, SAXException {
        BOMBuilder model = aBOM().withBO(aBO("someUglyNameMightNotAppear").withField(field).build());
        if (field instanceof RelationField) {
            RelationField f = (RelationField) field;
            model.withBO(f.getReference());
        }

        BusinessObjectModelConverter convertor = new BusinessObjectModelConverter();
        byte[] marshall = convertor.marshall(model.build());
        return convertor.unmarshall(marshall);
    }
}
