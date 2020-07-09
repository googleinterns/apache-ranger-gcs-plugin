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

import java.security.InvalidParameterException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static com.google.cloud.hadoop.ranger.gcs.permissionCheckService.utilities.ConfigParser.KEY_IP;
import static com.google.cloud.hadoop.ranger.gcs.permissionCheckService.utilities.ConfigParser.KEY_PORT;

public final class Config {
    // IP and port to host the permission check service.
    private final String hostIp;
    private final int hostPort;

    private Config(String hostIp, int hostPort) {
        this.hostIp = hostIp;
        this.hostPort = hostPort;
    }

    public String getHostIp() {
        return hostIp;
    }

    public int getHostPort() {
        return hostPort;
    }

    public static class ConfigBuilder {
        private static final String ERR_MISSING_KEY =
                "Missing required keys in configuration file. Required key=[%s], given key=[%s].";

        private final Set<String> required_key = new HashSet<>(Arrays.asList(KEY_IP, KEY_PORT));
        private final Set<String> added_key = new HashSet<>();

        private String hostIp;
        private int hostPort;

        public ConfigBuilder setHostIp(String hostIp) {
            this.hostIp = hostIp;
            added_key.add(KEY_IP);
            return this;
        }

        public ConfigBuilder setHostPort(int hostPort) {
            this.hostPort = hostPort;
            added_key.add(KEY_PORT);
            return this;
        }

        public Config build() {
            // Check if missing required keys in config file.
            if (! added_key.equals(required_key)) {
                throw new InvalidParameterException(String.format(ERR_MISSING_KEY,
                        String.join(", ", required_key),
                        String.join(", ", added_key)));
            }

            return new Config(hostIp, hostPort);
        }

    }
}