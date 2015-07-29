#!/bin/bash

#
# Usage: ./build-docker-image.sh {pierone.address}/{team}/fullstop ../deploy-fullstop/production.yaml b1 0.1
#

# load interrupt lib
. bash_libs/interrupt.sh
# enable ctrl+c prompt cancelation
trap ctrl_c SIGINT

IMAGE=$1
SENZA_CONFIGURATION_YAML=$2
STACK_VERSION=$3
APP_VERSION=$4
FULL_IMAGE=$IMAGE:$APP_VERSION

echo '##### mvn clean install -U #####'
mvn clean install -U

echo '##### Build scm-source.json #####'
./scm-source.sh

echo '##### Start docker #####'
boot2docker start

echo '##### Docker build #####'
docker build -t $FULL_IMAGE fullstop

echo '##### Pierone login####'
pierone login

echo '##### Docker push #####'
docker push $FULL_IMAGE

echo '##### mai authentication with default profile #####'
mai

echo '##### Print stack configuration #####'
senza print $SENZA_CONFIGURATION_YAML --region eu-west-1 $STACK_VERSION $APP_VERSION

echo '##### Create stack in aws with senza with --disable-rollback option #####'
senza create $SENZA_CONFIGURATION_YAML --disable-rollback --region eu-west-1 $STACK_VERSION $APP_VERSION

# clear trap
trap - SIGINT

echo '##### Show senza events #####'
senza events $SENZA_CONFIGURATION_YAML $STACK_VERSION --region eu-west-1 -w 2

echo '##### Show senza traffic #####'
senza traffic $SENZA_CONFIGURATION_YAML

echo '##### Set 100% traffic to new version #####'
senza traffic $SENZA_CONFIGURATION_YAML $STACK_VERSION 100
