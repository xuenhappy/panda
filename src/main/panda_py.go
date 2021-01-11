package main

//#include <stdlib.h>
import "C"

import (
	"bytes"
	"encoding/json"
	"fmt"
	"panda/darts"
	"unsafe"
)

//export freeCStr
func freeCStr(p *C.char) {
	C.free(unsafe.Pointer(p))
}

//export basicToken
func basicToken(str *C.char, pieceEng int) *C.char {
	data := C.GoString(str)
	atomlist := darts.BasiSplitStr(&data, pieceEng != 0)
	if len(atomlist) > 0 {
		buf := new(bytes.Buffer)
		err := json.NewEncoder(buf).Encode(atomlist)
		if err != nil {
			fmt.Println(err)
		}
		return C.CString(buf.String())
	}
	return nil
}

var charIndex map[string]int

//init charIndex
func init() {

}

//export buildFileTireDict
func buildFileTireDict(inputpath *C.char, outputpath *C.char) {
	err := darts.CompileTxtMatchDict(C.GoString(inputpath), C.GoString(outputpath))
	if err != nil {
		fmt.Println(err)
	}
}

//export develQSample
func develQSample(str *C.char) *C.char {
	data := C.GoString(str)
	alist := darts.BasiSplitStr(&data, false)
	sample := darts.ToDevelQSample(darts.NewAtomList(alist, &data), charIndex)
	if sample != nil {
		buf := new(bytes.Buffer)
		err := json.NewEncoder(buf).Encode(sample)
		if err != nil {
			fmt.Println(err)
		}
		return C.CString(buf.String())
	}
	return nil
}

func main1() {
	// go build -buildmode=c-shared -o ../devel/panda.so main/panda_py.go

}
