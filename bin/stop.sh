#!/bin/bash
kill -9 `ps -h | grep java | grep -v sbt-launch | grep -v grep | awk '{print $1}'`
