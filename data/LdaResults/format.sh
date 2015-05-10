#！bin/sh

####################################
#
#This File is used for sort by line.
#Input file is theta.
#
####################################
FILE=$1
echo $FILE
NEWFILE="NUM_"$FILE
cat -n $FILE > $NEWFILE
COUNT=`awk 'NR==1{print NF}' $NEWFILE`
echo $NEWFILE
#cat $NEWFILE | awk '{printf("%s\t%s\n",$1,$2)}' 
for((i=2;i<=$COUNT;i++));do
	awk  '{printf("%s\t%s\n",$1,$'$i')}' $NEWFILE > $i.sort
	sort -rgk2 $i.sort -o $i.sort
done
echo "##########"
echo $COUNT
