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
package org.bonitasoft.engine.bdm.builder;

import java.util.List;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.SimpleField;
import static org.bonitasoft.engine.bdm.builder.BusinessObjectBuilder.aBO;
import static org.bonitasoft.engine.bdm.builder.FieldBuilder.*;

/**
 * @author Colin PUY
 */
public class BusinessObjectModelBuilder {

    private final BusinessObjectModel businessObjectModel = new BusinessObjectModel();

    public static BusinessObjectModelBuilder aBOM() {
        return new BusinessObjectModelBuilder();
    }

    public BusinessObjectModelBuilder withBO(final BusinessObject bo) {
        businessObjectModel.addBusinessObject(bo);
        return this;
    }

    public BusinessObjectModelBuilder withBOs(final BusinessObject... bos) {
        for (final BusinessObject bo : bos) {
            businessObjectModel.addBusinessObject(bo);
        }
        return this;
    }

    public BusinessObjectModel build() {
        return businessObjectModel;
    }

    private BusinessObject buildMyBusinessObject() {
        return aBO("BusinessObject").withField(aStringField("stringField").nullable().build()).withField(aBooleanField("booleanField"))
                .withField(aDateField("dateField").notNullable().build()).withField(aDoubleField("doubleField").build())
                .withField(anIntegerField("integerField").build()).withField(aTextField("textField").build()).build();
    }

    public BusinessObjectModel buildDefaultBOM() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(buildMyBusinessObject());
        return bom;
    }

    public BusinessObjectModel buildBOMWithAnEmptyEntity() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(new BusinessObject());
        return bom;
    }

    public BusinessObjectModel buildBOMWithAnEmptyField() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        final BusinessObject businessObject = new BusinessObject();
        businessObject.setQualifiedName("BusinessObject");
        businessObject.addField(new SimpleField());
        bom.addBusinessObject(businessObject);
        return bom;
    }

    public BusinessObjectModel buildBOMWithUniqueConstraint() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        final BusinessObject employee = buildMyBusinessObject();
        employee.addUniqueConstraint("UC_string_double", "stringField", "doubleField");
        bom.addBusinessObject(employee);
        return bom;
    }

    public BusinessObjectModel buildBOMWithQuery() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        final BusinessObject employee = buildMyBusinessObject();
        employee.addQuery("employeeByName", "Select e FROM Employee e WHERE e.name='romain'", List.class.getName());
        bom.addBusinessObject(employee);
        return bom;
    }

    public BusinessObjectModel buildBOMWithIndex() {
        final BusinessObjectModel bom = new BusinessObjectModel();
        final BusinessObject employee = buildMyBusinessObject();
        employee.addIndex("idx_45", "stringField", "doubleField");
        bom.addBusinessObject(employee);
        return bom;
    }
}
