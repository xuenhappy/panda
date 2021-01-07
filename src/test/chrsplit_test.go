package test

import (
	"panda/darts"
	"testing"
)

func TestCharSplit(t *testing.T) {
	strs := "你好 hello word!"
	t.Log("lens", darts.StrWLen(&strs))

	darts.CharSplit(&strs, func(img, stype string, s, e int) {
		t.Log(img, stype, s, e)
	})
}
