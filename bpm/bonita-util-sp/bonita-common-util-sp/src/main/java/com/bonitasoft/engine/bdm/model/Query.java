/*******************************************************************************
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft is a trademark of BonitaSoft SA.
 * This software file is BONITASOFT CONFIDENTIAL. Not For Distribution.
 * For commercial licensing information, contact:
 * BonitaSoft, 32 rue Gustave Eiffel – 38000 Grenoble
 * or BonitaSoft US, 51 Federal Street, Suite 305, San Francisco, CA 94107
 *******************************************************************************/
package com.bonitasoft.engine.bdm.model;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;

/**
 * @author Matthieu Chaffotte
 * @author Romain Bioteau
 * @deprecated from version 7.0.0 on, use {@link org.bonitasoft.engine.bdm.model.Query} instead.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@Deprecated
public class Query {

    @XmlAttribute(required = true)
    private String name;

    @XmlAttribute(required = true)
    private String content;

    @XmlAttribute(required = true)
    private String returnType;

    @XmlElementWrapper(name = "queryParameters")
    @XmlElement(name = "queryParameter")
    private List<QueryParameter> queryParameters;

    public Query() {
        queryParameters = new ArrayList<QueryParameter>();
    }

    public Query(final String name, final String content, final String returnType) {
        this();
        this.name = name;
        this.content = content;
        this.returnType = returnType;
    }

    public String getName() {
        return name;
    }

    public void setName(final String name) {
        this.name = name;
    }

    public String getContent() {
        return content;
    }

    public void setContent(final String content) {
        this.content = content;
    }

    public QueryParameter addQueryParameter(final String name, final String className) {
        QueryParameter parameter = new QueryParameter(name, className);
        queryParameters.add(parameter);
        return parameter;
    }

    public List<QueryParameter> getQueryParameters() {
        return queryParameters;
    }

    public void setQueryParameters(final List<QueryParameter> queryParameters) {
        this.queryParameters = queryParameters;
    }

    public String getReturnType() {
        return returnType;
    }

    public void setReturnType(final String returnType) {
        this.returnType = returnType;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((content == null) ? 0 : content.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
        result = prime * result + ((queryParameters == null) ? 0 : queryParameters.hashCode());
        result = prime * result + ((returnType == null) ? 0 : returnType.hashCode());
        return result;
    }

    @Override
    public boolean equals(final Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        Query other = (Query) obj;
        if (content == null) {
            if (other.content != null)
                return false;
        } else if (!content.equals(other.content))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        if (queryParameters == null) {
            if (other.queryParameters != null)
                return false;
        } else if (!queryParameters.equals(other.queryParameters))
            return false;
        if (returnType == null) {
            if (other.returnType != null)
                return false;
        } else if (!returnType.equals(other.returnType))
            return false;
        return true;
    }

    @Override
    public String toString() {
        return "Query [name=" + name + ", content=" + content + ", returnType=" + returnType + ", queryParameters=" + queryParameters + "]";
    }

}
