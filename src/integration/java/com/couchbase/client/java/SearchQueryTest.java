/*
 * Copyright (C) 2015 Couchbase, Inc.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
 * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALING
 * IN THE SOFTWARE.
 */
package com.couchbase.client.java;

import static com.couchbase.client.java.fts.facet.SearchFacet.date;
import static com.couchbase.client.java.fts.facet.SearchFacet.numeric;
import static com.couchbase.client.java.fts.facet.SearchFacet.term;

import static org.assertj.core.api.Assertions.*;

import java.util.Map;

import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.fts.HighlightStyle;
import com.couchbase.client.java.fts.SearchParams;
import com.couchbase.client.java.fts.SearchQuery;
import com.couchbase.client.java.fts.queries.AbstractFtsQuery;
import com.couchbase.client.java.fts.result.SearchQueryResult;
import com.couchbase.client.java.fts.result.SearchQueryRow;
import com.couchbase.client.java.fts.result.facets.DateRange;
import com.couchbase.client.java.fts.result.facets.DateRangeFacetResult;
import com.couchbase.client.java.fts.result.facets.FacetResult;
import com.couchbase.client.java.fts.result.facets.NumericRange;
import com.couchbase.client.java.fts.result.facets.NumericRangeFacetResult;
import com.couchbase.client.java.fts.result.facets.TermFacetResult;
import com.couchbase.client.java.fts.result.facets.TermRange;
import com.couchbase.client.java.util.CouchbaseTestContext;
import com.couchbase.client.java.util.features.CouchbaseFeature;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Integration tests of the Search Query / FTS features.
 *
 * The FTS index "beer-search" must be created on the server with the following mapping for the type "beer":
 *
 *  - category | text | index | store | include in _all field | include term vectors
 *  - style | text | index | store | include in _all field | include term vectors
 *  - abv | number | index | include in _all field
 *  - updated | datetime | index | include in _all field
 *  - description | text | index | store | include in _all field | include term vectors
 *  - name | text | index | store | include in _all field | include term vectors
 *
 * @author Simon Baslé
 * @since 2.3
 */
public class SearchQueryTest {

    private static CouchbaseTestContext ctx;
    private static final String INDEX = "beer-search";

    @BeforeClass
    public static void init() throws InterruptedException {
        ctx = CouchbaseTestContext.builder()
                .bucketName("beer-sample")
                .flushOnInit(false)
                .adhoc(false)
                .build()
                .ignoreIfMissing(CouchbaseFeature.FTS_BETA);
    }

    @AfterClass
    public static void cleanup() {
        ctx.destroyBucketAndDisconnect();
    }

    @Test
    public void shouldSearchWithLimit() {
        AbstractFtsQuery query = SearchQuery.matchPhrase("hop beer");

        SearchQueryResult result = ctx.bucket().query(
                new SearchQuery(INDEX, query, SearchParams.build().limit(3)));

        assertThat(result).as("result").isNotNull();
        assertThat(result.metrics()).as("metrics").isNotNull();
        assertThat(result.metrics().totalHits()).as("totalHits").isGreaterThanOrEqualTo(3L);
        assertThat(result.hits()).as("hits").isNotEmpty();
        assertThat(result.hitsOrFail()).as("hitsOrFail").isNotEmpty();
        assertThat(result.hits()).as("hits == hitsOrFail").isEqualTo(result.hitsOrFail());
        assertThat(result.hits().size()).as("hits size").isLessThanOrEqualTo(3);
        assertThat(result.errors()).as("errors").isEmpty();

        for (SearchQueryRow row : result.hits()) {
            assertThat(row.id()).as("row id").isNotNull();
            assertThat(row.index()).as("row index").startsWith(INDEX);
            assertThat(row.score()).as("row score").isGreaterThan(0d);
            assertThat(row.explanation()).as("row explanation").isEqualTo(JsonObject.empty());
            assertThat(row.fields()).as("row fields").isEmpty();
            assertThat(row.fragments()).as("row fragments").isEmpty();
        }
    }

    @Test
    public void shouldSearchWithFields() {
        AbstractFtsQuery query = SearchQuery.matchPhrase("hop beer");

        SearchQueryResult result = ctx.bucket().query(
                new SearchQuery(INDEX, query, SearchParams.build()
                        .limit(3)
                        .fields("name")));

        for (SearchQueryRow row : result.hits()) {
            final Map<String, String> fields = row.fields();
            assertThat(fields).as("row fields size").hasSize(1);
            assertThat(fields).as("row field name").containsKey("name");
            assertThat(fields).as("row field empty name").doesNotContainEntry("name", "");
            assertThat(fields).as("row field null name").doesNotContainEntry("name", null);

            //sanity checks
            assertThat(row.id()).as("row id").isNotNull();
            assertThat(row.index()).as("row index").startsWith(INDEX);
            assertThat(row.score()).as("row score").isGreaterThan(0d);
            assertThat(row.explanation()).as("row explanation").isEqualTo(JsonObject.empty());
            assertThat(row.fragments()).as("row fragments").isEmpty();
        }
        //top level sanity checks
        assertThat(result).as("result").isNotNull();
        assertThat(result.metrics()).as("metrics").isNotNull();
        assertThat(result.metrics().totalHits()).as("totalHits").isGreaterThanOrEqualTo(3L);
        assertThat(result.hits()).as("hits").isNotEmpty();
        assertThat(result.hitsOrFail()).as("hitsOrFail").isNotEmpty();
        assertThat(result.hits()).as("hits == hitsOrFail").isEqualTo(result.hitsOrFail());
        assertThat(result.hits().size()).as("hits size").isLessThanOrEqualTo(3);
        assertThat(result.errors()).as("errors").isEmpty();
    }

    @Test
    public void shouldSearchWithFragments() {
        AbstractFtsQuery query = SearchQuery.matchPhrase("hop beer");

        SearchQueryResult result = ctx.bucket().query(
                new SearchQuery(INDEX, query, SearchParams.build()
                        .limit(3)
                        .highlight(HighlightStyle.HTML, "name")));

        for (SearchQueryRow row : result.hits()) {
            assertThat(row.fragments()).as("row fragments").isNotEmpty();

            //sanity checks
            assertThat(row.id()).as("row id").isNotNull();
            assertThat(row.index()).as("row index").startsWith(INDEX);
            assertThat(row.score()).as("row score").isGreaterThan(0d);
            assertThat(row.explanation()).as("row explanation").isEqualTo(JsonObject.empty());
            assertThat(row.fields()).as("row fields").isEmpty();
        }
        //top level sanity checks
        assertThat(result).as("result").isNotNull();
        assertThat(result.metrics()).as("metrics").isNotNull();
        assertThat(result.metrics().totalHits()).as("totalHits").isGreaterThanOrEqualTo(3L);
        assertThat(result.hits()).as("hits").isNotEmpty();
        assertThat(result.hitsOrFail()).as("hitsOrFail").isNotEmpty();
        assertThat(result.hits()).as("hits == hitsOrFail").isEqualTo(result.hitsOrFail());
        assertThat(result.hits().size()).as("hits size").isLessThanOrEqualTo(3);
        assertThat(result.errors()).as("errors").isEmpty();
    }

    @Test
    public void shouldSearchWithExplanation() {
        AbstractFtsQuery query = SearchQuery.matchPhrase("hop beer");
        SearchParams params = SearchParams.build()
                        .limit(3)
                        .explain();

        SearchQueryResult result = ctx.bucket().query(
                new SearchQuery(INDEX, query, params));
        System.out.println(query.export(params));

        for (SearchQueryRow row : result.hits()) {
            assertThat(row.explanation()).isNotEqualTo(JsonObject.empty());

            //sanity checks
            assertThat(row.id()).as("row id").isNotNull();
            assertThat(row.index()).as("row index").startsWith(INDEX);
            assertThat(row.score()).as("row score").isGreaterThan(0d);
            assertThat(row.fragments()).as("row fragments").isEmpty();
            assertThat(row.fields()).as("row fields").isEmpty();
        }
        //top level sanity checks
        assertThat(result).as("result").isNotNull();
        assertThat(result.metrics()).as("metrics").isNotNull();
        assertThat(result.metrics().totalHits()).as("totalHits").isGreaterThanOrEqualTo(3L);
        assertThat(result.hits()).as("hits").isNotEmpty();
        assertThat(result.hitsOrFail()).as("hitsOrFail").isNotEmpty();
        assertThat(result.hits()).as("hits == hitsOrFail").isEqualTo(result.hitsOrFail());
        assertThat(result.hits().size()).as("hits size").isLessThanOrEqualTo(3);
        assertThat(result.errors()).as("errors").isEmpty();
    }

    @Test
    public void shouldSearchWithFacets() {
        AbstractFtsQuery query = SearchQuery.match("beer");
        SearchParams searchParams = SearchParams.build()
                        .addFacets(term("foo", "name", 3),
                                date("bar", "updated", 1).addRange("old", null, "2014-01-01T00:00:00"),
                                numeric("baz", "abv", 2).addRange("strong", 4.9, null).addRange("light", null, 4.89)
                        );

        SearchQueryResult result = ctx.bucket().query(new SearchQuery(INDEX, query, searchParams));

        System.out.println(query.export(searchParams));
        System.out.println(result.facets());

        FacetResult f = result.facets().get("foo");
        assertThat(f).as("foo facet result").isInstanceOf(TermFacetResult.class);
        TermFacetResult foo = (TermFacetResult) f;
        assertThat(foo.name()).as("foo name").isEqualTo("foo");
        assertThat(foo.field()).as("foo field").isEqualTo("name");
        assertThat(foo.terms()).as("foo terms").hasSize(3);
        int totalFound = 0;
        for (TermRange range : foo.terms()) {
            totalFound += range.count();
            assertThat(range.count()).as("term count").isGreaterThan(0L);
        }
        assertThat(foo.total()).as("foo total == terms + other").isEqualTo(totalFound + foo.other());

        f = result.facets().get("bar");
        assertThat(f).as("bar facet result").isInstanceOf(DateRangeFacetResult.class);
        DateRangeFacetResult bar = (DateRangeFacetResult) f;
        assertThat(bar.name()).as("bar name").isEqualTo("bar");
        assertThat(bar.field()).as("bar field").isEqualTo("updated");
        assertThat(bar.dateRanges()).as("bar ranges").hasSize(1);
        totalFound = 0;
        for (DateRange range : bar.dateRanges()) {
            totalFound += range.count();
            assertThat(range.count()).as("bar range count").isGreaterThan(0L);
            assertThat(range.name()).as("bar range name").isEqualTo("old");
        }
        assertThat(bar.total()).as("bar total == ranges + other").isEqualTo(totalFound + bar.other());

        f = result.facets().get("baz");
        assertThat(f).as("baz").isInstanceOf(NumericRangeFacetResult.class);
        NumericRangeFacetResult baz = (NumericRangeFacetResult) f;
        assertThat(baz.name()).as("baz name").isEqualTo("baz");
        assertThat(baz.field()).as("baz field").isEqualTo("abv");
        assertThat(baz.numericRanges()).as("baz ranges").hasSize(2);
        totalFound = 0;
        for (NumericRange range : baz.numericRanges()) {
            totalFound += range.count();
            assertThat(range.count()).as("baz range count").isGreaterThan(0);
            assertThat(range.name()).as("baz range name").isIn("light", "strong");
        }
        assertThat(baz.total()).as("baz total == ranges + other").isEqualTo(totalFound + baz.other());
    }

    @Test
    public void shouldSetServerSideTimeoutInParamsBeforeExecuting() {
        AbstractFtsQuery query = SearchQuery.matchPhrase("salty beer");
        SearchParams searchParams = SearchParams.build();

        SearchQueryResult result = ctx.bucket().query(new SearchQuery(INDEX, query, searchParams));

        assertThat(result.status().isSuccess()).isTrue();
        assertThat(searchParams.getServerSideTimeout()).isNotNull();
        assertThat(searchParams.getServerSideTimeout()).isEqualTo(ctx.env().searchTimeout());
    }

}
