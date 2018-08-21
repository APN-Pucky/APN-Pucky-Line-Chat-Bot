#!/bin/bash
set -e
./gradlew compileJava
heroku local web
