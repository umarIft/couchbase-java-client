package com.couchbase.client.java.rbac;

import static org.junit.Assert.assertEquals;

import java.util.Arrays;
import com.couchbase.client.java.Bucket;
import com.couchbase.client.java.Cluster;
import com.couchbase.client.java.CouchbaseCluster;
import com.couchbase.client.java.auth.PasswordAuthenticator;
import com.couchbase.client.java.cluster.UserRole;
import com.couchbase.client.java.cluster.UserSettings;
import com.couchbase.client.java.search.SearchConsistency;
import com.couchbase.client.java.search.SearchQuery;
import com.couchbase.client.java.search.result.SearchQueryResult;
import com.couchbase.client.java.util.CouchbaseTestContext;
import com.couchbase.client.java.util.features.Version;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

/**
 * @author Subhashni Balakrishnan
 */
public class SearchServiceUserTest {
    private static CouchbaseTestContext ctx;
    private static String username = "searchServiceUser";
    private static String password = "password";
    private static String usernameWithNoPerms = "notASearchServiceUser";
    private static String searchIndex = "default-search";
    private static Cluster cluster;
    private static Bucket bucket;
    private static Cluster clusterWithNoFtsPerms;
    private static Bucket bucketWithNoFtsPerms;


    @BeforeClass
    public static void setup() throws Exception {
        ctx = CouchbaseTestContext.builder()
                .build()
                .ignoreIfClusterUnder(Version.parseVersion("5.0.0"))
                .ignoreIfSearchServiceNotFound()
                .ignoreIfSearchIndexDoesNotExist(searchIndex);

        ctx.clusterManager().upsertUser(username, UserSettings.build().password(password)
                .roles(Arrays.asList(new UserRole("fts_searcher", ctx.bucketName()),
                        new UserRole("data_monitoring", ctx.bucketName())))); //needs a data role similar to query else cccp will fail

        ctx.clusterManager().upsertUser(usernameWithNoPerms, UserSettings.build().password(password)
                .roles(Arrays.asList(new UserRole("data_monitoring", ctx.bucketName()))));

        Thread.sleep(100); //sleep a bit for the user to be async updated to memcached before opening bucket

        cluster = CouchbaseCluster.create(ctx.seedNode());
        cluster.authenticate(new PasswordAuthenticator(username, password));
        bucket = cluster.openBucket(ctx.bucketName());

        clusterWithNoFtsPerms = CouchbaseCluster.create(ctx.seedNode());
        clusterWithNoFtsPerms.authenticate(new PasswordAuthenticator(usernameWithNoPerms, password));
        bucketWithNoFtsPerms = clusterWithNoFtsPerms.openBucket(ctx.bucketName());
    }

    @AfterClass
    public static void tearDown() throws Exception {
        cluster.disconnect();
        clusterWithNoFtsPerms.disconnect();
        ctx.clusterManager().removeUser(username);
        ctx.clusterManager().removeUser(usernameWithNoPerms);
        ctx.destroyBucketAndDisconnect();
    }

    @Test
    public void testFtsSearchAuth() {
        SearchQueryResult result = bucket.query(new SearchQuery(searchIndex, SearchQuery.queryString("deadbeef"))
                .searchConsistency(SearchConsistency.NOT_BOUNDED));
        assertEquals("Fts search should be successful", true, result.status().isSuccess());

    }

    @Test
    @Ignore //should not work but works
    public void shouldFailForUserWithNoFtsSearchPerms() {
        bucketWithNoFtsPerms.query(new SearchQuery(searchIndex, SearchQuery.queryString("deadbeef"))
                .searchConsistency(SearchConsistency.NOT_BOUNDED));
    }
}