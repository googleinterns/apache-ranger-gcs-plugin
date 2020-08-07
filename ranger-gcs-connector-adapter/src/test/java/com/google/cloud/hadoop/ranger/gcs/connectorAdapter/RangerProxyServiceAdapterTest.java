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

import static org.junit.Assert.assertThrows;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.when;

import com.google.cloud.hadoop.ranger.gcs.utilities.RangerGcsPermissionCheckResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.nio.file.AccessDeniedException;

@RunWith(JUnit4.class)
public class RangerProxyServiceAdapterTest {
    private static final String TEST_RESOURCE = "bucket/path/to/object";


    @Mock
    RangerRequestHandler mockHandler;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void testHandleDeny() throws Exception{
        when(mockHandler.handle(any(RangerRequest.class), anyInt()))
                .thenReturn(RangerGcsPermissionCheckResult.Deny());

        RangerProxyServiceAdapter adapter = new RangerProxyServiceAdapter();
        adapter.init(mockHandler);

        assertThrows(AccessDeniedException.class, () -> {
            adapter.handle(
                new RangerRequest("test-user", "test-group",
                    TEST_RESOURCE, "read"));
        });
    }

    @Test
    public void testHandleAllow() throws Exception {
        when(mockHandler.handle(any(RangerRequest.class), anyInt()))
                .thenReturn(RangerGcsPermissionCheckResult.Allow());

        RangerProxyServiceAdapter adapter = new RangerProxyServiceAdapter();
        adapter.init(mockHandler);

        adapter.handle(
            new RangerRequest("test-user", "test-groups",
                TEST_RESOURCE, "read"));
    }
}
