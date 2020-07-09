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

public class RangerGcsPermissionCheckResult {
    private static final String ALLOW_MSG = "Access allowed.";
    private static final String DENY_MSG = "Access denied.";

    private String message;
    private RangerGcsPermissionCheckResultEnum type;

    private RangerGcsPermissionCheckResult(String msg, RangerGcsPermissionCheckResultEnum type) {
        this.message = msg;
        this.type = type;
    }

    public static RangerGcsPermissionCheckResult Allow() {
        return new RangerGcsPermissionCheckResult(ALLOW_MSG, RangerGcsPermissionCheckResultEnum.ALLOW);
    }

    public static RangerGcsPermissionCheckResult Deny() {
        return new RangerGcsPermissionCheckResult(DENY_MSG, RangerGcsPermissionCheckResultEnum.DENY);
    }

    public String getMessage() {
        return this.message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    @Override
    public String toString() {
        return String.format("%s(%s)", this.type, this.message);
    }

    /**
     * Compare equality. Only the result matters. Don't care about message.
     */
    public boolean equals(RangerGcsPermissionCheckResult other) {
        return this.type.equals(other.type);
    }

    private enum RangerGcsPermissionCheckResultEnum {
        ALLOW("Allow"), DENY("Deny");

        private final String type;

        private RangerGcsPermissionCheckResultEnum(String type) {
            this.type = type;
        }

        @Override
        public String toString() {
            return this.type;
        }
    }
}
