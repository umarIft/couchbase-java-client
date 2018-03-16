/*
 * Copyright (c) 2016 Couchbase, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.couchbase.client.java.fts.result.facets;

public class DateRange {

    private final String name;
    private final String start;
    private final String end;
    private final long count;

    public DateRange(String name, String start, String end, long count) {
        this.name = name;
        this.start = start;
        this.end = end;
        this.count = count;
    }

    public String name() {
        return name;
    }

    public String start() {
        return start;
    }

    public String end() {
        return end;
    }

    public long count() {
        return count;
    }

    @Override
    public String toString() {
        final StringBuffer sb = new StringBuffer("{");
        sb.append("name='").append(name).append('\'');
        if (start != null) {
            sb.append(", start='").append(start).append('\'');
        }
        if (end != null) {
            sb.append(", end='").append(end).append('\'');
        }
        sb.append(", count=").append(count);
        sb.append('}');
        return sb.toString();
    }
}
