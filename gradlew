#!/usr/bin/env sh
set -e
APP_HOME="$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)"
WRAPPER_JAR="$APP_HOME/gradle/wrapper/gradle-wrapper.jar"
if [ -f "$WRAPPER_JAR" ] && command -v java >/dev/null 2>&1; then
  exec java ${JAVA_OPTS:-} ${GRADLE_OPTS:-} -classpath "$WRAPPER_JAR" org.gradle.wrapper.GradleWrapperMain "$@"
fi
if command -v gradle >/dev/null 2>&1; then
  exec gradle "$@"
fi
echo "Gradle wrapper JAR not found ($WRAPPER_JAR) and 'gradle' is not installed." >&2
echo "Install Gradle, run with a local Gradle installation, or add gradle-wrapper.jar to gradle/wrapper/." >&2
exit 1
