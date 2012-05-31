#!/bin/sh
mvn dependency:list | grep org.bonitasoft | cut --delimiter="]" --fields=2- | sed "s/^[ |+\\-]*//" | sed "s/:jar:6.0-SNAPSHOT:compile//" | sed "s/^/\t\t\t\t\t\t\t\t\t\<include\>/" | sed "s/$/\<\/include\>/"|  grep -Ev 'bos-common|bos-client'| sort > shade.txt
