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

package com.google.cloud.hadoop.ranger.gcs.authorization;

import static com.google.common.truth.Truth.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.when;

import org.apache.ranger.plugin.model.RangerServiceDef;
import org.apache.ranger.plugin.policyengine.RangerAccessRequest;
import org.apache.ranger.plugin.policyengine.RangerAccessResult;
import org.apache.ranger.plugin.policyengine.RangerAccessResultProcessor;
import org.apache.ranger.plugin.service.RangerBasePlugin;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;

@RunWith(JUnit4.class)
public class RangerGcsPluginTest {
    private RangerGcsAccessRequest request;
    private final List<String> ACTIONS = Arrays.asList("read", "write");

    @Mock
    RangerBasePlugin mockPlugin;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Before
    public void setup() {
        request = new RangerGcsAccessRequest(new RangerGcsResource("test-bucket", "/"),
                "test-user", new HashSet<>(Arrays.asList("test-group1")), "read");
    }

    @Test
    public void testAccessAllowed() {
        // Setup a mock reply without associating with a request.
        RangerAccessResult ret = new RangerAccessResult(
                0, "test-service", new RangerServiceDef(), null);
        ret.setIsAccessDetermined(true);
        ret.setIsAllowed(true);

        when(mockPlugin.isAccessAllowed(any(RangerAccessRequest.class), any(RangerAccessResultProcessor.class)))
                .thenReturn(ret);

        RangerGcsPlugin gcsPlugin = new RangerGcsPlugin(mockPlugin);

        RangerAccessResult result = gcsPlugin.isAccessAllowed(request, ACTIONS, new NullHandler());

        assertThat(result.getIsAllowed()).isTrue();
    }

    @Test
    public void testAccessDenied() {
        // Setup a mock reply without associating with a request.
        RangerAccessResult ret = new RangerAccessResult(
                0, "test-service", new RangerServiceDef(), null);
        ret.setIsAllowed(false);

        when(mockPlugin.isAccessAllowed(any(RangerAccessRequest.class), any(RangerAccessResultProcessor.class)))
                .thenReturn(ret);

        RangerGcsPlugin gcsPlugin = new RangerGcsPlugin(mockPlugin);

        RangerAccessResult result = gcsPlugin.isAccessAllowed(request, ACTIONS, new NullHandler());

        assertThat(result.getIsAllowed()).isFalse();
    }

    // A null behavior handler that does nothing.
    private static class NullHandler implements RangerAccessResultProcessor {
        @Override
        public void processResult(RangerAccessResult rangerAccessResult) {
        }

        @Override
        public void processResults(Collection<RangerAccessResult> collection) {
        }
    }
}
