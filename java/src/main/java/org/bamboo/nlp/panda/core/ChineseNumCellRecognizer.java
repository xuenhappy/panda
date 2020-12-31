package org.bamboo.nlp.panda.core;

import java.io.IOException;
import java.util.Arrays;
import java.util.Map;
import java.util.TreeMap;

import org.bamboo.nlp.panda.core.CellMap.Cursor;

/**
 * Recognize the Chinese number {@link CellType.CNUM}
 * 
 * @author xuen
 *
 */
public class ChineseNumCellRecognizer implements CellRecognizer {
	private final static CharSequence[] CHINESE_NUM = "一二三四五六七八九壹贰叁肆伍陆柒捌玖".split("");
	private final static CharSequence[] CHINESE_UNIT = "零十百拾佰千仟万萬亿".split("");
	private final static String CHINESE_POINT = "点";
	static {
		Arrays.sort(CHINESE_NUM);
		Arrays.sort(CHINESE_UNIT);
	}

	private static final class Buffer {
		private CellMap.Cursor cursor;
		int state;
		int pos;

		public Buffer(Cursor cursor, int state, int pos) {
			super();
			this.cursor = cursor;
			this.state = state;
			this.pos = pos;
		}
	}

	protected static interface Action {
		/**
		 * do this action and seek to the next index
		 * 
		 * @param buffer
		 * @param baseStr
		 * @param map
		 * @param pos
		 * @return
		 */
		public int dothis(Buffer buffer, AtomList baseStr, CellMap map, int pos);
	}

	private static final class State {
		private Map<Integer, Action> states = new TreeMap<Integer, ChineseNumCellRecognizer.Action>();

		private void addState(int accept, Action action) {
			this.states.put(accept, action);
		}

		private Action trans(int input) {
			return states.get(input);
		}
	}

	private static final Map<Integer, State> root = new TreeMap<Integer, State>();
	private static final Action EMPTY_ACTION = new Action() {
		@Override
		public int dothis(Buffer buffer, AtomList baseStr, CellMap map, int pos) {
			return pos + 1;
		}
	};

	static {
		/**
		 * state:0 empty state; <br/>
		 * state:1 buffer ends number;<br/>
		 * state:2 buffer has unit;<br/>
		 * state:3 buffer has point;<br/>
		 * state:4 buffer is only unit;<br/>
		 */

		/**
		 * input:0 normal word;<br/>
		 * input:1 normal number word;<br/>
		 * input:2 number unit;<br/>
		 * input:3 point char;<br/>
		 */

		// state is 0
		State state_0 = new State();
		state_0.addState(0, EMPTY_ACTION);
		state_0.addState(1, new Action() {
			@Override
			public int dothis(Buffer buffer, AtomList baseStr, CellMap map, int pos) {
				buffer.state = 1;
				buffer.pos = pos;
				return pos + 1;
			}
		});
		state_0.addState(2, new Action() {
			@Override
			public int dothis(Buffer buffer, AtomList baseStr, CellMap map, int pos) {
				buffer.state = 4;
				buffer.pos = pos;
				return pos + 1;
			}
		});
		state_0.addState(3, EMPTY_ACTION);

		// state is 1
		State state_1 = new State();
		state_1.addState(0, new Action() {
			@Override
			public int dothis(Buffer buffer, AtomList baseStr, CellMap map, int pos) {
				if ((pos + 2) < baseStr.size() && baseStr.get(pos).image.equals("分")
						&& baseStr.get(pos + 1).image.equals("之") && typeOf(baseStr.get(pos + 2).image) == 1) {
					buffer.state = 3;
					return pos + 3;
				}
				buffer.cursor = map.addCell(buffer.cursor, new WordCell(baseStr.sub(buffer.pos, pos), buffer.pos, pos));// add
				buffer.cursor.val.addType(CellType.CCNUM); // data
				buffer.pos = -1;
				buffer.state = 0;
				return pos + 1;
			}
		});
		state_1.addState(1, EMPTY_ACTION);
		state_1.addState(2, new Action() {
			@Override
			public int dothis(Buffer buffer, AtomList baseStr, CellMap map, int pos) {
				buffer.state = 2;
				return pos + 1;
			}
		});
		state_1.addState(3, new Action() {
			@Override
			public int dothis(Buffer buffer, AtomList baseStr, CellMap map, int pos) {
				buffer.state = 3;
				return pos + 1;
			}
		});
		// state is 2
		State state_2 = new State();
		state_2.addState(0, new Action() {
			@Override
			public int dothis(Buffer buffer, AtomList baseStr, CellMap map, int pos) {
				buffer.cursor = map.addCell(buffer.cursor, new WordCell(baseStr.sub(buffer.pos, pos), buffer.pos, pos));// add
				buffer.cursor.val.addType(CellType.CCNUM); // data
				buffer.pos = -1;
				buffer.state = 0;
				return pos + 1;
			}
		});
		state_2.addState(1, new Action() {
			@Override
			public int dothis(Buffer buffer, AtomList baseStr, CellMap map, int pos) {
				buffer.state = 1;
				return pos + 1;
			}
		});
		state_2.addState(2, new Action() {
			@Override
			public int dothis(Buffer buffer, AtomList baseStr, CellMap map, int pos) {
				buffer.cursor = map.addCell(buffer.cursor, new WordCell(baseStr.sub(buffer.pos, pos), buffer.pos, pos));// add
				buffer.cursor.val.addType(CellType.CCNUM); // data
				buffer.pos = pos;
				buffer.state = 4;
				return pos + 1;
			}
		});
		state_2.addState(3, new Action() {
			@Override
			public int dothis(Buffer buffer, AtomList baseStr, CellMap map, int pos) {
				buffer.state = 3;
				return pos + 1;
			}
		});
		// state is 3
		State state_3 = new State();
		state_3.addState(0, new Action() {
			@Override
			public int dothis(Buffer buffer, AtomList baseStr, CellMap map, int pos) {
				if (baseStr.get(pos - 1).image.endsWith(CHINESE_POINT)) {
					buffer.cursor = map.addCell(buffer.cursor,
							new WordCell(baseStr.sub(buffer.pos, pos - 1), buffer.pos, pos - 1));// add data
				} else {
					buffer.cursor = map.addCell(buffer.cursor,
							new WordCell(baseStr.sub(buffer.pos, pos), buffer.pos, pos));// add data
				}

				buffer.cursor.val.addType(CellType.CCNUM);
				buffer.pos = -1;
				buffer.state = 0;
				return pos + 1;
			}
		});
		state_3.addState(1, EMPTY_ACTION);
		state_3.addState(2, new Action() {
			@Override
			public int dothis(Buffer buffer, AtomList baseStr, CellMap map, int pos) {
				if (baseStr.get(pos - 1).image.endsWith(CHINESE_POINT)) {
					buffer.cursor = map.addCell(buffer.cursor,
							new WordCell(baseStr.sub(buffer.pos, pos - 1), buffer.pos, pos - 1));// add data
				} else {
					buffer.cursor = map.addCell(buffer.cursor,
							new WordCell(baseStr.sub(buffer.pos, pos), buffer.pos, pos));// add data
				}
				buffer.cursor.val.addType(CellType.CCNUM);
				buffer.pos = pos;
				buffer.state = 4;
				return pos + 1;
			}
		});
		state_3.addState(3, new Action() {
			@Override
			public int dothis(Buffer buffer, AtomList baseStr, CellMap map, int pos) {
				if (baseStr.get(pos - 1).image.endsWith(CHINESE_POINT)) {
					buffer.cursor = map.addCell(buffer.cursor,
							new WordCell(baseStr.sub(buffer.pos, pos - 1), buffer.pos, pos - 1));// add data
				} else {
					buffer.cursor = map.addCell(buffer.cursor,
							new WordCell(baseStr.sub(buffer.pos, pos), buffer.pos, pos));// add data
				}
				buffer.cursor.val.addType(CellType.CCNUM);
				buffer.pos = -1;
				buffer.state = 0;
				return pos + 1;
			}
		});
		// state is 4
		State state_4 = new State();
		state_4.addState(0, new Action() {
			@Override
			public int dothis(Buffer buffer, AtomList baseStr, CellMap map, int pos) {
				buffer.cursor = map.addCell(buffer.cursor, new WordCell(baseStr.sub(buffer.pos, pos), buffer.pos, pos));// add
																														// data
				buffer.cursor.val.addType(CellType.CCNUM);
				buffer.pos = -1;
				buffer.state = 0;
				return pos + 1;
			}
		});
		state_4.addState(1, new Action() {
			@Override
			public int dothis(Buffer buffer, AtomList baseStr, CellMap map, int pos) {
				buffer.cursor = map.addCell(buffer.cursor, new WordCell(baseStr.sub(buffer.pos, pos), buffer.pos, pos));// add
				buffer.cursor.val.addType(CellType.CCNUM); // data
				buffer.pos = pos;
				buffer.state = 1;
				return pos + 1;
			}
		});
		state_4.addState(2, new Action() {
			@Override
			public int dothis(Buffer buffer, AtomList baseStr, CellMap map, int pos) {
				buffer.cursor = map.addCell(buffer.cursor, new WordCell(baseStr.sub(buffer.pos, pos), buffer.pos, pos));// add
				buffer.cursor.val.addType(CellType.CCNUM); // data
				buffer.pos = pos;
				return pos + 1;
			}
		});
		state_4.addState(3, new Action() {
			@Override
			public int dothis(Buffer buffer, AtomList baseStr, CellMap map, int pos) {
				buffer.state = 3;
				return pos + 1;
			}
		});

		// add last
		root.put(0, state_0);
		root.put(1, state_1);
		root.put(2, state_2);
		root.put(3, state_3);
		root.put(4, state_4);

	}

	@Override
	public void close() throws IOException {
	}

	@Override
	public void read(AtomList baseStr, CellMap map) {
		Buffer buffer = new Buffer(map.head(), 0, -1);
		int index = 0;
		while (index < baseStr.size()) {
			int input = typeOf(baseStr.get(index).image);
			index = root.get(buffer.state).trans(input).dothis(buffer, baseStr, map, index);
		}
		// do last
		root.get(buffer.state).trans(0).dothis(buffer, baseStr, map, baseStr.size());
	}

	private static int typeOf(CharSequence ch) {
		if (ch.length() != 1)
			return 0;
		int c = Arrays.binarySearch(CHINESE_NUM, ch);
		if (c >= 0)
			return 1;
		c = Arrays.binarySearch(CHINESE_UNIT, ch);
		if (c >= 0)
			return 2;
		if (ch.equals(CHINESE_POINT))
			return 3;
		return 0;
	}

}
