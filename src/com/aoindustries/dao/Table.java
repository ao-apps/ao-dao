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
package com.aoindustries.dao;

import com.aoindustries.dbc.NoRowException;
import com.aoindustries.util.WrappedException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

public interface Table<
	K extends Comparable<? super K>,
	R extends Row<K,?>
>
	extends Collection<R>
{

    /**
     * Gets the model this table is part of.
     */
    Model getModel();

    /**
     * Clears the caches for this table that apply to the current thread.
     * This is used to end a caching session, generally short-term and associated
     * with a single request or process.
     *
     * Any overriding method should call super.clearCaches().
     */
	default void clearCaches() {
		// Do nothing
	}

    /**
     * Called after the table is updated to ensure cache integrity.  Cache coherency
     * is maintained between users for global tables.  For per-user caches only
     * your own view is affected; no updates will be seen until the end
     * of their caching transaction, generally a web request.
     *
     * Any overriding method should call super.tableUpdated().
     */
	default void tableUpdated() {
		// Do nothing
	}

    @Override
	default void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    default boolean addAll(Collection<? extends R> c) {
        throw new UnsupportedOperationException();
    }

    @Override
	default boolean containsAll(Collection<?> c) {
        for(Object o : c) if(!contains(o)) return false;
        return true;
    }

    @Override
	default boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
	default boolean add(R e) {
        throw new UnsupportedOperationException();
    }

    @Override
    default <T> T[] toArray(T[] a) {
        try {
            return getRows().toArray(a);
        } catch(SQLException err) {
            throw new WrappedException(err);
        }
    }

    @Override
	default Object[] toArray() {
        try {
            return getRows().toArray();
        } catch(SQLException err) {
            throw new WrappedException(err);
        }
    }

    @Override
    default boolean contains(Object o) {
        return getMap().containsValue(o);
    }

    @Override
    default boolean isEmpty() {
        return size()==0;
    }

	/**
     * Gets the number of accessible rows in this table.
     * This also provides JavaBeans-compatible size.
     */
    default int getSize() throws SQLException {
        return getUnsortedRows().size();
    }

    @Override
    default int size() {
        try {
            return getSize();
        } catch(SQLException err) {
            throw new WrappedException(err);
        }
    }

	/**
     * Iterates the rows in sorted order.
     * This also provides JavaBeans-compatible iterator.
     * 
     * @see #getRows() Different calls may return different results, for
     *                 snapshot-like behavior see getRows.
     */
    @SuppressWarnings("unchecked")
    default Iterator<? extends R> getIterator() throws SQLException {
        return getRows().iterator();
    }

    /**
     * Iterates the rows in sorted order.
	 *
     * @see  #getIterator()
     */
    @Override
	@SuppressWarnings("unchecked")
    default Iterator<R> iterator() {
        try {
            return (Iterator<R>)getIterator();
        } catch(SQLException err) {
            throw new WrappedException(err);
        }
    }
    
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
	default String getName() {
		// This default implementation is based on the class simple name.
        return getClass().getSimpleName();
        /*
        String name = getClass().getName();
        int dotPos = name.lastIndexOf('.');
        return dotPos==-1 ? name : name.substring(dotPos+1);
         */
	}

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
	default K canonicalize(K key) {
		// This default implementation returns the key unmodified.
        return key;
	}

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
    default Set<? extends R> getOrderedRows(Iterable<? extends K> keys) throws NoRowException, SQLException {
		// This implementation iterates through the keys calling get.
        Iterator<? extends K> iter = keys.iterator();
        if(!iter.hasNext()) return Collections.emptySet();
        Set<R> results = new LinkedHashSet<>();
        do {
            results.add(get(iter.next()));
        } while(iter.hasNext());
        return Collections.unmodifiableSet(results);
    }

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
    default SortedSet<? extends R> getRows(Iterable<? extends K> keys) throws NoRowException, SQLException {
		// This implementation iterates through the keys calling get.
        Iterator<? extends K> iter = keys.iterator();
        if(!iter.hasNext()) return Collections.emptySortedSet();
        SortedSet<R> results = new TreeSet<>();
        do {
            results.add(get(iter.next()));
        } while(iter.hasNext());
        return Collections.unmodifiableSortedSet(results);
    }
}
