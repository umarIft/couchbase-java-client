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
package com.couchbase.client.java;

import com.couchbase.client.java.error.BucketDoesNotExistException;
import com.couchbase.client.java.error.InvalidPasswordException;
import com.couchbase.client.java.util.TestProperties;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;

/**
 * Basic test cases which verify functionality not bound to a {@link Bucket}.
 *
 * @author Michael Nitschinger
 * @since 2.0
 */
public class ConnectionTest  {

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfBucketIsNull() {
        Cluster cluster = CouchbaseCluster.create(TestProperties.seedNode());
        cluster.openBucket(null);
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowIllegalArgumentExceptionIfBucketIsEmpty() {
        Cluster cluster = CouchbaseCluster.create(TestProperties.seedNode());
        cluster.openBucket("");
    }

    @Test(expected = BucketDoesNotExistException.class)
    public void shouldThrowConfigurationExceptionForWrongBucketName() {
        Cluster cluster = CouchbaseCluster.create(TestProperties.seedNode());
        cluster.openBucket("someWrongBucketName");
    }

    @Test(expected = InvalidPasswordException.class)
    public void shouldThrowConfigurationExceptionForWrongBucketPassword() {
        Cluster cluster = CouchbaseCluster.create(TestProperties.seedNode());
        cluster.openBucket(TestProperties.bucket(), "completelyWrongPassword");
    }

    @Test
    public void shouldCacheBucketReference() {
        Cluster cluster = CouchbaseCluster.create(TestProperties.seedNode());
        Bucket bucket1 = cluster.openBucket(TestProperties.bucket(), TestProperties.password());
        Bucket bucket2 = cluster.openBucket(TestProperties.bucket(), TestProperties.password());

        assertEquals(bucket1.hashCode(), bucket2.hashCode());

        assertFalse(bucket1.isClosed());
        assertFalse(bucket2.isClosed());
        bucket1.close();
        assertTrue(bucket1.isClosed());
        assertTrue(bucket2.isClosed());

        Bucket bucket3 = cluster.openBucket(TestProperties.bucket(), TestProperties.password());

        assertNotEquals(bucket1.hashCode(), bucket3.hashCode());

        assertFalse(bucket3.isClosed());
    }
}
