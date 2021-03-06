/*
 * Copyright (c) 2012, 2019 Oracle and/or its affiliates. All rights reserved.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License v. 2.0, which is available at
 * http://www.eclipse.org/legal/epl-2.0.
 *
 * This Source Code may also be made available under the following Secondary
 * Licenses when the conditions for such availability set forth in the
 * Eclipse Public License v. 2.0 are satisfied: GNU General Public License,
 * version 2 with the GNU Classpath Exception, which is available at
 * https://www.gnu.org/software/classpath/license.html.
 *
 * SPDX-License-Identifier: EPL-2.0 OR GPL-2.0 WITH Classpath-exception-2.0
 */

package org.glassfish.jersey.message.internal;

import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Quality parameter constants.
 *
 * @author Marek Potociar
 */
public final class Quality {

    /**
     * A "highest first" qualified element comparator.
     *
     * An element with higher quality value will be sorted ahead of elements with lower quality value.
     */
    public static final Comparator<Qualified> QUALIFIED_COMPARATOR = new Comparator<Qualified>() {

        @Override
        public int compare(final Qualified o1, final Qualified o2) {
            // reverse comparison to achieve the "higher first" behavior.
            return Quality.compare(o2.getQuality(), o1.getQuality());
        }
    };

    /**
     * A "highest first" quality value comparator.
     *
     * A higher quality value will be sorted ahead of a lower quality value.
     */
    public static final Comparator<Integer> QUALITY_VALUE_COMPARATOR = new Comparator<Integer>() {

        @Override
        public int compare(final Integer q1, final Integer q2) {
            // reverse comparison to achieve the "higher first" behavior.
            return Quality.compare(q2, q1);
        }
    };

    /**
     * Prevents instantiation.
     */
    private Quality() {
        throw new AssertionError("Instantiation not allowed.");
    }

    /**
     * Quality HTTP header parameter name.
     */
    public static final String QUALITY_PARAMETER_NAME = "q";
    /**
     * Quality source HTTP header parameter name.
     */
    public static final String QUALITY_SOURCE_PARAMETER_NAME = "qs";
    /**
     * Minimum quality value.
     */
    public static final int MINIMUM = 0;
    /**
     * Maximum quality value.
     */
    public static final int MAXIMUM = 1000;
    /**
     * Default quality value.
     */
    public static final int DEFAULT = MAXIMUM;

    /**
     * Add a quality parameter to a HTTP header parameter map (if needed).
     *
     * @param parameters       a map of HTTP header parameters.
     * @param qualityParamName name of the quality parameter ("q" or "qs").
     * @param quality          quality value in [ppm].
     * @return parameter map containing the proper quality parameter if necessary.
     */
    static Map<String, String> enhanceWithQualityParameter(
            final Map<String, String> parameters, final String qualityParamName, final int quality) {

        if (quality == DEFAULT) {
            // special handling
            if (parameters == null || parameters.isEmpty() || !parameters.containsKey(qualityParamName)) {
                return parameters;
            }
        }


        if (parameters == null || parameters.isEmpty()) {
            return Collections.singletonMap(qualityParamName, qualityValueToString(quality));
        }

        try {
            // Try to update the original map first...
            parameters.put(qualityParamName, qualityValueToString(quality));
            return parameters;
        } catch (final UnsupportedOperationException uoe) {
            // Unmodifiable map - let's create a new copy...
            final Map<String, String> result = new HashMap<String, String>(parameters);
            result.put(qualityParamName, qualityValueToString(quality));
            return result;
        }
    }

    /**
     * Compares two {@code int} values numerically.
     * The value returned is identical to what would be returned by:
     * <pre>
     *    Integer.valueOf(x).compareTo(Integer.valueOf(y))
     * </pre>
     *
     * Note: Taken from {@code Integer.compare()} from JDK 7.
     *
     * @param  x the first {@code int} to compare
     * @param  y the second {@code int} to compare
     * @return the value {@code 0} if {@code x == y};
     *         a value less than {@code 0} if {@code x < y}; and
     *         a value greater than {@code 0} if {@code x > y}
     */
    private static int compare(final int x, final int y) {
        return (x < y) ? -1 : ((x == y) ? 0 : 1);
    }

    private static String qualityValueToString(final float quality) {
        final StringBuilder qsb = new StringBuilder(String.format(Locale.US, "%3.3f", (quality / 1000)));

        int lastIndex;
        while ((lastIndex = qsb.length() - 1) > 2 && qsb.charAt(lastIndex) == '0') {
            qsb.deleteCharAt(lastIndex);
        }
        return qsb.toString();
    }
}
