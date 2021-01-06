package darts

import (
	"github.com/deckarep/golang-set"
	"gonum.org/v1/gonum/mat"
)

// Atom is  the small cell in split
type Atom struct {
	Image   string     //image of the atom
	St, End int        // [start,end) of this atom in ori string
	Tags    mapset.Set // type is
}

//NewAtom crate the atom
func NewAtom(str *string, start, end int) *Atom {
	atom := new(Atom)
	atom.Image = *str
	atom.St = start
	atom.End = end
	return atom
}

//WCell is the split token parttern
type WCell struct {
	Word   *Atom      //the word in ori string
	St, Et int        //this cell [start,end) in the atom list
	Emb    mat.Vector // matrix os this
	Feat   uint16     //represent which type of this word this is used for tagger
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
	head               *Cursor //this cellMap size
	rows, colums, size int     // this cellmap countor
}

func (cmap *CellMap) indexMap() {
	node := cmap.head
	index := -1
	for node.lack != nil {
		node = node.lack
		index++
		node.idx = index
	}
}

func newCellMap() *CellMap {
	cmap := new(CellMap)
	cmap.head = new(Cursor)
	cmap.head.val = new(WCell)
	cmap.head.val.St = -1
	cmap.head.val.Et = 0
	return cmap
}

//CellRecognizer is code
type CellRecognizer interface {
	//recognizer all Wcell possable in the atomlist
	read(content []Atom, cmap *CellMap)
}

//CellPresenter is embed
type CellPresenter interface {
	//set the cmap val data embeding
	embed(context []Atom, cmap *CellMap)
}

//CellQuantizer interface of a prepre
type CellQuantizer interface {
	//distance of the pre and next cell
	distance(pre *WCell, next *WCell) float32
}
