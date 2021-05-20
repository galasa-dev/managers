#!/bin/bash

nohup ++JAVA_CMD++ -jar ++SIMPLATFORM_JAR++ > ++SIMPLATFORM_CONSOLE++ &

echo PROCESS=$!

sleep 2