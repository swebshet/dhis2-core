#!/bin/sh
set -e

# script adapted from https://raw.githubusercontent.com/dhis2/dhis2-core/63f627ee15ef5de5adf23bede768f42aef40159b/docker/tomcat-debian/docker-entrypoint.sh

DHIS2HOME=/opt/dhis2

# debug output to show as what user this runs and what ownership/permission /opt/dhis2 actually has
# ownership is set by us via JibOwnershipExtension see dhis-web-portal/pom.xml
id
ls -la $DHIS2HOME

# this actually shows that the script was only executed as root
#if [ "$(id -u)" = "0" ]; then
    chown -R 65534:65534 $DHIS2HOME
#fi

exec catalina.sh run