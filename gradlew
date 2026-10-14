#!/bin/sh
set -e
HERE="$(CDPATH='' cd -- "$(dirname -- "$0")" && pwd)"
JAR="$HERE/gradle/wrapper/gradle-wrapper.jar"
if [ ! -f "$JAR" ]; then
  echo "Missing verified Gradle wrapper JAR. Run: bash scripts/bootstrap-gradle-wrapper.sh" >&2
  exit 2
fi
exec java -classpath "$JAR" org.gradle.wrapper.GradleWrapperMain "$@"
