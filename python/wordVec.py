#!/usr/bin/env python  
# -*- coding: utf-8 -*- 
'''
Created on Jun 1, 2019
@author: xuen
'''


class VecDict():

    def __init__(self, dpath):
        self.dpath = dpath

    def wordnum(self):
        raise NotImplementedError

    def embeding(self, uni_words):
        raise NotImplementedError

    def close(self):
        raise NotImplementedError

    def wv_size(self):
        raise NotImplementedError

    def hasw(self, words):
        raise NotImplementedError


class H5VecDict(VecDict):

    def __init__(self, dpath, cache=True):
        import h5py
        h5file = h5py.File(dpath, 'r')
        self.cache = cache
        words = h5file['wds'][()]
        vec = h5file['ary']
        wnum, wszie = vec.shape
        assert wnum == words.shape[0], "bad dict file!"
        self._wnum = wnum
        self._wsize = wszie
        self.words_idx = dict((v, i) for (i, v) in enumerate(words))
        if cache:
            self.vec = vec[()]
        else:
            self.vec = vec 
            self.obj = h5file
    
    def embeding(self, uni_word):
        if uni_word in self.words_idx:
            return self.vec[self.words_idx[uni_word]]
        return None
       
    def hasw(self, words):
        return words in self.words_idx
    
    def wv_size(self):
        return self._wsize

    def wordnum(self):
        return self._wnum

    def close(self):
        if not self.cache:
            self.obj.close()
        
        
class SingleWordDict(VecDict):

    def __init__(self, dpath):
        VecDict.__init__(self, dpath)
        datas = set([])
        with open(dpath, encoding="utf-8") as osfile:
            for line in osfile:
                line = line.strip()
                if not line:
                    continue
                datas.add(line)
        datas = list(datas)
        datas.sort()
        self.datas = dict((w, i) for (i, w) in enumerate(datas))

    def wv_size(self):
        return 1

    def embeding(self, uni_word):
        if uni_word in self.datas:
            return self.datas[uni_word]
        return None

    def wordnum(self):
        return len(self.datas) + 1

    def close(self):
        pass

    def hasw(self, words):
        return words in self.datas
        
        
