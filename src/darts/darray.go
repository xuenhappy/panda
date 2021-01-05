package darts

/*
 * File: darray.go
 * Project: darts
 * File Created: Thursday, 31st December 2020 11:07:30 pm
 * Author: enxu (xuen@mokar.com)
 * -----
 * Last Modified: Thursday, 31st December 2020 11:07:37 pm
 * Modified By: enxu (xuen@mokahr.com)
 * -----
 * Copyright 2021 - 2020 Your Company, Moka
 */

import (
	"container/list"
	"sort"

	"github.com/emirpasic/gods/maps/treemap"
	"gonum.org/v1/gonum/integrate"
)

//StringIter is Used for parer input
type StringIter interface {
	//Next has next word
	Next() bool
	//Word is string
	Word() *string
	//Postion postion
	Postion() int
}

//StrLabelIter is a input
type StrLabelIter interface {
	//Next is hase next pair
	Next() bool
	//String is next string
	String() StringIter
	//String is next value
	Label() int
}

//Trie is a double array trie
type Trie struct {
	check, base, fail, l []int
	v                    []int
	output               [][]int
	maxlen               int
	codemap              map[string]int
}

// get string code
func (t *Trie) getcode(word *string) int {
	code, ok := t.codemap[*word]
	if !ok {
		return len(t.codemap) + 1
	}
	return code
}

//trans
func (t *Trie) transitionWithRoot(nodePos int, c int) int {
	b := t.base[nodePos]
	p := b + c + 1
	if b != t.check[p] {
		if nodePos == 0 {
			return 0
		}
		return -1
	}
	return p
}

// get state transpose
func (t *Trie) getstate(currentState int, character int) int {
	newCurrentState := t.transitionWithRoot(currentState, character)
	for newCurrentState == -1 {
		currentState = t.fail[currentState]
		newCurrentState = t.transitionWithRoot(currentState, character)
	}
	return newCurrentState
}

//ParseText parse a text list hit ( [start,end),tagidx)
func (t *Trie) ParseText(text StringIter, hit func(int, int, int)) {
	currentState, indexBufferPos := 0, 0
	indexBufer := make([]int, t.maxlen)
	for text.Next() {
		position := text.Postion()
		seq := text.Word()
		indexBufer[indexBufferPos%t.maxlen] = position
		indexBufferPos++
		currentState = t.getstate(currentState, t.getcode(seq))
		hitArray := t.output[currentState]
		for _, h := range hitArray {
			preIndex := (indexBufferPos - t.l[h]) % t.maxlen
			hit(indexBufer[preIndex], position+1, t.v[h])
		}
	}
}

//State is DFA mechine state
type State struct {
	depth   int          // the string length
	failure *State       // match failed use
	emits   []int        // emits
	success *treemap.Map //go map
	index   int          //index of the struct
}

func newState(depth int) *State {
	S := new(State)
	S.depth = depth
	S.success = treemap.NewWithIntComparator()
	return S
}

//insert sort a sort to keep it order
func insertSorted(s []int, e int) []int {
	i := sort.Search(len(s), func(i int) bool { return (s)[i] <= e })
	if i == len(s) {
		s = append(s, e)
		return s
	}
	if s[i] == e {
		return s
	}
	s = append(s, 0)
	copy(s[i+1:], s[i:])
	s[i] = e
	return s
}

//add emit
func (s *State) addEmit(keyword int) {
	if keyword != -100000 {
		s.emits = insertSorted(s.emits, keyword)
	}
}

//get l code
func (s *State) getMaxValueIDgo() int {
	if len(s.emits) < 1 {
		return -100000
	}
	return s.emits[0]
}

func (s *State) isAcceptable() bool {
	return s.depth > 0 && len(s.emits) > 0
}

func (s *State) setFailure(failState *State, fail []int) {
	s.failure = failState
	fail[s.index] = failState.index
}

func (s *State) nextState(character int, ignoreRootState bool) *State {
	nextState, _ := s.success.Get(character)
	if (!ignoreRootState) && (nextState == nil) && (s.depth == 0) {
		nextState = s
	}
	if nextState == nil {
		return nil
	}
	return nextState.(*State)
}

func (s *State) addState(character int) *State {
	nextS := s.nextState(character, true)
	if nextS == nil {
		nextS = newState(s.depth + 1)
		s.success.Put(character, nextS)
	}
	return nextS
}

//Builder is inner useed
type Builder struct {
	rootState *State
	trie      *Trie
	/**
	* whether the position has been used
	 */
	used []bool
	/**
	* the allocSize of the dynamic array
	 */
	allocSize int
	/**
	* a parameter controls the memory growth speed of the dynamic array
	 */
	progress int
	/**
	* the next position to check unused memory
	 */
	nextCheckPos int
	/**
	* the size of the key-pair sets
	 */
	size int
}

//zip no use data
func (b *Builder) zipWeight() {
	msize := b.size + int(len(b.trie.codemap)) + 1
	newBase := make([]int, msize)
	copy(b.trie.base[:b.size], newBase[:b.size])
	b.trie.base = newBase

	newCheck := make([]int, msize, msize)
	copy(b.trie.check[:b.size], newCheck[:b.size])
	b.trie.check = newCheck
}

func (b *Builder) constructFailureStates() {
	b.trie.fail = make([]int, b.size+1)
	b.trie.output = make([][]int, b.size+1)
	queue := list.New()

	cpy := func(ori []int) []int {
		dest := make([]int, len(ori))
		copy(ori, dest)
		return dest
	}

	for _, state := range b.rootState.success.Values() {
		depthOneState := state.(*State)
		depthOneState.setFailure(b.rootState, b.trie.fail)
		queue.PushBack(state)
		if len(depthOneState.emits) > 0 {
			b.trie.output[depthOneState.index] = cpy(depthOneState.emits)
		}

	}
	for queue.Len() > 0 {
		currentState := queue.Remove(queue.Front()).(*State)
		for _, key := range b.rootState.success.Keys() {
			transition := key.(int)
			targetState := currentState.nextState(transition, false)
			queue.PushBack(targetState)
			traceFailureState := currentState.failure
			for traceFailureState.nextState(transition, false) == nil {
				traceFailureState = traceFailureState.failure
			}
			newFailureState := traceFailureState.nextState(transition, false)
			targetState.setFailure(newFailureState, b.trie.fail)
			for _, e := range newFailureState.emits {
				targetState.addEmit(e)
			}
			b.trie.output[targetState.index] = cpy(targetState.emits)
		}
	}
}

func (b *Builder) addAllKeyword(kvs StrLabelIter) int {
	maxCode, index, t := 0, -1, b.trie
	for kvs.Next() {
		k, v := kvs.String(), kvs.Label()
		index++
		lens := 0
		currentState := b.rootState
		for k.Next() {
			s := k.Word()
			lens++
			code := b.trie.getcode(s)
			t.codemap[*s] = code
			currentState = currentState.addState(code)
		}
		currentState.addEmit(index)
		t.l = append(t.l, int(lens))
		maxCode += lens
		t.v = append(t.v, v)
	}
	return int(float32(maxCode+len(t.codemap))/1.5) + 1
}

//resize data
func (b *Builder) resize(newSize int) {
	base2 := make([]int, newSize)
	check2 := make([]int, newSize)
	used2 := make([]bool, newSize)
	if b.allocSize > 0 {
		copy(b.trie.base[:int(b.allocSize)], base2[:int(b.allocSize)])
		copy(b.trie.check[:int(b.allocSize)], check2[:int(b.allocSize)])
		copy(b.used[:int(b.allocSize)], used2[:int(b.allocSize)])
	}
	b.trie.base = base2
	b.trie.check = check2
	b.used = used2
	b.allocSize = newSize
}

//Pair si tmp used
type Pair struct {
	k interface{}
	v interface{}
}

func fetch(parent *State) []Pair {
	siblings := make([]Pair, 0, parent.success.Size()+1)
	if parent.isAcceptable() {
		fakeNode := newState(-(parent.depth + 1))
		fakeNode.addEmit(parent.getMaxValueIDgo())
		siblings = append(siblings, Pair{0, fakeNode})
	}
	it := parent.success.Iterator()
	for it.Next() {
		siblings = append(siblings, Pair{it.Key().(int) + 1, it.Value()})
	}
	return siblings
}

func (b *Builder) insert(queue *list.List) {
	tCurrent := queue.Remove(queue.Front()).(Pair)
	value, siblings := tCurrent.k.(int), tCurrent.v.([]Pair)

	begin, nonZeroNum := 0, 0
	first := true
	pos := b.nextCheckPos - 1
	if pos < siblings[0].k.(int) {
		pos = siblings[0].k.(int)
	}
	if b.allocSize <= pos {
		b.resize(pos + 1)
	}
	t := b.trie
	for {
		pos++
		if b.allocSize <= pos {
			b.resize(pos + 1)
		}
		if t.check[pos] != 0 {
			nonZeroNum++
			continue
		} else if first {
			b.nextCheckPos = pos
			first = false
		}

		begin = pos - siblings[0].k.(int)
		if b.allocSize <= (begin + siblings[len(siblings)-1].k.(int)) {
			b.resize(begin + siblings[len(siblings)-1].k.(int) + 100)
		}
		if b.used[begin] {
			continue
		}
		allIszero := true
		for i := 0; i < len(siblings); i++ {
			if t.check[begin+siblings[i].k.(int)] != 0 {
				allIszero = false
				break
			}
		}
		if allIszero {
			break
		}
	}

	if float32(nonZeroNum)/float32(pos-b.nextCheckPos+1) >= 0.95 {
		b.nextCheckPos = pos
	}
	b.used[begin] = true

	if b.size < begin+siblings[len(siblings)-1].k.(int)+1 {
		b.size = begin + siblings[len(siblings)-1].k.(int) + 1
	}
	for i := 0; i < len(siblings); i++ {
		t.check[begin+siblings[i].k.(int)] = begin
	}
	for i := 0; i < len(siblings); i++ {
		kv := siblings[i]
		newSiblings := fetch(kv.v.(*State))
		if len(newSiblings) < 1 {
			t.base[begin+kv.k.(int)] = -(kv.v.(*State).getMaxValueIDgo() + 1)
			b.progress++
		} else {
			queue.PushBack(Pair{begin + kv.k.(int), newSiblings})
		}
		kv.v.(*State).index = begin + kv.k.(int)
	}
	if value >= 0 {
		t.base[value] = begin
	}
}

func (b *Builder) build(kvs StrLabelIter) {
	maxCode := b.addAllKeyword(kvs)
	//build double array tire base on tire
	b.resize(maxCode + 10)
	b.trie.base[0] = 1
	siblings := fetch(b.rootState)
	if len(siblings) > 0 {
		queue := list.New()
		queue.PushBack(Pair{-1, siblings})
		for queue.Len() > 0 {
			b.insert(queue)
		}
	}
	//build failure table and merge output table
	b.constructFailureStates()
	b.rootState = nil
	b.zipWeight()
}

//Compile the data
func Compile(kvs StrLabelIter) *Trie {
	var builder Builder
	builder.rootState = new(State)
	builder.trie = new(Trie)
	builder.build(kvs)
	//clear mem
	result := builder.trie
	builder.trie = nil
	return result
}
