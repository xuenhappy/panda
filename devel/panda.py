'''
File: panda.py
Project: devel
File Created: Thursday, 31st December 2020 10:49:59 pm
Author: enxu (xuen@mokar.com)
-----
Last Modified: Thursday, 31st December 2020 10:50:03 pm
Modified By: enxu (xuen@mokahr.com)
-----
Copyright 2021 - 2020 Your Company, Moka
'''
import ctypes
import json
import os
__py_dir = os.path.split(os.path.realpath(__file__))[0]
lib = ctypes.cdll.LoadLibrary(os.path.join(__py_dir, 'panda.so'))

# token function
basicToken = lib.basicToken
basicToken.argtypes = [ctypes.c_char_p, ctypes.c_int]

develQSample=lib.develQSample
develQSample.argtypes = [ctypes.c_char_p]

def bToken(content, piceEng):
    if content is None:
        return []
    goResult = basicToken(content.encode("utf-8"), int(piceEng))
    go_str = str(ctypes.c_char_p(goResult).value, encoding="utf-8")
    lib.freeCStr(goResult)
    if go_str:
        return json.loads(go_str)
    return []


def toSample(content):
    if content is None:
        return []
    goResult = develQSample(content.encode("utf-8"))
    go_str = str(ctypes.c_char_p(goResult).value, encoding="utf-8")
    lib.freeCStr(goResult)
    if go_str:
        return json.loads(go_str)
    return []


str = toSample("中华任命hello words!")
print(str)
