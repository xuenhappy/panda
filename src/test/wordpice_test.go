package test

import (
	"panda/darts"
	"testing"
)

func TestWordPices(t *testing.T) {
	ori := "helloword"
	s := darts.SubEngWord(ori)
	t.Log(ori)
	t.Log("---------------------")
	t.Log(s)
}

func TestBasicSplit(t *testing.T) {
	ori := "你好啊 helloword 测试"
	s := darts.BasiSplitStr(&ori, true)
	t.Log(ori)
	t.Log("---------------------")
	t.Log(s)
}
