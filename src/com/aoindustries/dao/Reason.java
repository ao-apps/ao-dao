/*
 * ao-dao - Simple data access objects framework.
 * Copyright (C) 2011, 2013, 2015  AO Industries, Inc.
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

public interface Reason
	extends Comparable<Reason>
{

	/**
	 * Gets the textual reason.
	 */
    @Override
    String toString();

    /**
     * Merges this reason with the provided reason, if possible.
     *
     * @return  the new reason or <code>null</code> if they cannot be merged.
     */
	Reason merge(Reason other);
}
