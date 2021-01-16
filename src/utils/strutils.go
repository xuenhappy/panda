package utils

import (
	"encoding/json"
	"fmt"
	"os"
	"strings"
)

/*
 * File: strutils.go
 * Project: utils
 * this is a file use for string
 * File Created: Thursday, 31st December 2020 11:04:31 pm
 * Author: enxu (xuen@mokar.com)
 * -----
 * Last Modified: Thursday, 31st December 2020 11:04:40 pm
 * Modified By: enxu (xuen@mokahr.com)
 * -----
 * Copyright 2021 - 2020 Your Company, Moka
 */

var wordmap = make(map[string]string)

//Chr is int to string
func Chr(s int) string {
	return string(rune(s))
}
func loadEmpty(s, e int) {
	for x := s; x < e; x++ {
		wordmap[Chr(x)] = " "
	}
}

func loadBasic() {
	loadEmpty(0, 0x20+1)
	wordmap[Chr(0x7F)] = " "
	loadEmpty(8198, 8208)
	loadEmpty(8232, 8240)
	loadEmpty(8287, 8304)
	loadEmpty(0xFE00, 0xFE0F+1)
	for x := 65281; x <= 65374; x++ {
		wordmap[Chr(x)] = Chr(x - 65248)
	}
	wordmap[Chr(12288)] = Chr(32)
}

func loadSpecial() {
	wordmap["“"] = "\""
	wordmap["”"] = "\""
	wordmap["、"] = ","
	wordmap["〜"] = "~"
	wordmap["～"] = "~"
	wordmap["－"] = "-"
	wordmap["–"] = "-"
	wordmap["\r"] = ""
	wordmap["︳"] = "|"
	wordmap["▎"] = "|"
	wordmap["ⅰ"] = "i"
	wordmap["丨"] = "|"
	wordmap["│"] = "|"
	wordmap["︱"] = "|"
	wordmap["｜"] = "|"
	wordmap["／"] = "/"
	wordmap[Chr(173)] = "-"
	wordmap[Chr(8208)] = "-"
	wordmap[Chr(8209)] = "-"
	wordmap[Chr(8210)] = "-"
	wordmap[Chr(8211)] = "-"
	wordmap[Chr(8212)] = "-"
	wordmap[Chr(8213)] = "-"
	wordmap["【"] = "["
	wordmap["】"] = "]"
	wordmap["●"] = "·"
	wordmap["•"] = "·"
	wordmap["~"] = "-"
}

func loadUnVisual() {
	data := [...]int{0xf0b7, 0xf0b2, 0xf064, 0xf0e0, 0xf06c, 0xf034, 0xe6a5, 0xe6a3,
		0xe6a0, 0xE77C, 0xE76E, 0xf077, 0xe710, 0xe711, 0xe712, 0xe713, 0xe723, 0xe793, 0xf06c,
		0xf0d8, 0xf020, 0xFEFF, 0xF0FC, 0xF0FC, 0xE755, 0xE6D2, 0xE63C, 0xE734, 0xF074, 0xE622,
		0xF241, 0xE71B, 0xF148, 0xE973, 0xE96E, 0xE96A, 0xE97D, 0xE805, 0xE70D, 0xF258, 0xE7BB,
		0xE806, 0xE930, 0xE739, 0xF0A4, 0xE6A4, 0xE69E, 0xF06E, 0xF075, 0xF0B7, 0x009F, 0xF0B7,
		0xF076, 0xF09F, 0xF0A8, 0xE69F, 0xF097, 0xF0A1}
	for _, v := range data {
		wordmap[Chr(v)] = ""
	}
}

func loadFileMap() {
	filePtr, err := os.Open(GetResource("data/confusables.json"))
	if err != nil {
		panic(fmt.Errorf("Open file failed [Err:%s]", err.Error()))
	}
	defer filePtr.Close()
	var data interface{}
	err = json.NewDecoder(filePtr).Decode(&data)
	if err != nil {
		panic(fmt.Errorf("Decoder file failed [Err:%s]", err.Error()))
	}
	m := data.(map[string]interface{})
	for k, v := range m {
		for _, ch := range v.([]interface{}) {
			wordmap[ch.(string)] = k
		}
	}
}

func checkMap() {
	for k, v := range wordmap {
		if k == v {
			delete(wordmap, k)
		}
		s, ok := wordmap[v]
		if ok {
			wordmap[k] = s
		}
	}
	delete(wordmap, "\n")
	wordmap["\t"] = "    "

}

func init() {
	loadBasic()
	loadSpecial()
	loadUnVisual()
	loadFileMap()
	checkMap()
}

//MapWord is used for replace the unicode
func MapWord(input *string) string {
	r := []rune(*input)
	var builder strings.Builder
	for _, x := range r {
		news, ok := wordmap[string(x)]
		if ok {
			builder.WriteString(news)
			continue
		}
		builder.WriteRune(x)
	}
	return builder.String()
}
