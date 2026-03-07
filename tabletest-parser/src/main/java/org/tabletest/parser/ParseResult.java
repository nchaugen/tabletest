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

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.function.Supplier;

import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static java.util.Collections.unmodifiableList;
import static java.util.Collections.unmodifiableMap;
import static java.util.Collections.unmodifiableSet;

/**
 * Represents the result of a parsing operation.
 */
public interface ParseResult {

    static Success success(String consumed, String rest, List<Object> captures) {
        return new Success(consumed, rest, captures);
    }

    static Success success(String consumed, String rest) {
        return success(consumed, rest, emptyList());
    }

    static Failure failure(String rest) {
        return new Failure(rest);
    }

    /**
     * Determines if the parse operation succeeded.
     */
    default boolean isSuccess() {
        return this instanceof Success;
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
        return !rest().trim().isEmpty() || isFailure();
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
     */
    default ParseResult append(Supplier<ParseResult> nextResult) {
        if (this instanceof Failure) return this;
        Success success = (Success) this;
        ParseResult next = nextResult.get();
        if (next instanceof Failure) return next;
        return success.append((Success) next);
    }

    class Success implements ParseResult {
        private final String consumed;
        private final String rest;
        private final List<Object> captures;

        public Success(String consumed, String rest, List<Object> captures) {
            this.consumed = consumed;
            this.rest = rest;
            this.captures = unmodifiableList(new ArrayList<>(captures));
        }

        public String consumed() {
            return consumed;
        }

        @Override
        public String rest() {
            return rest;
        }

        @Override
        public List<Object> captures() {
            return captures;
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
            nextCaptures.add(consumed.trim().isEmpty() ? null : new StringValue(consumed.trim(), null));
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
            return new Success(consumed, rest, singletonList(unmodifiableList(new ArrayList<>(captures))));
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
            return new Success(consumed, rest, singletonList(unmodifiableSet(set)));
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
            Map<Object, Object> captureGroup = new LinkedHashMap<>();
            for (int i = 0; i < captures.size(); i += 2) {
                captureGroup.put(captures.get(i), captures.get(i + 1));
            }
            return new Success(consumed, rest, singletonList(unmodifiableMap(captureGroup)));
        }

        private Success append(Success nextResult) {
            List<Object> combined = new ArrayList<>(captures);
            combined.addAll(nextResult.captures);
            return new Success(
                consumed + nextResult.consumed,
                nextResult.rest,
                combined
            );
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Success)) return false;
            Success other = (Success) obj;
            return Objects.equals(consumed, other.consumed)
                && Objects.equals(rest, other.rest)
                && Objects.equals(captures, other.captures);
        }

        @Override
        public int hashCode() {
            return Objects.hash(consumed, rest, captures);
        }

        @Override
        public String toString() {
            return "Success[consumed=" + consumed + ", rest=" + rest + ", captures=" + captures + "]";
        }
    }

    class Failure implements ParseResult {
        private final String rest;

        public Failure(String rest) {
            this.rest = rest;
        }

        @Override
        public String rest() {
            return rest;
        }

        @Override
        public List<Object> captures() {
            return emptyList();
        }

        @Override
        public boolean equals(Object obj) {
            if (this == obj) return true;
            if (!(obj instanceof Failure)) return false;
            Failure other = (Failure) obj;
            return Objects.equals(rest, other.rest);
        }

        @Override
        public int hashCode() {
            return Objects.hash(rest);
        }

        @Override
        public String toString() {
            return "Failure[rest=" + rest + "]";
        }
    }
}
