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
