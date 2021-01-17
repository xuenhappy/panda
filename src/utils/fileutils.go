package utils

/*
#cgo linux LDFLAGS: -ldl
#include <dlfcn.h>
#include <stdlib.h>

const char *dllPath(void) {
	Dl_info dl_info;
	int rc=dladdr((void*)dllPath, &dl_info);
	if (!rc){
		return "";
	}
    return(dl_info.dli_fname);
}
*/
import "C"

import (
	"bufio"
	"io"
	"os"
	"path"
	"path/filepath"
)

//GetDllDir get a dll path
func GetDllDir() string {
	dllpath := C.GoString(C.dllPath())
	return filepath.Dir(dllpath)
}

//GetExeDir  get exe file path
func GetExeDir() string {
	ex, err := os.Executable()
	if err == nil {
		return filepath.Dir(ex)
	}
	exReal, err := filepath.EvalSymlinks(ex)
	if err != nil {
		panic(err)
	}
	return filepath.Dir(exReal)
}

//GetResource get the resource path
func GetResource(source string) string {
	if _, err := os.Stat(source); !os.IsNotExist(err) {
		return source
	} //exist in current pwd path

	spath := path.Join(os.Getenv("SOURCE_PATH"), source)
	if _, err := os.Stat(spath); !os.IsNotExist(err) {
		return spath
	} //exist in env path
	exedir := GetExeDir()
	if len(exedir) > 0 {
		spath = path.Join(exedir, source)
		if _, err := os.Stat(spath); !os.IsNotExist(err) {
			return spath
		} //exist in exe path
		spath = path.Join(exedir, "../", source)
		if _, err := os.Stat(spath); !os.IsNotExist(err) {
			return spath
		} //exist in exe father path
	}
	dlldir := GetDllDir()
	if len(dlldir) > 0 {
		spath = path.Join(dlldir, source)
		if _, err := os.Stat(spath); !os.IsNotExist(err) {
			return spath
		} //exist in dll path

		spath = path.Join(dlldir, "../", source)
		if _, err := os.Stat(spath); !os.IsNotExist(err) {
			return spath
		} //exist in dll father path
	}
	return source
}

//ReadLine read a text file
func ReadLine(fileName string, handler func(string) bool) error {
	f, err := os.Open(fileName)
	if err != nil {
		return err
	}
	defer f.Close()
	buf := bufio.NewReader(f)
	for {
		line, err := buf.ReadString('\n')
		if handler(line) {
			return nil
		}
		if err != nil {
			if err == io.EOF {
				return nil
			}
			return err
		}
	}
}
