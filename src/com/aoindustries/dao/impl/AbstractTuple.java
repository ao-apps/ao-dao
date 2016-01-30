/*
 * ao-dao - Simple data access objects framework.
 * Copyright (C) 2013, 2014, 2015, 2016  AO Industries, Inc.
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

import com.aoindustries.dao.Tuple;
import java.text.Collator;
import java.util.Arrays;

/**
 * Allows sets of columns to be used as multi-column keys.
 *
 * @author  AO Industries, Inc.
 */
abstract public class AbstractTuple<
	T extends AbstractTuple<T> & Comparable<? super T>
>
	implements Tuple<T>
{

	private final Collator collator;

	protected AbstractTuple(Collator collator) {
		this.collator = collator;
    }

	@Override
	abstract public Comparable<?>[] getColumns();

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append('(');
		Comparable<?>[] columns = getColumns();
		for(int i=0, len=columns.length; i<len; i++) {
			if(i>0) sb.append(',');
			sb.append(columns[i]);
		}
		sb.append(')');
		return sb.toString();
	}

	@Override
	public boolean equals(Object obj) {
		if(!(obj instanceof AbstractTuple<?>)) return false;
		AbstractTuple<?> other = (AbstractTuple<?>)obj;
		return Arrays.equals(getColumns(), other.getColumns());
	}

	@Override
	public int hashCode() {
		return Arrays.hashCode(getColumns());
	}

    @Override
    public int compareTo(T o) {
		Comparable<?>[] columns1 = getColumns();
		Comparable<?>[] columns2 = o.getColumns();
		int len1 = columns1.length;
		int len2 = columns2.length;
		int minLen = Math.min(len1, len2);
		for(int i=0; i<minLen; i++) {
			// Is it always possible to treat as Comparable<Object>?
			@SuppressWarnings("unchecked")
			Comparable<Object> column1 = (Comparable<Object>)columns1[i];

			Comparable<?> column2 = columns2[i];
			int diff;
			if(
				column1!=null
				&& column2!=null
				&& column1.getClass()==String.class
				&& column2.getClass()==String.class
			) {
				String s1 = column1.toString();
				String s2 = column2.toString();
				// TODO: If both strings begin with a number, sort by that first
				// TODO: This is for lot numbers, such as 1A, 1B, 2, 3, 10, 20, 100A
				diff = s1.equals(s2) ? 0 : collator.compare(s1, s2);
			} else {
				// Sort nulls as larger than any non-null
				if(column1 == null) {
					diff = column2 == null ? 0 : 1;
				} else {
					diff = column2 == null ? -1 : column1.compareTo(column2);
				}
			}
			if(diff!=0) return diff;
		}
		if(len2>minLen) return -1;
		if(len1>minLen) return 1;
		return 0;
    }
}
