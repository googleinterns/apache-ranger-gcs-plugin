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

import com.google.cloud.hadoop.ranger.gcs.permissionCheckService.utilities.RequestTicket;
import com.google.cloud.hadoop.ranger.gcs.utilities.RangerGcsHttpRequestKey;
import com.google.cloud.hadoop.ranger.gcs.utilities.RangerGcsPermissionCheckResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Validates access request tickets.
 * Reject a request if it's missing any of the required fields:
 *  user, userGroups, bucket, objectPath, actions
 */
public class TicketValidationHandler implements TicketHandler {
    private static final Log LOG = LogFactory.getLog(TicketValidationHandler.class);
    private static final String DENY_MSG = "Missing or invalid fields: [%s].";

    @Override
    public void handle(RequestTicket ticket) {
        if (LOG.isDebugEnabled())
            LOG.debug("==> TicketValidationHandler.handle(" + ticket.toString() + ")");

        List<String> missing = new ArrayList<>();

        if (ticket.getUser() == null) {
            missing.add(RangerGcsHttpRequestKey.USER);
        }

        if (ticket.getUserGroups().size() == 0) {
            missing.add(RangerGcsHttpRequestKey.USER_GROUPS);
        }

        if (ticket.getBucket() == null || ticket.getBucket().length() == 0 ||
                ticket.getObjectPath() == null || ticket.getObjectPath().length() == 0) {
            missing.add(RangerGcsHttpRequestKey.RESOURCE);
        }

        if (ticket.getActions() == null || ticket.getActions().size() == 0) {
            missing.add(RangerGcsHttpRequestKey.ACTIONS);
        }

        if (missing.size() > 0) {
            RangerGcsPermissionCheckResult result = RangerGcsPermissionCheckResult.Deny();
            result.setMessage(String.format(DENY_MSG, String.join(", ", missing)));
            ticket.setResult(result);
        }

        if (LOG.isDebugEnabled())
            LOG.debug("<== TicketValidationHandler.handle(" + ticket.toString() + "): " + String.join(",", missing));
    }
}
