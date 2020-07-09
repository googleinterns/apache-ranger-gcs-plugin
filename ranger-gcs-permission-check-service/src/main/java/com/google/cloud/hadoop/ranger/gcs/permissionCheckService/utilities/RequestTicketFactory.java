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

import com.google.cloud.hadoop.ranger.gcs.utilities.RangerGcsHttpRequestKey;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Parse http get query string into RequestTicket.
 */
public class RequestTicketFactory {
    private static final String URI_SPLIT = "&";
    private static final String URI_EQUAL = "=";
    private static final String LIST_SPLIT = ",";

    public static RequestTicket createRequestTicketFromHttpRequestUri(String uri) {
        RequestTicket ticket = new RequestTicket();

        // Http request url: [token1]&[token2]&[token3]...
        for (String token : uri.split(URI_SPLIT)) {
            // Token: [key]=[value]
            String[] entry = token.split(URI_EQUAL);
            switch (entry[0]) {

                case RangerGcsHttpRequestKey.USER:
                    ticket.setUser(entry[1]);
                    break;

                case RangerGcsHttpRequestKey.USER_GROUPS:
                    String[] groups = entry[1].split(LIST_SPLIT);
                    for (String group: groups)
                        ticket.addUserGroup(group);
                    break;

                case RangerGcsHttpRequestKey.RESOURCE:
                    String bucket = "", objectPath = "";

                    // Value can be "bucket/path", "bucket/" or "/path"
                    if (entry[1].contains("/")) {
                        // Slice at the first slash
                        // Bucket: all characters (can be none) before the first slash.
                        // Object path: all characters (can be none) after the first slash.
                        Pattern pattern = Pattern.compile("([^/]*)/(.*)");
                        Matcher matcher = pattern.matcher(entry[1]);
                        if (matcher.find()) {
                            bucket = matcher.group(1);
                            objectPath = matcher.group(2);

                            if (objectPath.length() == 0) {
                                objectPath = "*";
                            }
                        }
                    // Value is "bucket", no object path.
                    } else {
                        bucket = entry[1];
                        objectPath = "*";
                    }

                    ticket.setBucket(bucket);
                    ticket.setObjectPath(objectPath);
                    break;

                case RangerGcsHttpRequestKey.ACTIONS:
                    List<String> actions = new ArrayList<String>(Arrays.asList(entry[1].split(LIST_SPLIT)));
                    ticket.setActions(actions);
                    break;
            }
        }

        return ticket;
    }
}
