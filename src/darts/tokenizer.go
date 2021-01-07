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
