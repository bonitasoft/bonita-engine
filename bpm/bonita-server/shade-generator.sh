


#generate the shade
mvn dependency:list | grep org.bonitasoft | cut --delimiter="]" --fields=2- | sed "s/^[ |+\\-]*//" | sed "s/:jar:.*:compile//" | sed "s/^/\t\t\t\t\t\t\t\t\t\<include\>/" | sed "s/$/\<\/include\>/"|  grep -Ev 'bonita-common|bonita-client'| sort > shade.txt


#compare with old shade:
cat shade.txt | sed "s/\s*<include>//" | sed "s/<\/include>//" | sort > shade2.txt

#put the old shade (with the includes) in orgshade.txt
cat orgshade.txt | sed "s/\s*<include>//" | sed "s/<\/include>//" | sort > orgshade2.txt


#compare file shade2.txt and orgshade2.txt
