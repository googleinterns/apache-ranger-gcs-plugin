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

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.hadoop.ranger.gcs.permissionCheckService.utilities.RequestTicket;
import com.google.cloud.hadoop.ranger.gcs.utilities.RangerGcsPermissionCheckResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class TicketHandlerChainTest {
    private static final String USER = "test-user";


    @Test
    public void testHandlerOrder() {
        RequestTicket emptyTicket = new RequestTicket();

        TicketHandlerChain handler = new TicketHandlerChain();
        handler.append(new SetUserHandler());
        handler.append(new CheckUserHandler());

        handler.handle(emptyTicket);

        assertThat(emptyTicket.getResult().equals(RangerGcsPermissionCheckResult.Allow())).isTrue();
    }

    @Test
    public void testPanic() {
        RequestTicket emptyTicket = new RequestTicket();

        TicketHandlerChain handler = new TicketHandlerChain();
        handler.append(new CheckUserHandler());

        handler.handle(emptyTicket);

        assertThat(emptyTicket.getResult().equals(RangerGcsPermissionCheckResult.Deny())).isTrue();
    }

    private static class SetUserHandler implements TicketHandler {
        @Override
        public void handle(RequestTicket ticket) {
            if (ticket.getUser() == null) {
                ticket.setUser(USER);
            }
        }
    }

    private static class CheckUserHandler implements TicketHandler {
        @Override
        public void handle(RequestTicket ticket) {
            if (! ticket.getUser().equals(USER)) {
                throw new RuntimeException();
            } else {
                ticket.setResult(RangerGcsPermissionCheckResult.Allow());
            }
        }
    }
}
