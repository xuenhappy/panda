package darts

import (
	"fmt"
	"panda/utils"
	"path"
)

type fReq struct {
	next map[string]int
	sum  int
}

type wTable struct {
	untokenMap map[string]int
	tokeMap    map[string]*fReq
}

func (t *wTable) loadUntoken(path string) {

}

func (t *wTable) loadToken(path string) {

}

func (t *wTable) Read(content []*Atom, cmap *CellMap) {

}

func (t *wTable) Embed(context []*Atom, cmap *CellMap) {
	//do nothing
}

func (t *wTable) Distance(pre *WCell, next *WCell) float32 {
	return 0.0
}

var table *wTable
var splitter *Segment

func init() {
	table = new(wTable)
	epath, _ := utils.GetExePath()
	fpath := path.Join(epath, "data/ueng.txt")
	table.loadUntoken(fpath)
	fPpath := path.Join(epath, "data/uepice.txt")
	table.loadToken(fPpath)
	splitter = NewSegment(table)
	splitter.AddCellRecognizer(table)
}

// class WordsFreq():
//     def __init__(self):
//         self.freq = 0
//         self.next = {}

//     def addnext(self, words, num):
//         self.freq += num
//         self.next[words] = self.next.get(words, 0)+num

// class Table(CellQuantizer, CellRecognizer):
//     def __init__(self, tablefile):
//         charsmap = {}
//         self.parfreq = 10000
//         with open(tablefile, encoding="utf-8") as fd:
//             for line in fd:
//                 line = line.strip()
//                 if not line:
//                     continue
//                 lines = line.split(" ")
//                 if len(lines) != 3:
//                     continue
//                 self.parfreq += int(lines[2])
//                 if lines[0] in charsmap:
//                     charsmap[lines[0]].addnext(lines[1], int(lines[2]))
//                 else:
//                     wf = WordsFreq()
//                     wf.addnext(lines[1], int(lines[2]))
//                     charsmap[lines[0]] = wf
//                 if lines[1] in charsmap:
//                     charsmap[lines[1]].freq += int(lines[2])
//                 else:
//                     wf = WordsFreq()
//                     wf.freq = int(lines[2])
//                     charsmap[lines[1]] = wf

//         self.charmap = charsmap
//         self.maxlen = max(len(w) for w in charsmap.keys())
//         self.sumf = max(v.freq for v in charsmap.values())*100+10
//         self.minq = min(v.freq for v in charsmap.values())

//     def distance(self, w1, w2):
//         w1 = w1.word.image
//         w2 = w2.word.image
//         freq_w1 = self.minq
//         freq_w1w2 = 0
//         freq_w2 = self.minq
//         if w1 in self.charmap:
//             o = self.charmap[w1]
//             freq_w1 = o.freq
//             if w2 in o.next:
//                 freq_w1w2 = o.next[w2]
//         if w2 in self.charmap:
//             freq_w2 = self.charmap[w2].freq

//         if freq_w1w2 == 0:
//             freq_w1w2 = min(freq_w1, freq_w2)/2
//         p = freq_w1w2/freq_w1*0.65+0.35*min(freq_w1, freq_w2)/self.sumf
//         return -math.log(p)

//     def embed(self, cmap, context):
//         pass

//     def read(self, content, cmap):
//         cur = cmap.head
//         content = "".join(w.image for w in content)
//         lens = min(self.maxlen, len(content))
//         for i in range(len(content)-1):
//             for j in range(2, lens+1):
//                 if i+j > len(content):
//                     break
//                 w = content[i:i+j]
//                 if w in self.charmap:
//                     cur = cmap.addCell(WCell(Atom(w, (i, i+j)), (i, i+j)), cur)

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
		atoms = append(atoms, NewAtom(&w, i, i+1))
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
