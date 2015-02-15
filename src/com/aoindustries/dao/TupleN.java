/*
 * ao-dao - Simple data access objects framework.
 * Copyright (C) 2013, 2015  AO Industries, Inc.
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

/**
 * A compound key with any number of columns of the same type.
 *
 * @author  AO Industries, Inc.
 */
public interface TupleN<
	C extends Comparable<? super C>,
	T extends TupleN<C,T> & Comparable<? super T>
>
	extends Tuple<T>
{

	@Override
	C[] getColumns();
}
