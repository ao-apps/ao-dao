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

import com.aoindustries.dao.Model;
import com.aoindustries.dao.Row;
import com.aoindustries.dao.Table;
import com.aoindustries.dbc.NoRowException;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.SortedMap;
import java.util.SortedSet;

/**
 * An empty table does not return any rows and never finds any object.
 */
public class EmptyTable<
	K extends Comparable<? super K>,
	R extends Row<K,?>
>
	implements Table<K,R>
{

    private final Model model;

	protected EmptyTable(Model model) {
        this.model = model;
    }

    @Override
    public Model getModel() {
        return model;
    }

	@Override
	public Map<K, ? extends R> getMap() {
		return Collections.emptyMap();
	}

	@Override
	public SortedMap<K, ? extends R> getSortedMap() {
		return Collections.emptySortedMap();
	}

	@Override
    public Set<? extends R> getUnsortedRows() {
		return Collections.emptySet();
    }

    @Override
    public SortedSet<? extends R> getRows() {
		return Collections.emptySortedSet();
    }

    @Override
    public R get(K key) throws NoRowException {
		throw new NoRowException(getName()+" not found: "+key);
    }
}
