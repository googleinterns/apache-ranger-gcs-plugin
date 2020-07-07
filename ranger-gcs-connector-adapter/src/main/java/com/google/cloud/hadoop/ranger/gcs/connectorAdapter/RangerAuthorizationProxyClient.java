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

import com.google.cloud.hadoop.ranger.gcs.utilities.RangerGcsHttpRequestKey;
import com.google.cloud.hadoop.ranger.gcs.utilities.RangerGcsPermissionCheckResult;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Handle Ranger authorization request.
 * Send requests to a specified proxy server and reply results to callers.
 */
public class RangerAuthorizationProxyClient {
    private static final String RESULT = "result";
    private static final String MESSAGE = "message";
    private static final String RESULT_DENY = "deny";
    private static final String RESULT_ALLOW = "allow";

    private final String host;

    /**
     * @param host The proxy server's url.
     */
    public RangerAuthorizationProxyClient(String host) {
        this.host = host;
    }

    /**
     * Handle a RangerRequest with a connection timeout (in second).
     * @param request Request to Ranger plugin.
     * @param timeout Connection timeout (in second).
     * @return Permission check result, which is eight allow or deny, with a message.
     * @throws IOException Happens if http connection fails.
     */
    public RangerGcsPermissionCheckResult handle(RangerRequest request, int timeout) throws IOException {
        // Construct HTTP GET query
        String queryString = String.format("?%s=%s&%s=%s&%s=%s&%s=%s",
                RangerGcsHttpRequestKey.USER, request.getUser(),
                RangerGcsHttpRequestKey.USER_GROUPS, request.getUserGroups(),
                RangerGcsHttpRequestKey.RESOURCE, request.getResource(),
                RangerGcsHttpRequestKey.ACTIONS, request.getActions());

        String result = sendHttpGet(host + queryString, timeout);

        JsonElement jsonElement = new JsonParser().parse(result);

        RangerGcsPermissionCheckResult ret;
        if (jsonElement.getAsJsonObject().get(RESULT).getAsString().equals(RESULT_DENY)) {
            ret = RangerGcsPermissionCheckResult.Deny();
            ret.setMessage(jsonElement.getAsJsonObject().get(MESSAGE).getAsString());
        } else if (jsonElement.getAsJsonObject().get(RESULT).getAsString().equals(RESULT_ALLOW)) {
            ret = RangerGcsPermissionCheckResult.Allow();
            ret.setMessage(jsonElement.getAsJsonObject().get(MESSAGE).getAsString());
        } else {
            ret = RangerGcsPermissionCheckResult.Deny();
        }
        return ret;
    }

    // Post HTTP GET. Timeout in second.
    private String sendHttpGet(String query, int timeout) throws IOException{
        StringBuilder result = new StringBuilder();
        URL url = new URL(query);
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setRequestMethod("GET");
        connection.setConnectTimeout(timeout * 1000); // In ms.
        BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(connection.getInputStream()));
        String line;
        while ((line = bufferedReader.readLine()) != null) {
            result.append(line);
        }
        bufferedReader.close();
        return result.toString();
    }
}
