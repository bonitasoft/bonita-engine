#!/bin/sh

cd ../../../
./gradlew build -x test
cd -

export VERSION=`cat ../platform-setup/build/resources/main/PLATFORM_ENGINE_VERSION`

echo "========================================"
echo "version:${VERSION}"
echo "========================================"

export E2E_DIR="build/e2e-distrib"
export ZIP=Bonita-platform-setup-${VERSION}.zip

rm -rf ${E2E_DIR}
unzip -q -d ${E2E_DIR} build/distributions/${ZIP}
unzip -q -d ${E2E_DIR}-jar-exploded ${E2E_DIR}/lib/platform-setup-${VERSION}.jar

echo "========================================"
echo "platform-setup-${VERSION}.jar exploded:"
tree ${E2E_DIR}-jar-exploded
echo "========================================"

ls -l ${E2E_DIR}/../platform-setup-${VERSION}-tests.jar
unzip -q -d ${E2E_DIR}-tests-jar-exploded target/platform-setup-${VERSION}-tests.jar

echo "========================================"
echo "platform-setup-${VERSION}-test.jar exploded:"
tree ${E2E_DIR}-tests-jar-exploded
echo "========================================"

echo "========================================"
echo "distribution structure:"
echo "========================================"
tree -L 3 ${E2E_DIR}

echo "========================================"
echo "check permissions:"
echo "========================================"
ls -ltrR ${E2E_DIR}



