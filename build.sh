#!/bin/sh

help(){
cat<<HELP

	==========help function==========
	USAGE:
		./build.sh start end 
		
		make sure your xls file int current dir!!!
		start	:	begin parser xml from row start, start min value is 1 if start < 1 defalut is 1	
		end : 	end parser xls to end row if end < 0 ,defualt is all rows, end must greatter than start	
	==========help end==========

HELP
}

#if [ -z $1 ]; then
#	start=-1	
#else 
#	start=$1
#fi
#
#if [ -z $2 ]; then
#	end=-1
#else
#	end=$2
#fi
#
#if [ $start -gt $end ]; then
#	help
#	return
#fi
#
rm ./bin -rf 2>/dev/null
rm ./src/*.class 2>/dev/null

mkdir bin

echo "javac ./src/*"
javac -cp ./jxl.jar -target 1.6 ./src/* -d ./bin

echo "java Utils"
java -Djava.ext.dirs=./ Utils $*
