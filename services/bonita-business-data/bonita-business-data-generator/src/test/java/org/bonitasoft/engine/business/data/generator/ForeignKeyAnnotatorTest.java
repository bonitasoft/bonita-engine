/**
 * Copyright (C) 2015-2017 BonitaSoft S.A.
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
package org.bonitasoft.engine.business.data.generator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doReturn;

import java.io.StringWriter;
import java.io.Writer;
import java.util.Collection;
import java.util.Map;

import org.bonitasoft.engine.bdm.model.BusinessObject;
import org.bonitasoft.engine.bdm.model.field.RelationField;
import org.bonitasoft.engine.business.data.generator.client.ClientBDMCodeGenerator;
import org.hibernate.annotations.ForeignKey;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;

import com.sun.codemodel.JAnnotationUse;
import com.sun.codemodel.JAnnotationValue;
import com.sun.codemodel.JDefinedClass;
import com.sun.codemodel.JFieldVar;
import com.sun.codemodel.JFormatter;

/**
 * @author Laurent Leseigneur
 */
@RunWith(MockitoJUnitRunner.class)
public class ForeignKeyAnnotatorTest {

    public static final String EXPECTED_UNIQUE_NAME = "EntityPojo_fieldName_businessObjectSimpleName";

    private CodeGenerator codeGenerator;

    private JDefinedClass jDefinedClass;

    private JFieldVar jFieldVar;

    @Mock
    private RelationField relationField;

    @Mock
    private BusinessObject businessObject;

    private ForeignKeyAnnotator foreignKeyAnnotator;

    @Before
    public void before() throws Exception {
        codeGenerator = new ClientBDMCodeGenerator();
        foreignKeyAnnotator = new ForeignKeyAnnotator(codeGenerator);
        jDefinedClass = codeGenerator.addClass(EntityPojo.class.getName());
        jFieldVar = codeGenerator.addField(jDefinedClass, "fieldName", EntityPojo.class);
        doReturn(businessObject).when(relationField).getReference();
        doReturn("businessObjectSimpleName").when(businessObject).getSimpleName();
    }

    @Test
    public void should_annotate_with_foreign_key_name() throws Exception {
        // when
        foreignKeyAnnotator.annotateForeignKeyName(jDefinedClass, jFieldVar, relationField);

        // then
        final Collection<JAnnotationUse> annotations = jFieldVar.annotations();
        assertThat(annotations).hasSize(1);
        final JAnnotationUse jAnnotationUse = annotations.iterator().next();
        final Map<String, JAnnotationValue> annotationMembers = jAnnotationUse.getAnnotationMembers();
        assertThat(annotationMembers).containsKey("name");
        assertThat(jAnnotationUse.getAnnotationClass().fullName()).isEqualTo(ForeignKey.class.getCanonicalName());

        final JAnnotationValue jAnnotationValue = annotationMembers.get("name");

        Writer stringWriter = new StringWriter();
        JFormatter jFormatter = new JFormatter(stringWriter);
        jAnnotationValue.generate(jFormatter);
        final String foreignKeyName = stringWriter.toString();
        assertThat(foreignKeyName).isEqualTo(
                "\"FK_" + foreignKeyAnnotator.getReducedUniqueName(jDefinedClass, jFieldVar, relationField) + "\"");

        assertThat(foreignKeyName.length()).as("should match db vendor name limitation").isLessThanOrEqualTo(30);
    }

    @Test
    public void should_generate_reduced_name() throws Exception {
        // when
        final int reducedUniqueName = foreignKeyAnnotator.getReducedUniqueName(jDefinedClass, jFieldVar, relationField);

        // then
        assertThat(reducedUniqueName).as("should be a positive value").isGreaterThan(0);
    }

}
