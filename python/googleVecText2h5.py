#coding=utf-8
'''
Created on Dec 24, 2019
create h5 dict from google vec text file
@author: xuen
'''
import sys
import h5py
import numpy as np
import re
import subprocess

def getfileline(filei):
    l=subprocess.getoutput("wc -l %s"%filei)
    return int(l.split()[0])

SX=re.compile(" (?! )")
if len(sys.argv)<3:
    print ("python %s [input_txt_file] [output_h5_file]"%sys.argv[0])
    exit(0)


def iter_data(filei,max_line=5e5):
    words,arys=[],[]
    with open(filei) as fd:
        for line in fd:
            line=line.rstrip()
            if not line:
                continue
            lines=SX.split(line)
            if len(lines)<10:
                print("bad line:",line)
                continue
            words.append(lines[0])
            arys.append([float(l) for l in lines[1:]])
            if len(words)>max_line:
                yield np.asarray(arys,dtype=np.float32),words
                arys,words=[],[]
    if words:
        yield np.asarray(arys,dtype=np.float32),words


with h5py.File(sys.argv[2],"w") as f:
    line_num=getfileline(sys.argv[1])
    fd=iter_data(sys.argv[1])
    arys,words=next(fd)
    row_count,dims=arys.shape
    wset=f.create_dataset("wds",data=words,dtype=h5py.special_dtype(vlen=unicode),maxshape=(line_num,),compression="gzip")
    dset=f.create_dataset("ary",data=arys,chunks=(1,dims),maxshape=(line_num,dims),compression="gzip")
    for arys,words in fd:
        dset.resize(row_count + arys.shape[0], axis=0)
        dset[row_count:] = arys

        wset.resize(row_count + len(words), axis=0)
        wset[row_count:] = words

        row_count += arys.shape[0]

    print("shape:",row_count,dims)
    print("chunks:",dset.chunks,dset.compression)
    for key in f.keys():
        print(f[key].name,f[key].shape)
        print(f[key][-3:])

