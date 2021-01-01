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

const UINT64_MAX = ^uint64(0)

type StringIter interface{
	next() (*string, bool) // a string iter
}

type KVIter interface{
	next() (StringIter, *string) // k,v iter
}

 // trie 
type Trie struct{
	check,base,fail,l *[]uint64
	v *[]string
	output *[]*[]uint64
	maxlen uint32
	codemap *map[string]uint64
}

// get string code
func (t *Trie) getcode(String *word) uint64{
	code,ok :=t.codemap[*word]
	if(!ok){
		return len(t.codemap)+1
	}
	return code
}

//trans
func (t *Trie) transitionWithRoot(nodePos uint64,  c uint64) uint64{
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
func (t *Trie) getState(currentState uint64, character uint64) uint64{
	newCurrentState := t.transitionWithRoot(currentState, character)
	for{
		if newCurrentState != UINT64_MAX {
			break
		}
		currentState = t.fail[currentState]
		newCurrentState = t.transitionWithRoot(currentState, character)
	}
	return newCurrentState
}


//parse a text list
func (t *Trie) ParseText(text StringIter, hit func(uint64,uint64,string)){
	currentState := 0
	var index_bufer [t.maxlen]uint64
	index_buffer_pos := 0
	for seq,skip:=text.netext();seq!=nil;seq,skip:=text.netext(){
		if skip{
			continue
		}
		index_bufer[index_buffer_pos % t.maxlen] = position
		index_buffer_pos += 1
		currentState = t.getState(currentState, t.getCode(seq))
		hitArray = t.output[currentState]
		if hitArray!=nil{
			continue
		}
		for _,h:= range hitArray{
			pre_index := (index_buffer_pos-t.l[h]) % t.maxlen
			hit(index_bufer[pre_index], position+1, t.v[h])
		}		
	}
}



class _State():
    def __init__(self, depth=0):
        self.depth = depth
        self.failure = None
        self.emits = set()
        self.success = {}
        self.index = 0

    def getLargestValueId(self):
        if not self.emits:
            return None
        return max(self.emits)

    def isAcceptable(self):
        return self.depth > 0 and len(self.emits) > 0

    def setFailure(self, failState, fail):
        self.failure = failState
        fail[self.index] = failState.index

    def nextState(self, character, ignoreRootState=False):
        nextState = self.success.get(character, None)
        if (not ignoreRootState) and (nextState is None) and (self.depth == 0):
            nextState = self
        return nextState

    def addState(self, character):
        nextS = self.nextState(character, True)
        if nextS is None:
            nextS = _State(self.depth + 1)
            self.success[character] = nextS
        return nextS

    def getStates(self):
        return iter(v for (k, v) in sorted(self.success.items(), key=lambda x: x[0]))

    def getTransitions(self):
        return sorted(self.success.keys())


class _Builder():
    def __init__(self):
        self.rootState = _State()
        self.used = []
        self.base = []
        self.check = []
        self.v = None
        self.codemap = {}
        self.l = None
        self.allocSize = 0
        self.progress = 0
        self.nextCheckPos = 0
        self.size = 0

    def build(self, kvs, strKey):
        max_code = self._addAllKeyword(kvs, strKey)
        # build double array tire base on tire
        self._buildDoubleArrayTrie(max_code)
        self.used = None
        # build failure table and merge output table
        self._constructFailureStates()
        self.rootState = None
        self._loseWeight()
        return _Trie(self.check, self.base, self.fail, self.output, self.v, self.l, self.codemap)

    def resize(self, newSize):
        base2 = [0]*newSize
        check2 = [0]*newSize
        used2 = [False]*newSize
        if self.allocSize > 0:
            base2[:self.allocSize] = self.base[:self.allocSize]
            check2[:self.allocSize] = self.check[:self.allocSize]
            used2[:self.allocSize] = self.used[:self.allocSize]

        self.base = base2
        self.check = check2
        self.used = used2
        self.allocSize = newSize
        return self.allocSize

    def _addAllKeyword(self, kvs, strKey):
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

    def _fetch(self, parent):
        siblings = []
        if parent.isAcceptable():
            fakeNode = _State(-(parent.depth + 1))
            fakeNode.emits.add(parent.getLargestValueId())
            siblings.append((0, fakeNode))
        for (k, v) in sorted(parent.success.items(), key=lambda x: x[0]):
            siblings.append((k + 1, v))
        return siblings

    def _insert(self, siblings):
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

    def _buildDoubleArrayTrie(self, max_code):
        self.progress = 0
        self.resize(max_code+10)
        self.base[0] = 1
        self.nextCheckPos = 0
        root_node = self.rootState
        siblings = self._fetch(root_node)
        if not siblings:
            return
        self._insert(siblings)

    def _constructFailureStates(self):
        self.fail = [0]*(self.size+1)
        self.output = [None]*(self.size+1)
        queue = []

        for depthOneState in self.rootState.getStates():
            depthOneState.setFailure(self.rootState, self.fail)
            queue.append(depthOneState)
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
                self._constructOutput(targetState)

    def _constructOutput(self, targetState):
        emit = targetState.emits
        if (emit is None) or len(emit) == 0:
            return
        self.output[targetState.index] = list(emit)

    def _loseWeight(self):
        nbase = [0]*(self.size+len(self.codemap)+1)
        nbase[:self.size] = self.base[:self.size]
        self.base = nbase

        ncheck = [0]*(self.size + len(self.codemap) + 1)
        ncheck[:self.size] = self.check[:self.size]
        self.check = ncheck


func Compile(kv_iter KVIter) *Trie{
    return _Builder().build(kv_iter, strKey)
}