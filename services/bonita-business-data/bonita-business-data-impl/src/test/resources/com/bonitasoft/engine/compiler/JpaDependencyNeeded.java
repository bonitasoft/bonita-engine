package com.bonitasoft;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class JpaDependencyNeeded {

    @Id()
    @GeneratedValue(strategy = GenerationType.AUTO)
    private String id;
}
