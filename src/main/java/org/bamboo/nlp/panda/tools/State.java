package org.bamboo.nlp.panda.tools;

import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

/**
 * this double trie state
 * 
 * @author xuen
 *
 */
public class State {

	/**
	 * the depth of this state
	 */
	protected final int depth;

	/**
	 * fail state,if no match
	 */
	private State failure = null;

	/**
	 * the effective state
	 */
	private Set<Integer> emits = null;
	/**
	 * goto map
	 */
	private Map<Integer, State> success = new TreeMap<Integer, State>();

	/**
	 * index of in array
	 */
	private int index;

	public State() {
		this(0);
	}

	public State(int depth) {
		this.depth = depth;
	}

	public int getDepth() {
		return this.depth;
	}

	/**
	 * 添加一个匹配到的模式串（这个状态对应着这个模式串)
	 *
	 * @param keyword
	 */
	public void addEmit(int keyword) {
		if (this.emits == null) {
			this.emits = new TreeSet<Integer>(Collections.reverseOrder());
		}
		this.emits.add(keyword);
	}

	public Integer getLargestValueId() {
		if (emits == null || emits.size() == 0)
			return null;
		return emits.iterator().next();
	}

	public void addEmit(Collection<Integer> emits) {
		for (Integer emit : emits)
			addEmit(emit);

	}

	public Collection<Integer> emit() {
		return this.emits == null ? Collections.<Integer>emptyList() : this.emits;
	}

	public boolean isAcceptable() {
		return this.depth > 0 && this.emits != null;
	}

	public State failure() {
		return this.failure;
	}

	public void setFailure(State failState, int fail[]) {
		this.failure = failState;
		fail[index] = failState.index;
	}

	private State nextState(Integer character, boolean ignoreRootState) {
		State nextState = this.success.get(character);
		if (!ignoreRootState && nextState == null && this.depth == 0) {
			nextState = this;
		}
		return nextState;
	}

	public State nextState(Integer character) {
		return nextState(character, false);
	}

	public State nextStateIgnoreRootState(Integer character) {
		return nextState(character, true);
	}

	public State addState(int character) {
		State nextState = nextStateIgnoreRootState(character);
		if (nextState == null) {
			nextState = new State(this.depth + 1);
			this.success.put(character, nextState);
		}
		return nextState;
	}

	public Collection<State> getStates() {
		return this.success.values();
	}

	public Collection<Integer> getTransitions() {
		return this.success.keySet();
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder("State[").append("depth=").append(depth).append(", ID=").append(index)
				.append(", emits=").append(emits).append(", success=").append(success.keySet()).append(", failureID=")
				.append(failure == null ? "-1" : failure.index).append(", failure=").append(failure).append(']');
		return sb.toString();
	}

	public Map<Integer, State> getSuccess() {
		return success;
	}

	public int getIndex() {
		return index;
	}

	public void setIndex(int index) {
		this.index = index;
	}
}
