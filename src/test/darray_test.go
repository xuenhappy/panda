package test

import (
	"bytes"
	"panda/darts"
	"testing"
)

type CharIter struct {
	seq *string
}

func chrs(v *string) *CharIter {
	out := new(CharIter)
	out.seq = v
	return out
}

func (seq *CharIter) IterStr(do func(*string, int) bool) {
	for idx, chr := range []rune(*seq.seq) {
		data := string(chr)
		if do(&data, idx) {
			break
		}
	}
}

type StrPairList struct {
	datas []darts.Pair
}

func (list *StrPairList) IterPair(do func(darts.StringIter, int) bool) {
	for _, pair := range list.datas {
		strlist, label := pair.K.(darts.StringIter), pair.V.(int)
		if do(strlist, label) {
			break
		}
	}
}

func (list *StrPairList) addData(v *string, l int) {
	list.datas = append(list.datas, darts.Pair{K: chrs(v), V: l})
}

func TestParseText(t *testing.T) {
	s1, s2, s3 := "abcd", "abc", "bcd"
	t.Log("-------------------ori-----------------")
	t.Log(s1)
	t.Log(s2)
	t.Log(s3)
	data := StrPairList{}
	data.addData(&s1, 0)
	data.addData(&s2, 1)
	data.addData(&s3, 2)
	trie := darts.Compile(&data)
	findstr := "dcfabcdef"
	finditer := chrs(&findstr)
	t.Log("-------------------find----------------")
	t.Log(findstr)
	buf := new(bytes.Buffer)
	err := trie.WriteToBytes(buf)
	if err != nil {
		t.Log(err)
	}
	ss := new(darts.Trie)
	err = ss.ReadFromBytes(buf)
	if err != nil {
		t.Log(err)
	}
	ss.ParseText(finditer, func(s, e, l int) bool {
		t.Log("find ", s, e, l, findstr[s:e])
		return false
	})

}
