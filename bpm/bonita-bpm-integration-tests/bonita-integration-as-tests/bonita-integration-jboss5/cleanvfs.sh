#!/bin/bash

export WORK_DIR=$(cd $(dirname "$0"); pwd)
cd $WORK_DIR/target/jboss-5.1.0.GA/server/default/tmp/vfs-nested.tmp
echo Clean in the folder $WORK_DIR/target/jboss-5.1.0.GA/server/default/tmp/vfs-nested.tmp

#list files | grep only specific name | keep last two | remove the filtered files
function clean
{
ls -t | grep $1 | sed -e '1,2d' | xargs rm
}

i=0

while [ $i -le 20 ]
do
(( i++ ))
clean antlr
clean aopalliance
clean asm-analysis
clean asm-commons
clean asm-tree
clean asm-util
clean bonita-common
clean bonita-deploy-ejb3
clean bonita-server
clean c3p0
clean commons-codec
clean commons-collections
clean commons-dbcp
clean commons-fileupload
clean commons-io
clean commons-pool
clean jaxb-xjc
clean jnr-x86asm
clean commons-logging
clean commons-pool
clean dom4j
clean ecj
clean groovy-all
clean h2
clean hazelcast
clean hazelcast-hibernate
clean hibernate-commons-annotations
clean hibernate-core
clean hibernate-ehcache
clean hibernate-entitymanager
clean hibernate-jpa
clean http-client
clean http-core
clean istack-commons-runtime
clean jackson-core
clean jackson-databind
clean javassist
clean jaxb-core
clean jaxb-xjc
clean jbcrypt
clean jboss-logging
clean jffi
clean jnr-constants
clean jnr-ffi
clean jnr-posix
clean jtidy
clean logback-classic
clean logback-core
clean quartz
clean slf4j-api
clean spring-aop
clean spring-asm
clean spring-beans
clean spring-context
clean spring-core
clean spring-expression
clean xbean-classloader
clean xmlpull
clean xpp3_min
clean xstream
sleep 30s
done

