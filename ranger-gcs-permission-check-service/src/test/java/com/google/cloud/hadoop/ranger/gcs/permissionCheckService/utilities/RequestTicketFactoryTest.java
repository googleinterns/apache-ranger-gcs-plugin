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

package com.google.cloud.hadoop.ranger.gcs.permissionCheckService.utilities;

import static com.google.common.truth.Truth.assertThat;

import com.google.cloud.hadoop.ranger.gcs.utilities.RangerGcsHttpRequestKey;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@RunWith(JUnit4.class)
public class RequestTicketFactoryTest {
    private static final String USER = "test-user";
    private static final Set<String> USERGROUPS = new HashSet<>(Arrays.asList("g1", "g2"));

    private static final String BUCKET = "test-bucket";
    private static final String OBJECT = "/path/some/dir/file";
    private static final String ROOT = "/";

    private static final List<String> ACTION = Arrays.asList("act1", "act2");

    private static final String AND = "&";

    @Test
    public void testParseAll() {
        String url = RangerGcsHttpRequestKey.USER + "=" + USER + AND +
                RangerGcsHttpRequestKey.USER_GROUPS + "=" + String.join(",", USERGROUPS) + AND +
                RangerGcsHttpRequestKey.RESOURCE + "=" + BUCKET + OBJECT + AND +
                RangerGcsHttpRequestKey.ACTIONS + "=" + String.join(",",  ACTION);

        RequestTicket ticket = RequestTicketFactory.createRequestTicketFromHttpRequestUri(url);

        assertThat(ticket.getUser()).isEqualTo(USER);
        assertThat(ticket.getUserGroups().containsAll(USERGROUPS)).isTrue();
        assertThat(USERGROUPS.containsAll(ticket.getUserGroups())).isTrue();
        assertThat(ticket.getActions().containsAll(ACTION)).isTrue();
        assertThat(ACTION.containsAll(ticket.getActions())).isTrue();

        // Not empty is enough here. Leave the correctness for other tests.
        assertThat(ticket.getBucket().isEmpty()).isFalse();
        assertThat(ticket.getObjectPath().isEmpty()).isFalse();
    }

    /**
     * Test parsing resource string: bucket/object.
     */
    @Test
    public void testParseResourceBucketObject() {
        String resource = BUCKET + OBJECT;

        RequestTicket ticket = new RequestTicket();

        RequestTicketFactory.parseResourceString(ticket, resource);

        assertThat(ticket.getBucket()).isEqualTo(BUCKET);
        assertThat(ticket.getObjectPath()).isEqualTo(OBJECT);
    }

    @Test
    public void testParseResourceOnlyBucket() {
        // Only bucket name, no slash
        String resource = BUCKET;

        RequestTicket ticket = new RequestTicket();

        RequestTicketFactory.parseResourceString(ticket, resource);

        assertThat(ticket.getBucket()).isEqualTo(BUCKET);
        assertThat(ticket.getObjectPath()).isEqualTo(ROOT);
    }

    @Test
    public void testPaseResourceBucketSlash() {
        // Bucket name and a slash. No object path.
        String resource = BUCKET;

        RequestTicket ticket = new RequestTicket();

        RequestTicketFactory.parseResourceString(ticket, resource);

        assertThat(ticket.getBucket()).isEqualTo(BUCKET);
        assertThat(ticket.getObjectPath()).isEqualTo(ROOT);
    }
}
