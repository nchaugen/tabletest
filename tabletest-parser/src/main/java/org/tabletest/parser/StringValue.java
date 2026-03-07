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

import java.util.Objects;

/**
 * Represents a parsed string value with information about how it was quoted in the source.
 */
public class StringValue {
    private final String value;
    private final Character quoteChar;

    public StringValue(String value, Character quoteChar) {
        this.value = value;
        this.quoteChar = quoteChar;
    }

    public String value() {
        return value;
    }

    public Character quoteChar() {
        return quoteChar;
    }

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

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof StringValue)) return false;
        StringValue other = (StringValue) obj;
        return Objects.equals(value, other.value) && Objects.equals(quoteChar, other.quoteChar);
    }

    @Override
    public int hashCode() {
        return Objects.hash(value, quoteChar);
    }

    @Override
    public String toString() {
        return "StringValue[value=" + value + ", quoteChar=" + quoteChar + "]";
    }
}
