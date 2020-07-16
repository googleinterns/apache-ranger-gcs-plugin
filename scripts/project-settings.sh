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

#######################
# Environment setting #
#######################

#-----------------------------
# Ranger installation location
#-----------------------------
# Installation location of Ranger server.
if [[ -z $RANGER_HOME ]] ; then
	RANGER_HOME="/usr/lib/ranger"
fi

# Installation location of Hadoop.
if [[ -z $HADOOP_HOME ]] ; then
	HADOOP_HOME="/usr/lib/hadoop"
fi

# Ranger server host name.
# For example: http://<my-ranger-server-host>
if [[ -z $RANGER_HOST ]] ; then
	# Resolve server host if not set
	# This section only works on Dataproc clusters
	source /usr/local/share/google/dataproc/bdutil/bdutil_env.sh
	source /usr/local/share/google/dataproc/bdutil/bdutil_helpers.sh
	RANGER_HOST=http://$(get_metadata_master).$(dnsdomainname)
fi

# Ranger server port
RANGER_PORT=6080

######################
# Deployment Setting #
######################

#----------------
# Service setting
#----------------
# The name of auto-created Ranger service.
DEFAULT_SERVICE_NAME="gcs"

#------------------------------
# Permission check proxy server
#------------------------------
# Hostname and port that the proxy server will listen on
# Hostname shouldn't contain protocol. Need to cut "http://".
PROXY_SERVER_HOST=${RANGER_HOST#"http://"}
PROXY_SERVER_PORT=6621

#-----------------
# Systemd settings
#-----------------
# Directory for placing systemd service definition
SYSTEMD_DIR="/etc/systemd/system/"

####################
# Project Settings #
####################
# Project environment setting.
MODULE_SERVICE="ranger-gcs-service"
MODULE_PROXY_SERVER="ranger-gcs-permission-check-service"
MODULE_PLUGIN="ranger-gcs-plugin"
MODULE_ADAPTER="ranger-gcs-connector-adapter"
TARGET_DIR="target"

