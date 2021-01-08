package darts

import (
	"bufio"
	"compress/gzip"
	"encoding/gob"
	"fmt"
	"os"
	"panda/utils"
	"regexp"
	"strings"
)

/*
 * File: tokenizer.go
 * Project: darts
 * use for the tokenzier
 * File Created: Thursday, 7th January 2021 2:41:50 pm
 * Author: enxu (xuen@mokar.com)
 * -----
 * Last Modified: Thursday, 7th January 2021 2:41:56 pm
 * Modified By: enxu (xuen@mokahr.com)
 * -----
 * Copyright 2021 - 2021 Your Company, Moka
 */

//BasiSplitStr spli the basic str
func BasiSplitStr(str *string, piceEng bool) []*Atom {
	if str == nil {
		return nil
	}
	result := make([]*Atom, 0, len(*str))
	if len(*str) < 2 {
		result = append(result, NewAtom(str, 0, 1))
	}
	if !piceEng {
		CharSplit(str, func(img, stype string, s, e int) {
			atom := NewAtom(&img, s, e)
			atom.AddType(stype)
			result = append(result, atom)
		})
		return result
	}
	CharSplit(str, func(img, stype string, s, e int) {
		if stype == "<ENG>" {
			pices := SubEngWord(img)
			lems := s
			for _, w := range pices {
				alen := len(w)
				if w[alen-1] == '@' || w[alen-1] == '$' {
					alen--
				}
				if alen < 1 {
					continue
				}
				atom := NewAtom(&w, lems, lems+alen)
				lems += alen
				atom.AddType(stype)
				result = append(result, atom)
			}

		} else {
			atom := NewAtom(&img, s, e)
			atom.AddType(stype)
			result = append(result, atom)
		}
	})
	return result
}

var segment Segment

//init the segment
func init() {

}

//AtomStrIter iter atom
type AtomStrIter struct {
	atoms     []*Atom
	skipEmpty bool
	skipPos   bool
}

//NewAtomStrIter make a AtomStrIter
func NewAtomStrIter(atoms []*Atom, skipEmpty, skipPos bool) *AtomStrIter {
	return &AtomStrIter{atoms, skipEmpty, skipPos}
}

//IterStr iter the atom list
func (alist *AtomStrIter) IterStr(dfunc func(*string, int) bool) {
	if !(alist.skipEmpty || alist.skipPos) {
		for i, a := range alist.atoms {
			if dfunc(&a.Image, i) {
				break
			}
		}
		return
	}

	for i, a := range alist.atoms {
		if alist.skipEmpty && a.Tags != nil && a.Tags.Contains("<EMPTY>") {
			continue
		}

		if alist.skipPos && a.Tags != nil && a.Tags.Contains("<POS>") {
			continue
		}
		if dfunc(&a.Image, i) {
			break
		}
	}
}

//FilePairIter is a input
type FilePairIter struct {
	filepath  string
	skipEmpty bool
	skipPos   bool
	label     map[string]int
}

//NewFilePairIter return a new NewFilePairIter
func NewFilePairIter(path string, skipEmpty, skipPos bool) *FilePairIter {
	fIter := new(FilePairIter)
	fIter.filepath = path
	fIter.skipEmpty = skipEmpty
	fIter.skipPos = skipPos
	fIter.label = make(map[string]int)
	return fIter
}

//IterPairFile iter files
func (fIter *FilePairIter) IterPairFile(dfunc func(StringIter, int) bool) {
	splitRe, _ := regexp.Compile(`\s*,\s*`)
	err := utils.ReadLine(fIter.filepath, func(line string) bool {
		line = strings.TrimSpace(line)
		lines := splitRe.Split(line, 2)
		if len(lines) != 2 {
			fmt.Println("bad line support", line)
			return false
		}
		atom := BasiSplitStr(&lines[1], false)
		iter := NewAtomStrIter(atom, fIter.skipEmpty, fIter.skipPos)
		label, ok := fIter.label[lines[0]]
		if !ok {
			label = len(fIter.label) + 1
			fIter.label[lines[0]] = label
		}
		dfunc(iter.IterStr, label)
		return false
	})
	if err != nil {
		fmt.Printf("Open file failed [Err:%s]\n", err.Error())
	}
}

//CompileTxtMatchDict compile a txt to trie
func CompileTxtMatchDict(txtPath, outpath string) error {
	fp, err := os.Create(outpath)
	if err != nil {
		fmt.Printf("Open file failed [Err:%s]\n", err.Error())
		return err
	}
	defer fp.Close()
	w := gzip.NewWriter(bufio.NewWriter(fp))
	fIter := NewFilePairIter(txtPath, true, false)
	trie := CompileTrie(fIter.IterPairFile)
	trie.WriteToBytes(w)
	gob.NewEncoder(w).Encode(fIter.label)
	defer w.Close()
	return err
}

//LoadTrieDict load trie data
func LoadTrieDict(dataPath string) (*Trie, map[int]string) {
	fp, err := os.Open(dataPath)
	if err != nil {
		fmt.Printf("Open file failed [Err:%s]\n", err.Error())
		return nil, nil
	}
	defer fp.Close()
	w, err1 := gzip.NewReader(bufio.NewReader(fp))
	if err1 != nil {
		fmt.Printf("Open file failed [Err:%s]\n", err1.Error())
		return nil, nil
	}
	defer w.Close()
	trie := new(Trie)
	trie.ReadFromBytes(w)
	var label interface{}
	err2 := gob.NewDecoder(w).Decode(label)
	if err2 != nil {
		fmt.Printf("Open file failed [Err:%s]\n", err2.Error())
		return nil, nil
	}
	rlabel := make(map[int]string)
	for k, v := range label.(map[string]int) {
		rlabel[v] = k
	}
	return trie, rlabel
}

//Split split the string and tag it
func Split(content *string, maxMode bool, tagUse bool) []Pair {
	atomlist := BasiSplitStr(content, false)
	wlist := segment.SmartCut(atomlist, maxMode)
	if wlist != nil {
		result := make([]Pair, len(wlist))
		for i, cell := range wlist {
			result[i] = Pair{K: cell.Word.Image, V: cell.GetTypes()}
		}
		return result
	}
	return nil
}
