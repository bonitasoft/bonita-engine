#!/bin/sh

generate_oracle (){
	PATHTOFILE=$1
	FILENAME=$2
	cat $PATHTOFILE/$FILENAME | sed -f oracle.sed > $PATHTOFILE/../oracle/${FILENAME};
}

generate_mysql (){
	PATHTOFILE=$1
	FILENAME=$2
	cat $PATHTOFILE/$FILENAME | sed -f mysql.sed > $PATHTOFILE/../mysql/${FILENAME};
}

generate_postgres (){
	PATHTOFILE=$1
	FILENAME=$2
	cat $PATHTOFILE/$FILENAME | sed -f postgres.sed > $PATHTOFILE/../postgres/${FILENAME};
}

generate_sqlserver (){
	PATHTOFILE=$1
	FILENAME=$2
	cat $PATHTOFILE/$FILENAME | sed -f sqlserver.sed > $PATHTOFILE/../sqlserver/${FILENAME};
}

parse (){
	PATHTOFILE=${1%/*}
	FILENAME=${1##*/}
	generate_oracle $PATHTOFILE $FILENAME;
	generate_mysql $PATHTOFILE $FILENAME;
	generate_postgres $PATHTOFILE $FILENAME;
	generate_sqlserver $PATHTOFILE $FILENAME;
}

files=$(find ../sql/h2 -name *.sql | grep -v "QuartzTables")

for f in ${files}
do
	parse $f
done
