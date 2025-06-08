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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.*;
import static java.util.Collections.unmodifiableList;
import static java.util.stream.Collectors.*;

/**
 * Represents the result of a parsing operation. Abstract sealed class
 * extended by Success and Failure subclasses.
 */
public abstract sealed class ParseResult permits ParseResult.Success, ParseResult.Failure {

    private ParseResult() {}

    static Success success(String value, String rest, List<Object> captures) {
        return new Success(value, rest, captures);
    }

    static Success success(String value, String rest) {
        return success(value, rest, List.of());
    }

    static Failure failure() {
        return new Failure();
    }

    /**
     * Determines if the parse operation succeeded.
     *
     * @return true if successful, false otherwise
     */
    public abstract boolean isSuccess();

    abstract String rest();

    /**
     * Retrieves captured objects from the parse result.
     * Captures represent portions of input that matched specific patterns.
     *
     * @return list of captured objects
     */
    public abstract List<Object> captures();

    ParseResult append(Supplier<ParseResult> nextResult) {
        return switch (this) {
            case Failure ignored -> this;
            case Success success -> switch (nextResult.get()) {
                case Failure nextFailure -> nextFailure;
                case Success nextSuccess -> success.append(nextSuccess);
            };
        };
    }

    static final class Success extends ParseResult {
        private final String value;
        private final String rest;
        private final List<Object> captures;

        private Success(String value, String rest, List<Object> captures) {
            this.value = value;
            this.rest = rest;
            this.captures = List.copyOf(captures);
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        @Override
        String rest() {
            return rest;
        }

        @Override
        public List<Object> captures() {
            return captures;
        }

        ParseResult.Success capture() {
            ArrayList<Object> nextCaptures = new ArrayList<>(captures);
            nextCaptures.add(value);
            return new Success(value, rest, nextCaptures);
        }

        ParseResult.Success captureTrimmed() {
            ArrayList<Object> nextCaptures = new ArrayList<>(captures);
            nextCaptures.add(value.trim());
            return new Success(value, rest, nextCaptures);
        }

        ParseResult.Success captureGroup() {
            return new Success(value, rest, List.of(List.copyOf(captures)));
        }

        ParseResult.Success captureSet() {
            return new Success(value, rest, List.of(Set.copyOf(captures)));
        }

        ParseResult.Success captureNamedElements() {
            if (captures.size() % 2 != 0) {
                throw new IllegalStateException("Capture group must have an even number of elements to form a map");
            }
            Map<String, Object> captureGroup =
                IntStream.range(0, captures.size())
                    .filter(i -> i % 2 == 0)
                    .mapToObj(i -> Map.entry((String) captures.get(i), captures.get(i + 1)))
                    .collect(toUnmodifiableMap(Map.Entry::getKey, Map.Entry::getValue));

            return new Success(value, rest, List.of(unmodifiableMap(captureGroup)));
        }

        private ParseResult.Success append(ParseResult.Success nextResult) {
            return new Success(
                value + nextResult.value,
                nextResult.rest,
                Stream.concat(captures.stream(), nextResult.captures.stream()).toList()
            );
        }

        @Override
        public boolean equals(Object o) {
            if (o == null || getClass() != o.getClass()) return false;

            Success other = (Success) o;
            return Objects.equals(value, other.value) && Objects.equals(rest, other.rest);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, rest);
        }

        @Override
        public String toString() {
            return "Result.Success[" + value + ", " + rest + ", " + captures + "]";
        }

    }

    static final class Failure extends ParseResult {
        private Failure() {}

        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        String rest() {
            return "";
        }

        @Override
        public List<Object> captures() {
            return List.of();
        }

        @Override
        public String toString() {
            return "Result.Failure";
        }

        @Override
        public int hashCode() {
            return super.hashCode();
        }

        @Override
        public boolean equals(Object other) {
            return (other != null && getClass() == other.getClass());
        }
    }
}
