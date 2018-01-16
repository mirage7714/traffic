#!/bin/bash
main=/home/hadoop/taipei

cd ${main}

wget https://tcgbusfs.blob.core.windows.net/blobtisv/GetVD.gz
gzip -d GetVD.gz
python3 dataProcess.py GetVD
rm GetVD
