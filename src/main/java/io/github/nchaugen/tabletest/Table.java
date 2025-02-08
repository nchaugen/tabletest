package io.github.nchaugen.tabletest;

import java.util.List;
import java.util.Objects;
import java.util.function.Function;
import java.util.stream.Stream;

public final class Table {

    private final Row header;
    private final List<Row> data;

    public Table(List<Row> rows) {
        if (rows.isEmpty()) {
            throw new IllegalArgumentException("Table must have at least one row");
        }
        this.header = rows.getFirst();
        this.data = rows.subList(1, rows.size());
    }

    public int rowCount() {
        return data.size();
    }

    public int columnCount() {
        return !data.isEmpty() ? data.getFirst().cellCount() : 0;
    }

    public <T> Stream<T> map(Function<Row, T> mapper) {
        return data.stream().map(mapper);
    }

    public Row row(int index) {
        return data.get(index);
    }

    public List<String> headers() {
        return header.cells().stream().map(Object::toString).map(String::trim).toList();
    }

    public String header(int index) {
        return headers().get(index);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == this) return true;
        if (obj == null || obj.getClass() != this.getClass()) return false;
        var that = (Table) obj;
        return Objects.equals(this.header, that.header) &&
               Objects.equals(this.data, that.data);
    }

    @Override
    public int hashCode() {
        return Objects.hash(header, data);
    }

    @Override
    public String toString() {
        return "Table[" +
               "header=" + header + ", " +
               "data=" + data + ']';
    }
}
