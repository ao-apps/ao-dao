/*
 * ao-dao - Simple data access objects framework.
 * Copyright (C) 2011, 2013, 2015, 2016  AO Industries, Inc.
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

import com.aoindustries.dao.Reason;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public abstract class AbstractReason
	implements Reason
{

    /**
     * Merges a single reason, if not null.
     * Helper for the generation of cannot remove reasons.
     *
     * @return  the (possibly) new list.
     */
    public static List<AbstractReason> addReason(List<AbstractReason> reasons, AbstractReason newReason) {
        if(newReason!=null) {
            int size = reasons.size();
            if(size==0) {
                reasons = new ArrayList<>();
                reasons.add(newReason);
            } else {
                boolean needAdd = true;
                for(int c=0; c<size; c++) {
                    AbstractReason merged = reasons.get(c).merge(newReason);
                    if(merged!=null) {
                        reasons.set(c, merged);
                        needAdd = false;
                        break;
                    }
                }
                if(needAdd) reasons.add(newReason);
            }
        }
        return reasons;
    }

    /**
     * Combines two lists.
     * Helper for the generation of cannot remove reasons.
     *
     * @return  the (possibly) new list.
     */
    public static List<AbstractReason> addReasons(List<AbstractReason> reasons, List<AbstractReason> newReasons) {
        for(AbstractReason newReason : newReasons) reasons = addReason(reasons, newReason);
        return reasons;
    }

    /**
     * Adds a cannot remove reason if the provided collection is non-null and
     * non-empty.
     * Helper for the generation of cannot remove reasons.
     *
     * @return  the (possibly) new list.
     *
     * @see  Removable#getCannotRemoveReasons()
     */
    public static List<AbstractReason> addUsedByReason(List<AbstractReason> reasons, Collection<?> dependencies, String singularName, String pluralName) {
        // TODO: This should use application resources and keys passed-in instead of direct text
        if(dependencies!=null) {
            int count = dependencies.size();
            if(count>0) {
                reasons = addReason(
                    reasons,
                    new AggregateReason(
                        count,
                        "Used by ",
                        "Used by ",
                        ' ' + singularName + '.',
                        ' ' + pluralName + "."
                    )
                );
            }
        }
        return reasons;
    }

    @Override
    abstract public String toString();

	@Override
    abstract public AbstractReason merge(Reason other);
}
