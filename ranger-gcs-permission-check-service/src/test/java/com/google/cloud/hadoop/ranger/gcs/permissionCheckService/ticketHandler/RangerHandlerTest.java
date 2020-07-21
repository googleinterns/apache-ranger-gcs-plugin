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
import static org.mockito.Matchers.*;
import static org.mockito.Mockito.when;

import com.google.cloud.hadoop.ranger.gcs.permissionCheckService.utilities.RangerGcsPluginWrapper;
import com.google.cloud.hadoop.ranger.gcs.permissionCheckService.utilities.RequestTicket;
import com.google.cloud.hadoop.ranger.gcs.utilities.RangerGcsPermissionCheckResult;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;

import java.util.Arrays;
import java.util.List;

@RunWith(JUnit4.class)
public class RangerHandlerTest {
    private static final String USER = "test-user";
    private static final String GROUP = "dummy-group";
    private static final String BUCKET = "bucket";
    private static final String OBJECT = "object/path";
    private static final List<String> ACTIONS = Arrays.asList("act1", "act2");

    @Mock
    private RangerGcsPluginWrapper mockWrapper;

    /**
     * Test if the handler can correctly set the result to the ticket.
     */
    @Test
    public void testHandler() {
        RequestTicket ticket = new RequestTicket();
        ticket.setUser(USER);
        ticket.addUserGroup(GROUP);
        ticket.setBucket(BUCKET);
        ticket.setObjectPath(OBJECT);
        ticket.setActions(ACTIONS);

        when(mockWrapper.isAccessAllowed(anyString(), anySet(), anyString(), anyString(), anyList()))
                .thenReturn(RangerGcsPermissionCheckResult.Deny());

        TicketHandler handler = new RangerHandler(mockWrapper);
        handler.handle(ticket);

        assertThat(ticket.getResult().equals(RangerGcsPermissionCheckResult.Deny())).isTrue();
    }
}
