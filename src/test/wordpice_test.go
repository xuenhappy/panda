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
