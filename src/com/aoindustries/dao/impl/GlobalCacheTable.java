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
 * Caches results by querying the entire table upon first use, the cache is
 * persistent and shared by all users.
 * <ol>
 *   <li>All rows are loaded and stored unsorted</li>
 *   <li>allRowsLoaded is called, given unsorted rows</li>
 *   <li>Map is built upon first call to get(K)</li>
 *   <li>Rows are sorted upon first call to getRows</li>
 * </ol>
 * TODO: Coordinate invalidation between nodes in cluster
 * TODO: Coordinate invalidation between PHP and Java
 * TODO: Once both done, more aggressively use global caches for better remote database performance
 * TODO: Publish PHP version on PECL
 */
abstract public class GlobalCacheTable<
	K extends Comparable<? super K>,
	R extends Row<K,?>
>
	extends AbstractTable<K,R>
{

    private final Object unsortedRowsCacheLock = new Object();
    private Set<? extends R> unsortedRowsCache = null;

    private final Object sortedRowsCacheLock = new Object();
    private SortedSet<? extends R> sortedRowsCache = null;

    private final Object rowCacheLock = new Object();
    private boolean rowCacheLoaded = false;
    private final Map<K,R> rowCache = new HashMap<>();

    protected GlobalCacheTable(Class<K> keyClass, Class<R> rowClass, Model model) {
        super(keyClass, rowClass, model);
    }

    /**
     * Clears the global caches when the table is updated.
     */
    @Override
    public void tableUpdated() {
        super.tableUpdated();
        synchronized(unsortedRowsCacheLock) {
            unsortedRowsCache = null;
        }
        synchronized(sortedRowsCacheLock) {
            sortedRowsCache = null;
        }
        synchronized(rowCacheLock) {
            rowCacheLoaded = false;
            rowCache.clear();
        }
    }

    @Override
    public Set<? extends R> getUnsortedRows() throws SQLException {
        synchronized(unsortedRowsCacheLock) {
            Set<? extends R> rows = unsortedRowsCache;
            if(rows==null) {
                rows = Collections.unmodifiableSet(getRowsNoCache());
                allRowsLoaded(rows);
                unsortedRowsCache = rows;
            }
            return rows;
        }
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
        synchronized(sortedRowsCacheLock) {
            SortedSet<? extends R> rows = sortedRowsCache;
            if(rows==null) {
                rows = Collections.unmodifiableSortedSet(new TreeSet<R>(getUnsortedRows()));
                sortedRowsCache = rows;
            }
            return rows;
        }
    }

    @Override
    public R get(K key) throws NoRowException, SQLException {
        synchronized(rowCacheLock) {
            if(!rowCacheLoaded) {
                // Load all rows in a single query
                rowCache.clear();
                for(R row : getUnsortedRows()) if(rowCache.put(canonicalize(row.getKey()), row)!=null) throw new SQLException("Duplicate key: "+row.getKey());
                rowCacheLoaded = true;
            }
            R row = rowCache.get(canonicalize(key));
            if(row==null) throw new NoRowException(getName()+" not found: "+key);
            return row;
        }
    }

    abstract protected Set<? extends R> getRowsNoCache() throws SQLException;
}
