/*
 * Copyright (C) 2015 BonitaSoft S.A.
 * BonitaSoft, 32 rue Gustave Eiffel - 38000 Grenoble
 * This library is free software; you can redistribute it and/or modify it under the terms
 * of the GNU Lesser General Public License as published by the Free Software Foundation
 * version 2.1 of the License.
 * This library is distributed in the hope that it will be useful, but WITHOUT ANY WARRANTY;
 * without even the implied warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.
 * See the GNU Lesser General Public License for more details.
 * You should have received a copy of the GNU Lesser General Public License along with this
 * program; if not, write to the Free Software Foundation, Inc., 51 Franklin Street, Fifth
 * Floor, Boston, MA 02110-1301, USA.
 */

package org.bonitasoft.engine.test;

import java.io.Serializable;
import java.util.ArrayList;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * @author mazourd
 */
public class ReachedDataInstanceList {
    ArrayList<ReachedDataInstance> listData;

    public ReachedDataInstanceList(ArrayList<ReachedDataInstance> listData) {
        this.listData = listData;
    }

    public ArrayList<ReachedDataInstance> getListData() {
        return listData;
    }

    public void setListData(ArrayList<ReachedDataInstance> listData) {
        this.listData = listData;
    }

    public ReachedDataInstanceList hasSize(int size){
        assertThat(listData.size() == size);
        return this;
    }
    public ReachedDataInstanceList containsValue (Serializable... element){
       boolean contains = false;
        for (int i = 0; i < listData.size(); i++) {
                try {
                    listData.get(i).hasValue(element);
                    contains = true;
                } catch (AssertionError e) {
                    //ignore
                }
            }
            assertThat(contains);
        return this;
    }
    public ReachedDataInstanceList containsValues (Serializable... elements){
        boolean contains;
        for (Serializable element : elements) {
            contains = false;
            for (int i = 0; i < listData.size(); i++) {
                try {
                    listData.get(i).hasValue(element);
                    contains = true;
                } catch (AssertionError e) {
                    //ignore
                }
            }
            assertThat(contains);
        }
        return this;
    }

    public int getSize(){
        return listData.size();
    }
}
