/*
 * ao-dao - Simple data access objects framework.
 * Copyright (C) 2011, 2013, 2015  AO Industries, Inc.
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
package com.aoindustries.dao.dbc;

import com.aoindustries.dao.Model;
import com.aoindustries.dao.Report;
import com.aoindustries.dao.Table;
import com.aoindustries.dbc.Database;
import com.aoindustries.dbc.DatabaseCallable;
import com.aoindustries.dbc.DatabaseConnection;
import com.aoindustries.dbc.DatabaseRunnable;
import java.sql.SQLException;
import java.text.Collator;
import java.util.Collections;
import java.util.Map;

/**
 * A base implementation of <code>DaoDatabase</code>.
 */
abstract public class AbstractModel
	implements Model
{

    /**
     * A single Collator for shared use.
     */
    private static final Collator collator = Collator.getInstance();

    /**
     * By default, sorts in the system locale.
     */
    @Override
    public Collator getCollator() {
        return collator;
    }

    /**
     * Gets the underlying database that should be used at this moment in time.
     * It is possible that the database will change in an fail-over state.
     * Within a single transaction, however, the database returned must be the
     * same.
     */
    abstract protected Database getDatabase() throws SQLException;

    /**
     * Clears all caches for all tables for the current thread.
     */
    @Override
    public void clearAllCaches() {
        for(Table<?,?> table : getTables().values()) table.clearCaches();
    }

    /**
     * Uses a ThreadLocal to make sure an entire transaction is executed against the same
     * underlying database.  This way, nothing funny will happen if master/slave databases
     * are switched mid-transaction.
     */
    protected final ThreadLocal<Database> transactionDatabase = new ThreadLocal<>();

    protected <V> V executeTransaction(DatabaseCallable<V> callable) throws SQLException {
        Database database = transactionDatabase.get();
        if(database!=null) {
            // Reuse current database
            return database.executeTransaction(callable);
        } else {
            // Get database
            database=getDatabase();
            transactionDatabase.set(database);
            try {
                return database.executeTransaction(callable);
            } finally {
                transactionDatabase.remove();
            }
        }
    }

    protected void executeTransaction(DatabaseRunnable runnable) throws SQLException {
        Database database = transactionDatabase.get();
        if(database!=null) {
            // Reuse current database
            database.executeTransaction(runnable);
        } else {
            // Get database
            database=getDatabase();
            transactionDatabase.set(database);
            try {
                database.executeTransaction(runnable);
            } finally {
                transactionDatabase.remove();
            }
        }
    }

    @Override
    public void executeTransaction(final Runnable runnable) throws SQLException {
        executeTransaction((DatabaseConnection db) -> {
			runnable.run();
		});
    }

    /**
     * By default, there are no reports.
     */
    @Override
    public Map<String,? extends Report> getReports() throws SQLException {
        return Collections.emptyMap();
    }
}
