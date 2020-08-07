/*
 *
 *  * Copyright 2020 Google Inc. All Rights Reserved.
 *  *
 *  * Licensed under the Apache License, Version 2.0 (the "License");
 *  * you may not use this file except in compliance with the License.
 *  * You may obtain a copy of the License at
 *  *
 *  *    http://www.apache.org/licenses/LICENSE-2.0
 *  *
 *  * Unless required by applicable law or agreed to in writing, software
 *  * distributed under the License is distributed on an "AS IS" BASIS,
 *  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  * See the License for the specific language governing permissions and
 *  * limitations under the License.
 *
 */

package com.google.cloud.hadoop.ranger.gcs.connectorAdapter;

import com.google.cloud.hadoop.ranger.gcs.utilities.RangerGcsPermissionCheckResult;
import com.google.cloud.hadoop.gcsio.authorization.AuthorizationHandler;
import java.io.IOException;
import java.net.URI;
import java.util.Map;

import java.nio.file.AccessDeniedException;
import java.util.List;

/**
 * An authorization provider that send authorization requests to Ranger proxy server.
 */
public class RangerProxyServiceAdapter implements AuthorizationHandler {
    private static final String PROPERTY_HOST_NAME = "ranger.proxy.host.name";

    // Timeout for http request to Ranger proxy server. 3 seconds should be more then enough.
    private static final int TIMEOUT = 3;

    private static final String DENY_MSG = "Access Denied. Unsupported/invalid request content.";
    private static final String CONNECTION_ERR_MSG =  "Can not connect to Ranger proxy server.";

    private String hostName;
    private RangerRequestHandler rangerRequestHandler;
    private RangerRequestFactory rangerRequestFactory;

    public RangerProxyServiceAdapter() {
        try {
            this.rangerRequestFactory = new RangerRequestFactory();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * @param hostName Ranger proxy server's address.
     */
    public void init(String hostName) {
        rangerRequestHandler = new RangerAuthorizationProxyClient(hostName);
    }

    public void init(RangerRequestHandler rangerRequestHandler) {
        this.rangerRequestHandler = rangerRequestHandler;
    }

    /**
     * Authorize RangerRequests.
     * @param requests Storage requests containing action and GCS resource information.
     * @throws AccessDeniedException Thrown when denied by Ranger or encounter other errors.
     */
    public void handle(List<RangerRequest> requests)
            throws AccessDeniedException {
        for (RangerRequest request: requests) {
            handle(request);
        }
    }

    /**
     * Authorize a RangerRequest.
     * @param request A RangerRequest.
     * @throws AccessDeniedException Thrown when denied by Ranger or encounter other errors.
     */
    public void handle(RangerRequest request) throws AccessDeniedException {
        RangerGcsPermissionCheckResult result;
        try {
            result = rangerRequestHandler.handle(request, TIMEOUT);
        } catch (Exception e) {
            throw new AccessDeniedException(CONNECTION_ERR_MSG + " " + e.getMessage());
        }

        if (result.equals(RangerGcsPermissionCheckResult.Deny())) {
            throw new AccessDeniedException(result.getMessage());
        }
    }

    @Override
    public void setProperties(Map<String, String> configuration) {
        hostName = configuration.get(PROPERTY_HOST_NAME);
        init(hostName);
    }

    @Override
    public void handleListObject(URI resource) throws AccessDeniedException {
        handle(rangerRequestFactory.createReadRequestOnDir(resource));
    }

    @Override
    public void handleInsertObject(URI resource) throws AccessDeniedException {
        handle(rangerRequestFactory.createWriteRequestOnDir(resource));
    }

    @Override
    public void handleComposeObject(URI destination, List<URI> sources) throws AccessDeniedException {
        handle(rangerRequestFactory.createComposeObjectRequests(destination, sources));
    }

    @Override
    public void handleGetObject(URI resource) throws AccessDeniedException {
        handle(rangerRequestFactory.createReadRequestOnObject(resource));
    }

    @Override
    public void handleDeleteObject(URI resource) throws AccessDeniedException {
        handle(rangerRequestFactory.createWriteRequestOnDir(resource));
    }

    @Override
    public void handleRewriteObject(URI source, URI destination) throws AccessDeniedException {
        handle(rangerRequestFactory.createSourceToDestinationRequests(source, destination));
    }

    @Override
    public void handleCopyObject(URI source, URI destination) throws AccessDeniedException {
        handle(rangerRequestFactory.createSourceToDestinationRequests(source, destination));

    }

    @Override
    public void handlePatchObject(URI resource) throws AccessDeniedException {
        handle(rangerRequestFactory.createWriteRequestOnObject(resource));
    }

    @Override
    public void handleListBucket() throws AccessDeniedException {
        // Do nothing.
        // Project level access control is out of scope.
    }

    @Override
    public void handleInsertBucket(URI uri) throws AccessDeniedException {
        // Do nothing.
        // Project level access control is out of scope.
    }

    @Override
    public void handleGetBucket(URI uri) throws AccessDeniedException {
        // Do nothing.
        // Project level access control is out of scope.
    }

    @Override
    public void handleDeleteBucket(URI uri) throws AccessDeniedException {
        // Do nothing.
        // Project level access control is out of scope.
    }
}
