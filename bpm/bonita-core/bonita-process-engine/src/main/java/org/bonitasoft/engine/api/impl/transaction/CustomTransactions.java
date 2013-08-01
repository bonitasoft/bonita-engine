package org.bonitasoft.engine.api.impl.transaction;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Indicates that a Bonita API method handles transactions itself, and thus does not need to use the generic transaction mechanism.
 * 
 * @author Emmanuel Duchastenier
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface CustomTransactions {

}
