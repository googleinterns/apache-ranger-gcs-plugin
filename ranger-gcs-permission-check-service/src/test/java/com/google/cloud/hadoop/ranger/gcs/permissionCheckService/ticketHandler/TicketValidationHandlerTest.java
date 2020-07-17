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

import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class TicketValidationHandlerTest {
    private static final String USER = "test-user";
    private static final String GROUP = "dummy-group";
    private static final String BUCKET = "bucket";
    private static final String OBJECT = "object/dir/file";
    private static final List<String> ACTIONS = Arrays.asList("act1", "act2");

    @Test
    public void testAllPass() {
        RequestTicket ticket = new RequestTicket();
        ticket.setUser(USER);
        ticket.addUserGroup(GROUP);
        ticket.setBucket(BUCKET);
        ticket.setObjectPath(OBJECT);
        ticket.setActions(ACTIONS);
        ticket.setResult(RangerGcsPermissionCheckResult.Allow());

        TicketHandler handler = new TicketValidationHandler();

        handler.handle(ticket);

        assertThat(ticket.getResult().equals(RangerGcsPermissionCheckResult.Allow())).isTrue();
    }

    @Test
    public void testMissingUser() {
        RequestTicket ticket = new RequestTicket();
        ticket.addUserGroup(GROUP);
        ticket.setBucket(BUCKET);
        ticket.setObjectPath(OBJECT);
        ticket.setActions(ACTIONS);
        ticket.setResult(RangerGcsPermissionCheckResult.Allow());

        TicketHandler handler = new TicketValidationHandler();

        handler.handle(ticket);

        assertThat(ticket.getResult().equals(RangerGcsPermissionCheckResult.Deny())).isTrue();
    }

    @Test
    public void testMissingUserGroups() {
        RequestTicket ticket = new RequestTicket();
        ticket.setUser(USER);
        ticket.setBucket(BUCKET);
        ticket.setObjectPath(OBJECT);
        ticket.setActions(ACTIONS);
        ticket.setResult(RangerGcsPermissionCheckResult.Allow());

        TicketHandler handler = new TicketValidationHandler();

        handler.handle(ticket);

        assertThat(ticket.getResult().equals(RangerGcsPermissionCheckResult.Deny())).isTrue();
    }

    @Test
    public void testMissingBucket() {
        RequestTicket ticket = new RequestTicket();
        ticket.setUser(USER);
        ticket.addUserGroup(GROUP);
        ticket.setObjectPath(OBJECT);
        ticket.setActions(ACTIONS);
        ticket.setResult(RangerGcsPermissionCheckResult.Allow());

        TicketHandler handler = new TicketValidationHandler();

        handler.handle(ticket);

        assertThat(ticket.getResult().equals(RangerGcsPermissionCheckResult.Deny())).isTrue();
    }

    @Test
    public void testMissingObject() {
        RequestTicket ticket = new RequestTicket();
        ticket.setUser(USER);
        ticket.addUserGroup(GROUP);
        ticket.setBucket(BUCKET);
        ticket.setActions(ACTIONS);
        ticket.setResult(RangerGcsPermissionCheckResult.Allow());

        TicketHandler handler = new TicketValidationHandler();

        handler.handle(ticket);

        assertThat(ticket.getResult().equals(RangerGcsPermissionCheckResult.Deny())).isTrue();
    }

    @Test
    public void testMissingActions() {
        RequestTicket ticket = new RequestTicket();
        ticket.setUser(USER);
        ticket.addUserGroup(GROUP);
        ticket.setBucket(BUCKET);
        ticket.setObjectPath(OBJECT);
        ticket.setResult(RangerGcsPermissionCheckResult.Allow());

        TicketHandler handler = new TicketValidationHandler();

        handler.handle(ticket);

        assertThat(ticket.getResult().equals(RangerGcsPermissionCheckResult.Deny())).isTrue();
    }
}
