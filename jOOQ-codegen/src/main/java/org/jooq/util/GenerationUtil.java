/**
 * Copyright (c) 2009-2014, Data Geekery GmbH (http://www.datageekery.com)
 * All rights reserved.
 *
 * This work is dual-licensed
 * - under the Apache Software License 2.0 (the "ASL")
 * - under the jOOQ License and Maintenance Agreement (the "jOOQ License")
 * =============================================================================
 * You may choose which license applies to you:
 *
 * - If you're using this work with Open Source databases, you may choose
 *   either ASL or jOOQ License.
 * - If you're using this work with at least one commercial database, you must
 *   choose jOOQ License
 *
 * For more information, please visit http://www.jooq.org/licenses
 *
 * Apache Software License 2.0:
 * -----------------------------------------------------------------------------
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * jOOQ License and Maintenance Agreement:
 * -----------------------------------------------------------------------------
 * Data Geekery grants the Customer the non-exclusive, timely limited and
 * non-transferable license to install and use the Software under the terms of
 * the jOOQ License and Maintenance Agreement.
 *
 * This library is distributed with a LIMITED WARRANTY. See the jOOQ License
 * and Maintenance Agreement for more details: http://www.jooq.org/licensing
 */
package org.jooq.util;

import static java.util.Arrays.asList;
import static java.util.Collections.unmodifiableSet;

import java.util.HashSet;
import java.util.Set;

import org.jooq.SQLDialect;
import org.jooq.exception.SQLDialectNotSupportedException;
import org.jooq.util.h2.H2DataType;

/**
 * @author Lukas Eder
 */
class GenerationUtil {

    private static Set<String> JAVA_KEYWORDS = unmodifiableSet(new HashSet<String>(asList(
         "abstract",
         "assert",
         "boolean",
         "break",
         "byte",
         "case",
         "catch",
         "char",
         "class",
         "const",
         "continue",
         "default",
         "double",
         "do",
         "else",
         "enum",
         "extends",
         "false",
         "final",
         "finally",
         "float",
         "for",
         "goto",
         "if",
         "implements",
         "import",
         "instanceof",
         "interface",
         "int",
         "long",
         "native",
         "new",
         "package",
         "private",
         "protected",
         "public",
         "return",
         "short",
         "static",
         "strictfp",
         "super",
         "switch",
         "synchronized",
         "this",
         "throw",
         "throws",
         "transient",
         "true",
         "try",
         "void",
         "volatile",
         "while")));

    /**
     * Take a literal (e.g. database column) and make it a Java identifier to be
     * used without case-change as an enum identifier
     * <p>
     * [#959] These literals are escaped if they collide with reserved words.
     * This implementation is meant as a fix for [#959]. These types of
     * collisions have to be generally reviewed again, when allowing for more
     * control over generated source code, as of [#408][#911]
     */
    public static String convertToJavaIdentifier(String literal) {
        if (JAVA_KEYWORDS.contains(literal)) {
            return literal + "_";
        }

        StringBuilder sb = new StringBuilder();

        if ("".equals(literal)) {
            return "_";
        }

        for (int i = 0; i < literal.length(); i++) {
            char c = literal.charAt(i);

            if (!Character.isJavaIdentifierPart(c)) {
                sb.append(escape(c));
            }
            else if (i == 0 && !Character.isJavaIdentifierStart(literal.charAt(0))) {
                sb.append("_");
                sb.append(c);
            }
            else {
                sb.append(c);
            }
        }

        return sb.toString();
    }

    private static String escape(char c) {
        if (c == ' ' || c == '-')
            return "_";
        else
            return "_" + Integer.toHexString(c);
    }

    /**
     * Take a qualified Java type and make it a simple type
     *
     * @see Class#getSimpleName()
     */
    static String getSimpleJavaType(String qualifiedJavaType) {
        if (qualifiedJavaType == null) {
            return null;
        }

        return qualifiedJavaType.replaceAll(".*\\.", "");
    }

    /**
     * Gets the base type for an array type, depending on the RDBMS dialect
     */
    static String getArrayBaseType(SQLDialect dialect, String t, String u) {
        switch (dialect) {
            case POSTGRES: {

                // The convention is to prepend a "_" to a type to get an array type
                if (u != null && u.startsWith("_")) {
                    return u.substring(1);
                }

                // But there are also arrays with a "vector" suffix
                else {
                    return u;
                }
            }

            case H2: {
                return H2DataType.OTHER.getTypeName();
            }

            /* [pro] xx
            xxxx xxxxx
            xx [/pro] */
            case HSQLDB: {

                // In HSQLDB 2.2.5, there has been an incompatible INFORMATION_SCHEMA change around the
                // ELEMENT_TYPES view. Arrays are now described much more explicitly
                if ("ARRAY".equalsIgnoreCase(t)) {
                    return "OTHER";
                }

                // This is for backwards compatibility
                else {
                    return t.replace(" ARRAY", "");
                }
            }
        }

        throw new SQLDialectNotSupportedException("getArrayBaseType() is not supported for dialect " + dialect);
    }

    /**
     * Generate a range between two bounds
     *
     * @param from The lower bound (inclusive)
     * @param to The upper bound (inclusive)
     * @return A range from <code>from</code> to <code>to</code>
     */
    public static Integer[] range(Integer from, Integer to) {
        Integer[] result = new Integer[to - from + 1];

        for (int i = from; i <= to; i++) {
            result[i - from] = i;
        }

        return result;
    }
}
