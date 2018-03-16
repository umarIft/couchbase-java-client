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
package com.couchbase.client.java.datastructures;

import java.util.ArrayList;
import java.util.List;

import com.couchbase.client.core.CouchbaseException;
import com.couchbase.client.core.message.ResponseStatus;
import com.couchbase.client.core.message.kv.subdoc.multi.Lookup;
import com.couchbase.client.core.message.kv.subdoc.multi.Mutation;
import com.couchbase.client.java.CouchbaseAsyncBucket;
import com.couchbase.client.java.document.JsonArrayDocument;
import com.couchbase.client.java.document.json.JsonArray;
import com.couchbase.client.java.error.DocumentAlreadyExistsException;
import com.couchbase.client.java.error.DocumentDoesNotExistException;
import com.couchbase.client.java.error.subdoc.MultiMutationException;
import com.couchbase.client.java.error.subdoc.PathInvalidException;
import com.couchbase.client.java.subdoc.DocumentFragment;
import com.couchbase.client.java.subdoc.SubdocOperationResult;
import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

/**
 * Couchbase list data structure backed by JsonArrayDocument
 *
 * @param <E> Type of element
 * @author subhashni
 */

public class CouchbaseList<E> {

    private final String docId;
    private final CouchbaseAsyncBucket bucket;

    public CouchbaseList(CouchbaseAsyncBucket bucket, String docId) {
        this.bucket = bucket;
        this.docId = docId;
    }

    private Observable<JsonArrayDocument> createDocument(final E element) {
        return bucket.upsert(JsonArrayDocument.create(docId, JsonArray.create().add(element)));
    }

    public Observable<E> get(final int index) {
        final Func1<DocumentFragment<Lookup>, E> mapResult = new
                Func1<DocumentFragment<Lookup>, E>() {
                    @Override
                    public E call(DocumentFragment<Lookup> documentFragment) {
                        ResponseStatus status = documentFragment.status(0);
                        if (status == ResponseStatus.SUCCESS) {
                            return (E) documentFragment.content(0);
                        } else {
                            if (status == ResponseStatus.SUBDOC_PATH_NOT_FOUND) {
                                throw new IndexOutOfBoundsException();
                            } else {
                                throw new CouchbaseException(status.toString());
                            }
                        }
                    }
                };

        final Func1<Throwable, Observable<? extends E>> convertSubdocException = new
                Func1<Throwable, Observable<? extends E>>() {
                    @Override
                    public Observable<? extends E> call(Throwable throwable) {
                        if (throwable instanceof PathInvalidException) {
                            return Observable.error(new IndexOutOfBoundsException());
                        }
                        return Observable.error(throwable);
                    }
                };

        return Observable.defer(new Func0<Observable<E>>() {
            @Override
            public Observable<E> call() {
                return bucket.lookupIn(docId).get("[" + Integer.toString(index) + "]")
                        .execute()
                        .map(mapResult)
                        .onErrorResumeNext(convertSubdocException);
            }
        });
    }

    public Observable<Boolean> push(final E element) {
        return push(element, MutationOptionBuilder.build());
    }

    public Observable<Boolean> push(final E element, final MutationOptionBuilder optionBuilder) {
        return Observable.defer(new Func0<Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call() {
                return subdocPushLast(element, optionBuilder)
                        .map(getMapResultFnForPushAndShift());
            }
        });
    }

    private Observable<DocumentFragment<Mutation>> subdocPushLast(final E element,
                                                                  final MutationOptionBuilder optionBuilder) {
        final Func1<JsonArrayDocument, DocumentFragment<Mutation>> mapFullDocResulttoSubdoc = new
                Func1<JsonArrayDocument, DocumentFragment<Mutation>>() {
                    @Override
                    public DocumentFragment<Mutation> call(JsonArrayDocument document) {
                        List<SubdocOperationResult<Mutation>> list;
                        list = new ArrayList<SubdocOperationResult<Mutation>>();
                        list.add(SubdocOperationResult.createResult(null, Mutation.ARRAY_PUSH_LAST,
                                ResponseStatus.SUCCESS, element));
                        return new DocumentFragment<Mutation>(document.id(), document.cas(),
                                document.mutationToken(),
                                list);
                    }
                };

        final Func1<Throwable, Observable<? extends DocumentFragment<Mutation>>> retryIfDocExists = new
                Func1<Throwable, Observable<? extends DocumentFragment<Mutation>>>() {
                    @Override
                    public Observable<? extends DocumentFragment<Mutation>> call(Throwable throwable) {
                        if (throwable instanceof DocumentAlreadyExistsException) {
                            return subdocPushLast(element, optionBuilder);
                        } else {
                            return Observable.error(throwable);
                        }
                    }
                };
        return bucket.mutateIn(docId).arrayAppend("", element, false)
                .withCas(optionBuilder.cas)
                .withDurability(optionBuilder.persistTo, optionBuilder.replicateTo)
                .withExpiry(optionBuilder.expiry)
                .execute()
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends DocumentFragment<Mutation>>>() {
                    @Override
                    public Observable<? extends DocumentFragment<Mutation>> call(Throwable throwable) {
                        if (throwable instanceof DocumentDoesNotExistException) {
                            return createDocument(element)
                                    .map(mapFullDocResulttoSubdoc)
                                    .onErrorResumeNext(retryIfDocExists);
                        } else {
                            if (throwable instanceof MultiMutationException) {
                                ResponseStatus status = ((MultiMutationException) throwable).firstFailureStatus();
                                List<SubdocOperationResult<Mutation>> list;
                                list = new ArrayList<SubdocOperationResult<Mutation>>();
                                list.add(SubdocOperationResult.createResult(null, Mutation.ARRAY_PUSH_LAST,
                                        status, null));
                                return Observable.just(new DocumentFragment<Mutation>(null, 0,
                                        null,
                                        list));
                            } else {
                                return Observable.error(throwable);
                            }
                        }
                    }
                });
    }

    public Observable<Boolean> remove(int index) {
        return remove(index, MutationOptionBuilder.build());
    }

    public Observable<Boolean> remove(final int index, final MutationOptionBuilder mutationOptionBuilder) {
        return Observable.defer(new Func0<Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call() {
                return subdocRemove(index, mutationOptionBuilder)
                        .map(getMapResultFnForRemoveAndSet());
            }
        });
    }

    private Observable<DocumentFragment<Mutation>> subdocRemove(final int index,
                                                                final MutationOptionBuilder mutationOptionBuilder) {
        return bucket.mutateIn(docId).remove("[" + Integer.toString(index) + "]")
                .withCas(mutationOptionBuilder.cas)
                .withExpiry(mutationOptionBuilder.expiry)
                .withDurability(mutationOptionBuilder.persistTo, mutationOptionBuilder.replicateTo)
                .execute();
    }

    public Observable<Boolean> shift(final E element) {
        return shift(element, MutationOptionBuilder.build());
    }

    public Observable<Boolean> shift(final E element, final MutationOptionBuilder optionBuilder) {
        return Observable.defer(new Func0<Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call() {
                return subdocPushFirst(element, optionBuilder)
                        .map(getMapResultFnForPushAndShift());
            }
        });
    }


    private Observable<DocumentFragment<Mutation>> subdocPushFirst(final E element,
                                                                   final MutationOptionBuilder optionBuilder) {

        final Func1<JsonArrayDocument, DocumentFragment<Mutation>> mapFullDocResultToSubDoc = new
                Func1<JsonArrayDocument, DocumentFragment<Mutation>>() {
                    @Override
                    public DocumentFragment<Mutation> call(JsonArrayDocument document) {
                        List<SubdocOperationResult<Mutation>> list;
                        list = new ArrayList<SubdocOperationResult<Mutation>>();
                        list.add(SubdocOperationResult.createResult(null, Mutation.ARRAY_PUSH_FIRST,
                                ResponseStatus.SUCCESS, element));
                        return new DocumentFragment<Mutation>(document.id(), document.cas(),
                                document.mutationToken(),
                                list);
                    }
                };
        final Func1<Throwable, Observable<? extends DocumentFragment<Mutation>>> retryIfDocExists = new
                Func1<Throwable, Observable<? extends DocumentFragment<Mutation>>>() {
                    @Override
                    public Observable<? extends DocumentFragment<Mutation>> call(Throwable throwable) {
                        return subdocPushFirst(element, optionBuilder);
                    }
                };

        return bucket.mutateIn(docId).arrayPrepend("", element, false)
                .withCas(optionBuilder.cas)
                .withDurability(optionBuilder.persistTo, optionBuilder.replicateTo)
                .withExpiry(optionBuilder.expiry)
                .execute()
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends DocumentFragment<Mutation>>>() {
                    @Override
                    public Observable<? extends DocumentFragment<Mutation>> call(Throwable throwable) {
                        if (throwable instanceof DocumentDoesNotExistException) {
                            return createDocument(element)
                                    .map(mapFullDocResultToSubDoc)
                                    .onErrorResumeNext(retryIfDocExists);
                        } else {
                            if (throwable instanceof MultiMutationException) {
                                ResponseStatus status = ((MultiMutationException) throwable).firstFailureStatus();
                                List<SubdocOperationResult<Mutation>> list;
                                list = new ArrayList<SubdocOperationResult<Mutation>>();
                                list.add(SubdocOperationResult.createResult(null, Mutation.ARRAY_PUSH_FIRST, status, null));
                                return Observable.just(new DocumentFragment<Mutation>(null, 0, null, list));
                            } else {
                                return Observable.error(throwable);
                            }
                        }
                    }
                });
    }

    public Observable<Boolean> set(int index, final E element) {
        return set(index, element, MutationOptionBuilder.build());
    }

    public Observable<Boolean> set(final int index, final E element, final MutationOptionBuilder optionBuilder) {
        return Observable.defer(new Func0<Observable<Boolean>>() {
            @Override
            public Observable<Boolean> call() {
                return subdocInsert(index, element, optionBuilder)
                        .map(getMapResultFnForRemoveAndSet());
            }
        });
    }


    private Observable<DocumentFragment<Mutation>> subdocInsert(final int index,
                                                                final E element,
                                                                final MutationOptionBuilder optionBuilder) {
        final Func1<JsonArrayDocument, DocumentFragment<Mutation>> mapFullDocResultToSubdoc = new
                Func1<JsonArrayDocument, DocumentFragment<Mutation>>() {
                    @Override
                    public DocumentFragment<Mutation> call(JsonArrayDocument document) {
                        List<SubdocOperationResult<Mutation>> list;
                        list = new ArrayList<SubdocOperationResult<Mutation>>();
                        list.add(SubdocOperationResult.createResult(null, Mutation.ARRAY_INSERT, ResponseStatus.SUCCESS, element));
                        return new DocumentFragment<Mutation>(document.id(), document.cas(), document.mutationToken(), list);
                    }
                };

        final Func1<Throwable, Observable<? extends DocumentFragment<Mutation>>> retryIfDocExists = new
                Func1<Throwable, Observable<? extends DocumentFragment<Mutation>>>() {
                    @Override
                    public Observable<? extends DocumentFragment<Mutation>> call(Throwable throwable) {
                        if (throwable instanceof DocumentAlreadyExistsException) {
                            return subdocInsert(index, element, optionBuilder);
                        } else {
                            return Observable.error(throwable);
                        }
                    }
                };
        return bucket.mutateIn(docId).arrayInsert("[" + Integer.toString(index) + "]", element)
                .withCas(optionBuilder.cas)
                .withDurability(optionBuilder.persistTo, optionBuilder.replicateTo)
                .withExpiry(optionBuilder.expiry)
                .execute()
                .onErrorResumeNext(new Func1<Throwable, Observable<? extends DocumentFragment<Mutation>>>() {
                    @Override
                    public Observable<? extends DocumentFragment<Mutation>> call(Throwable throwable) {
                        if (throwable instanceof DocumentDoesNotExistException) {
                            return createDocument(element)
                                    .map(mapFullDocResultToSubdoc)
                                    .onErrorResumeNext(retryIfDocExists);
                        } else {
                            if (throwable instanceof MultiMutationException) {
                                ResponseStatus status = ((MultiMutationException) throwable).firstFailureStatus();
                                List<SubdocOperationResult<Mutation>> list;
                                list = new ArrayList<SubdocOperationResult<Mutation>>();
                                list.add(SubdocOperationResult.createResult(null, Mutation.ARRAY_INSERT,
                                        status, null));
                                return Observable.just(new DocumentFragment<Mutation>(null, 0,
                                        null,
                                        list));
                            } else {
                                return Observable.error(throwable);
                            }
                        }
                    }
                });
    }

    public Observable<Integer> size() {
        return Observable.defer(new Func0<Observable<Integer>>() {
            @Override
            public Observable<Integer> call() {
                return bucket.get(docId, JsonArrayDocument.class)
                        .map(new Func1<JsonArrayDocument, Integer>() {
                            @Override
                            public Integer call(JsonArrayDocument document) {
                                return document.content().size();
                            }
                        });
            }
        });
    }

    private Func1<DocumentFragment<Mutation>, Boolean> getMapResultFnForPushAndShift() {
        return new Func1<DocumentFragment<Mutation>, Boolean>() {
            @Override
            public Boolean call(DocumentFragment<Mutation> documentFragment) {
                ResponseStatus status = documentFragment.status(0);
                if (status == ResponseStatus.SUCCESS) {
                    return true;
                } else {
                    if (status == ResponseStatus.TOO_BIG) {
                        throw new IllegalStateException("List full");
                    } else {
                        throw new CouchbaseException(status.toString());
                    }
                }
            }
        };
    }

    private Func1<DocumentFragment<Mutation>, Boolean> getMapResultFnForRemoveAndSet() {
        return new Func1<DocumentFragment<Mutation>, Boolean>() {
            @Override
            public Boolean call(DocumentFragment<Mutation> documentFragment) {
                ResponseStatus status = documentFragment.status(0);
                if (status == ResponseStatus.SUCCESS) {
                    return true;
                } else {
                    if (status == ResponseStatus.SUBDOC_PATH_NOT_FOUND || status == ResponseStatus.SUBDOC_PATH_INVALID) {
                        throw new IndexOutOfBoundsException();
                    } else {
                        throw new CouchbaseException(status.toString());
                    }
                }
            }
        };
    }
}