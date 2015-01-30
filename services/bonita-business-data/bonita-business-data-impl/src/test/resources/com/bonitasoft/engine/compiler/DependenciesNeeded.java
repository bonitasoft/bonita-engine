/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft;

import java.io.Externalizable;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import org.external.dependency.ExternalInterface;

/**
 * JPA and org.external.dependency.ExternalInterface (located in external-lib.jar) needed to be compiled
 */
@Entity
public class DependenciesNeeded implements ExternalInterface {

    @Id()
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
}
