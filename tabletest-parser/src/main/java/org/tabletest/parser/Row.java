/*
 * Copyright 2025-present Nils Christian Haugen
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.tabletest.parser;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Immutable representation of a table row with cell values.
 *
 * @param values list of cell values
 */
public record Row(List<Object> values, List<String> headers) {

    public Row(List<Object> values) {
        this(values, List.of());
    }

    /**
     * Returns the number of values in this row.
     *
     * @return value count (0 if row is empty)
     */
    public int valueCount() {
        return !values.isEmpty() ? values.size() : 0;
    }

    /**
     * Conditionally removes the first value in the row returned.
     *
     * @param test boolean condition to test
     * @return new row with first value removed if test is false, otherwise this row is returned.
     */
    public Row skipFirstUnless(boolean test) {
       return test ? this : new Row(values.stream().skip(1).toList(), headers.stream().skip(1).toList());
    }

    /**
     * Conditionally removes the first value in the row returned.
     *
     * @param test boolean condition to test
     * @return new row with first value removed if test is true, otherwise this row is returned.
     */
    public Row skipFirstIf(boolean test) {
       return test ? new Row(values.stream().skip(1).toList(), headers.stream().skip(1).toList()) : this;
    }

    /**
     * Maps each value with its index to a new value.
     *
     * @param <T> result type
     * @param mapper function taking (index, value) and returning transformed value
     * @return stream of transformed values
     */
    public <T>Stream<T> mapIndexed(BiFunction<Integer, Object, T> mapper) {
        return IntStream.range(0, values.size())
            .mapToObj(i -> mapper.apply(i, values.get(i)));
    }

    /**
     * Gets value at specified index.
     *
     * @param index zero-based index
     * @return value
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public Object value(int index) {
        return values.get(index);
    }

    public Row withHeaders(List<String> headers) {
        return new Row(values, headers);
    }

    public String header(int index) {
        if (headers.isEmpty() || index < 0 || index >= headers.size()) {
            throw new IndexOutOfBoundsException("Invalid header index: " + index);
        }
        return headers.get(index);
    }
}
