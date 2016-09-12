package org.bonitasoft.engine.persistence;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Arrays;

import org.junit.Test;

/**
 * @author Laurent Leseigneur
 */
public class OrderByTypeTest {

    @Test
    public void should_have_a_sql_keyword_for_each_orderBy() {
        for (OrderByType orderByType : Arrays.asList(OrderByType.values())) {
            assertThat(orderByType.getSqlKeyword()).as("should have a sql valid key word").isNotNull();
        }

    }

}
