package org.bonitasoft.engine.configuration;

import org.springframework.context.annotation.Conditional;

/**
 * @author Danila Mazour
 */

@Conditional(OnPropertyCondition.class)
public @interface ConditionalOnProperty {

    /**
     * The name of the boolean property that activate the target class
     */
    String value();

    boolean enableIfMissing();



}
