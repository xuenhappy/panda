package darts

import (
	"bufio"
	"compress/gzip"
	"encoding/gob"
	"fmt"
	"github.com/deckarep/golang-set"
	"os"
	"panda/utils"
	"path"
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

//AtomStrIter iter atom
type AtomStrIter struct {
	atoms     []*Atom
	skipEmpty bool
	skipPos   bool
}

//SubAtomList Sub a atonlist
func SubAtomList(atoms []*Atom, start, end int) *Atom {
	var buf strings.Builder
	tags := mapset.NewSet()
	for i := start; i < end; i++ {
		buf.WriteString(atoms[i].Image)
		if atoms[i].Tags != nil {
			tags = tags.Intersect(atoms[i].Tags)
		}
	}
	if buf.Len() > 0 {
		str := buf.String()
		atom := NewAtom(&str, atoms[start].St, atoms[end-1].End)
		if tags.Cardinality() > 0 {
			atom.Tags = tags
		}
		return atom
	}
	return nil
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
		if len(line) < 1 {
			return false
		}
		lines := splitRe.Split(line, 2)
		if len(lines) != 2 {
			fmt.Println("bad line support", line)
			return false
		}
		atom := BasiSplitStr(&lines[1], false)
		if len(atom) < 2 {
			return false
		}
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
		return err
	}
	buf := bufio.NewWriter(fp)
	w := gzip.NewWriter(buf)
	defer func() {
		w.Close()
		buf.Flush()
		fp.Close()
	}()
	fIter := NewFilePairIter(txtPath, true, false)
	trie := CompileTrie(fIter.IterPairFile)
	err = trie.WriteToBytes(w)
	if err != nil {
		return err
	}
	return gob.NewEncoder(w).Encode(fIter.label)
}

//LoadTrieDict load trie data
func LoadTrieDict(dataPath string) (*Trie, map[int]string, error) {
	fp, err := os.Open(dataPath)
	if err != nil {
		return nil, nil, err
	}
	defer fp.Close()
	w, err1 := gzip.NewReader(bufio.NewReader(fp))
	if err1 != nil {
		return nil, nil, err1
	}
	defer w.Close()
	trie := new(Trie)
	err3 := trie.ReadFromBytes(w)
	if err3 != nil {
		return nil, nil, err3
	}
	var label map[string]int
	err2 := gob.NewDecoder(w).Decode(&label)
	if err2 != nil {
		return nil, nil, err2
	}
	rlabel := make(map[int]string)
	for k, v := range label {
		rlabel[v] = k
	}
	return trie, rlabel, nil
}

//DictCellRecognizer is a dict regconize
type DictCellRecognizer struct {
	label map[int]string
	trie  *Trie
}

//NewDictCellRecognizer create a dict
func NewDictCellRecognizer(fpath string) (*DictCellRecognizer, error) {
	dict := new(DictCellRecognizer)
	var err error = nil
	dict.trie, dict.label, err = LoadTrieDict(fpath)
	if err != nil {
		return nil, err
	}
	return dict, nil
}

//Read is read a cell
func (dict *DictCellRecognizer) Read(content []*Atom, cmap *CellMap) {
	cur := cmap.Head
	dict.trie.ParseText(NewAtomStrIter(content, true, false).IterStr, func(start, end, label int) bool {
		cell := NewWcell(SubAtomList(content, start, end), start, end)
		l, ok := dict.label[label]
		if ok {
			cell.Word.AddType(l)
		}
		cur = cmap.AddCell(cell, cur)
		return false
	})
}

var segment *Segment

//init the segment
func init() {
	segment = NewSegment(nil)
	filepath, _ := utils.GetExePath()
	//filepath= "/home/enxu/Documents/workspace/panda/"
	filepath = path.Join(filepath, "data/panda.dict")
	dict, err := NewDictCellRecognizer(filepath)
	if err != nil {
		fmt.Printf("Open file failed [Err:%s]\n", err.Error())
		return
	}
	segment.AddCellRecognizer(dict)
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
