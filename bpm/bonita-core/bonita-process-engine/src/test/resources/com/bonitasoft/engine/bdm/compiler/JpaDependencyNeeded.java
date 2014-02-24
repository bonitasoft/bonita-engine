package com.bonitasoft.engine.bdm.compiler;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class JpaDependencyNeeded {

    @Id()
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
}
