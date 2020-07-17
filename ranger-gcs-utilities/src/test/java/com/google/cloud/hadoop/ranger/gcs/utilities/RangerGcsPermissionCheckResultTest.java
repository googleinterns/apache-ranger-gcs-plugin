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

package com.google.cloud.hadoop.ranger.gcs.utilities;

import static com.google.common.truth.Truth.assertThat;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;

@RunWith(JUnit4.class)
public class RangerGcsPermissionCheckResultTest {

    @Test
    public void testSetMessage() {
        String MSG = "Some message!!";

        RangerGcsPermissionCheckResult result = RangerGcsPermissionCheckResult.Allow();

        result.setMessage(MSG);

        assertThat(result.getMessage()).isEqualTo(MSG);
    }

    /**
     * Make sure the equals method doesn't get influence by the message.
     */
    @Test
    public void testEqualsOnlyCheckType() {
        RangerGcsPermissionCheckResult deny1 = RangerGcsPermissionCheckResult.Deny();
        RangerGcsPermissionCheckResult deny2 = RangerGcsPermissionCheckResult.Deny();

        RangerGcsPermissionCheckResult allow1 = RangerGcsPermissionCheckResult.Allow();
        RangerGcsPermissionCheckResult allow2 = RangerGcsPermissionCheckResult.Allow();

        deny2.setMessage("Some random message");
        allow2.setMessage("Some message here!!");

        assertThat(deny1.equals(deny2)).isTrue();
        assertThat(allow1.equals(allow2)).isTrue();
    }
}
