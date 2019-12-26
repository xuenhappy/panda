package org.bamboo.nlp.panda.tools;

import java.util.*;


/**
 * a double array trie 
 * @author xuen
 *
 * @param <V>
 */
public class DoubleArrayTrie<V> {

	/**
	 * check array of the Double Array Trie structure
	 */
	protected int[] check;
	/**
	 * base array of the Double Array Trie structure
	 */
	protected int[] base;
	/**
	 * fail table of the Aho Corasick automata
	 */
	protected int[] fail;
	/**
	 * output table of the Aho Corasick automata
	 */
	protected int[][] output;
	/**
	 * outer value array
	 */
	protected V[] v;

	/**
	 * the length of every key
	 */
	protected int[] l;

	/**
	 * the size of base and check array
	 */
	protected int size;

	/**
	 * the char seq code map;
	 */
	protected Map<CharSequence, Integer> codeMap;

	/**
	 * buid code map
	 * 
	 * @param datas
	 */
	protected int buildCodeMap(List<? extends StrList> datas) {
		int max_code = 0;
		if (codeMap == null)
			codeMap = new HashMap<CharSequence, Integer>(2000);
		for (StrList ent : datas) {
			for (CharSequence seq : ent) {
				if (codeMap.containsKey(seq))
					continue;
				codeMap.put(seq, codeMap.size() + 1);
			}
			max_code += ent.size();
		}
		return max_code + datas.size();
	}

	protected Integer getCode(CharSequence seq) {
		if (codeMap.containsKey(seq))
			return codeMap.get(seq);
		return codeMap.size() + 1;
	}

	/**
	 * Parse text
	 *
	 * @param text      The text
	 * @param processor A processor which handles the output
	 */
	public void parseText(StrList text, IHit<V> processor) {
		int position = 1;
		int currentState = 0;
		for (CharSequence seq : text) {
			currentState = getState(currentState, getCode(seq));
			int[] hitArray = output[currentState];
			if (hitArray == null) {
				++position;
				continue;
			}

			for (int hit : hitArray) {
				if (!processor.hit(position - l[hit], position, v[hit]))
					return;
			}
			++position;
		}
	}

	/**
	 * Checks that string contains at least one substring
	 *
	 * @param text source text to check
	 * @return {@code true} if string contains at least one substring
	 */
	public boolean matches(StrList text) {
		int currentState = 0;
		for (CharSequence chr : text) {
			currentState = getState(currentState, getCode(chr));
			int[] hitArray = output[currentState];
			if (hitArray != null)
				return true;
		}
		return false;
	}

	/**
	 * Get value by a String key, just like a map.get() method
	 *
	 * @param key The key
	 * @return value if exist otherwise it return null
	 */
	public V get(StrList key) {
		int index = exactMatchSearch(key);
		if (index >= 0)
			return v[index];
		return null;
	}

	/**
	 * Update a value corresponding to a key
	 *
	 * @param key   the key
	 * @param value the value
	 * @return successful or not（failure if there is no key）
	 */
	public boolean set(StrList key, V value) {
		int index = exactMatchSearch(key);
		if (index >= 0) {
			v[index] = value;
			return true;
		}
		return false;
	}

	/**
	 * Pick the value by index in value array <br>
	 * Notice that to be more efficiently, this method DO NOT check the parameter
	 *
	 * @param index The index
	 * @return The value
	 */
	public V get(int index) {
		return v[index];
	}

	/**
	 * Processor handles the output when hit a keyword
	 */
	public interface IHit<V> {
		/**
		 * Hit a keyword, you can use some code like text.substring(begin, end) to get
		 * the keyword
		 *
		 * @param begin the beginning index, inclusive.
		 * @param end   the ending index, exclusive.
		 * @param value the value assigned to the keyword
		 * @return Return true for continuing the search and false for stopping it.
		 */
		boolean hit(int begin, int end, V value);
	}

	/**
	 * transmit state, supports failure function
	 *
	 * @param currentState
	 * @param character
	 * @return
	 */
	private int getState(int currentState, int character) {
		int newCurrentState = transitionWithRoot(currentState, character); // 先按success跳转
		while (newCurrentState == -1) // 跳转失败的话，按failure跳转
		{
			currentState = fail[currentState];
			newCurrentState = transitionWithRoot(currentState, character);
		}
		return newCurrentState;
	}

	/**
	 * transition of a state, if the state is root and it failed, then returns the
	 * root
	 *
	 * @param nodePos
	 * @param c
	 * @return
	 */
	protected int transitionWithRoot(int nodePos, int c) {
		int b = base[nodePos];
		int p;

		p = b + c + 1;
		if (b != check[p]) {
			if (nodePos == 0)
				return 0;
			return -1;
		}

		return p;
	}

	/**
	 * Build a AhoCorasickDoubleArrayTrie from a map
	 *
	 * @param map a map containing key-value pairs
	 */
	public void build(List<? extends StrList> keys, List<V> tags) {
		new Builder().build(keys, tags);
	}

	/**
	 * match exactly by a key
	 *
	 * @param key the key
	 * @return the index of the key, you can use it as a perfect hash function
	 */
	public int exactMatchSearch(StrList key) {
		return exactMatchSearch(key, 0, 0, 0);
	}

	/**
	 * match exactly by a key
	 *
	 * @param key
	 * @param pos
	 * @param len
	 * @param nodePos
	 * @return
	 */
	private int exactMatchSearch(StrList key, int pos, int len, int nodePos) {
		if (len <= 0)
			len = key.size();
		if (nodePos <= 0)
			nodePos = 0;

		int result = -1;

		return getMatched(pos, len, result, key, base[nodePos]);
	}

	private int getMatched(int pos, int len, int result, StrList keyChars, int b1) {
		int b = b1;
		int p;

		for (CharSequence chr : keyChars) {
			p = b + getCode(chr) + 1;
			if (b == check[p])
				b = base[p];
			else
				return result;
		}

		p = b; // transition through '\0' to check if it's the end of a word
		int n = base[p];
		if (b == check[p]) // yes, it is.
		{
			result = -n - 1;
		}
		return result;
	}

	/**
	 * @return the size of the keywords
	 */
	public int size() {
		return v.length;
	}

	/**
	 * A builder to build the AhoCorasickDoubleArrayTrie
	 */
	private class Builder {
		/**
		 * the root state of trie
		 */
		private State rootState = new State();
		/**
		 * whether the position has been used
		 */
		private boolean[] used;
		/**
		 * the allocSize of the dynamic array
		 */
		private int allocSize;
		/**
		 * a parameter controls the memory growth speed of the dynamic array
		 */
		private int progress;
		/**
		 * the next position to check unused memory
		 */
		private int nextCheckPos;

		/**
		 * Build from a map
		 *
		 * @param map a map containing key-value pairs
		 */
		@SuppressWarnings("unchecked")
		public void build(List<? extends StrList> keys, List<V> tags) {
			assert keys.size() == tags.size();
			int max_code = buildCodeMap(keys);
			v = (V[]) tags.toArray();
			l = new int[v.length];
			// 构建二分trie树
			addAllKeyword(keys);
			// 在二分trie树的基础上构建双数组trie树
			buildDoubleArrayTrie(max_code);
			used = null;
			// 构建failure表并且合并output表
			constructFailureStates();
			rootState = null;
			loseWeight();
		}

		/**
		 * fetch siblings of a parent node
		 *
		 * @param parent   parent node
		 * @param siblings parent node's child nodes, i . e . the siblings
		 * @return the amount of the siblings
		 */
		private int fetch(State parent, List<Map.Entry<Integer, State>> siblings) {
			if (parent.isAcceptable()) {
				State fakeNode = new State(-(parent.getDepth() + 1)); // 此节点是parent的子节点，同时具备parent的输出
				fakeNode.addEmit(parent.getLargestValueId());
				siblings.add(new AbstractMap.SimpleEntry<Integer, State>(0, fakeNode));
			}
			for (Map.Entry<Integer, State> entry : parent.getSuccess().entrySet()) {
				siblings.add(new AbstractMap.SimpleEntry<Integer, State>(entry.getKey() + 1, entry.getValue()));
			}
			return siblings.size();
		}

		/**
		 * add a keyword
		 *
		 * @param keyword a keyword
		 * @param index   the index of the keyword
		 */
		private void addKeyword(StrList keyword, int index) {
			State currentState = this.rootState;
			for (CharSequence seq : keyword)
				currentState = currentState.addState(getCode(seq));
			currentState.addEmit(index);
			l[index] = keyword.size();
		}

		/**
		 * add a collection of keywords
		 *
		 * @param keywordSet the collection holding keywords
		 */
		private void addAllKeyword(Collection<? extends StrList> keywordSet) {
			int i = 0;
			for (StrList keyword : keywordSet)
				addKeyword(keyword, i++);
		}

		/**
		 * construct failure table
		 */
		private void constructFailureStates() {
			fail = new int[size + 1];
			output = new int[size + 1][];
			Queue<State> queue = new LinkedList<State>();

			// 第一步，将深度为1的节点的failure设为根节点
			for (State depthOneState : this.rootState.getStates()) {
				depthOneState.setFailure(this.rootState, fail);
				queue.add(depthOneState);
				constructOutput(depthOneState);
			}

			// 第二步，为深度 > 1 的节点建立failure表，这是一个bfs
			while (!queue.isEmpty()) {
				State currentState = queue.remove();
				for (Integer transition : currentState.getTransitions()) {
					State targetState = currentState.nextState(transition);
					queue.add(targetState);

					State traceFailureState = currentState.failure();
					while (traceFailureState.nextState(transition) == null) {
						traceFailureState = traceFailureState.failure();
					}
					State newFailureState = traceFailureState.nextState(transition);
					targetState.setFailure(newFailureState, fail);
					targetState.addEmit(newFailureState.emit());
					constructOutput(targetState);
				}
			}
		}

		/**
		 * construct output table
		 */
		private void constructOutput(State targetState) {
			Collection<Integer> emit = targetState.emit();
			if (emit == null || emit.size() == 0)
				return;
			int[] output = new int[emit.size()];
			Iterator<Integer> it = emit.iterator();
			for (int i = 0; i < output.length; ++i) {
				output[i] = it.next();
			}
			DoubleArrayTrie.this.output[targetState.getIndex()] = output;
		}

		private void buildDoubleArrayTrie(int max_code) {
			progress = 0;
			resize(max_code + 10);
			// init data
			base[0] = 1;
			nextCheckPos = 0;
			State root_node = this.rootState;
			List<Map.Entry<Integer, State>> siblings = new ArrayList<Map.Entry<Integer, State>>(
					root_node.getSuccess().entrySet().size());
			fetch(root_node, siblings);
			if (!siblings.isEmpty())
				insert(siblings);
		}

		/**
		 * allocate the memory of the dynamic array
		 *
		 * @param newSize of the new array
		 * @return the new-allocated-size
		 */
		private int resize(int newSize) {
			int[] base2 = new int[newSize];
			int[] check2 = new int[newSize];
			boolean[] used2 = new boolean[newSize];
			if (allocSize > 0) {
				System.arraycopy(base, 0, base2, 0, allocSize);
				System.arraycopy(check, 0, check2, 0, allocSize);
				System.arraycopy(used, 0, used2, 0, allocSize);
			}

			base = base2;
			check = check2;
			used = used2;

			return allocSize = newSize;
		}

		/**
		 * insert the siblings to double array trie
		 *
		 * @param siblings the siblings being inserted
		 * @return the position to insert them
		 */
		private int insert(List<Map.Entry<Integer, State>> siblings) {
			int begin = 0;
			int pos = Math.max(siblings.get(0).getKey() + 1, nextCheckPos) - 1;
			int nonzero_num = 0;
			int first = 0;

			if (allocSize <= pos)
				resize(pos + 1);

			outer:
			// 此循环体的目标是找出满足base[begin + a1...an] == 0的n个空闲空间,a1...an是siblings中的n个节点
			while (true) {
				pos++;

				if (allocSize <= pos)
					resize(pos + 1);

				if (check[pos] != 0) {
					nonzero_num++;
					continue;
				} else if (first == 0) {
					nextCheckPos = pos;
					first = 1;
				}

				begin = pos - siblings.get(0).getKey(); // 当前位置离第一个兄弟节点的距离
				if (allocSize <= (begin + siblings.get(siblings.size() - 1).getKey()))
					resize(begin + siblings.get(siblings.size() - 1).getKey() + 100);

				if (used[begin])
					continue;

				for (int i = 1; i < siblings.size(); i++)
					if (check[begin + siblings.get(i).getKey()] != 0)
						continue outer;

				break;
			}

			if (1.0 * nonzero_num / (pos - nextCheckPos + 1) >= 0.95)
				nextCheckPos = pos; // 从位置 next_check_pos 开始到 pos 间，如果已占用的空间在95%以上，下次插入节点时，直接从 pos 位置处开始查找
			used[begin] = true;

			size = (size > begin + siblings.get(siblings.size() - 1).getKey() + 1) ? size
					: begin + siblings.get(siblings.size() - 1).getKey() + 1;

			for (Map.Entry<Integer, State> sibling : siblings) {
				check[begin + sibling.getKey()] = begin;
			}

			for (Map.Entry<Integer, State> sibling : siblings) {
				List<Map.Entry<Integer, State>> new_siblings = new ArrayList<Map.Entry<Integer, State>>(
						sibling.getValue().getSuccess().entrySet().size() + 1);

				if (fetch(sibling.getValue(), new_siblings) == 0) // 叶子节点
				{
					base[begin + sibling.getKey()] = (-sibling.getValue().getLargestValueId() - 1);
					progress++;
				} else {
					int h = insert(new_siblings); // deep visit
					base[begin + sibling.getKey()] = h;
				}
				sibling.getValue().setIndex(begin + sibling.getKey());
			}
			return begin;
		}

		/**
		 * free the unnecessary memory
		 */
		private void loseWeight() {
			int[] nbase = new int[size + codeMap.size() + 1];
			System.arraycopy(base, 0, nbase, 0, size);
			base = nbase;
			int[] ncheck = new int[size + codeMap.size() + 1];
			System.arraycopy(check, 0, ncheck, 0, size);
			check = ncheck;
		}
	}
}
