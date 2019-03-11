#!/bin/bash

if [ $# -lt 1 ]
then
  echo "usage: ./set-version.sh <version-number>"
  echo "   example <version-number>: 1-7"
  exit 1
fi

versionnumber=$1
sed -i .bak "s/PCC-VERSION/$versionnumber/g" README.md
rm README.md.bak
