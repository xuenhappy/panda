package darts

import (
	"fmt"
	"github.com/sbinet/npyio/npz"
	"gonum.org/v1/gonum/mat"
	"math"
)

type actFunc func(i, j int, v float64) float64

//DenseNet is a dense net
type DenseNet struct {
	w      mat.Matrix
	b      mat.Vector
	active actFunc
}

//Sigmoid a active function
func Sigmoid(r, c int, z float64) float64 {
	return 1.0 / (1 + math.Exp(-1*z))
}

//Tanh active func
func Tanh(r, c int, z float64) float64 {
	return math.Tanh(z)
}

func applyCopy(fn actFunc, m mat.Matrix) mat.Matrix {
	r, c := m.Dims()
	o := mat.NewDense(r, c, nil)
	o.Apply(fn, m)
	return o
}

func liner(input, w mat.Matrix, b mat.Vector) *mat.Dense {
	var c mat.Dense
	c.Mul(input, w)
	if b != nil { //add blas
		r, _ := c.Dims()
		var x mat.VecDense
		for i := 0; i < r; i++ {
			x.AddVec(c.RowView(i), b)
			c.SetRow(i, x.RawVector().Data)
		}
	}
	return &c
}

//Forward do net work
func (dense *DenseNet) Forward(input mat.Matrix) mat.Matrix {
	c := liner(input, dense.w, dense.b)
	if dense.active != nil {
		c.Apply(dense.active, c)
	}
	return c
}

//SetAct set the active
func (dense *DenseNet) SetAct(act actFunc) {
	dense.active = act
}

//RNNCell is rnn cell
type RNNCell interface {
	//the state size use for the rnn
	stateDim() (int, int)
	//the state
	forward(input, state mat.Matrix) (mat.Matrix, mat.Matrix)
}

//GRUCell is a RNN network,this used for
type GRUCell struct {
	hh, ih mat.Matrix
	hb, ib mat.Vector
}

//readMatFromNpz read a matrix from numpy npz file
func readMatFromNpz(rz npz.Reader, name string) (mat.Matrix, error) {
	key := fmt.Sprintf("%s.npy", name)
	header := rz.Header(key)
	if header == nil {
		return nil, fmt.Errorf("could not find the matrix named %s", name)
	}
	shape := header.Descr.Shape
	if len(shape) > 2 {
		return nil, fmt.Errorf("only support 2d or 1d matrix,but %s id %dd", name, len(header.Descr.Shape))
	}
	size := shape[0]
	if len(shape) == 2 {
		size *= shape[1]
	}
	data := make([]float64, 0, size)
	err := rz.Read(key, &data)
	if err != nil {
		return nil, err
	}
	if len(shape) == 2 {
		return mat.NewDense(shape[0], shape[1], data), nil
	}
	return mat.NewVecDense(shape[0], data), nil

}

//NewGRU make a gru cell
func NewGRU(rz npz.Reader, base string) *GRUCell {
	cell := new(GRUCell)
	if len(base) > 0 {
		base = fmt.Sprintf("%s.", base)
	}
	m, err := readMatFromNpz(rz, fmt.Sprintf("%s%s", base, "rnn_hh_weight"))
	if err != nil {
		panic(err)
	}
	m, err = readMatFromNpz(rz, fmt.Sprintf("%s%s", base, "rnn_ih_weight"))
	if err != nil {
		panic(err)
	}
	cell.ih = m

	m, err = readMatFromNpz(rz, fmt.Sprintf("%s%s", base, "rnn_hh_blas"))
	if err != nil {
		panic(err)
	}
	cell.hb = m.(mat.Vector)
	m, err = readMatFromNpz(rz, fmt.Sprintf("%s%s", base, "rnn_ih_blas"))
	if err != nil {
		panic(err)
	}
	cell.ib = m.(mat.Vector)
	return cell
}

//NewDenseNet make a dense
func NewDenseNet(rz npz.Reader, base string) *DenseNet {
	net := new(DenseNet)
	if len(base) > 0 {
		base = fmt.Sprintf("%s.", base)
	}
	m, err := readMatFromNpz(rz, fmt.Sprintf("%s%s", base, "weight"))
	if err != nil {
		panic(err)
	}
	net.w = m
	m, err = readMatFromNpz(rz, fmt.Sprintf("%s%s", base, "blas"))
	if err != nil {
		panic(err)
	}
	net.b = m.(mat.Vector)
	return net
}
