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

package com.google.cloud.hadoop.ranger.gcs.permissionCheckService.ticketHandler;

import com.google.cloud.hadoop.ranger.gcs.permissionCheckService.RangerGcsPluginSingletonWrapper;
import com.google.cloud.hadoop.ranger.gcs.permissionCheckService.utilities.RangerGcsPluginWrapper;
import com.google.cloud.hadoop.ranger.gcs.permissionCheckService.utilities.RequestTicket;
import com.google.cloud.hadoop.ranger.gcs.utilities.RangerGcsPermissionCheckResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

/**
 * Delegate authorization job to Ranger plugin.
 */
public class RangerHandler implements TicketHandler {
    private static final Log LOG = LogFactory.getLog(RangerHandler.class);

    // Append request detail to Ranger's response message.
    // Ranger's response message should be a complete sentence, so no period here.
    private static final String ACCESS_DENY_MSG = "%s Request=%s.";

    private final RangerGcsPluginWrapper wrapper;

    public RangerHandler() {
        wrapper = RangerGcsPluginSingletonWrapper.getInstance();
    }

    public RangerHandler(RangerGcsPluginWrapper wrapper) {
        this.wrapper = wrapper;
    }

    @Override
    public void handle(RequestTicket ticket) {
        if (LOG.isDebugEnabled())
            LOG.debug("==> RangerHandler.handle(" + ticket.toString() + ")");

        if (ticket.getResult() != null && ticket.getResult().equals(RangerGcsPermissionCheckResult.Deny())) {
            if (LOG.isDebugEnabled())
                LOG.debug("<== RangerHandler.handle(" + ticket.toString() + "): denied by other handlers in the chain.");
            return;
        }

         RangerGcsPermissionCheckResult result = wrapper.isAccessAllowed(ticket.getUser(), ticket.getUserGroups(),
                 ticket.getBucket(), ticket.getObjectPath(), ticket.getActions());

         // Enrich access deny message. Make it more useful for user and admin to understand.
         if (result.equals(RangerGcsPermissionCheckResult.Deny())) {
             result.setMessage(ticketToDenyMsg(result, ticket));
         }

         ticket.setResult(result);

        if (LOG.isDebugEnabled())
            LOG.debug("<== RangerHandler.handle(" + ticket.toString() + "): " + result.toString());
    }

    private static String ticketToDenyMsg(RangerGcsPermissionCheckResult result, RequestTicket ticket) {
        return String.format(ACCESS_DENY_MSG, result.getMessage(), new StringBuilder()
                .append("[resource=").append(ticket.getBucket()).append(ticket.getObjectPath())
                .append(", action=(").append(String.join(",", ticket.getActions()))
                .append(")]")
                .toString());
    }
}
