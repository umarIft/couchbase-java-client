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
package com.couchbase.client.java.fts.queries;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.fts.SearchParams;
import com.couchbase.client.java.fts.SearchQuery;

public class BooleanQuery extends SearchQuery {

    private final ConjunctionQuery must;
    private final DisjunctionQuery mustNot;
    private final DisjunctionQuery should;

    public BooleanQuery(SearchParams searchParams) {
        super(searchParams);
        this.must = new ConjunctionQuery(SearchParams.build());
        this.should = new DisjunctionQuery(SearchParams.build());
        this.mustNot = new DisjunctionQuery(SearchParams.build());
    }

    public BooleanQuery shouldMin(int minForShould) {
        this.should.min(minForShould);
        return this;
    }

    public BooleanQuery must(SearchQuery... mustQueries) {
        must.and(mustQueries);
        return this;
    }

    public BooleanQuery mustNot(SearchQuery... mustNotQueries) {
        mustNot.or(mustNotQueries);
        return this;
    }
    public BooleanQuery should(SearchQuery... shouldQueries) {
        should.or(shouldQueries);
        return this;
    }

    @Override
    public BooleanQuery boost(double boost) {
        super.boost(boost);
        return this;
    }

    @Override
    protected void injectParams(JsonObject input) {
        boolean mustIsEmpty = must == null || must.childQueries().isEmpty();
        boolean mustNotIsEmpty = mustNot == null || mustNot.childQueries().isEmpty();
        boolean shouldIsEmpty = should == null || should.childQueries().isEmpty();

        if (mustIsEmpty && mustNotIsEmpty && shouldIsEmpty) {
            throw new IllegalArgumentException("Boolean query needs at least one of must, mustNot and should");
        }

        if (!mustIsEmpty) {
            JsonObject jsonMust = JsonObject.create();
            must.injectParamsAndBoost(jsonMust);
            input.put("must", jsonMust);
        }

        if (!mustNotIsEmpty) {
            JsonObject jsonMustNot = JsonObject.create();
            mustNot.injectParamsAndBoost(jsonMustNot);
            input.put("must_not", jsonMustNot);
        }

        if (!shouldIsEmpty) {
            JsonObject jsonShould = JsonObject.create();
            should.injectParamsAndBoost(jsonShould);
            input.put("should", jsonShould);
        }
    }
}
