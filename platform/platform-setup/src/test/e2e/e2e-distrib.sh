#!/bin/sh

cd ../../../
./gradlew build -x test
cd -

export VERSION=`cat ../platform-resources/build/resources/main/PLATFORM_ENGINE_VERSION`

echo "========================================"
echo "version:${VERSION}"
echo "========================================"

export E2E_DIR="build/e2e-distrib"
export ZIP=Bonita-platform-setup-${VERSION}.zip

rm -rf ${E2E_DIR}
unzip -q -d ${E2E_DIR} build/distributions/${ZIP}
rm -rf ${E2E_DIR}-jar-exploded
unzip -q -d ${E2E_DIR}-jar-exploded ${E2E_DIR}/lib/platform-setup-${VERSION}.jar

echo "========================================"
echo "platform-setup-${VERSION}.jar exploded:"
tree ${E2E_DIR}-jar-exploded
echo "========================================"

echo "========================================"
echo "distribution structure:"
echo "========================================"
tree -L 3 ${E2E_DIR}

echo "========================================"
echo "check permissions:"
echo "========================================"
ls -ltrR ${E2E_DIR}



