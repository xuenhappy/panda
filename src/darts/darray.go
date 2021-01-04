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
	"sort"
	"github.com/emirpasic/gods/maps/treemap"
)

//==================================================================================Basic
const UINT32_MAX = ^uint32(0)

type StringIter interface{
	next() (*string, bool) // a string iter
}

type KVIter interface{
	next() (StringIter, uint32) // k,v iter
}

 //===================================================================================Double Array Trie
type Trie struct{
	check,base,fail,l []uint32
	v []uint32
	output [][]uint32
	maxlen uint32
	codemap map[string]uint32
}

// get string code
func (t *Trie) getcode(string *word) uint32{
	code,ok :=t.codemap[*word]
	if(!ok){
		return len(t.codemap)+1
	}
	return code
}

//trans
func (t *Trie) transitionWithRoot(nodePos uint32,  c uint32) uint32{
	b := t.base[nodePos]
	p := b + c + 1
	if (b != t.check[p]){
		if (nodePos == 0){
			return 0
		}
		return UINT64_MAX
	}
	return p
}

// get state transpose
func (t *Trie) getState(currentState uint32, character uint32) uint32{
	newCurrentState := t.transitionWithRoot(currentState, character)
	for{
		if newCurrentState != UINT32_MAX {
			break
		}
		currentState = t.fail[currentState]
		newCurrentState = t.transitionWithRoot(currentState, character)
	}
	return newCurrentState
}


//parse a text list hit ( [start,end),tagidx)
func (t *Trie) ParseText(text StringIter, hit func(uint32,uint32,uint32)){
	currentState := 0
	index_bufer:=[t.maxlen]uint32{}
	index_buffer_pos := 0
	for seq,skip:=text.netext();seq!=nil;seq,skip:=text.netext(){
		if skip{
			continue
		}
		index_bufer[index_buffer_pos % t.maxlen] = position
		index_buffer_pos += 1
		currentState = t.getState(currentState, t.getCode(seq))
		hitArray = t.output[currentState]
		for _,h:= range hitArray{
			pre_index := (index_buffer_pos-t.l[h]) % t.maxlen
			hit(index_bufer[pre_index], position+1, t.v[h])
		}		
	}
}



//=========================================================================================DFA
//DFA mechine state
type State struct{
	depth int // the string length 
	failure *State // match failed use
	emits []uint32 // emits
	success *treemap.Map //go map
	index uint32 //index of the struct
}


func newState(int depth){
	S := new(State)
	S.depth=depth
	S.success=treemap.NewWithIntComparator()
	return S
}

//insert sort a sort to keep it order
func insertSorted(s []int, e int)[]int{
	i := sort.Search(len(s), func(i int) bool { return (s)[i] <=e })
	if i==len(s){
		s=append(s,e)
		return s
	}
	if s[i]==e{
		return s
	}
	s=append(s, 0)
	copy(s[i+1:], s[i:])
	s[i] = e
	return s
}   

//add emit
func (s *State) addEmit(uint32 keyword){
	if keyword!=UINT32_MAX{
		s.emits=insertSorted(s.emits,keyword)
	}
}

//get l code
func (s *State) getLargestValueId()uint32{
	if len(s.emits)<1{
		return UINT32_MAX
	}
	return s.emits[0]
}

func (s *State) isAcceptable(){
	return s.depth > 0 && len(s.emits)>0
}

func (s *State) setFailure(failState *state, fail []uint64){
	s.failure = failState
	fail[s.index] = failState.index
}

func (s *State) nextState(character uint32, ignoreRootState bool) *State{
	nextState:=s.success[character]
	if (!ignoreRootState) && (nextState!=nil) && (s.depth == 0){
		nextState = s
	}
	return nextState
}

func (s *State) nextState(character uint32) *State{
	return s.nextState(character,false)
}

func (s *State) addState(character uint32) *State{
	nextS := s.nextState(character, true)
	if nextS==nil{
		nextS = newState(s.depth+1)
		s.success[character]=nextS
	}
	return nextS
}



//=============================================================================Builder
type Builder struct{
	rootState *State
	trie *Trie
	/**
	* whether the position has been used
	*/
	used []bool
	/**
	* the allocSize of the dynamic array
	*/
	allocSize uint32
	/**
	* a parameter controls the memory growth speed of the dynamic array
	*/
	progress uint32
	/**
	* the next position to check unused memory
	*/
	nextCheckPos uint32
	/**
	* the size of the key-pair sets
	*/
	size uint32
}



//zip no use data
func (b *Builder) zipWeight(){
	msize=b.size+len(b.codemap)+1
	newBase:=make([]uint32,msize,msize)
	copy(b.trie.base[:b.size],newBase[:b.size])
	b.trie.base = newBase

	newCheck :=make([]uint32,msize,msize)
	copy(b.trie.check[:b.size],newCheck[:b.size])
	b.trie.check = newCheck
}



func (b * Builder) constructFailureStates(){
	b.trie.fail=make([]uint32,b.size+1)
	b.trie.output=make([][]uint32,b.size+1)
	
	queue = []

	for depthOneState in self.rootState.getStates():
		depthOneState.setFailure(self.rootState, self.fail)
		queue.append(depthOneState)
		b.trie.output[targetState.index] = depthOneState.emits
		self._constructOutput(depthOneState)

	while len(queue) > 0:
		currentState = queue.pop(0)
		for transition in currentState.getTransitions():
			targetState = currentState.nextState(transition)
			queue.append(targetState)
			traceFailureState = currentState.failure
			while traceFailureState.nextState(transition) is None:
				traceFailureState = traceFailureState.failure
			newFailureState = traceFailureState.nextState(transition)
			targetState.setFailure(newFailureState, self.fail)
			targetState.emits |= newFailureState.emits
			b.trie.output[targetState.index] = targetState.emits
}




func (b *Builder) build(kvs KVIter){
	max_code := b.addAllKeyword(kvs)
	//build double array tire base on tire
	b.buildDoubleArrayTrie(max_code)
	//build failure table and merge output table
	b.constructFailureStates()
	b.rootState = nil
	b.zipWeight()
}
//resize data
func (b *Builder) resize(newSize uint32){
	base2 := make([]uint32,newSize,newSize)
	check2 := make([]uint32,newSize,newSize)
	used2 := make([]bool,newSize,newSize)
	if b.allocSize > 0{
		copy(b.trie.base[:int(b.allocSize)],base2[:int(b.allocSize)])
		copy(b.trie.check[:int(b.allocSize)],check2[:int(b.allocSize)])
		copy(b.used[:int(b.allocSize)],used2[:int(b.allocSize)])
	}
	b.trie.base=base2
	b.trie.check=check2
	b.used=used2
	b.allocSize=newSize
}

func _addAllKeyword(self, kvs, strKey){
	max_code = 0
	self.l = []
	self.v = []
	for index, kv in enumerate(kvs):
		currentState = self.rootState
		lens = 0
		for seq in kv[0]:
			seq = strKey(seq)
			if seq is None:
				continue
			lens += 1
			_seq = str(seq).lower()
			_code = self.codemap.get(_seq, len(self.codemap)+1)
			self.codemap[_seq] = _code
			currentState = currentState.addState(_code)
		if lens < 1:
			continue
		currentState.emits.add(index)
		self.l.append(lens)
		max_code += lens
		self.v.append(kv[1])
	return int((max_code+len(self.codemap))/1.5) + 1
}

func fetch(parent *State){
	siblings:=make([],parent.Size()+1)
	if parent.isAcceptable():
		fakeNode := newState(-(parent.depth + 1))
		fakeNode.addEmit(parent.getLargestValueId())
		siblings.append((0, fakeNode))
	for (k, v) in sorted(parent.success.items(), key=lambda x: x[0]):
		siblings.append((k + 1, v))
	return siblings
}

func _insert(self, siblings){
	begin = 0
	pos = max(siblings[0][0] + 1, self.nextCheckPos) - 1
	nonzero_num = 0
	first = 0
	if self.allocSize <= pos:
		self.resize(pos + 1)
	while True:
		pos += 1
		if self.allocSize <= pos:
			self.resize(pos + 1)
		if self.check[pos] != 0:
			nonzero_num += 1
			continue
		elif first == 0:
			self.nextCheckPos = pos
			first = 1

		begin = pos - siblings[0][0]
		if self.allocSize <= (begin + siblings[-1][0]):
			self.resize(begin + siblings[-1][0]+100)
		if self.used[begin]:
			continue
		flag = False
		for kv in siblings:
			if self.check[begin + kv[0]] != 0:
				flag = True
				break

		if not flag:
			break

	if 1.0 * nonzero_num/(pos - self.nextCheckPos + 1) >= 0.95:
		self.nextCheckPos = pos
	self.used[begin] = True

	if self.size < begin + siblings[-1][0] + 1:
		self.size = begin + siblings[-1][0] + 1

	for kv in siblings:
		self.check[begin + kv[0]] = begin

	for kv in siblings:
		new_siblings = self._fetch(kv[1])
		if not new_siblings:
			self.base[begin + kv[0]] = -(kv[1].getLargestValueId()+1)
			self.progress += 1
		else:
			self.base[begin + kv[0]] = self._insert(new_siblings)
		kv[1].index = begin + kv[0]
	return begin
}

func _buildDoubleArrayTrie(self, max_code){
	self.progress = 0
	self.resize(max_code+10)
	self.base[0] = 1
	self.nextCheckPos = 0
	root_node = self.rootState
	siblings = self._fetch(root_node)
	if not siblings:
		return
	self._insert(siblings)
}






//compile the data
func Compile(kv_iter KVIter) *Trie{
	var builder Builder
	builder.rootState=new(State)
	builder.trie=new(Trie)
	builder.build(kv_iter)
	//clear mem
	result:=builder.trie
	builder.trie=nil
    return result
}