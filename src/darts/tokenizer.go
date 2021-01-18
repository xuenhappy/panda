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
	return fIter
}

//IterPairFile iter files
func (fIter *FilePairIter) IterPairFile(dfunc func(StringIter, []int) bool) {
	lineNum := 0
	labels := make(map[string]int)
	splitRe, _ := regexp.Compile(`\s*:\s*`)
	TagRe, _ := regexp.Compile(`\s*,\s*`)
	err := utils.ReadLine(fIter.filepath, func(line string) bool {
		lineNum++
		line = strings.TrimSpace(line)
		if len(line) < 1 { //empty str
			return false
		}
		lines := splitRe.Split(line, 2)
		if len(lines) != 2 { //bad line
			fmt.Printf("bad line[%d] support [%s]\n", lineNum, line)
			return false
		}
		atom := BasiSplitStr(&lines[1], false)
		if len(atom) < 2 { //single word not used
			return false
		}
		iter := NewAtomList(atom, &lines[1])
		strTags := TagRe.Split(lines[0], -1)
		itags := make([]int, len(strTags))
		for i, tag := range strTags {
			label, ok := labels[tag]
			if !ok {
				label = len(labels) + 1
				labels[tag] = label
			}
			itags[i] = label
		}
		dfunc(iter.StrIterFuc(true, false), itags)
		return false
	})
	fIter.label = labels
	if err != nil {
		panic(fmt.Errorf("Open file failed [Err:%s]", err.Error()))
	}
}

//CompileTxtMatchDict compile a txt to trie
func CompileTxtMatchDict(txtPath, outpath string) error {
	fp, err := os.Create(outpath)
	if err != nil {
		return err
	}
	defer fp.Close()
	buf := bufio.NewWriter(fp)
	defer buf.Flush()
	w := gzip.NewWriter(buf)
	defer w.Close()
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
func (dict *DictCellRecognizer) Read(content *AtomList, cmap *CellMap) {
	cur := cmap.Head
	dict.trie.ParseText(content.StrIterFuc(true, false), func(start, end int, labels []int) bool {
		cell := NewWcell(content.SubAtomList(start, end), start, end)
		for _, label := range labels {
			l, ok := dict.label[label]
			if ok {
				cell.Word.AddType(l)
			}
		}
		cur = cmap.AddCell(cell, cur)
		return false
	})
}

var segment *Segment

//init the segment
func init() {
	segment = NewSegment(nil)
	dict, err := NewDictCellRecognizer(utils.GetResource("data/panda.pd"))
	if err != nil {
		fmt.Printf("Open file failed [Err:%s]\n", err.Error())
		return
	}
	segment.AddCellRecognizer(dict)
}

//Split split the string and tag it
func Split(content *string, maxMode bool, tagUse bool) []Pair {
	atomlist := NewAtomList(BasiSplitStr(content, false), content)
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

//ToDevelQSample build a devel sample to network use
func ToDevelQSample(atomList *AtomList) []*Atom {
	if len(atomList.List) < 2 { //too short
		return nil
	}
	cmap := segment.buildMap(atomList)
	data := make([]*Atom, 0, cmap.size)
	cmap.IterRow(nil, -1, func(c *Cursor) {
		data = append(data, c.val.Word)
	})
	return data
}
