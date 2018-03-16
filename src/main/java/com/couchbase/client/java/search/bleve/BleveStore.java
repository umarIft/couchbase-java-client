/**
 * Copyright (C) 2015 Couchbase, Inc.
 * <p/>
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 * <p/>
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 * <p/>
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */

package com.couchbase.client.java.search.bleve;

import com.couchbase.client.java.document.json.JsonObject;

/**
 * @author Sergey Avseyev
 */
public class BleveStore {
    public static final String KV_STORE_NAME = "forestdb";

    private final String kvStoreName;
    
    protected BleveStore(Builder builder) {
        kvStoreName = builder.kvStoreName;
    }

    public String kvStoreName() {
        return kvStoreName;
    }

    public static Builder builder() {
        return new Builder();
    }

    public JsonObject json() {
        JsonObject json = JsonObject.create();
        json.put("kvStoreName", kvStoreName);
        return json;
    }

    public static class Builder {

        private String kvStoreName = KV_STORE_NAME;

        protected Builder() {
        }

        public BleveStore build() {
            return new BleveStore(this);
        }

        public Builder kvStoreName(String kvStoreName) {
            this.kvStoreName = kvStoreName;
            return this;
        }
    }

}
