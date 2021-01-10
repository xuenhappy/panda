package main

import (
	"fmt"
	"os"
	"panda/darts"
)

func main1() {
	if len(os.Args) < 3 {
		fmt.Println("panda_devel input ouputs")
		return
	}
	darts.CompileTxtMatchDict(os.Args[1], os.Args[2])
}

func main() {
	trie, label, err := darts.LoadTrieDict(os.Args[1])
	if err != nil {
		fmt.Println(err.Error())
		return
	}
	str := "南京市长江大桥一代新人换旧人通车了"
	atoms := darts.NewAtomList(darts.BasiSplitStr(&str, false), &str)
	trie.ParseText(atoms.StrIterFuc(true, false), func(start, end, lab int) bool {
		atom := atoms.SubAtomList(start, end)
		l, _ := label[lab]
		fmt.Println(atom, l)
		return false
	})

}
