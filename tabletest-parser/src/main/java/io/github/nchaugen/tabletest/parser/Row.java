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
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Immutable representation of a table row with cell values.
 *
 * @param cells list of cell values
 */
public record Row(List<Object> cells) {

    /**
     * Returns the number of cells in this row.
     *
     * @return cell count (0 if row is empty)
     */
    public int cellCount() {
        return !cells.isEmpty() ? cells.size() : 0;
    }

    /**
     * Maps each cell with its index to a new value.
     *
     * @param <T> result type
     * @param mapper function taking (index, value) and returning transformed value
     * @return stream of transformed values
     */
    public <T>Stream<T> mapIndexed(BiFunction<Integer, Object, T> mapper) {
        return IntStream.range(0, cells.size())
            .mapToObj(i -> mapper.apply(i, cells.get(i)));
    }

    /**
     * Gets cell value at specified index.
     *
     * @param index zero-based cell index
     * @return cell value
     * @throws IndexOutOfBoundsException if index is invalid
     */
    public Object cell(int index) {
        return cells.get(index);
    }

}
