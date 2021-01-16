package darts

import (
	"fmt"
	"strings"

	"github.com/deckarep/golang-set"
	"gonum.org/v1/gonum/mat"
)

// Atom is  the small cell in split
type Atom struct {
	Image   string     //image of the atom
	St, End int        // [start,end) of this atom in ori string
	Tags    mapset.Set // type is set(string)
}

//String is add a type to atom
func (atom *Atom) String() string {
	return fmt.Sprintf("%s[%d,%d)", atom.Image, atom.St, atom.End)
}

//NewAtom crate the atom
func NewAtom(str *string, start, end int) *Atom {
	atom := new(Atom)
	atom.Image = *str
	atom.St = start
	atom.End = end
	return atom
}

//AddType is add a type to atom
func (atom *Atom) AddType(t string) {
	if atom.Tags == nil {
		atom.Tags = mapset.NewSet()
	}
	atom.Tags.Add(t)
}

//AddTypes is add the types
func (atom *Atom) AddTypes(types mapset.Set) {
	if types == nil {
		return
	}
	if atom.Tags == nil {
		atom.Tags = mapset.NewSet()
	}
	atom.Tags = atom.Tags.Union(types)
}

//AtomList is aarray of atoms
type AtomList struct {
	Str  *string //ori image
	List []*Atom //lsit data
}

//SubAtomList Sub a atonlist
func (alist *AtomList) SubAtomList(start, end int) *Atom {
	var buf strings.Builder
	tags := mapset.NewSet()
	for i := start; i < end; i++ {
		buf.WriteString(alist.List[i].Image)
		if alist.List[i].Tags != nil {
			tags = tags.Intersect(alist.List[i].Tags)
		}
	}
	if buf.Len() > 0 {
		str := buf.String()
		atom := NewAtom(&str, alist.List[start].St, alist.List[end-1].End)
		if tags.Cardinality() > 0 {
			atom.Tags = tags
		}
		return atom
	}
	return nil
}

//NewAtomList make a atom List
func NewAtomList(atoms []*Atom, img *string) *AtomList {
	return &AtomList{Str: img, List: atoms}
}

//StrIterFuc iter the atom list
func (alist *AtomList) StrIterFuc(skipEmpty, skipPos bool) StringIter {
	if !(skipEmpty || skipPos) {
		return func(dfunc func(*string, int) bool) {
			for i, a := range alist.List {
				if dfunc(&a.Image, i) {
					break
				}
			}
		}
	}
	return func(dfunc func(*string, int) bool) {
		se, sp := skipEmpty, skipPos
		for i, a := range alist.List {
			if se && a.Tags != nil && a.Tags.Contains("<EMPTY>") {
				continue
			}
			if sp && a.Tags != nil && a.Tags.Contains("<POS>") {
				continue
			}
			if dfunc(&a.Image, i) {
				break
			}
		}
	}
}

//WCell is the split token parttern
type WCell struct {
	Word   *Atom      //the word in ori string
	St, Et int        //this cell [start,end) in the atom list
	Emb    mat.Vector // matrix os this
	Feat   uint16     //represent which type of this word this is used for tagger
}

//AddTypes add a give types
func (cell *WCell) AddTypes(types mapset.Set) {
	cell.Word.AddTypes(types)
}

//GetTypes give the type to arry
func (cell *WCell) GetTypes() []string {
	if cell.Word.Tags == nil {
		return nil
	}
	tags := cell.Word.Tags.ToSlice()
	stags := make([]string, len(tags))
	for i, t := range tags {
		stags[i] = t.(string)
	}
	return stags
}

//NewWcell create a WCell
func NewWcell(atom *Atom, s, e int) *WCell {
	cell := new(WCell)
	cell.Word = atom
	cell.St = s
	cell.Et = e
	return cell
}

//Cursor map cursor used
type Cursor struct {
	pre, lack *Cursor // pointer of this node pre and lack
	val       *WCell  // val of the node
	idx       int     // index of this course in list
}

func newCur(value *WCell, pre, next *Cursor) *Cursor {
	cur := new(Cursor)
	cur.val = value
	cur.pre = pre
	cur.lack = next
	return cur
}

//CellMap used
type CellMap struct {
	Head               *Cursor //this cellMap size
	rows, colums, size int     // this cellmap countor
}

func newCellMap() *CellMap {
	cmap := new(CellMap)
	cmap.Head = new(Cursor)
	cmap.Head.idx = -1
	cmap.Head.val = new(WCell)
	cmap.Head.val.St = -1
	cmap.Head.val.Et = 0
	return cmap
}

func (cmap *CellMap) indexMap() {
	node := cmap.Head
	node.idx = -1
	index := node.idx
	for node.lack != nil {
		node = node.lack
		index++
		node.idx = index
	}
}

//IterRow is iter the cellmap ,if row<0 tits iter all
func (cmap *CellMap) IterRow(cur *Cursor, row int, dfunc func(*Cursor)) {
	if cur == nil {
		cur = cmap.Head
	}
	if row < 0 { //Iter all if row is negtive
		for cur.lack != nil {
			dfunc(cur.lack)
			cur = cur.lack
		}
		return
	}
	for cur.lack != nil { //Iter the give row from start
		n := cur.lack
		if n.val.St < row {
			cur = n
			continue
		}
		if n.val.St != row {
			break
		}
		dfunc(n)
		cur = n
	}
}

// AddNext do add the cell to give cir next
func (cmap *CellMap) AddNext(cursor *Cursor, cell *WCell) *Cursor {
	if cell.St > cmap.rows {
		cmap.rows = cell.St
	}
	if cell.Et > cmap.colums {
		cmap.colums = cell.Et
	}

	for cursor.lack != nil {
		n := cursor.lack
		if n.val.St < cell.St {
			cursor = n
			continue
		}
		if n.val.St == cell.St {
			if n.val.Et < cell.Et {
				cursor = n
				continue
			}
			if n.val.Et == cell.Et {
				n.val.AddTypes(cell.Word.Tags)
				return n
			}
		}
		m := newCur(cell, cursor, n)
		cmap.size++
		cursor.lack = m
		n.pre = m
		return m
	}
	cursor.lack = newCur(cell, cursor, nil)
	cmap.size++
	return cursor.lack
}

//AddPre add a cell to next
func (cmap *CellMap) AddPre(cur *Cursor, cell *WCell) *Cursor {
	if cell.St > cmap.rows {
		cmap.rows = cell.St
	}
	if cell.Et > cmap.colums {
		cmap.colums = cell.Et
	}
	for cur.pre != cmap.Head {
		n := cur.pre
		if n.val.St > cell.St {
			cur = n
			continue
		}
		if n.val.St == cell.St {
			if n.val.Et > cell.Et {
				cur = n
				continue
			}
			if n.val.Et == cell.Et {
				n.val.AddTypes(cell.Word.Tags)
				return n
			}
		}
		m := newCur(cell, n, cur)
		cmap.size++
		cur.pre = m
		n.lack = m
		return m
	}
	cur.pre = newCur(cell, cmap.Head, cur)
	cmap.size++
	return cur.pre
}

//AddCell add a cell to a cur
func (cmap *CellMap) AddCell(cell *WCell, cur *Cursor) *Cursor {
	if cur == nil {
		return cmap.AddNext(cmap.Head, cell)
	}
	if cur.val.St < cell.St {
		return cmap.AddNext(cur, cell)
	}
	if (cur.val.St == cell.St) && (cur.val.Et <= cell.Et) {
		return cmap.AddNext(cur, cell)
	}
	return cmap.AddPre(cur, cell)
}

//CellRecognizer is code
type CellRecognizer interface {
	//recognizer all Wcell possable in the atomlist
	Read(content *AtomList, cmap *CellMap)
}

//CellQuantizer interface of a prepre
type CellQuantizer interface {
	//set the cmap val data embeding
	Embed(context *AtomList, cmap *CellMap)
	//distance of the pre and next cell
	Distance(pre *WCell, next *WCell) float32
}

func splitContent(cmap *CellMap, quantizer CellQuantizer, context *AtomList) []*WCell {
	buildGraph := func() map[int][][2]float32 {
		cmap.indexMap()
		quantizer.Embed(context, cmap)
		var tmp [][2]float32
		graph := make(map[int][][2]float32)
		cmap.IterRow(nil, 0, func(cur *Cursor) {
			tmp = append(tmp, [2]float32{float32(cur.idx), 0.0})
		})
		graph[-1] = tmp
		cmap.IterRow(nil, -1, func(pre *Cursor) {
			tmp = [][2]float32{}
			cmap.IterRow(pre, pre.val.Et, func(next *Cursor) {
				dist := quantizer.Distance(pre.val, next.val)
				if dist > 0.0 {
					tmp = append(tmp, [2]float32{float32(next.idx), dist})
				}
			})
			if len(tmp) < 1 {
				tmp = append(tmp, [2]float32{float32(cmap.size), 0.0})
			}
			graph[pre.idx] = tmp
		})

		return graph
	}

	selectPath := func(graph map[int][][2]float32) []int {
		// init
		sz := len(graph)
		dist, prev := make([]float32, sz), make([]int, sz)
		dist[0] = -1.0
		for j := 1; j < len(dist); j *= 2 {
			copy(dist[j:], dist[:j])
		}
		prev[0] = -2
		for j := 1; j < len(prev); j *= 2 {
			copy(prev[j:], prev[:j])
		}
		used := mapset.NewSet()
		for j := 1; j < sz; j++ {
			used.Add(j)
		}
		visted := mapset.NewSet()

		for _, nw := range graph[-1] {
			node, weight := int(nw[0]), nw[1]
			dist[node] = weight
			visted.Add(node)
			prev[node] = -1
		}

		// dijkstra
		for used.Contains(sz - 1) {
			minDist, u := float32(3.4e30), 0
			used.Intersect(visted).Each(func(i interface{}) bool {
				idx := i.(int)
				if dist[idx] < minDist {
					minDist = dist[idx]
					u = idx
				}
				return false
			})
			if u == sz-1 {
				break
			}
			used.Remove(u)
			for _, nw := range graph[u] {
				node, weight := int(nw[0]), nw[1]
				if !used.Contains(node) {
					continue
				}
				c := dist[u] + weight
				visted.Add(node)
				if (dist[node] < 0) || (c < dist[node]) {
					dist[node] = c
					prev[node] = u
				}
			}
		}

		// select
		bestPaths := make([]int, 1, sz)
		bestPaths[0] = sz - 1
		for bestPaths[len(bestPaths)-1] > -1 {
			last := bestPaths[len(bestPaths)-1]
			bestPaths = append(bestPaths, prev[last])
		}
		for i, j := 0, len(bestPaths)-1; i < j; i, j = i+1, j-1 {
			bestPaths[i], bestPaths[j] = bestPaths[j], bestPaths[i]
		}
		return bestPaths
	}
	nodeUse := mapset.NewSet()
	for _, idx := range selectPath(buildGraph()) {
		nodeUse.Add(idx)
	}
	result := make([]*WCell, 0, nodeUse.Cardinality())
	cmap.IterRow(nil, -1, func(cur *Cursor) {
		if nodeUse.Contains(cur.idx) {
			result = append(result, cur.val)
		}
	})
	return result
}

//Segment is used for segment
type Segment struct {
	cellRecognizers []CellRecognizer
	quantizer       CellQuantizer
}

//NewSegment make a new segment
func NewSegment(q CellQuantizer) *Segment {
	s := new(Segment)
	s.quantizer = q
	s.cellRecognizers = make([]CellRecognizer, 0, 2)
	return s
}

//AddCellRecognizer add a CellRecognizer
func (seg *Segment) AddCellRecognizer(r CellRecognizer) {
	if r != nil {
		seg.cellRecognizers = append(seg.cellRecognizers, r)
	}
}

func (seg *Segment) buildMap(atomList *AtomList) *CellMap {
	cmap := newCellMap()
	cur := cmap.Head
	for i, atom := range atomList.List {
		cur = cmap.AddNext(cur, NewWcell(atom, i, i+1))
	}

	for _, recognizer := range seg.cellRecognizers {
		recognizer.Read(atomList, cmap)
	}

	return cmap
}

//SmartCut cut a atoml list
func (seg *Segment) SmartCut(atomList *AtomList, maxMode bool) []*WCell {
	if len(atomList.List) < 1 {
		return nil
	}
	if len(atomList.List) == 1 {
		res := make([]*WCell, 1)
		res[0] = NewWcell(atomList.List[0], 0, 1)
		return res
	}
	cmap := seg.buildMap(atomList)
	if maxMode {
		res := make([]*WCell, 0, cmap.size)
		cmap.IterRow(nil, -1, func(cur *Cursor) {
			res = append(res, cur.val)
		})
		return res
	}
	return splitContent(cmap, seg.quantizer, atomList)
}
