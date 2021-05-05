#!/bin/bash


nohup ++JAVA_CMD++ -jar ++BOOT_JAR++ --bootstrap ++BOOTSTRAP++ --remotemaven ++MAVEN_REPO++ --localmaven ++MAVEN_LOCAL++ --obr  mvn:dev.galasa/dev.galasa.uber.obr/++MAVEN_VERSION++/obr --trace $2 > $1 &

echo PROCESS=$!

sleep 2