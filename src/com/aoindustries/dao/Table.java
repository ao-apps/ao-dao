/*
 * ao-dao - Simple data access objects framework.
 * Copyright (C) 2011, 2012, 2013, 2015  AO Industries, Inc.
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

import com.aoindustries.dbc.NoRowException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

public interface Table<
	K extends Comparable<? super K>,
	R extends Row<K,? extends R>
>
	extends Collection<R>
{

    /**
     * Gets the database this table is part of.
     */
    DaoDatabase getDatabase();

    /**
     * Clears the caches for this table that apply to the current thread.
     * This is used to end a caching session, generally short-term and associated
     * with a single request or process.
     *
     * Any overriding method should call super.clearCaches().
     */
    void clearCaches();

    /**
     * Called after the table is updated to ensure cache integrity.  Cache coherency
     * is maintained between users for global tables.  For per-user caches only
     * your own view is affected; no updates will be seen until the end
     * of their caching transaction, generally a web request.
     *
     * Any overriding method should call super.tableUpdated().
     */
    void tableUpdated();

    /**
     * Gets thte number of accessible rows in this table.
     * This also provides JavaBeans-compatible size.
     */
    int getSize() throws SQLException;

    /**
     * Iterates the rows in sorted order.
     * This also provides JavaBeans-compatible iterator.
     * 
     * @see #getRows() Different calls may return different results, for
     *                 snapshot-like behavior see getRows.
     */
    Iterator<? extends R> getIterator() throws SQLException;

    /**
     * @see  #getIterator()
     */
    @Override
    Iterator<R> iterator();
    
    /**
     * Gets a map view of this table.
     */
    Map<K,? extends R> getMap();

    /**
     * Gets a sorted map view of this table.
     */
    SortedMap<K,? extends R> getSortedMap();

    /**
     * Gets the table name.
     */
    String getName();

    /**
     * Gets all rows in no particular order.
     *
     * This is an unmodifiable snapshot of the data and will not change over time.
     * It may be iterated multiple times with the same results.  The contents
     * are not changed by the transactions of the current user or any other user.
     */
    Set<? extends R> getUnsortedRows() throws SQLException;

    /**
     * Gets all rows, sorted by their natural ordering.
     *
     * This is an unmodifiable snapshot of the data and will not change over time.
     * It may be iterated multiple times with the same results.  The contents
     * are not changed by the transactions of the current user or any other user.
     */
    SortedSet<? extends R> getRows() throws SQLException;

    /**
     * Gets the canonical key used for internal indexing.  In the case of case-
     * insensitive matching, the key may have upper-case and lower-case matches,
     * while the canonicalKey will convert to one format for matching.  Any
     * matches are performed on the canonical form the the query.
     */
    K canonicalize(K key);

    /**
     * Gets the row with the provided key.
     *
     * @throws NoRowException if not found
     * @throws SQLException if database error occurs
     */
    R get(K key) throws NoRowException, SQLException;

    /**
     * Gets an unmodifiable set of each object corresponding to the set of keys.
     * The elements will be in the set in the same order as the keys iterator.
     * If a key is found twice, the element we be in the position of the first
     * key.
     *
     * This is an unmodifiable snapshot of the data and will not change over time.
     * It may be iterated multiple times with the same results.  The contents
     * are not changed by the transactions of the current user or any other user.
     *
     * @throws NoRowException if any key is not found
     * @throws SQLException if database error occurs
     */
    Set<? extends R> getOrderedRows(Iterable<? extends K> keys) throws NoRowException, SQLException;

    /**
     * Gets an unmodifiable sorted set of each object corresponding to the set of
     * keys, sorted by their natural ordering.
     *
     * This is an unmodifiable snapshot of the data and will not change over time.
     * It may be iterated multiple times with the same results.  The contents
     * are not changed by the transactions of the current user or any other user.
     *
     * @throws NoRowException if any key is not found
     * @throws SQLException if database error occurs
     */
    SortedSet<? extends R> getRows(Iterable<? extends K> keys) throws NoRowException, SQLException;
}
