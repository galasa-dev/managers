#!/bin/ash

#
# Copyright contributors to the Galasa project
#
# SPDX-License-Identifier: EPL-2.0
#
mkdir /var/mail
groupadd --non-unique --gid ${JMETER_GROUP_ID:-1000} jmeter
useradd  --non-unique --uid ${JMETER_USER_ID:-1000} --no-log-init --create-home --gid jmeter jmeter
chown jmeter:jmeter /jmeter
tail -f /dev/null
