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

//export buildFileTireDict
func buildFileTireDict(inputpath *C.char, outputpath *C.char) {
	err := darts.CompileTxtMatchDict(C.GoString(inputpath), C.GoString(outputpath))
	if err != nil {
		fmt.Println(err)
	}
}

//QSample is so use
type QSample struct {
	Tokens []*darts.Atom
	Cells  []*darts.Atom
}

//export develQSample
func develQSample(str *C.char) *C.char {
	var sample QSample
	data := C.GoString(str)
	alist := darts.BasiSplitStr(&data, false)
	sample.Tokens = darts.BasiSplitStr(&data, true)
	sample.Cells = darts.ToDevelQSample(darts.NewAtomList(alist, &data))

	buf := new(bytes.Buffer)
	err := json.NewEncoder(buf).Encode(sample)
	if err != nil {
		fmt.Println(err)
	}
	return C.CString(buf.String())
}

func main() {
	// go build -buildmode=c-shared -o ../devel/panda.so main/panda_py.go

}
