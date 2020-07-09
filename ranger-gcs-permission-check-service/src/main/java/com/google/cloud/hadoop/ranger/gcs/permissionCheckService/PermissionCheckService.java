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

import com.google.cloud.hadoop.ranger.gcs.permissionCheckService.ticketHandler.*;
import com.google.cloud.hadoop.ranger.gcs.permissionCheckService.utilities.Config;
import com.google.cloud.hadoop.ranger.gcs.permissionCheckService.utilities.ConfigParser;
import com.google.cloud.hadoop.ranger.gcs.permissionCheckService.utilities.RequestTicket;
import com.google.cloud.hadoop.ranger.gcs.permissionCheckService.utilities.RequestTicketFactory;
import com.google.cloud.hadoop.ranger.gcs.utilities.RangerGcsPermissionCheckResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.io.IOException;
import java.util.function.Function;

/**
 * This is the entry point of Ranger permission check service.
 * Execute the main method to start, or initiate an instance of this class and call .start() method.
 */
public class PermissionCheckService {
    private static final Log LOG = LogFactory.getLog(PermissionCheckService.class);

    private final Config config;
    private RangerGcsHttpServer server;


    public static void main (String[] args) throws Exception{
        if (LOG.isDebugEnabled())
            LOG.debug("==> PermissionCheckService.main(" + String.join(", ", args) + ")");

        PermissionCheckService instance;
        try {
            instance = new PermissionCheckService();
        } catch (Exception e) {
            if (LOG.isErrorEnabled())
                LOG.error("Cannot initiate PermissionCheckService", e);
            throw e;
        }

        try {
            instance.start();
        } catch (IOException e) {
            if (LOG.isErrorEnabled())
                LOG.error("Cannot start proxy server.", e);
            throw e;
        }

        if (LOG.isDebugEnabled())
            LOG.debug("<== PermissionCheckService.main(" + String.join(", ", args) + ")");
    }

    public PermissionCheckService() throws Exception {
        if (LOG.isDebugEnabled())
            LOG.debug("==> PermissionCheckService()");

        try {
            config = new ConfigParser().parseConfig();
        } catch (Exception e) {
            if (LOG.isErrorEnabled())
                LOG.error("Cannot parse config file for permission check service.", e);
            throw e;
        }

        if (LOG.isDebugEnabled())
            LOG.debug("<== PermissionCheckService()");
    }

    public void start() throws IOException {
        if (LOG.isDebugEnabled())
            LOG.debug("==> PermissionCheckService.start()");

        RangerGcsPluginSingletonWrapper wrapper = RangerGcsPluginSingletonWrapper.getInstance();
        wrapper.init();

        server = new RangerGcsHttpServer(config.getHostIp(), config.getHostPort());
        server.start(new RangerGcsHttpRequestHandler());

        if (LOG.isDebugEnabled())
            LOG.debug("<== PermissionCheckService.start()");
    }

    /**
     * To gracefully stop all underlying daemon threads and give some time for pending jobs.
     * This is not necessary, since there won't be any consequence if we just let JVM kill those threads.
     */
    public void stop() {
        if (LOG.isDebugEnabled())
            LOG.debug("==> PermissionCheckService.stop()");

        server.stop();
        RangerGcsPluginSingletonWrapper wrapper = RangerGcsPluginSingletonWrapper.getInstance();
        wrapper.stop();

        if (LOG.isDebugEnabled())
            LOG.debug("<== PermissionCheckService.stop()");
    }

    /**
     * Setup HTTP request handler.
     * This is a wrapper for the ticket and ticket handler system for the HTTP server.
     */
    private static class RangerGcsHttpRequestHandler implements Function<String, RangerGcsPermissionCheckResult> {
        private final TicketHandler handler;

        /**
         * Initiate a chain of handler.
         * The handler will be called in order.
         * The handlers should edit the given ticket directly.
         * All of the handler is guaranteed to be executed. There is no early return.
         */
        public RangerGcsHttpRequestHandler() {
            handler = new TicketHandlerChain();
            ((TicketHandlerChain)handler).append(new TicketValidationHandler());
            ((TicketHandlerChain)handler).append(new RangerHandler());
            ((TicketHandlerChain)handler).append(new LogProcessResultHandler());
        }

        public RangerGcsHttpRequestHandler(TicketHandler handler) {
            this.handler = handler;
        }

        @Override
        public RangerGcsPermissionCheckResult apply(String query) {
            RequestTicket ticket = RequestTicketFactory.createRequestTicketFromHttpRequestUri(query);
            handler.handle(ticket);
            return ticket.getResult();
        }
    }
}
