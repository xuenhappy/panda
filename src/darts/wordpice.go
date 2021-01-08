package darts

import (
	"fmt"
	"math"
	"panda/utils"
	"path"
	"regexp"
	"strconv"
	"strings"
)

type fReq struct {
	next map[string]int
	sum  int
}

type wTable struct {
	untokenMap map[string]int
	tokeMap    map[string]*fReq
	maxCodeLen int
	minFreq    int
	sumFreq    int
}

func (t *wTable) loadUntoken(path string) {
	spaceRe, _ := regexp.Compile(`\s+`)
	err := utils.ReadLine(path, func(line string) bool {
		lines := spaceRe.Split(line, 2)
		if len(lines) == 2 {
			num, _ := strconv.Atoi(lines[1])
			t.untokenMap[lines[0]] = num
		}
		return false
	})
	if err != nil {
		fmt.Printf("Open file failed [Err:%s]\n", err.Error())
	}
}

func (t *wTable) loadToken(path string) {
	spaceRe, _ := regexp.Compile(`\s+`)
	err := utils.ReadLine(path, func(line string) bool {
		lines := spaceRe.Split(line, 3)
		if len(lines) == 3 {
			num, _ := strconv.Atoi(lines[2])
			//add token pre num
			frq, ok := t.tokeMap[lines[0]]
			if !ok {
				frq = new(fReq)
				frq.next = make(map[string]int)
				t.tokeMap[lines[0]] = frq
			}
			freq1, _ := frq.next[lines[1]]
			frq.next[lines[1]] = num + freq1
			frq.sum += num
			//add token next num
			frq, ok = t.tokeMap[lines[1]]
			if !ok {
				frq = new(fReq)
				frq.next = make(map[string]int)
				t.tokeMap[lines[1]] = frq
			}
			frq.sum += num

		}
		return false
	})
	if err != nil {
		fmt.Printf("Open file failed [Err:%s]\n", err.Error())
		return
	}
	//static
	for k, v := range t.tokeMap {
		if len(k) > t.maxCodeLen {
			t.maxCodeLen = len(k)
		}
		if v.sum < t.minFreq {
			t.minFreq = v.sum
		}
		if v.sum > t.sumFreq {
			t.sumFreq = v.sum
		}
		t.sumFreq = t.sumFreq + len(t.tokeMap) + 1
	}
}

func (t *wTable) Read(content []*Atom, cmap *CellMap) {
	cur := cmap.Head
	lens := t.maxCodeLen
	if lens > len(content) {
		lens = len(content)
	}
	var builder strings.Builder
	for i := 0; i < len(content)-1; i++ {
		builder.Reset()
		builder.WriteString(content[i].Image)
		for j := 2; j <= lens; j++ {
			if i+j > len(content) {
				break
			}
			builder.WriteString(content[i+j-1].Image)
			img := builder.String()
			_, ok := t.tokeMap[img]
			if ok {
				atom := NewAtom(&img, content[i].St, content[i+j-1].End)
				cur = cmap.AddNext(cur, NewWcell(atom, i, i+j))
			}
		}
	}

}

func (t *wTable) Embed(context []*Atom, cmap *CellMap) {
	//do nothing
}

func (t *wTable) Distance(pre *WCell, next *WCell) float32 {
	w1, w2 := pre.Word.Image, next.Word.Image
	w1Freq, w2Freq, w1w2Freq := t.minFreq, t.minFreq, 0
	nextT, ok := t.tokeMap[w1]
	if ok {
		w1Freq = nextT.sum
		freq, exist := nextT.next[w2]
		if exist {
			w1w2Freq = freq
		}
	}
	nextT, ok = t.tokeMap[w2]
	if ok {
		w2Freq = nextT.sum
	}
	if w1w2Freq == 0 {
		w1w2Freq = w1Freq
		if w2Freq < w1w2Freq {
			w1w2Freq = w2Freq
		}
		w1w2Freq /= 2
	}
	p := 0.65 * (float64(w1w2Freq) / float64(w1Freq))
	p += 0.35 * (float64(w1Freq) / float64(t.sumFreq))
	return -float32(math.Log(p))
}

var table *wTable
var splitter *Segment

func init() {
	table = new(wTable)
	table.tokeMap = make(map[string]*fReq)
	table.untokenMap = make(map[string]int)
	table.minFreq = math.MaxInt32
	epath, _ := utils.GetExePath()
	//epath = "/Users/xuen/Documents/workspace/panda"
	fpath := path.Join(epath, "data/ueng.txt")
	table.loadUntoken(fpath)
	fPpath := path.Join(epath, "data/uepice.txt")
	table.loadToken(fPpath)
	splitter = NewSegment(table)
	splitter.AddCellRecognizer(table)
}

//SubEngWord split the english words
func SubEngWord(engw string) []string {
	_, exists := table.untokenMap[engw]
	if exists {
		return []string{engw}
	}

	words := fmt.Sprintf("%s$", engw)
	atoms := make([]*Atom, len(words))
	for i, word := range words {
		w := string(word)
		atoms[i] = NewAtom(&w, i, i+1)
	}
	wcellList := splitter.SmartCut(atoms, false)
	result := make([]string, len(wcellList))
	for i, cell := range wcellList {
		if i < len(wcellList)-1 {
			result[i] = fmt.Sprintf("%s@", cell.Word.Image)
		} else {
			result[i] = cell.Word.Image
		}
	}
	return result
}
