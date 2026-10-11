#!/bin/sh

# ----------------------------------------------------------------------------
# Gradle Start Up Script for POSIX platforms
# ----------------------------------------------------------------------------

APP_HOME=$(cd "$(dirname "$0")" && pwd -P)
CLASSPATH="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"

# If wrapper jar is missing or empty, delegate to system gradle if available
if [ ! -s "$CLASSPATH" ]; then
    if command -v gradle >/dev/null 2>&1; then
        exec gradle "$@"
    else
        echo "Gradle wrapper jar not found. Please install Gradle or run 'gradle wrapper'." >&2
        exit 1
    fi
fi

# Determine the Java command to use
if [ -n "$JAVA_HOME" ] ; then
    JAVACMD="$JAVA_HOME/bin/java"
else
    JAVACMD="java"
fi

if ! command -v "$JAVACMD" >/dev/null 2>&1; then
    echo "ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH." >&2
    exit 1
fi

exec "$JAVACMD" "-Dorg.gradle.appname=gradlew" -classpath "$CLASSPATH" org.gradle.wrapper.GradleWrapperMain "$@"
