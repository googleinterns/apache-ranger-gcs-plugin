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

import com.google.cloud.hadoop.ranger.gcs.utilities.RangerGcsPermissionCheckResult;
import org.apache.ranger.plugin.model.RangerServiceDef;
import org.apache.ranger.plugin.policyengine.RangerAccessResult;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnit;
import org.mockito.junit.MockitoRule;

import java.util.*;

@RunWith(JUnit4.class)
public class RangerGcsAuthorizerTest {

    @Mock
    RangerGcsPlugin mockPlugin;

    @Rule
    public MockitoRule mockitoRule = MockitoJUnit.rule();

    @Test
    public void testAllow() {
        // Construct a null result without associating with a request.
        RangerAccessResult ret = new RangerAccessResult(
                0, "test-service", new RangerServiceDef(), null);
        ret.setIsAccessDetermined(true);
        ret.setIsAllowed(true);

        when(mockPlugin.isAccessAllowed(any(RangerGcsAccessRequest.class), any(List.class))).thenReturn(ret);

        RangerGcsAuthorizer authorizer = new RangerGcsAuthorizer(mockPlugin);

        RangerGcsPermissionCheckResult result = authorizer.isAccessAllowed(
                "test-user",
                new HashSet<>(Collections.singletonList("test-group")),
                "test-bucket",
                "test/path/obj",
                Arrays.asList("read", "write"));

        assertThat(result.equals(RangerGcsPermissionCheckResult.Allow())).isTrue();
    }

    @Test
    public void testDeny() {
        // Construct a null result without associating with a request.
        RangerAccessResult ret = new RangerAccessResult(
                0, "test-service", new RangerServiceDef(), null);
        ret.setIsAllowed(false);
        when(mockPlugin.isAccessAllowed(any(RangerGcsAccessRequest.class), any(List.class))).thenReturn(ret);

        RangerGcsAuthorizer authorizer = new RangerGcsAuthorizer(mockPlugin);

        RangerGcsPermissionCheckResult result = authorizer.isAccessAllowed(
                "test-user",
                new HashSet<>(Collections.singletonList("test-group")),
                "test-bucket",
                "test/path/obj",
                Arrays.asList("read", "write"));

        assertThat(result.equals(RangerGcsPermissionCheckResult.Deny())).isTrue();
    }
}
