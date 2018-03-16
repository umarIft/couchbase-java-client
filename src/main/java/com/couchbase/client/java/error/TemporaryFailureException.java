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
package com.couchbase.client.java.error;

import com.couchbase.client.core.CouchbaseException;

/**
 * Thrown when the server reports a temporary failure.
 *
 * This is exception is very likely retryable.
 *
 * @author Michael Nitschinger
 * @since 2.1.0
 */
public class TemporaryFailureException extends CouchbaseException {

    public TemporaryFailureException() {
    }

    public TemporaryFailureException(String message) {
        super(message);
    }

    public TemporaryFailureException(String message, Throwable cause) {
        super(message, cause);
    }

    public TemporaryFailureException(Throwable cause) {
        super(cause);
    }
}
