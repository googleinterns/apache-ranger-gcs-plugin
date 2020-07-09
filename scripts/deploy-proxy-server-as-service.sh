#!/bin/bash

# Copyright 2020 Google Inc. All Rights Reserved.
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#    http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.

set -e

PROJECT_ROOT=$(dirname "$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )")
source $PROJECT_ROOT/scripts/project-settings.sh

################
# Copy configs #
################

CONF_DIR="/etc/gcs/conf"

mkdir -p $CONF_DIR

cp $PROJECT_ROOT/$MODULE_PROXY_SERVER/conf/* $CONF_DIR

###############################################
# Replace ip and port in proxy server setting #
###############################################

sed -i "s|<value>proxy-server-host</value>|<value>${PROXY_SERVER_HOST}</value>|g" $CONF_DIR/ranger-gcs-permission-check-service.xml
sed -i "s|<value>proxy-server-port</value>|<value>${PROXY_SERVER_PORT}</value>|g" $CONF_DIR/ranger-gcs-permission-check-service.xml

#########################
# Create Detination Dir #
#########################

DESTINATION=$RANGER_HOME/ranger-gcs-plugin-proxy-server

mkdir -p $DESTINATION

###########################
# Move Jar to Destination #
###########################

JAR_NAME=$(ls $PROJECT_ROOT/$MODULE_PROXY_SERVER/$TARGET_DIR/ | grep -E "^$MODULE_PROXY_SERVER.*\.jar$")

SERVICE_JAR_NAME=ranger-gcs-plugin-proxy-server.jar

cp $PROJECT_ROOT/$MODULE_PROXY_SERVER/$TARGET_DIR/$JAR_NAME $DESTINATION/$SERVICE_JAR_NAME

##################################
# Deploy Systemd Service Setting #
##################################

cp $PROJECT_ROOT/scripts/ranger-gcs-plugin-proxy-server.service $SYSTEMD_DIR
