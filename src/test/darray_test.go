package test

import (
	"panda/darts"
	"testing"
)

type CharIter struct {
	seq   string
	index int
}

func chrs(v *string) *CharIter {
	out := new(CharIter)
	out.index = -1
	out.seq = *v
	return out
}

func (seq *CharIter) Next() bool {
	seq.index++
	if seq.index < len(seq.seq) {
		return true
	}
	return false
}

func (seq *CharIter) Word() *string {
	out := seq.seq[seq.index : seq.index+1]
	return &out
}

func (seq *CharIter) Postion() int {
	return seq.index
}

type StrPair struct {
	list  *CharIter
	label int
}

type StrPairList struct {
	datas []*StrPair
	idx   int
}

func (list *StrPairList) Next() bool {
	list.idx++
	return list.idx < len(list.datas)
}

func (list *StrPairList) String() darts.StringIter {
	return list.datas[list.idx].list
}

//Postion postion
func (list *StrPairList) Label() int {
	return list.datas[list.idx].label
}

func (list *StrPairList) addData(v *string, l int) {
	str := chrs(v)
	list.datas = append(list.datas, &StrPair{str, l})
}

func TestParseText(t *testing.T) {
	s1, s2, s3 := "abcd", "abc", "bcd"
	t.Log("-------------------ori-----------------")
	t.Log(s1)
	t.Log(s2)
	t.Log(s3)
	data := StrPairList{}
	data.idx = -1
	data.addData(&s1, 0)
	data.addData(&s2, 1)
	data.addData(&s3, 2)
	trie := darts.Compile(&data)
	findstr := "dcfabcdef"
	finditer := chrs(&findstr)

	d, _ := trie.WriteToBytes()
	ss := darts.ReadFromBytes(d)

	ss.ParseText(finditer, func(s, e, l int) {
		t.Log("find ", s, e, l, findstr[s:e])
	})

}
