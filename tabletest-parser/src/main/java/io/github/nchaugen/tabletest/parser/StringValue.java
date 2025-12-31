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

/**
 * Represents a parsed string value with information about how it was quoted in the source.
 *
 * @param value the string value (without surrounding quotes)
 * @param quoteChar the character used for quoting, or null if unquoted
 */
public record StringValue(String value, Character quoteChar) {

    public static StringValue unquoted(String value) {
        return new StringValue(value, null);
    }

    public static StringValue singleQuoted(String value) {
        return new StringValue(value, '\'');
    }

    public static StringValue doubleQuoted(String value) {
        return new StringValue(value, '"');
    }

    /**
     * Returns the string value with original quotes restored.
     *
     * @return the string value with surrounding quotes as they appeared in the source
     */
    public String withQuotes() {
        return quoteChar == null ? value : quoteChar + value + quoteChar;
    }
}
