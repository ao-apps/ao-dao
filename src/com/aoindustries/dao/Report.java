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

import java.sql.SQLException;
import java.util.List;
import java.util.Map;

/**
 * One report generated from the underlying model.
 */
public interface Report {

    public static interface Parameter {
        public enum Type {
            TEXT {
                @Override
                public Object parse(String str) {
                    return str;
                }
            },
            INTEGER {
                @Override
                public Object parse(String str) {
                    return Integer.parseInt(str);
                }
            };

            /**
             * Converts this value to a string.
             */
            /*
            public String toString(Object value) {
                return value.toString();
            }*/

            /**
             * Parses this value from a string.
             */
            public abstract Object parse(String str);
        }

        /**
         * Gets the name of this parameter.
         */
        String getName();

        /**
         * Gets a display label for this parameter in the user locale.
         */
        String getLabel();

        /**
         * Gets the type of this parameter.
         */
        Type getType();

        /**
         * Gets the set of valid values or <code>null</code> if the user may
         * enter a value.
         */
        Iterable<? extends Object> getValidValues() throws SQLException;
    }

    enum Alignment {
        left,
        right,
        center
    }

    public static interface Column {
        /**
         * Gets the constant name of this column.
         */
        String getName();

        /**
         * Gets a display label for this column in the user locale.
         */
        String getLabel();

        /**
         * Gets the display alignment of this column.
         */
        Alignment getAlignment();
    }

    public static interface Result {
        List<? extends Column> getColumns() throws SQLException;

        Iterable<? extends Iterable<?>> getTableData() throws SQLException;
    }

    /**
     * Gets the constant name of this report.
     */
    String getName();

    /**
     * Gets a display title of this report in the user locale.
     */
    String getTitle();

    /**
     * Gets a display title of this report in the user locale with the provided parameters.
     */
    String getTitle(Map<String,? extends Object> parameterValues);

    /**
     * Gets a description of this report in the user locale.
     */
    String getDescription();

    /**
     * Gets a description of this report in the user locale with the provided parameters.
     */
    String getDescription(Map<String,? extends Object> parameterValues);

    /**
     * Gets the set of parameters that this report requires.
     */
    Iterable<? extends Parameter> getParameters();

    /**
     * Executes the report and gets the results.
     */
    Result executeReport(Map<String,? extends Object> parameterValues) throws SQLException;
}
