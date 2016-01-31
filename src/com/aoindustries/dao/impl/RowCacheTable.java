/*
 * ao-dao - Simple data access objects framework.
 * Copyright (C) 2011, 2012, 2013, 2014, 2015, 2016  AO Industries, Inc.
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
import com.aoindustries.lang.ObjectUtils;
import java.sql.SQLException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 * Caches results on a per-row basis.
 */
abstract public class RowCacheTable<
	K extends Comparable<? super K>,
	R extends Row<K,?>
>
	extends AbstractTable<K,R>
{

    protected final ThreadLocal<Set<? extends R>> unsortedRowsCache = new ThreadLocal<>();

    private final ThreadLocal<SortedSet<? extends R>> sortedRowsCache = new ThreadLocal<>();

    private final ThreadLocal<Map<K,R>> rowCache = new ThreadLocal<Map<K,R>>() {
        @Override
        protected Map<K,R> initialValue() {
            return new HashMap<>();
        }
    };

    protected RowCacheTable(Class<K> keyClass, Class<R> rowClass, Model model) {
        super(keyClass, rowClass, model);
    }

    private void clearCaches0() {
        unsortedRowsCache.remove();
        sortedRowsCache.remove();
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

            // Populate rowCache fully
            Map<K,R> cache = rowCache.get();
            cache.clear();
            for(R row : rows) if(cache.put(canonicalize(row.getKey()), row)!=null) throw new SQLException("Duplicate key: "+row.getKey());

            allRowsLoaded(rows);
            unsortedRowsCache.set(rows);
        }
        return rows;
    }

    /**
     * Called when all rows have been loaded at once.  This allows for subclasses
     * to populate any views or caches in a more efficient manner than row-by-row.
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
    public R get(final K key) throws NoRowException, SQLException {
        final K canonicalKey = canonicalize(key);
        Map<K,R> cache = rowCache.get();
        if(cache.containsKey(canonicalKey)) {
            R row = cache.get(canonicalKey);
            if(row==null) throw new NoRowException(getName()+" not found: "+key);
            return row;
        }

        // Doesn't exist when all rows have been loaded
        if(unsortedRowsCache.get()!=null) throw new NoRowException(getName()+" not found: "+key);

        // Try single row query - cache hits and misses
        try {
            R row = getNoCache(canonicalKey);
            addToCache(canonicalKey, row);
            return row;
        } catch(NoRowException err) {
            cache.put(canonicalKey, null);
            throw new NoRowException(getName()+" not found: "+key, err);
        }
    }

    /**
     * Adds a single object to the cache.
     */
    protected void addToCache(K canonicalKey, R row) {
        assert ObjectUtils.equals(canonicalize(row.getKey()), canonicalKey);
        rowCache.get().put(canonicalKey, row);
    }

    abstract protected R getNoCache(K canonicalKey) throws NoRowException, SQLException;

    abstract protected Set<? extends R> getRowsNoCache() throws SQLException;
}
