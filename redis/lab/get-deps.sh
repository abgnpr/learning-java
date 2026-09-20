#!/usr/bin/env bash
# Downloads the Jedis client and its runtime dependencies into lib/.
# No build tool: the labs run on Java 21's single-file source launcher
# with lib/* on the classpath. Jars are gitignored.
set -euo pipefail
cd "$(dirname "$0")"
mkdir -p lib

M2=https://repo1.maven.org/maven2
JARS=(
  "redis/clients/jedis/5.2.0/jedis-5.2.0.jar"
  "org/apache/commons/commons-pool2/2.12.0/commons-pool2-2.12.0.jar"
  "org/slf4j/slf4j-api/2.0.13/slf4j-api-2.0.13.jar"
  "org/slf4j/slf4j-simple/2.0.13/slf4j-simple-2.0.13.jar"
  "com/google/code/gson/gson/2.11.0/gson-2.11.0.jar"
  "org/json/json/20240303/json-20240303.jar"
  "org/apache/commons/commons-lang3/3.14.0/commons-lang3-3.14.0.jar"
)

for path in "${JARS[@]}"; do
  name="${path##*/}"
  if [ -f "lib/$name" ]; then
    echo "have    $name"
    continue
  fi
  echo "fetch   $name"
  curl -fsSL --max-time 120 -o "lib/$name" "$M2/$path"
done

echo
echo "lib/ ready:"
ls -1 lib/
