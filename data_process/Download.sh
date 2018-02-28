#!/bin/bash
main=/home/hadoop/taipei

cd ${main}

wget 	https://tcgbusfs.blob.core.windows.net/blobtisv/GetVD.xml.gz
gzip -d GetVD.xml.gz
python3 dataProcess.py GetVD.xml
rm GetVD
