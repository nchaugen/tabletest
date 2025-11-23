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
package io.github.nchaugen.tabletest.renderer;

import java.util.Arrays;
import java.util.List;

import static java.util.Collections.emptyList;

public record AsciidocListFormat(AsciidocListType type, List<String> style) {
    private static final List<String> descriptionDelimiters = List.of("::", ":::", "::::", ";;");

    public String getBullet(int nestLevel) {
        return type == AsciidocListType.DESCRIPTION
            ? descriptionDelimiters.get(nestLevel % descriptionDelimiters.size())
            : type.bullet.repeat(nestLevel + 1);
    }

    public String getStyle(int nestLevel) {
        return style.isEmpty() || nestLevel >= style.size() ? "" : "\n[" + style.get(nestLevel) + "]";
    }

    public static AsciidocListFormat unordered() {
        return unordered(emptyList());
    }

    public static AsciidocListFormat unordered(String... style) {
        return unordered(Arrays.asList(style));
    }

    public static AsciidocListFormat unordered(List<String> style) {
        return new AsciidocListFormat(AsciidocListType.UNORDERED, style);
    }

    public static AsciidocListFormat ordered() {
        return ordered(emptyList());
    }

    public static AsciidocListFormat ordered(String... style) {
        return ordered(Arrays.asList(style));
    }

    public static AsciidocListFormat ordered(List<String> style) {
        return new AsciidocListFormat(AsciidocListType.ORDERED, style);
    }

    public static AsciidocListFormat description() {
        return new AsciidocListFormat(AsciidocListType.DESCRIPTION, emptyList());
    }
}
