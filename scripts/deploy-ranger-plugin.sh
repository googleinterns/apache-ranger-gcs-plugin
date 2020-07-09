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

cp $PROJECT_ROOT/$MODULE_PLUGIN/conf/* $CONF_DIR/

################################################
# Replace URL and service name in Security.xml #
################################################

sed -i "s|<value>http://policymanagerhost:port</value>|<value>${RANGER_HOST}:${RANGER_PORT}</value>|g" $CONF_DIR/ranger-gcs-security.xml

sed -i "s|<value>ranger-service-name</value>|<value>$DEFAULT_SERVICE_NAME</value>|g" $CONF_DIR/ranger-gcs-security.xml

############################
# Replace URL in Audit.xml #
############################

sed -i "s|<value>http://solrhosturl:8983/solr/ranger_audits</value>|<value>${RANGER_HOST}:8983/solr/ranger_audits</value>|g" $CONF_DIR/ranger-gcs-audit.xml
