#!/bin/sh

help (){
	echo "$0 <BONITA_SOURCE_ROOT>";
}

generate_oracle (){
	PATHTOFILE=$1
	FILENAME=$2
	PATTERNFILENAME=$3
	cat $PATHTOFILE/$FILENAME | sed -f oracle.sed > $PATHTOFILE/oracle-${PATTERNFILENAME};
}

generate_mysql (){
	PATHTOFILE=$1
	FILENAME=$2
	PATTERNFILENAME=$3
	cat $PATHTOFILE/$FILENAME | sed -f mysql.sed > $PATHTOFILE/mysql-${PATTERNFILENAME};
}

generate_postgres (){
	PATHTOFILE=$1
	FILENAME=$2
	PATTERNFILENAME=$3
	cat $PATHTOFILE/$FILENAME | sed -f postgres.sed > $PATHTOFILE/postgres-${PATTERNFILENAME};
}

generate_sqlserver (){
	PATHTOFILE=$1
	FILENAME=$2
	PATTERNFILENAME=$3
	cat $PATHTOFILE/$FILENAME | sed -f sqlserver.sed > $PATHTOFILE/sqlserver-${PATTERNFILENAME};
}

parse (){
	PATHTOFILE=${1%/*}
	FILENAME=${1##*/}
	PATTERNFILENAME=${FILENAME#*-}
	generate_oracle $PATHTOFILE $FILENAME $PATTERNFILENAME;
	generate_mysql $PATHTOFILE $FILENAME $PATTERNFILENAME;
	generate_postgres $PATHTOFILE $FILENAME $PATTERNFILENAME;
	generate_sqlserver $PATHTOFILE $FILENAME $PATTERNFILENAME;
}

if [ $# != 1 ]; then
	help;
	exit 0;
fi

files=$(find $1 -name h2*.sql | grep -v "QuartzTables")

for f in ${files}
do
	parse $f
done
