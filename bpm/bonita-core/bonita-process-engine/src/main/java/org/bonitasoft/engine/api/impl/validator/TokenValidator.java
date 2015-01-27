/*******************************************************************************
 * Copyright (C) 2014 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 ******************************************************************************/
package org.bonitasoft.engine.api.impl.validator;


/**
 * @author Elias Ricken de Medeiros
 *
 */
public class TokenValidator {

    private String token;

    private String error;

    public TokenValidator(String token) {
        this.token = token;
    }

    public boolean validate() {
        if(token == null || !token.matches("((\\p{Alnum})|-|\\.|_|~)+")) {
            error = "The token '"
                    + token
                    + "' is invalid: the token can not be null or empty and should contain only alpha numeric characters and the following special characters '-', '.', '_' or '~'";
            return false;
        }
        return  true;
    }

    public String getError() {
        return error;
    }
}
