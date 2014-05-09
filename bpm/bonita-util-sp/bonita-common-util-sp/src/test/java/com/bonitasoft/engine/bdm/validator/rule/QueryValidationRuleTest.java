/**
 * Copyright (C) 2013 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 2.0 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.bonitasoft.engine.bdm.validator.rule;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.model.BusinessObject;
import com.bonitasoft.engine.bdm.model.BusinessObjectModel;
import com.bonitasoft.engine.bdm.model.Query;
import com.bonitasoft.engine.bdm.model.UniqueConstraint;
import com.bonitasoft.engine.bdm.model.field.SimpleField;
import com.bonitasoft.engine.bdm.validator.ValidationStatus;

/**
 * @author Romain Bioteau
 *
 */
public class QueryValidationRuleTest {

	private QueryValidationRule queryValidationRule;

	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		queryValidationRule = new QueryValidationRule();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
	}

	@Test
	public void shouldAppliesTo_UniqueConstraint() throws Exception {
		assertThat(queryValidationRule.appliesTo(new BusinessObjectModel())).isFalse();
		assertThat(queryValidationRule.appliesTo(new BusinessObject())).isFalse();
		assertThat(queryValidationRule.appliesTo(new SimpleField())).isFalse();
		assertThat(queryValidationRule.appliesTo(new UniqueConstraint())).isFalse();
		assertThat(queryValidationRule.appliesTo(new Query())).isTrue();
	}

	@Test(expected=IllegalArgumentException.class)
	public void shouldCheckRule_throw_IllegalArgumentException() throws Exception {
		queryValidationRule.checkRule(new BusinessObject());
	}

	@Test
	public void shouldCheckRule_return_valid_status() throws Exception {
		Query q = new Query("findByName", "Select toto where titi = toto",List.class.getName());
		ValidationStatus checkRule = queryValidationRule.checkRule(q);
		assertThat(checkRule.isOk()).isTrue();
	}

	@Test
	public void shouldCheckRule_return_error_status_if_no_name() throws Exception {
		Query q = new Query("", "Select toto where titi = toto",List.class.getName());
		ValidationStatus checkRule = queryValidationRule.checkRule(q);
		assertThat(checkRule.isOk()).isFalse();
		
		q = new Query(null, "Select toto where titi = toto",List.class.getName());
		checkRule = queryValidationRule.checkRule(q);
		assertThat(checkRule.isOk()).isFalse();
	}

	@Test
	public void shouldCheckRule_return_error_status_if_name_too_long() throws Exception {
		Query q = new Query("dsfhsdjkhfdjskfhjksdhfjksdhfjkshfjksdhfjksdhfjksdhfjksdhfjkdsfhjkdshfjjkdshfjskdhfjksdhfdjskhfhfjkhsdfkjsdhfksduzeiryzueiryuzieryuigsdhjgfhjgriuzegrjkg", "Select toto where titi = toto",List.class.getName());
		ValidationStatus checkRule = queryValidationRule.checkRule(q);
		assertThat(checkRule.isOk()).isFalse();
	}

	@Test
	public void shouldCheckRule_return_error_status_if_no_content() throws Exception {
		Query q = new Query("toto", "",List.class.getName());
		ValidationStatus checkRule = queryValidationRule.checkRule(q);
		assertThat(checkRule.isOk()).isFalse();

		q = new Query("toto", null,List.class.getName());
		checkRule = queryValidationRule.checkRule(q);
		assertThat(checkRule.isOk()).isFalse();
	}
	
	@Test
    public void shouldCheckRule_return_error_status_if_no_returnType() throws Exception {
        Query q = new Query("toto", "select","");
        ValidationStatus checkRule = queryValidationRule.checkRule(q);
        assertThat(checkRule.isOk()).isFalse();

        q = new Query("toto", "select","");
        checkRule = queryValidationRule.checkRule(q);
        assertThat(checkRule.isOk()).isFalse();
    }

}
