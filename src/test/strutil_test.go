package test

import (
	"panda/utils"
	"testing"
)

func TestWordmap(t *testing.T) {
	t.Log("---------------------")
	oristr := "你好１２３４５６ＡＢＣＤＥss··㊁·"
	t.Log(oristr)
	t.Log(utils.MapWord(&oristr))
}
