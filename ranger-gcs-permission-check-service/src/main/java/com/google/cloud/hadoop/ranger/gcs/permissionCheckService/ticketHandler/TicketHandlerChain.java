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
import com.google.cloud.hadoop.ranger.gcs.utilities.RangerGcsPermissionCheckResult;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Ticket handler chain.
 * Involve the handlers in order.
 */
public class TicketHandlerChain implements TicketHandler {
    private static final Log LOG = LogFactory.getLog(TicketHandlerChain.class);
    private final List<TicketHandler> handlerList = new ArrayList<>();

    /**
     * All handler are guaranteed to be executed.
     * There is no shortcut logic.
     *
     * When encounter uncaught exception, panic and reject the request.
     */
    @Override
    public void handle(RequestTicket ticket) {
        for(TicketHandler handler: handlerList) {
            try {
                handler.handle(ticket);
            // Panic. Reject.
            } catch (Exception e) {
                if (LOG.isErrorEnabled())
                    LOG.error("Error handling ticket: " + ticket.toString(), e);
                RangerGcsPermissionCheckResult result = RangerGcsPermissionCheckResult.Deny();
                ticket.setResult(result);
            }
        }
    }

    public void append(TicketHandler handler) {
        this.handlerList.add(handler);
    }
}
