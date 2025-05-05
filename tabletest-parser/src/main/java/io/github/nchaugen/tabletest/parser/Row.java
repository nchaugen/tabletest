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
