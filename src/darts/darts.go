package darts

import (
	"github.com/deckarep/golang-set"
	"gonum.org/v1/gonum/mat"
)

// Atom is  the small cell in split
type Atom struct {
	image   string     //image of the atom
	st, end int32      // [start,end) of this atom in ori string
	tags    mapset.Set // type is
}

//WCell is the split token parttern
type WCell struct {
	word   *Atom      //the word in ori string
	st, et int32      //this cell [start,end) in the atom list
	emb    mat.Vector // matrix os this
	feat   uint16     //represent which type of this word this is used for tagger
}

//Cursor map cursor used
type Cursor struct {
	pre, lack *Cursor // pointer of this node pre and lack
	val       *WCell  // val of the node
	idx       uint32  // index of this course in list
}

//CellMap used
type CellMap struct {
	head               Cursor //this cellMap size
	rows, colums, size uint32 // this cellmap countor
}

//CellRecognizer is code
type CellRecognizer interface {
	//recognizer all Wcell possable in the atomlist
	read(content *[]Atom, cmap *CellMap)
}

//CellPresenter is embed
type CellPresenter interface {
	//set the cmap val data embeding
	embed(context *[]Atom, cmap *CellMap)
}

//CellQuantizer interface of a prepre
type CellQuantizer interface {
	//distance of the pre and next cell
	distance(pre *WCell, next *WCell) float32
}