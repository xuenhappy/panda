package darts

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
	//TODO(xuen): a pice eng
	return result
}

var segment Segment

//init the segment
func init() {

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
