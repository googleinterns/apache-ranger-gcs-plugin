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
import com.google.cloud.hadoop.util.authorization.AuthorizationHandler;
import com.google.cloud.hadoop.util.authorization.StorageRequestSummary;
import org.apache.hadoop.conf.Configuration;

import java.io.IOException;
import java.nio.file.AccessDeniedException;
import java.util.List;

/**
 * An authorization provider that send authorization requests to Ranger proxy server.
 */
public class RangerProxyServiceAdapter implements AuthorizationHandler {
    Configuration configuration;
    private static final String PROPERTY_HOST_NAME = "fs.gs.ranger.proxy.host.name";

    // Timeout for http request to Ranger proxy server. 3 seconds should be more then enough.
    private static final int TIMEOUT = 3;

    private static final String DENY_MSG = "Access Denied. Unsupported/invalid request content.";
    private static final String CONNECTION_ERR_MSG =  "Can not connect to Ranger proxy server.";

    private RangerAuthorizationProxyClient RangerRequestHandler;

    /**
     * @param hostName Ranger proxy server's address.
     */
    public void init(String hostName) {
        RangerRequestHandler = new RangerAuthorizationProxyClient(hostName);
    }

    /**
     * Authorize a StorageRequest.
     * @param storageRequestSummary Storage request containing action and GCS resource information.
     * @throws AccessDeniedException Thrown when denied by Ranger or encounter other errors.
     */
    @Override
    public void handle(StorageRequestSummary storageRequestSummary)
            throws AccessDeniedException {
        if (RangerRequestHandler == null) {
            init(getConf().get(PROPERTY_HOST_NAME));
        }

        List<RangerRequest> requests;
        try {
            requests = RangerRequestFactory.createRangerRequest(storageRequestSummary);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        for (RangerRequest request: requests) {
            if (request.equals(RangerRequestFactory.DENY)) {
                throw new AccessDeniedException(DENY_MSG);
            }

            RangerGcsPermissionCheckResult result;
            try {
                result = RangerRequestHandler.handle(request, TIMEOUT);
            } catch (IOException e) {
                throw new AccessDeniedException(CONNECTION_ERR_MSG + " " + e.getMessage());
            }

            if (result.equals(RangerGcsPermissionCheckResult.Deny())) {
                throw new AccessDeniedException(result.getMessage());
            }
        }
    }

    @Override
    public void setConf(Configuration configuration) {
        this.configuration = configuration;
    }

    @Override
    public Configuration getConf() {
        return configuration;
    }
}
