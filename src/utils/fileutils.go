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
	"errors"
	"io"
	"os"
	"os/exec"
	"path"
	"path/filepath"
	"strings"
)

//GetExePath  get exe file path
func GetExePath() (string, error) {
	file, err := exec.LookPath(os.Args[0])
	if err != nil {
		return "", err
	}
	path, err := filepath.Abs(file)
	if err != nil {
		return "", err
	}
	i := strings.LastIndex(path, "/")
	if i < 0 {
		i = strings.LastIndex(path, "\\")
	}
	if i < 0 {
		return "", errors.New("error: Can't find '/' or '.'")
	}
	return string(path[0 : i+1]), nil
}

//GetResource get the resource path
func GetResource(source string) string {
	if _, err := os.Stat(source); !os.IsNotExist(err) {
		return source
	} //exist in current pwd path
	spath, err := GetExePath()
	if err == nil {
		spath = path.Join(spath, source)
		if _, err = os.Stat(spath); !os.IsNotExist(err) {
			return spath
		} //exist in exe path
	}
	spath = path.Join(os.Getenv("SOURCE_PATH"), source)
	if _, err = os.Stat(spath); !os.IsNotExist(err) {
		return spath
	} //exist in exe path
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
