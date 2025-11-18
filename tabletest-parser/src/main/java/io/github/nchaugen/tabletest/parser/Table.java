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
package io.github.nchaugen.tabletest.parser;

import java.util.List;
import java.util.function.Function;
import java.util.stream.Stream;

import static java.util.Objects.requireNonNull;

/**
 * Immutable representation of a table with header row and data rows.
 */
public record Table(Row header, List<Row> rows) {

    /**
     * Creates a table with the provided rows. First row expected to be the header,
     * remaining rows expected to be the data.
     *
     * @param table list containing at least one row (header)
     * @throws IllegalArgumentException if rows is empty
     */
    public Table(List<Row> table) {
        this(validateAndGetHeader(table), getDataRows(table));
    }

    private static Row validateAndGetHeader(List<Row> table) {
        requireAtLeastOneRow(table);
        return table.getFirst();
    }

    private static List<Row> getDataRows(List<Row> table) {
        return table.subList(1, table.size());
    }

    private static void requireAtLeastOneRow(List<Row> table) {
        if (table == null || table.isEmpty()) {
            throw new IllegalArgumentException("Table must have at least one row");
        }
    }

    public Table {
        requireNonNull(header, "Header row cannot be null");
        requireNonNull(rows, "Data rows cannot be null");
    }

    /**
     * Returns the number of data rows (excludes header).
     *
     * @return count of data rows
     */
    public int rowCount() {
        return rows.size();
    }

    /**
     * Returns the number of columns based on the header row.
     *
     * @return column count
     */
    public int columnCount() {
        return header.valueCount();
    }

    /**
     * Applies a mapping function to each data row.
     *
     * @param <T> type of transformed objects
     * @param mapper function to transform rows
     * @return stream of transformed objects
     */
    public <T> Stream<T> map(Function<Row, Stream<T>> mapper) {
        return rows.stream().flatMap(mapper);
    }

    /**
     * Retrieves data row by index.
     *
     * @param index zero-based row index
     * @return row at the specified index
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public Row row(int index) {
        return rows.get(index);
    }

    /**
     * Returns the header row as a list of trimmed strings.
     *
     * @return list of header values
     */
    public List<String> headers() {
        return header.values().stream().map(Object::toString).map(String::trim).toList();
    }

    /**
     * Gets header value at the specified column index.
     *
     * @param index zero-based column index
     * @return trimmed header value
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public String header(int index) {
        return headers().get(index);
    }

    public Table withHeadersInRows() {
        return new Table(
            header,
            rows.stream()
                .map(row -> row.withHeaders(this.headers()))
                .toList()
        );
    }

}
