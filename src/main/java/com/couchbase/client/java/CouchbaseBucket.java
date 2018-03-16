package com.couchbase.client.java;

import com.couchbase.client.core.ClusterFacade;
import com.couchbase.client.core.config.CouchbaseBucketConfig;
import com.couchbase.client.core.message.CouchbaseResponse;
import com.couchbase.client.core.message.ResponseStatus;
import com.couchbase.client.core.message.binary.*;
import com.couchbase.client.core.message.cluster.GetClusterConfigRequest;
import com.couchbase.client.core.message.cluster.GetClusterConfigResponse;
import com.couchbase.client.core.message.config.FlushRequest;
import com.couchbase.client.core.message.config.FlushResponse;
import com.couchbase.client.core.message.query.GenericQueryRequest;
import com.couchbase.client.core.message.query.GenericQueryResponse;
import com.couchbase.client.core.message.view.ViewQueryRequest;
import com.couchbase.client.core.message.view.ViewQueryResponse;
import com.couchbase.client.java.bucket.ViewQueryMapper;
import com.couchbase.client.java.convert.Converter;
import com.couchbase.client.java.convert.JacksonJsonConverter;
import com.couchbase.client.java.document.Document;
import com.couchbase.client.java.document.JsonDocument;
import com.couchbase.client.java.document.LongDocument;
import com.couchbase.client.java.document.json.JsonObject;
import com.couchbase.client.java.query.Query;
import com.couchbase.client.java.query.QueryResult;
import com.couchbase.client.java.query.ViewQuery;
import com.couchbase.client.java.query.ViewResult;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.util.CharsetUtil;
import rx.Observable;
import rx.functions.Func1;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CouchbaseBucket implements Bucket {

  private final String bucket;
  private final String password;
  private final ClusterFacade core;
  private final Map<Class<?>, Converter<?, ?>> converters;

    public CouchbaseBucket(final ClusterFacade core, final String name, final String password) {
        bucket = name;
        this.password = password;
        this.core = core;

        converters = new HashMap<Class<?>, Converter<?, ?>>();
        converters.put(JsonDocument.class, new JacksonJsonConverter());
    }

    @Override
    public Observable<JsonDocument> get(final String id) {
        return get(id, JsonDocument.class);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D extends Document<?>> Observable<D> get(D document) {
        return (Observable<D>) get(document.id(), document.getClass());
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D extends Document<?>> Observable<D> get(final String id, final Class<D> target) {
        return core.<GetResponse>send(new GetRequest(id, bucket)).map(new Func1<GetResponse, D>() {
            @Override
            public D call(final GetResponse response) {
                Converter<?, Object> converter = (Converter<?, Object>) converters.get(target);
                Object content = response.status() == ResponseStatus.SUCCESS ? converter.decode(response.content()) : null;
                return (D) converter.newDocument(id, content, response.cas(), 0, response.status());
            }
        });
    }

    @Override
    public Observable<JsonDocument> getReplica(final String id, final ReplicaMode type) {
        return getReplica(id, JsonDocument.class, type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D extends Document<?>> Observable<D> getReplica(final D document, final ReplicaMode type) {
        return (Observable<D>) getReplica(document.id(), document.getClass(), type);
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D extends Document<?>> Observable<D> getReplica(final String id, final Class<D> target,
        final ReplicaMode type) {

        Observable<GetResponse> incoming;
        if (type == ReplicaMode.ALL) {
            incoming = core
                .<GetClusterConfigResponse>send(new GetClusterConfigRequest())
                .map(new Func1<GetClusterConfigResponse, Integer>() {
                    @Override
                    public Integer call(GetClusterConfigResponse response) {
                        CouchbaseBucketConfig conf = (CouchbaseBucketConfig) response.config().bucketConfig(bucket);
                        return conf.numberOfReplicas();
                    }
                }).flatMap(new Func1<Integer, Observable<BinaryRequest>>() {
                    @Override
                    public Observable<BinaryRequest> call(Integer max) {
                        List<BinaryRequest> requests = new ArrayList<BinaryRequest>();

                        requests.add(new GetRequest(id, bucket));
                        for (int i = 0; i < max; i++) {
                            requests.add(new ReplicaGetRequest(id, bucket, (short)(i+1)));
                        }
                        return Observable.from(requests);
                    }
                }).flatMap(new Func1<BinaryRequest, Observable<GetResponse>>() {
                    @Override
                    public Observable<GetResponse> call(BinaryRequest req) {
                        return core.send(req);
                    }
                });
        } else {
            incoming = core.send(new ReplicaGetRequest(id, bucket, (short) type.ordinal()));
        }

        return incoming.map(new Func1<GetResponse, D>() {
            @Override
            public D call(final GetResponse response) {
                Converter<?, Object> converter = (Converter<?, Object>) converters.get(target);
                Object content = response.status() == ResponseStatus.SUCCESS ? converter.decode(response.content()) : null;
                return (D) converter.newDocument(id, content, response.cas(), 0, response.status());
            }
        });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D extends Document<?>> Observable<D> insert(final D document) {
        final Converter<?, Object> converter = (Converter<?, Object>) converters.get(document.getClass());
        ByteBuf content = converter.encode(document.content());
        return core
            .<InsertResponse>send(new InsertRequest(document.id(), content, bucket))
            .map(new Func1<InsertResponse, D>() {
                @Override
                public D call(InsertResponse response) {
                    return (D) converter.newDocument(document.id(), document.content(), response.cas(),
                        document.expiry(), response.status());
                }
            });
    }

    @Override
    @SuppressWarnings("unchecked")
    public <D extends Document<?>> Observable<D> upsert(final D document) {
        final Converter<?, Object> converter = (Converter<?, Object>) converters.get(document.getClass());
        ByteBuf content = converter.encode(document.content());
        return core
            .<UpsertResponse>send(new UpsertRequest(document.id(), content, bucket))
            .map(new Func1<UpsertResponse, D>() {
                @Override
                public D call(UpsertResponse response) {
                    return (D) converter.newDocument(document.id(), document.content(), response.cas(), document.expiry(),
                        response.status());
                }
            });
    }

  @Override
  @SuppressWarnings("unchecked")
  public <D extends Document<?>> Observable<D> replace(final D document) {
    final Converter<?, Object> converter = (Converter<?, Object>) converters.get(document.getClass());
    ByteBuf content = converter.encode(document.content());
    return core.<ReplaceResponse>send(new ReplaceRequest(document.id(), content, bucket))
      .map(new Func1<ReplaceResponse, D>() {
        @Override
        public D call(ReplaceResponse response) {
            return (D) converter.newDocument(document.id(), document.content(), response.cas(), document.expiry(),
                response.status());
        }
      });
  }

  @Override
  @SuppressWarnings("unchecked")
  public <D extends Document<?>> Observable<D> remove(final D document) {
      final Converter<?, Object> converter = (Converter<?, Object>) converters.get(document.getClass());
    RemoveRequest request = new RemoveRequest(document.id(), document.cas(),
      bucket);
    return core.<RemoveResponse>send(request).map(new Func1<RemoveResponse, D>() {
      @Override
      public D call(RemoveResponse response) {
          return (D) converter.newDocument(document.id(), document.content(), document.cas(), document.expiry(),
              response.status());
      }
    });
  }

  @Override
  public Observable<JsonDocument> remove(final String id) {
    return remove(id, JsonDocument.class);
  }

  @Override
  @SuppressWarnings("unchecked")
  public <D extends Document<?>> Observable<D> remove(final String id, final Class<D> target) {
    Converter<?, ?> converter = converters.get(target);
    return remove((D) converter.newDocument(id, null, 0, 0, null));
  }

  @Override
  public Observable<ViewResult> query(final ViewQuery query) {
    final ViewQueryRequest request = new ViewQueryRequest(query.getDesign(), query.getView(), query.isDevelopment(),
        query.toString(), bucket, password);

    return core
        .<ViewQueryResponse>send(request)
        .flatMap(new ViewQueryMapper(converters))
        .map(new Func1<JsonObject, ViewResult>() {
            @Override
            public ViewResult call(JsonObject object) {
                return new ViewResult(object.getString("id"), object.get("key"), object.get("value"));
            }
        }
    );
  }

    @Override
    public Observable<QueryResult> query(final Query query) {
        return query(query.toString());
    }

    @Override
    public Observable<QueryResult> query(final String query) {
        final Converter<?, ?> converter = converters.get(JsonDocument.class);
        GenericQueryRequest request = new GenericQueryRequest(query, bucket, password);
        return core
            .<GenericQueryResponse>send(request)
            .filter(new Func1<GenericQueryResponse, Boolean>() {
                @Override
                public Boolean call(GenericQueryResponse response) {
                    return response.content() != null;
                }
            })
            .map(new Func1<GenericQueryResponse, QueryResult>() {
                @Override
                public QueryResult call(GenericQueryResponse response) {
                    ByteBuf content = Unpooled.copiedBuffer(response.content(), CharsetUtil.UTF_8);
                    QueryResult result = new QueryResult((JsonObject) converter.decode(content));
                    content.release();
                    return result;
                }
            });
    }

    @Override
    public Observable<Boolean> flush() {
        final String markerKey = "__flush_marker";
        return core
            .send(new UpsertRequest(markerKey, Unpooled.copiedBuffer(markerKey, CharsetUtil.UTF_8), bucket))
            .flatMap(new Func1<CouchbaseResponse, Observable<FlushResponse>>() {
                @Override
                public Observable<FlushResponse> call(CouchbaseResponse res) {
                    return core.send(new FlushRequest(bucket, password));
                }
            }).flatMap(new Func1<FlushResponse, Observable<? extends Boolean>>() {
                @Override
                public Observable<? extends Boolean> call(FlushResponse flushResponse) {
                    if (flushResponse.isDone()) {
                        return Observable.just(true);
                    }
                    while(true) {
                        GetResponse res = core.<GetResponse>send(new GetRequest(markerKey, bucket)).toBlocking().single();
                        if (res.status() == ResponseStatus.NOT_EXISTS) {
                            return Observable.just(true);
                        }
                    }
                }
            });
    }

    @Override
    public Observable<LongDocument> counter(final String id, final long delta, final long initial, final int expiry) {
        core.<CounterResponse>send(new CounterRequest(id, delta, initial, expiry, bucket))
        return null;
    }
}
