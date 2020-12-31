# coding="utf-8"
'''
File: chutils.py
Project: darts
File Created: Friday, 26th June 2020 1:22:54 pm
Author: Xu En (xuen@mokar.com)
-----
Last Modified: Friday, 26th June 2020 1:22:59 pm
Modified By: Xu En (xuen@mokahr.com)
-----
Copyright 2021 - 2020 Your Company, Moka
'''

import os
__all__ = ['charSplit', 'charType', 'utf8Len', 'wordLen']

__py_dir = os.path.split(os.path.realpath(__file__))[0]


def __loadCharTable(tabelfile):
    charsmap = {}
    tagname = None
    with open(tabelfile, encoding="utf-8") as fd:
        for line in fd:
            line = line.strip()
            if not line:
                continue
            if line.startswith("-%"):
                tagname = "<%s>" % line[2:]
                continue
            for ch in line:
                charsmap[ch] = tagname

    return charsmap


def __loadSpecialChars(chmap):
    for code in range(33):
        chmap[chr(code)] = '<EMPTY>'
    for code in range(127, 161):
        chmap[chr(code)] = '<EMPTY>'
    for ch in u'\u00A0\u0020\u3000':
        chmap[ch] = '<EMPTY>'
    return chmap


__CHAR_MAP = __loadSpecialChars(__loadCharTable(os.path.join(__py_dir, "char_table.txt")))


def charType(ch):
    if ch in __CHAR_MAP:
        return __CHAR_MAP[ch]
    code = ord(ch)
    if code < 255:
        if 'A' <= ch <= 'Z':
            return '<ENG>'
        if 'a' <= ch <= 'z':
            return '<ENG>'
        if '0' <= ch <= '9':
            return '<NUM>'
        return '<POS>'
    if '０' <= ch <= '９':
        return '<NUM>'
    if 'Ａ' <= ch <= 'Ｚ':
        return '<ENG>'
    if 0x4e00 <= code <= 0x9fa5:
        return '<CJK>'
    if 0x3130 <= code <= 0x318F:
        return '<CJK>'
    if 0xAC00 <= code <= 0xD7A3:
        return '<CJK>'
    if 0x0800 <= code <= 0x4e00:
        return '<CJK>'
    if 0x3400 <= code <= 0x4DB5:
        return '<CJK>'
    return '<UNK>'


def wordLen(unichrs):
    nums = 0
    buf_type = ''
    for ch in unichrs:
        ctype = charType(ch)
        if ctype != buf_type:
            if (ctype == '<EMPTY>' and buf_type == '<POS>') or (buf_type == '<EMPTY>' and ctype == '<POS>'):
                buf_type = '<POS>'
                continue

            if buf_type:
                nums += 1
                buf_type = ''

        buf_type = ctype
        if buf_type == "<CJK>":
            nums += 1
            buf_type = ''
            continue
    if buf_type:
        nums += 1
    return nums


def charSplit(unicode_strs):
    chr_buffer = []
    buf_type = None
    for ch in unicode_strs:
        ctype = charType(ch)
        if ctype != buf_type:
            if (ctype == '<EMPTY>' and buf_type == '<POS>') or (buf_type == '<EMPTY>' and ctype == '<POS>'):
                buf_type = '<POS>'
                chr_buffer.append(ch)
                continue

            if chr_buffer:
                yield "".join(chr_buffer), buf_type
            chr_buffer = []

        buf_type = ctype

        if buf_type == "<CJK>":
            yield ch, buf_type
            continue

        # uplower split
        #if len(chr_buffer)>0 and buf_type == "<ENG>":
        #    if 'a'<=chr_buffer[-1]<='z' and 'A'<=ch<='Z':
        #        yield "".join(chr_buffer), buf_type
        #        chr_buffer = []

        chr_buffer.append(ch)

    if chr_buffer:
        yield "".join(chr_buffer), buf_type


def __utf8len(c):
    codepoint = ord(c)
    if codepoint <= 0x7f:
        return 1
    if codepoint <= 0x7ff:
        return 2
    if codepoint <= 0xffff:
        return 3
    if codepoint <= 0x10ffff:
        return 4
    raise ValueError('Invalid Unicode character: ' + hex(codepoint))


def utf8Len(s):
    """
    claulate the string utf8 len without create a bytes
    """
    return sum(__utf8len(c) for c in s)
