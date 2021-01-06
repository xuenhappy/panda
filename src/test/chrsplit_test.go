package test

import (
	"panda/darts"
	"testing"
)

func TestCharSplit(t *testing.T) {
	strs := "你好 hello word!"
	t.Log("lens", darts.StrWLen(&strs))

	splt := darts.CharSplit(&strs)
	t.Log(splt)

}
