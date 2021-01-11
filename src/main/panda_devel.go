package main

import (
	"fmt"
	"github.com/clagraff/argparse"
	"os"
	"panda/darts"
	"strings"
)

func splitStr(path, tokens string) {
	trie, label, err := darts.LoadTrieDict(path)
	if err != nil {
		fmt.Println(err.Error())
		return
	}
	atoms := darts.NewAtomList(darts.BasiSplitStr(&tokens, false), &tokens)
	trie.ParseText(atoms.StrIterFuc(true, false), func(start, end int, labels []int) bool {
		atom := atoms.SubAtomList(start, end)
		var tagsBuilder strings.Builder
		for _, lab := range labels {
			l, _ := label[lab]
			tagsBuilder.WriteString(l)
		}
		fmt.Println(atom, tagsBuilder.String())
		return false
	})

}

func main1() {
	//create a complie tire dict action
	createCompile := argparse.NewParser("compile a file dict to dict", func(parser *argparse.Parser, ns *argparse.Namespace, overArgs []string, err error) {
		if err != nil {
			fmt.Println(err)
			return
		}
		args := ns.Get("io").([]string)
		err = darts.CompileTxtMatchDict(args[0], args[1])
		if err != nil {
			fmt.Println(err)
		}
	})
	createCompile.AddOption(argparse.NewArg("io", "io", "[input dict file] [outfile]").Nargs(2).Required())
	createCompile.AddHelp()

	split := argparse.NewParser("split a string", func(parser *argparse.Parser, ns *argparse.Namespace, overArgs []string, err error) {
		if err != nil {
			fmt.Println(err)
			return
		}
		args := ns.Get("data").([]string)
		splitStr(args[0], args[1])
	})

	split.AddOption(argparse.NewArg("inputs", "data", "[dict path] [string]").Nargs(2).Required())
	split.AddHelp()

	p := argparse.NewParser("panda devel tools", func(parser *argparse.Parser, ns *argparse.Namespace, overArgs []string, err error) {
		if err != nil {
			switch err.(type) {
			case argparse.ShowHelpErr, argparse.ShowVersionErr:
				return
			default:
				fmt.Println(err)
				parser.ShowHelp()
			}
			return
		}
	})
	p.AddParser("compile", createCompile)
	p.AddParser("split", split)
	p.AddHelp().AddVersion()
	p.Parse(os.Args[1:]...)
}
