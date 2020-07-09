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

import org.apache.ranger.plugin.policyengine.RangerAccessResourceImpl;

public class RangerGcsResource extends RangerAccessResourceImpl {
    private static final String BUCKET = "bucket";
    private static final String OBJECT_PATH = "object-path";

    public RangerGcsResource (String bucket) {
        this(bucket, null);
    }

    public RangerGcsResource (String bucket, String objectPath) {
        setValue(BUCKET, bucket);
        setValue(OBJECT_PATH, objectPath);
    }

    public String getBucket () {
        return (String) getValue(BUCKET);
    }

    public String getObjectPath () {
        return (String) getValue(OBJECT_PATH);
    }
}
