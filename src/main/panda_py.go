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

func main() {
	// go build -buildmode=c-shared -o ../devel/panda.so main/panda_py.go

}
