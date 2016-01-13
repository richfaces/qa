/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010-2016, Red Hat, Inc. and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.richfaces.tests.metamer.bean.rich;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.faces.bean.ManagedBean;
import javax.faces.bean.ManagedProperty;
import javax.faces.bean.SessionScoped;

import org.richfaces.component.UICollapsibleSubTable;
import org.richfaces.model.SortMode;
import org.richfaces.tests.metamer.Attributes;
import org.richfaces.tests.metamer.ColumnSortingMap;
import org.richfaces.tests.metamer.model.Employee;
import org.richfaces.tests.metamer.model.Employee.Sex;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Managed bean for rich:collapsibleSubTable.
 *
 * @author <a href="mailto:ppitonak@redhat.com">Pavol Pitonak</a>
 * @version $Revision: 23004 $
 */
@ManagedBean(name = "richSubTableBean")
@SessionScoped
public class RichCollapsibleSubTableBean implements Serializable {

    private static Logger logger;
    private static final long serialVersionUID = -1L;

    private Attributes attributes;

    @ManagedProperty("#{model.employees}")
    private List<Employee> employees;
    // expanded
    private Map<Sex, Boolean> expanded = new HashMap<Sex, Boolean>();
    // expanded state for employee detail (for RF-11656)
    private Map<Employee, Boolean> expandedEmployee = new HashMap<Employee, Boolean>();
    // facets
    private Map<String, String> facets = new HashMap<String, String>();
    // filtering
    private Map<String, Object> filtering = new HashMap<String, Object>();

    private List<List<Employee>> lists;
    private Map<Sex, Integer> pages = new HashMap<Sex, Integer>(2);
    // sorting
    private ColumnSortingMap sorting = new ColumnSortingMap() {

        private static final long serialVersionUID = 1L;

        @Override
        protected Attributes getAttributes() {
            return attributes;
        }
    };
    // true = model, false = empty table
    private boolean state;

    public Attributes getAttributes() {
        return attributes;
    }

    public List<Employee> getEmployees() {
        return employees;
    }

    public Map<Sex, Boolean> getExpanded() {
        return expanded;
    }

    public Map<Employee, Boolean> getExpandedEmployee() {
        return expandedEmployee;
    }

    public Map<String, String> getFacets() {
        return facets;
    }

    public Map<String, Object> getFiltering() {
        return filtering;
    }

    public List<List<Employee>> getLists() {
        return lists;
    }

    public Map<Sex, Integer> getPages() {
        return pages;
    }

    public ColumnSortingMap getSorting() {
        return sorting;
    }

    public ColumnSortingMap getSortingBuiltIn(boolean isMan) {
        return sorting;
    }

    /**
     * Initializes the managed bean.
     */
    @PostConstruct
    public void init() {
        logger = LoggerFactory.getLogger(getClass());
        logger.debug("initializing bean " + getClass().getName());

        attributes = Attributes.getComponentAttributesFromFacesConfig(UICollapsibleSubTable.class, getClass());

        attributes.setAttribute("rendered", true);
        attributes.setAttribute("rows", 5);
        attributes.setAttribute("sortMode", SortMode.single);

        // TODO these attributes have to be tested in another way
        attributes.remove("expanded");
        attributes.remove("columns");
        attributes.remove("selection");
        attributes.remove("filterVar");
        attributes.remove("iterationStatusVar");
        attributes.remove("componentState");
        attributes.remove("rowKeyVar");
        attributes.remove("stateVar");
        attributes.remove("var");
        attributes.remove("value");

        List<Employee> men = new ArrayList<Employee>();
        List<Employee> women = new ArrayList<Employee>();

        for (Employee e : employees) {
            if (e.getSex() == Sex.MALE) {
                men.add(e);
            } else {
                women.add(e);
            }
        }

        lists = new ArrayList<List<Employee>>();
        lists.add(men);
        lists.add(women);

        expanded.put(Sex.MALE, true);
        expanded.put(Sex.FEMALE, true);

        state = true;

        // facets initial values
        facets.put("noData", "There is no data.");
        facets.put("header", "Header");
        facets.put("footer", "Footer");
        facets.put("nameHeader", "Name Header");
        facets.put("nameFooter", "Name Footer");
        facets.put("titleHeader", "Title Header");
        facets.put("titleFooter", "Title Footer");
        facets.put("birthdateHeader", "Birthday Header");
        facets.put("birthdateFooter", "Birthday Footer");

        pages.put(Sex.MALE, 1);
        pages.put(Sex.FEMALE, 1);
    }

    public boolean isState() {
        return state;
    }

    public void setAttributes(Attributes attributes) {
        this.attributes = attributes;
    }

    public void setEmployees(List<Employee> employees) {
        this.employees = employees;
    }

    public void setExpandedEmployee(Map<Employee, Boolean> expandedEmployee) {
        this.expandedEmployee = expandedEmployee;
    }

    public void setLists(List<List<Employee>> lists) {
        this.lists = lists;
    }

    public void setState(boolean state) {
        this.state = state;
    }
}
