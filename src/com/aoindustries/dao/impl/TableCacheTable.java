/*
 * ao-dao - Simple data access objects framework.
 * Copyright (C) 2011, 2012, 2013, 2015, 2016  AO Industries, Inc.
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
package com.aoindustries.dao.impl;

import com.aoindustries.dao.Model;
import com.aoindustries.dao.Row;
import com.aoindustries.dbc.NoRowException;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Caches results by querying the entire table upon first use.  The cache is
 * per-request and per-user.
 * <ol>
 *   <li>All rows are loaded and stored unsorted</li>
 *   <li>allRowsLoaded is called, given unsorted rows</li>
 *   <li>Map is built upon first call to get(K)</li>
 *   <li>Rows are sorted upon first call to getRows</li>
 * </ol>
 */
abstract public class TableCacheTable<
	K extends Comparable<? super K>,
	R extends Row<K,?>
>
	extends AbstractTable<K,R>
{

    protected final ThreadLocal<Set<? extends R>> unsortedRowsCache = new ThreadLocal<>();

    private final ThreadLocal<SortedSet<? extends R>> sortedRowsCache = new ThreadLocal<>();

    private final ThreadLocal<Boolean> rowCachedLoaded = new ThreadLocal<Boolean>() {
        @Override
        protected Boolean initialValue() {
            return Boolean.FALSE;
        }
    };

    private final ThreadLocal<Map<K,R>> rowCache = new ThreadLocal<Map<K,R>>() {
        @Override
        protected Map<K,R> initialValue() {
            return new HashMap<>();
        }
    };

    protected TableCacheTable(Class<K> keyClass, Class<R> rowClass, Model model) {
        super(keyClass, rowClass, model);
    }

    private void clearCaches0() {
        unsortedRowsCache.remove();
        sortedRowsCache.remove();
        rowCachedLoaded.set(Boolean.FALSE);
        rowCache.get().clear();
    }

    /**
     * Clears all caches for the current thread.
     */
    @Override
    public void clearCaches() {
        super.clearCaches();
        clearCaches0();
    }

    /**
     * When the table is updated, all caches are cleared for the current thread.
     */
    @Override
    public void tableUpdated() {
        super.tableUpdated();
        clearCaches0();
    }

    @Override
    public Set<? extends R> getUnsortedRows() throws SQLException {
        Set<? extends R> rows = unsortedRowsCache.get();
        if(rows==null) {
            rows = Collections.unmodifiableSet(getRowsNoCache());
            allRowsLoaded(rows);
            unsortedRowsCache.set(rows);
        }
        return rows;
    }

    /**
     * Called when all rows have been loaded at once.  This allows for subclasses
     * to populate any views or caches.
     *
     * This default implementation does nothing.
     */
    protected void allRowsLoaded(Set<? extends R> rows) throws SQLException {
        // Does nothing.
    }

    @Override
    public SortedSet<? extends R> getRows() throws SQLException {
        SortedSet<? extends R> rows = sortedRowsCache.get();
        if(rows==null) {
            rows = Collections.unmodifiableSortedSet(new TreeSet<R>(getUnsortedRows()));
            sortedRowsCache.set(rows);
        }
        return rows;
    }

    @Override
    public R get(K key) throws NoRowException, SQLException {
        Map<K,R> cache = rowCache.get();
        if(!rowCachedLoaded.get()) {
            // Load all rows in a single query
            cache.clear();
            for(R row : getUnsortedRows()) if(cache.put(canonicalize(row.getKey()), row)!=null) throw new SQLException("Duplicate key: "+row.getKey());
            rowCachedLoaded.set(Boolean.TRUE);
        }
        R row = cache.get(canonicalize(key));
        if(row==null) throw new NoRowException(getName()+" not found: "+key);
        return row;
    }

    abstract protected Set<? extends R> getRowsNoCache() throws SQLException;
}
