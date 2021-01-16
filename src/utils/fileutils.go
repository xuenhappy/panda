/*
 * File: fileutils.go
 * Project: utils
 * File Created: Thursday, 31st December 2020 10:57:11 pm
 * Author: enxu (xuen@mokar.com)
 * -----
 * Last Modified: Thursday, 31st December 2020 10:57:16 pm
 * Modified By: enxu (xuen@mokahr.com)
 * -----
 * Copyright 2021 - 2020 Your Company, Moka
 */
package utils

import (
	"bufio"
	"io"
	"os"
	"path"
	"path/filepath"
)

//GetExeDir  get exe file path
func GetExeDir() string {
	var dirAbsPath string
	ex, err := os.Executable()
	if err == nil {
		dirAbsPath = filepath.Dir(ex)
		return dirAbsPath
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
	spath := path.Join(GetExeDir(), source)
	if _, err := os.Stat(spath); !os.IsNotExist(err) {
		return spath
	} //exist in exe path

	spath = path.Join(os.Getenv("SOURCE_PATH"), source)
	if _, err := os.Stat(spath); !os.IsNotExist(err) {
		return spath
	} //exist in env path
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
