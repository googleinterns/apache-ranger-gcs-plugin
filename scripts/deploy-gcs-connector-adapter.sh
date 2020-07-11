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

PROJECT_ROOT=$(dirname "$( cd "$( dirname "${BASH_SOURCE[0]}" )" >/dev/null 2>&1 && pwd )")
source $PROJECT_ROOT/scripts/project-settings.sh

######################################
# Copy adapter jar to hadoop lib dir #
######################################

JAR_NAME=$(ls $PROJECT_ROOT/$MODULE_ADAPTER/$TARGET_DIR/ | grep -E "^$MODULE_ADAPTER.*\.jar$")

DST="$HADOOP_HOME/lib"

cp $PROJECT_ROOT/$MODULE_ADAPTER/$TARGET_DIR/$JAR_NAME $DST
