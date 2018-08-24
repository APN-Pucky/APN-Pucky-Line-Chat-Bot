#!/bin/bash
set -e
#./gradlew compileJava
./gradlew build
heroku local web
