/*
 * ao-dao - Simple data access objects framework.
 * Copyright (C) 2016  AO Industries, Inc.
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
import com.aoindustries.dbc.NoRowException;
import com.aoindustries.util.AoCollections;
import java.sql.SQLException;
import java.util.Collections;
import java.util.Set;
import java.util.SortedSet;

/**
 * An empty table does not return any rows and never finds any object.
 */
public class EmptyTable<
	K extends Comparable<? super K>,
	R extends Row<K,? extends R>
>
	extends AbstractTable<K,R>
{

    protected EmptyTable(Class<K> keyClass, Class<R> rowClass, DaoDatabase database) {
        super(keyClass, rowClass, database);
    }

    @Override
    public Set<? extends R> getUnsortedRows() {
		return Collections.emptySet();
    }

    @Override
    public SortedSet<? extends R> getRows() {
		return AoCollections.emptySortedSet();
    }

    @Override
    public R get(K key) throws NoRowException {
		throw new NoRowException(getName()+" not found: "+key);
    }
}
