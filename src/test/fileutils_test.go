package test

import (
	"panda/utils"
	"testing"
)

func TestGetPath(t *testing.T) {
	x, _ := utils.GetExePath()
	t.Log(x)
}
