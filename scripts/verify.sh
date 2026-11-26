#!/bin/sh
set -eu
cd "$(dirname "$0")/.."
./gradlew clean build
mkdir -p build/verification
for run in 1 2; do
    java -jar build/libs/consistent-hash-ring.jar "build/verification/placements-$run.bin" > "build/verification/result-$run.md"
done
cmp build/verification/placements-1.bin build/verification/placements-2.bin
cmp build/verification/result-1.md build/verification/result-2.md
cat build/verification/result-1.md
