package io.github.nchaugen.tabletest;

import java.util.List;
import java.util.function.BiFunction;
import java.util.stream.IntStream;
import java.util.stream.Stream;

public record Row(List<Object> cells) {

    public int cellCount() {
        return !cells.isEmpty() ? cells.size() : 0;
    }

    public <T>Stream<T> mapIndexed(BiFunction<Integer, Object, T> mapper) {
        return IntStream.range(0, cells.size())
            .mapToObj(i -> mapper.apply(i, cells.get(i)));
    }

    public Object cell(int index) {
        return cells.get(index);
    }

}
