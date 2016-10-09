/*
 * ao-dao - Simple data access objects framework.
 * Copyright (C) 2011, 2013, 2015, 2016  AO Industries, Inc.
 *     support@aoindustries.com
 *     7262 Bull Pen Cir
 *     Mobile, AL 36695
 *
 * This file is part of ao-dao.
 *
 * ao-dao is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ao-dao is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with ao-dao.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.aoindustries.dao;

import java.sql.SQLException;
import java.text.Collator;
import java.util.Collections;
import java.util.Map;

/**
 * A model is a collection of tables, and a collection of reports.
 */
public interface Model {

    /**
     * Gets the name of this model.
     */
    String getName();

    /**
     * Gets the collator used by this model.
     */
    Collator getCollator();

    /**
     * Gets the set of all tables in this model.  This is a map keyed on table
     * name to be useful in JSP EL without requiring a separate getter for each
     * table.
     */
    Map<String,? extends Table<?,?>> getTables();

    /**
     * Clears all caches for all tables for the current thread.
     */
    default void clearAllCaches() {
        for(Table<?,?> table : getTables().values()) table.clearCaches();
    }

    /**
     * Executes a transaction between any number of calls to this model and its tables.
     */
    void executeTransaction(Runnable runnable) throws SQLException;

    /**
     * Gets the set of all reports that are supported by this repository implementation, keyed on its unique name.
     */
    default Map<String,? extends Report> getReports() throws SQLException {
		// By default, there are no reports.
        return Collections.emptyMap();
    }
}
