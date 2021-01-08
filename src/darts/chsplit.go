/*
 * File: chsplit.go
 * Project: darts
 * File Created: Wednesday, 6th January 2021 12:29:32 pm
 * Author: enxu (xuen@mokar.com)
 * -----
 * Last Modified: Wednesday, 6th January 2021 12:29:57 pm
 * Modified By: enxu (xuen@mokahr.com)
 * -----
 * Copyright 2021 - 2021 Your Company, Moka
 */

package darts

import (
	"encoding/json"
	"fmt"
	"os"
	"panda/utils"
	"path"
	"strings"
)

//basic use
var charTypeMap map[string]string = map[string]string{}

const emptyFlag = "<EMPTY>"

func loadTableFile() {
	filepath, _ := utils.GetExePath()
	//filepath= "/home/enxu/Documents/workspace/panda/"
	filepath = path.Join(filepath, "data/wordtype.json")
	filePtr, err := os.Open(filepath)
	if err != nil {
		fmt.Printf("Open file failed [Err:%s]\n", err.Error())
		return
	}
	defer filePtr.Close()
	var data interface{}
	err = json.NewDecoder(filePtr).Decode(&data)
	if err != nil {
		fmt.Println("Decoder failed", err.Error())
		return
	}
	m := data.(map[string]interface{})
	for k, v := range m {
		for _, ch := range v.([]interface{}) {
			charTypeMap[ch.(string)] = k
		}
	}
}

func loadEmpty(s, e int) {
	for x := s; x < e; x++ {
		charTypeMap[utils.Chr(x)] = emptyFlag
	}
}

func loadBasic() {
	loadEmpty(0, 0x20+1)
	charTypeMap["\t"] = emptyFlag
	charTypeMap["\n"] = emptyFlag
	charTypeMap["\r"] = emptyFlag
	charTypeMap[" "] = emptyFlag
	charTypeMap[utils.Chr(0x7F)] = emptyFlag
	loadEmpty(8198, 8208)
	loadEmpty(8232, 8240)
	loadEmpty(8287, 8304)
	loadEmpty(0xFE00, 0xFE0F+1)
	charTypeMap[utils.Chr(12288)] = emptyFlag
}

//init the charTypeMap
func init() {
	loadTableFile()
	loadBasic()
}

//CharType given a char string type
func CharType(chr rune) string {
	types, ok := charTypeMap[string(chr)]
	if ok {
		return types
	}
	//asicii
	if chr < 255 {
		if 'A' <= chr && chr <= 'Z' {
			return "<ENG>"
		}
		if 'a' <= chr && chr <= 'z' {
			return "<ENG>"
		}
		if '0' <= chr && chr <= '9' {
			return "<NUM>"
		}
		return "<POS>"
	}

	if '０' <= chr && chr <= '９' {
		return "<NUM>"
	}
	if 'Ａ' <= chr && chr <= 'Ｚ' {
		return "<ENG>"
	}
	if 0x4e00 <= chr && chr <= 0x9fa5 {
		return "<CJK>"
	}
	if 0x3130 <= chr && chr <= 0x318F {
		return "<CJK>"
	}
	if 0xAC00 <= chr && chr <= 0xD7A3 {
		return "<CJK>"
	}
	if 0x0800 <= chr && chr <= 0x4e00 {
		return "<CJK>"
	}
	if 0x3400 <= chr && chr <= 0x4DB5 {
		return "<CJK>"
	}
	return "<UNK>"
}

//CharSplit split the strs use words
func CharSplit(strs *string, accept func(string, string, int, int)) {
	var charBuffer strings.Builder
	var bufType string
	uStrs := []rune(*strs)
	bufstart := 0
	for idx, ch := range uStrs {
		ctype := CharType(ch)
		if strings.Compare(ctype, bufType) != 0 {
			//blow is special
			if ctype == "<EMPTY>" && bufType == "<POS>" {
				bufType = "<POS>"
				charBuffer.WriteRune(ch)
				continue
			}
			if bufType == "<EMPTY>" && ctype == "<POS>" {
				bufType = "<POS>"
				charBuffer.WriteRune(ch)
				continue
			}
			//start a new
			if charBuffer.Len() > 0 {
				accept(charBuffer.String(), bufType, bufstart, idx)
			}
			bufstart = idx
			charBuffer.Reset()
		}
		bufType = ctype
		if strings.Compare(bufType, "<CJK>") == 0 {
			accept(string(ch), bufType, idx, idx+1)
			charBuffer.Reset()
			continue
		}
		charBuffer.WriteRune(ch)
	}
	if charBuffer.Len() > 0 {
		accept(charBuffer.String(), bufType, bufstart, len(uStrs))
	}
}

//StrWLen restun the string word Len
func StrWLen(strs *string) int {
	nums, bufType := 0, ""
	for _, ch := range []rune(*strs) {
		ctype := CharType(ch)
		if ctype != bufType {
			if bufType == "<EMPTY>" && ctype == "<POS>" {
				bufType = "<POS>"
				continue
			}
			if ctype == "<EMPTY>" && bufType == "<POS>" {
				bufType = "<POS>"
				continue
			}
			if bufType != "" {
				nums++
				bufType = ""
			}
		}
		bufType = ctype
		if bufType == "<CJK>" {
			nums++
			bufType = ""
			continue
		}
	}
	if bufType != "" {
		nums++
	}
	return nums
}

func chrUTF8len(codepoint rune) int {
	if codepoint <= 0x7f {
		return 1
	}
	if codepoint <= 0x7ff {
		return 2
	}
	if codepoint <= 0xffff {
		return 3
	}
	if codepoint <= 0x10ffff {
		return 4
	}
	return 0
}

//UTF8Len claulate the string utf8 len without create a bytes
func UTF8Len(s *string) int {
	lens := 0
	for _, chr := range []rune(*s) {
		lens += chrUTF8len(chr)
	}
	return lens
}
