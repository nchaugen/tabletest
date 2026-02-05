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

import java.util.*;
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
     */
    default boolean isSuccess() {
        return switch(this) {
            case Success __ -> true;
            case Failure __ -> false;
        };
    }

    /**
     * Determines if the parse operation failed.
     */
    default boolean isFailure() {
        return !isSuccess();
    }

    /**
     * Determines if the parse operation is incomplete, that is, if there is more input to parse or if the parse failed.
     */
    default boolean isIncomplete() {
        return !rest().isBlank() || isFailure();
    }

    String rest();

    /**
     * Retrieves captured values from the parse result.
     * Captures represent parsed values that was stored using the capture or captureTrimmed methods.
     */
    List<Object> captures();

    /**
     * Concatenates two parse results if both succeeded, joining the consumed strings and captures,
     * and keeping the rest of the appended. If this parse result is a failure, nothing is appended.
     * If the next parse result is a failure, the next parse result is returned.
     * @param nextResult
     * @return
     */
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


        /**
         * Stores the parsed value as a capture. If nothing was consumed, an empty string is stored.
         */
        Success capture(Character quoteChar) {
            ArrayList<Object> nextCaptures = new ArrayList<>(captures);
            nextCaptures.add(new StringValue(consumed, quoteChar));
            return new Success(consumed, rest, nextCaptures);
        }

        /**
         * Stores the parsed value as a capture, trimming off leading and trailing whitespace.
         * An empty or blank value is stored as null.
         */
        Success captureTrimmed() {
            ArrayList<Object> nextCaptures = new ArrayList<>(captures);
            nextCaptures.add(consumed.isBlank() ? null : new StringValue(consumed.trim(), null));
            return new Success(consumed, rest, nextCaptures);
        }

        /**
         * Collects captured values into a list, retaining capture order. Null values are not allowed.
         *
         * @throws TableTestParseException if any of the captured values are null
         */
        Success collectCapturesToList() {
            if (captures.stream().anyMatch(Objects::isNull)) {
                throw new TableTestParseException("Cannot collect null values to list: " + captures);
            }
            return new Success(consumed, rest, List.of(List.copyOf(captures)));
        }

        /**
         * Collects captured values into a set, retaining capture order. Null values are not allowed.
         *
         * @throws TableTestParseException if any of the captured values are null
         */
        Success collectCapturesToSet() {
            if (captures.stream().anyMatch(Objects::isNull)) {
                throw new TableTestParseException("Cannot collect null values to set: " + captures);
            }
            Set<Object> set = new LinkedHashSet<>(captures);
            return new Success(consumed, rest, List.of(Collections.unmodifiableSet(set)));
        }

        /**
         * Collects captured values into a map, pairwise transforming them into key-value pairs.
         * Capture order is retained, so first two captures becomes the first element of the map,
         * the next two become the second, etc. Null values are not allowed.
         *
         * @throws TableTestParseException if there is an uneven number of captures or any of the values are null
         */
        Success collectCapturesToMap() {
            if (captures.size() % 2 != 0) {
                throw new TableTestParseException("Must have an even number of captures to collect to map");
            }
            if (captures.stream().anyMatch(Objects::isNull)) {
                throw new TableTestParseException("Cannot collect null values to map: " + captures);
            }
            Map<Object, Object> captureGroup =
                IntStream.range(0, captures.size())
                    .filter(i -> i % 2 == 0)
                    .mapToObj(i -> Map.entry(captures.get(i), captures.get(i + 1)))
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
        public List<Object> captures() {
            return List.of();
        }
    }
}
