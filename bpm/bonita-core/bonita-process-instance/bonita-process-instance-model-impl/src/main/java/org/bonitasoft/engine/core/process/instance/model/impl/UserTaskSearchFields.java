///**
// * Copyright (C) 2012 BonitaSoft S.A.
// * BonitaSoft, 31 rue Gustave Eiffel - 38000 Grenoble
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU General Public License as published by
// * the Free Software Foundation, either version 2.0 of the License, or
// * (at your option) any later version.
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// * GNU General Public License for more details.
// * You should have received a copy of the GNU General Public License
// * along with this program. If not, see <http://www.gnu.org/licenses/>.
// */
//package org.bonitasoft.engine.core.process.instance.model.impl;
//
//import java.util.HashSet;
//import java.util.Set;
//
//import org.bonitasoft.engine.persistence.SearchFields;
//
///**
// * @author julien.mege
// */
//public class UserTaskSearchFields implements SearchFields {
//
//    private static final long serialVersionUID = -9396191384598219L;
//
//    private final String value;
//
//    private static Set<String> fields = new HashSet<String>(3);
//
//    static {
//        fields.add("firstName");
//        fields.add("lastName");
//    }
//
//    public UserTaskSearchFields(final String value) {
//        super();
//        this.value = value;
//    }
//
//    @Override
//    public String getValue() {
//        return value;
//    }
//
//    @Override
//    public Set<String> getFields() {
//        return fields;
//    }
//
//}
