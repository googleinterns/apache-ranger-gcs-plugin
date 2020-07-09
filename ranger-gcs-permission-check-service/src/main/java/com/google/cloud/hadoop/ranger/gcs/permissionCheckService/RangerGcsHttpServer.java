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

package com.google.cloud.hadoop.ranger.gcs.permissionCheckService;

import com.google.cloud.hadoop.ranger.gcs.utilities.RangerGcsPermissionCheckResult;
import com.google.gson.JsonObject;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpServer;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.util.function.Function;

/**
 * HTTP server that hands over incoming requests to handlers, and reply results to clients.
 * The server focuses on receiving HTTP requests and sending response.
 * The java.util.Function interface understand how to operate the tickets and ticketHandlers.
 */
public class RangerGcsHttpServer {
    private static final Log LOG = LogFactory.getLog(RangerGcsHttpServer.class);

    private final String host;
    private final int port;
    protected Function<String, RangerGcsPermissionCheckResult> requestHandler = null;

    private HttpServer server;

    public RangerGcsHttpServer (String host, int port) {
        this.host = host;
        this.port = port;
    }

    /**
     * Callers need to register a callback function to handle queries.
     */
    public void start(Function<String, RangerGcsPermissionCheckResult> requestHandler) throws IOException {
        if (LOG.isDebugEnabled())
            LOG.debug("==> RangerGcsHttpServer.start()");

        if (requestHandler == null) {
            throw new IllegalArgumentException("Cannot start HTTP server. Request handler shouldn't be null.");
        }

        this.requestHandler = requestHandler;

        server = HttpServer.create(new InetSocketAddress(host, port), 0); // Backlog will be auto scaled.
        server.setExecutor(null); // Use default executor.
        server.createContext("/", new HttpRequestHandler(requestHandler));
        server.start();

        if (LOG.isDebugEnabled())
            LOG.debug("<== RangerGcsHttpServer.start()");
    }

    /**
     * Gracefully stop the HTTP server.
     * This stops the server from accepting new requests,
     *  and give it a few seconds to finish ongoing jobs.
     */
    public void stop() {
        server.stop(3);
    }

    private static class HttpRequestHandler implements HttpHandler {
        private static final Log LOG = LogFactory.getLog(HttpRequestHandler.class);
        private final Function<String, RangerGcsPermissionCheckResult> requestHandler;

        public HttpRequestHandler(Function<String, RangerGcsPermissionCheckResult> requestHandler) {
            super();
            this.requestHandler = requestHandler;
        }

        @Override
        public void handle(HttpExchange request) throws IOException{
            if (LOG.isDebugEnabled())
                LOG.debug("==> HttpRequestHandler.handle(" + request.getRequestURI() + ")");

            String query = request.getRequestURI().getQuery();

            RangerGcsPermissionCheckResult result = requestHandler.apply(query);

            if (result == null) {
                result = RangerGcsPermissionCheckResult.Deny();
            }

            JsonObject jsonResponse = new JsonObject();
            if (result.equals(RangerGcsPermissionCheckResult.Allow()))
                jsonResponse.addProperty("result", "allow");
            else if (result.equals(RangerGcsPermissionCheckResult.Deny()))
                jsonResponse.addProperty("result", "deny");

            jsonResponse.addProperty("message", result.getMessage());
            String responseString = jsonResponse.toString();

            request.sendResponseHeaders(200, responseString.length());
            OutputStream rs = request.getResponseBody();
            rs.write(responseString.getBytes());
            rs.close();

            if (LOG.isDebugEnabled())
                LOG.debug("<== HttpRequestHandler.handle(" + request.getRequestURI() + "): " + responseString);
        }
    }
}
