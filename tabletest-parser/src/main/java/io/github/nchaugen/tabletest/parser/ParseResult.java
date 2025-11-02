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
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import static java.util.Collections.*;

/**
 * Represents the result of a parsing operation.
 */
public sealed interface ParseResult permits ParseResult.Success, ParseResult.Failure {

    static Success success(String consumed, String rest, List<Object> captures) {
        return new Success(consumed, rest, captures);
    }

    static Success success(String consumed, String rest) {
        return success(consumed, rest, List.of());
    }

    static Failure failure(String rest) {
        return new Failure(rest);
    }

    /**
     * Determines if the parse operation succeeded.
     * @return true if successful, false otherwise
     */
    boolean isSuccess();

    /**
     * Determines if the parse operation failed.
     * @return true if failure, false otherwise
     */
    default boolean isFailure() {
        return !isSuccess();
    }

    default boolean hasRest() {
        return !rest().isBlank();
    }

    String rest();

    /**
     * Retrieves captured objects from the parse result.
     * Captures represent portions of input that matched specific patterns.
     *
     * @return list of captured objects
     */
    List<Object> captures();

    default ParseResult append(Supplier<ParseResult> nextResult) {
        return switch (this) {
            case Failure ignored -> this;
            case Success success -> switch (nextResult.get()) {
                case Failure nextFailure -> nextFailure;
                case Success nextSuccess -> success.append(nextSuccess);
            };
        };
    }

    record Success(String consumed, String rest, List<Object> captures) implements ParseResult {
        public Success {
            captures = Collections.unmodifiableList(new ArrayList<>(captures));
        }

        @Override
        public boolean isSuccess() {
            return true;
        }

        Success capture() {
            ArrayList<Object> nextCaptures = new ArrayList<>(captures);
            nextCaptures.add(consumed);
            return new Success(consumed, rest, nextCaptures);
        }

        Success captureTrimmed() {
            ArrayList<Object> nextCaptures = new ArrayList<>(captures);
            nextCaptures.add(consumed.isBlank() ? null : consumed.trim());
            return new Success(consumed, rest, nextCaptures);
        }

        Success collectCapturesToList() {
            return new Success(consumed, rest, List.of(List.copyOf(captures)));
        }

        Success collectCapturesToSet() {
            Set<Object> set = new LinkedHashSet<>(captures.size());
            set.addAll(captures);
            return new Success(consumed, rest, List.of(Collections.unmodifiableSet(set)));
        }

        Success collectCapturesToMap() {
            if (captures.size() % 2 != 0) {
                throw new IllegalStateException("Must have an even number of captures to collect to map");
            }
            Map<String, Object> captureGroup =
                IntStream.range(0, captures.size())
                    .filter(i -> i % 2 == 0)
                    .mapToObj(i -> Map.entry((String) captures.get(i), captures.get(i + 1)))
                    .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (a, b) -> b,
                        LinkedHashMap::new
                    ));

            return new Success(consumed, rest, List.of(unmodifiableMap(captureGroup)));
        }

        private Success append(Success nextResult) {
            return new Success(
                consumed + nextResult.consumed,
                nextResult.rest,
                Stream.concat(captures.stream(), nextResult.captures.stream()).toList()
            );
        }
    }

    record Failure(String rest) implements ParseResult {
        @Override
        public boolean isSuccess() {
            return false;
        }

        @Override
        public List<Object> captures() {
            return List.of();
        }
    }
}
