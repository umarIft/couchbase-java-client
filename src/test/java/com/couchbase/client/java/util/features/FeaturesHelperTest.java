/*
 * Copyright (C) 2014 Couchbase, Inc.
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
package com.couchbase.client.java.util.features;

import static org.junit.Assert.*;

import com.couchbase.client.java.cluster.DefaultClusterInfo;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.document.json.JsonObject;
import org.junit.Test;

/**
 * Verifies the behavior of {@link FeaturesHelper}.
 *
 * @author Simon Baslé
 * @since 2.1.0
 */
public class FeaturesHelperTest {

    @Test
    public void shouldDetectCorrectMinimumDespiteGarbage() throws Exception {
        JsonObject node1 = JsonObject.create()
                .put("version", "3.0.2");
        JsonObject node2 = JsonObject.create()
                .put("version", "1.4.6-dp_1324");
        DefaultClusterInfo info = new DefaultClusterInfo(
                JsonObject.create().put("nodes", JsonArray.from(node1, node2)));

        assertEquals(new Version(1, 4, 6), FeaturesHelper.getMinVersion(info));
        assertFalse(FeaturesHelper.checkAvailable(info, CouchbaseFeature.SPATIAL_QUERY));
    }

    @Test
    public void shouldReturnFalseIfBadInfo() {
        JsonObject goodNode = JsonObject.create()
                                     .put("version", "3.0.2");
        JsonObject badNode = JsonObject.create()
                                     .put("nope", "1.4.6-dp_1324");
        DefaultClusterInfo info1 = new DefaultClusterInfo(
                JsonObject.create().put("nodess", JsonArray.from(goodNode)));
        DefaultClusterInfo info2 = new DefaultClusterInfo((JsonObject.create()
            .put("nodes", "string")));
        DefaultClusterInfo info3 = new DefaultClusterInfo((JsonObject.create()
            .put("nodes", JsonArray.from("notANode"))));
        DefaultClusterInfo info4 = new DefaultClusterInfo(JsonObject.create()
            .put("nodes", JsonArray.from(badNode)));

        assertEquals(Version.NO_VERSION, FeaturesHelper.getMinVersion(info1));
        assertEquals(Version.NO_VERSION, FeaturesHelper.getMinVersion(info2));
        assertEquals(Version.NO_VERSION, FeaturesHelper.getMinVersion(info3));
        assertEquals(Version.NO_VERSION, FeaturesHelper.getMinVersion(info4));

        assertFalse(FeaturesHelper.checkAvailable(info1, CouchbaseFeature.KV));
        assertFalse(FeaturesHelper.checkAvailable(info2, CouchbaseFeature.KV));
        assertFalse(FeaturesHelper.checkAvailable(info3, CouchbaseFeature.KV));
        assertFalse(FeaturesHelper.checkAvailable(info4, CouchbaseFeature.KV));

    }

    @Test
    public void shouldReturnFalseIfBadVersionFormat() {
        JsonObject node1 = JsonObject.create()
                                     .put("version", "3.0.2");
        JsonObject node2 = JsonObject.create()
                                     .put("version", "1z.4.6-dp_1324");
        DefaultClusterInfo info = new DefaultClusterInfo(
                JsonObject.create().put("nodes", JsonArray.from(node1, node2)));

        assertEquals(Version.NO_VERSION, FeaturesHelper.getMinVersion(info));
        assertFalse(FeaturesHelper.checkAvailable(info, CouchbaseFeature.KV));
    }
}