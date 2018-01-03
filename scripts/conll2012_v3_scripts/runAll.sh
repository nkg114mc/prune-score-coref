#!/bin/bash

# runAll.sh <path_to_ontonotes_top_level_data> "top_level_dir_containing_skel_files" <train|test>
 # E.g.
# runAll.sh "ontonotes-release-4.0/data/" "conll-2011/v2/data/train/" train

 ## Step 1: delete auto files.
if [ $3 == 'train' ]; then 
 echo "Step 1: DELETING AUTO FILES AS THEY WILL NOT BE USED".
 dir="$2/data/english/annotations"
 find $dir -name "*v2_auto*" -exec rm "{}" \
else
 echo "Step 1: delete auto files.: Skipped since this test data."
fi

# Step 2: skel to conll
echo "Step 2: CONVERTING SKELETON FILES TO CONLL FORMAT".
./skeleton2conll.sh -D $1 $2

# Step 3: conll to coref
echo "Step 3: CREATING COREF FILE FROM CONLL".
./conll2coreference.sh $2

# Step 4: conll to name
echo "Step 4: CREATING NAME FILE FROM CONLL".
./conll2name.sh $2

# Step 5: conll to parse
echo "Step 5: CREATING PARSE FILE FROM CONLL".
./conll2parse.sh $2

# Step 6: renaming all files by removing v2_gold
echo "Step 6: RENAMING ALL FILES BY REMOVING THE STRING $str FROM THEIR NAMES".

if [ $2 == 'train' ]; then
 str="v2_gold_"
else
 str="v2_auto_"
fi

files=`find $2 -type f`
for file in $files
do
	newfilename=${file//$str/""}
	mv $file $newfilename
done
