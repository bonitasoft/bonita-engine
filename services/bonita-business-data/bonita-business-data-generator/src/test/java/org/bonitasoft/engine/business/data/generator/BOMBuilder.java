/**
 * Copyright (C) 2019 Bonitasoft S.A.
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
package org.bonitasoft.engine.business.data.generator;

import java.util.List;

import org.bonitasoft.engine.bdm.BusinessObjectModelConverter;
import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.BusinessObjectModel;
import org.bonitasoft.engine.bdm.model.field.FieldType;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.bdm.model.field.SimpleField;

public class BOMBuilder {

    public static BOMBuilder aBOM() {
        return new BOMBuilder();
    }

    public BusinessObjectModel build() {
        final SimpleField firstName = new SimpleField();
        firstName.setName("firstName");
        firstName.setType(FieldType.STRING);
        final SimpleField lastName = new SimpleField();
        lastName.setName("lastName");
        lastName.setType(FieldType.STRING);
        final BusinessObject employee = new BusinessObject();
        employee.setQualifiedName("com.company.Employee");
        employee.addField(firstName);
        employee.addField(lastName);
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(employee);
        return bom;
    }

    public BusinessObjectModel buildModelWithSingleBoolean() {
        final SimpleField hungry = new SimpleField();
        hungry.setName("hungry");
        hungry.setType(FieldType.BOOLEAN);

        final BusinessObject dinosaurBO = new BusinessObject();
        dinosaurBO.setQualifiedName("com.company.Dinosaur");
        dinosaurBO.addField(hungry);

        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(dinosaurBO);
        return bom;
    }

    /**
     * BPA-833: a BOOLEAN attribute and a relation whose names differ only in the case of their first letter, so
     * that both yield <code>getHungry()</code> and the alias must give way for the entity to compile. Such a model
     * passes {@code BusinessObjectModelValidator}, which is the premise the whole second-pass guard rests on.
     */
    public BusinessObjectModel buildModelWithBooleanCollidingWithRelation() {
        return dinosaurWithBooleanAndRelation("Hungry", RelationField.FetchType.EAGER);
    }

    /**
     * BPA-833: a BOOLEAN attribute named <code>Class</code>. Its alias would be <code>getClass()</code>, and
     * {@link Object#getClass()} is <code>final</code>, so the generated entity would not compile. The attribute name
     * is accepted by the BDM validator, so such a model installs today.
     */
    public BusinessObjectModel buildModelWithBooleanNamedClass() {
        final SimpleField clazz = new SimpleField();
        clazz.setName("Class");
        clazz.setType(FieldType.BOOLEAN);

        final BusinessObject bo = new BusinessObject();
        bo.setQualifiedName("com.company.Reserved");
        bo.addField(clazz);

        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(bo);
        return bom;
    }

    /**
     * BPA-833: a BOOLEAN attribute and a LAZY relation whose names match case-insensitively but differ beyond the
     * first letter, so that their accessors are distinct methods but collapse to one key in the client
     * lazy-loading proxy, which lower-cases the whole name. Deploys today, the columns being HUNGRY and HUNGRY_PID.
     */
    public BusinessObjectModel buildModelWithBooleanCollidingCaseInsensitivelyWithLazyRelation() {
        return dinosaurWithBooleanAndRelation("HUNGRY", RelationField.FetchType.LAZY);
    }

    /**
     * A <code>com.company.Dinosaur</code> carrying a BOOLEAN <code>hungry</code> and one relation to
     * <code>com.company.Prey</code>. The relation's name and fetch type are what each BPA-833 case turns on, so
     * they are all that its callers vary.
     *
     * @param relationName matching <code>hungry</code> case-insensitively, differing in the first letter to make
     *        the two accessors collide outright, beyond it to make them collide only in the client proxy's
     *        lower-cased key
     */
    private BusinessObjectModel dinosaurWithBooleanAndRelation(final String relationName,
            final RelationField.FetchType fetchType) {
        final SimpleField hungry = new SimpleField();
        hungry.setName("hungry");
        hungry.setType(FieldType.BOOLEAN);

        final SimpleField name = new SimpleField();
        name.setName("name");
        name.setType(FieldType.STRING);

        final BusinessObject preyBO = new BusinessObject();
        preyBO.setQualifiedName("com.company.Prey");
        preyBO.addField(name);

        final RelationField relation = new RelationField();
        relation.setType(RelationField.Type.AGGREGATION);
        relation.setFetchType(fetchType);
        relation.setName(relationName);
        relation.setCollection(Boolean.FALSE);
        relation.setNullable(Boolean.TRUE);
        relation.setReference(preyBO);

        final BusinessObject dinosaurBO = new BusinessObject();
        dinosaurBO.setQualifiedName("com.company.Dinosaur");
        dinosaurBO.addField(hungry);
        dinosaurBO.addField(relation);

        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(preyBO);
        bom.addBusinessObject(dinosaurBO);
        return bom;
    }

    public BusinessObjectModel buildModelWithMultipleBoolean() {
        final SimpleField booleans = new SimpleField();
        booleans.setName("booleanList");
        booleans.setType(FieldType.BOOLEAN);
        booleans.setCollection(true);

        final BusinessObject booleansBO = new BusinessObject();
        booleansBO.setQualifiedName("com.company.Demo");
        booleansBO.addField(booleans);

        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(booleansBO);
        return bom;
    }

    public BusinessObjectModel buildModelWithAllSupportedTypes() {
        final BusinessObject invoice = new BusinessObject();
        invoice.setQualifiedName("com.company.pojo.ComplexInvoice");
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(invoice);
        return bom;
    }

    public BusinessObjectModel buildModelWithConstrainedFields() {
        final BusinessObject constrained = new BusinessObject();
        constrained.setQualifiedName("com.company.pojo.ConstrainedItem");
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(constrained);
        return bom;
    }

    public BusinessObjectModel buildComplex() {
        final SimpleField firstName = new SimpleField();
        firstName.setName("firstName");
        firstName.setType(FieldType.STRING);
        final SimpleField lastName = new SimpleField();
        lastName.setName("lastName");
        lastName.setType(FieldType.STRING);
        final BusinessObject employee = new BusinessObject();
        employee.setQualifiedName("com.company.Employee");
        employee.addField(firstName);
        employee.addField(lastName);

        employee.addQuery("getEmployee", "SELECT e FROM Employee e", List.class.getName());
        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(employee);
        return bom;
    }

    public BusinessObjectModel buildPerson() {
        final SimpleField nickNames = new SimpleField();
        nickNames.setName("nickNames");
        nickNames.setType(FieldType.STRING);
        nickNames.setLength(Integer.valueOf(15));
        nickNames.setCollection(Boolean.TRUE);

        final BusinessObject employee = new BusinessObject();
        employee.setQualifiedName("com.company.Person");
        employee.addField(nickNames);

        final BusinessObjectModel bom = new BusinessObjectModel();
        bom.addBusinessObject(employee);
        return bom;
    }

    public byte[] buildZip() {
        final BusinessObjectModelConverter converter = new BusinessObjectModelConverter();
        try {
            return converter.zip(build());
        } catch (final Exception e) {
            throw new RuntimeException("Unable to build BOM zip");
        }
    }

}
