/**
 * 
 */
package com.bonitasoft.engine.bdm.validator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.*;

import java.util.Arrays;

import org.junit.Before;
import org.junit.Test;

import com.bonitasoft.engine.bdm.UniqueConstraint;

/**
 * @author Romain
 * 
 */
public class QueryNameUtilTest {

    /**
     * @throws java.lang.Exception
     */
    @Before
    public void setUp() throws Exception {
    }

    @Test
    public void should_createQueryNameForUniqueConstraint_return_queryname() throws Exception {
        UniqueConstraint uniqueConstraint = new UniqueConstraint();
        uniqueConstraint.setFieldNames(Arrays.asList("name"));
        String queryNameForUniqueConstraint = QueryNameUtil.createQueryNameForUniqueConstraint("org.bonita.Employee", uniqueConstraint);
        assertThat(queryNameForUniqueConstraint).isEqualTo("getEmployeeByName");

    }
}
