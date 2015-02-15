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
package com.aoindustries.dao.impl;

import com.aoindustries.dao.DaoDatabase;
import com.aoindustries.dao.Row;
import com.aoindustries.dao.Table;
import com.aoindustries.dbc.NoRowException;
import com.aoindustries.util.AoCollections;
import com.aoindustries.util.WrappedException;
import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.TreeSet;

abstract public class AbstractTable<
	K extends Comparable<? super K>,
	R extends Row<K,? extends R>
>
	implements Table<K,R>
{

    private final Class<K> keyClass;
    private final Class<R> rowClass;
    private final DaoDatabase database;

    class TableMap implements Map<K,R> {

        @Override
        public int size() {
            return AbstractTable.this.size();
        }

        @Override
        public boolean isEmpty() {
            return size()==0;
        }

        @Override
        public boolean containsKey(Object key) {
            return get(key)!=null;
        }

        @Override
        public boolean containsValue(Object value) {
            if(value!=null && rowClass.isInstance(value)) {
                try {
                    R row = AbstractTable.this.get(rowClass.cast(value).getKey());
                    if(row==null) throw new AssertionError();
                    return true;
                } catch(NoRowException err) {
                    return false;
                } catch(SQLException err) {
                    throw new WrappedException(err);
                }
            } else {
                return false;
            }
        }

        @Override
        public R get(Object key) {
            if(key!=null && keyClass.isInstance(key)) {
                try {
                    R row = AbstractTable.this.get(keyClass.cast(key));
                    if(row==null) throw new AssertionError();
                    return row;
                } catch(NoRowException err) {
                    return null;
                } catch(SQLException err) {
                    throw new WrappedException(err);
                }
            } else {
                return null;
            }
        }

        @Override
        public R put(K key, R value) {
            throw new UnsupportedOperationException();
        }

        @Override
        public R remove(Object key) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void putAll(Map<? extends K, ? extends R> map) {
            throw new UnsupportedOperationException();
        }

        @Override
        public void clear() {
            throw new UnsupportedOperationException();
        }

        @Override
        public Set<K> keySet() {
            throw new UnsupportedOperationException("TODO: Not supported yet.");
        }

        @Override
        @SuppressWarnings("unchecked")
        public Collection<R> values() {
            try {
                return (Collection<R>)getUnsortedRows();
            } catch(SQLException err) {
                throw new WrappedException(err);
            }
        }

        @Override
        public Set<Map.Entry<K,R>> entrySet() {
            throw new UnsupportedOperationException("TODO: Not supported yet.");
        }
    }

    class TableSortedMap extends TableMap implements SortedMap<K,R> {
        @Override
        public Comparator<? super K> comparator() {
            return null;
        }

        @Override
        public SortedMap<K, R> subMap(K fromKey, K toKey) {
            throw new UnsupportedOperationException("TODO: Not supported yet.");
        }

        @Override
        public SortedMap<K, R> headMap(K toKey) {
            throw new UnsupportedOperationException("TODO: Not supported yet.");
        }

        @Override
        public SortedMap<K, R> tailMap(K fromKey) {
            throw new UnsupportedOperationException("TODO: Not supported yet.");
        }

        @Override
        public K firstKey() throws NoSuchElementException {
            try {
                return getRows().first().getKey();
            } catch(SQLException err) {
                throw new WrappedException(err);
            }
        }

        @Override
        public K lastKey() {
            try {
                return getRows().last().getKey();
            } catch(SQLException err) {
                throw new WrappedException(err);
            }
        }
    }

    protected final Map<K,R> map = new TableMap();

    protected final SortedMap<K,R> sortedMap = new TableSortedMap();

    protected AbstractTable(Class<K> keyClass, Class<R> rowClass, DaoDatabase database) {
        this.keyClass = keyClass;
        this.rowClass = rowClass;
        this.database = database;
    }

    @Override
    public DaoDatabase getDatabase() {
        return database;
    }

    /**
     * {@inheritDoc}
     *
     * This default implementation does nothing.
     */
    @Override
    public void clearCaches() {
    }

    /**
     * {@inheritDoc}
     *
     * This default implementation does nothing.
     */
    @Override
    public void tableUpdated() {
    }

    @Override
    public void clear() {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean retainAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean removeAll(Collection<?> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean addAll(Collection<? extends R> c) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        for(Object o : c) if(!map.containsValue(o)) return false;
        return true;
    }

    @Override
    public boolean remove(Object o) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean add(R e) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        try {
            return getRows().toArray(a);
        } catch(SQLException err) {
            throw new WrappedException(err);
        }
    }

    @Override
    public Object[] toArray() {
        try {
            return getRows().toArray();
        } catch(SQLException err) {
            throw new WrappedException(err);
        }
    }

    @Override
    public boolean contains(Object o) {
        return map.containsValue(o);
    }

    @Override
    public boolean isEmpty() {
        return size()==0;
    }

    @Override
    public int getSize() throws SQLException {
        return getUnsortedRows().size();
    }

    @Override
    public int size() {
        try {
            return getSize();
        } catch(SQLException err) {
            throw new WrappedException(err);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public Iterator<? extends R> getIterator() throws SQLException {
        return (Iterator<R>)getRows().iterator();
    }

    /**
     * Iterates the rows in sorted order.
     */
    @Override
	@SuppressWarnings("unchecked")
    public Iterator<R> iterator() {
        try {
            return (Iterator<R>)getIterator();
        } catch(SQLException err) {
            throw new WrappedException(err);
        }
    }

    @Override
    public Map<K,? extends R> getMap() {
        return map;
    }

    @Override
    public SortedMap<K,? extends R> getSortedMap() {
        return sortedMap;
    }

    /**
     * {@inheritDoc}  This default implementation is based on the class simple name.
     */
    @Override
    public String getName() {
        return getClass().getSimpleName();
        /*
        String name = getClass().getName();
        int dotPos = name.lastIndexOf('.');
        return dotPos==-1 ? name : name.substring(dotPos+1);
         */
    }

    /**
     * {@inheritDoc}
     *
     * This default implementation returns the key unmodified.
     */
    @Override
    public K canonicalize(K key) {
        return key;
    }

    /**
     * {@inheritDoc}  This implementation iterates through the keys calling get.
     */
    @Override
    public Set<? extends R> getOrderedRows(Iterable<? extends K> keys) throws NoRowException, SQLException {
        Iterator<? extends K> iter = keys.iterator();
        if(!iter.hasNext()) return Collections.emptySet();
        Set<R> results = new LinkedHashSet<R>();
        do {
            results.add(get(iter.next()));
        } while(iter.hasNext());
        return Collections.unmodifiableSet(results);
    }

    /**
     * {@inheritDoc}  This implementation iterates through the keys calling get.
     */
    @Override
    public SortedSet<? extends R> getRows(Iterable<? extends K> keys) throws NoRowException, SQLException {
        Iterator<? extends K> iter = keys.iterator();
        if(!iter.hasNext()) return AoCollections.emptySortedSet();
        SortedSet<R> results = new TreeSet<R>();
        do {
            results.add(get(iter.next()));
        } while(iter.hasNext());
        return Collections.unmodifiableSortedSet(results);
    }
}
