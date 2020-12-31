package org.bamboo.nlp.panda.core;

import java.util.ArrayList;
import java.util.LinkedList;

import org.bamboo.nlp.panda.tools.StrTools;
import org.bamboo.nlp.panda.tools.StrTools.CharType;
/**
 * split a string to base atom that the string can not split any more!
 * @author xuen
 *
 */
public final class BaseLex {

	/**
	 * split the given data
	 * 
	 * @param strs
	 * @return
	 */
	public static AtomList splitStr(CharSequence str) {
		LinkedList<Atom> tmp = new LinkedList<Atom>();
		StringBuilder buf = new StringBuilder();
		CharType bt = CharType.UNK;
		int buf_st = -1;
		CharType t;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);

			t = StrTools.typeOfChar(c);
			if (buf.length() > 0 && bt != t) {
				tmp.add(new Atom(buf.toString(), buf_st, i,changeType(bt)));
				buf.setLength(0);
				bt = CharType.UNK;
				buf_st = -1;
			}

			if (t == CharType.CJK) {
				tmp.add(new Atom(c + "", i, i + 1,CellType.CHW));
				continue;
			}
			buf.append(c);
			bt = t;
			if (buf_st == -1)
				buf_st = i;

		}
		if (buf.length() > 0) {
			tmp.add(new Atom(buf.toString(), buf_st, str.length(),changeType(bt)));
		}

		return new AtomList(tmp.toArray(new Atom[tmp.size()]));
	}

	/**
	 * the same split
	 * @param str
	 * @return
	 */
	public static ArrayList<String> splitStr2(CharSequence str) {
		ArrayList<String> tmp = new ArrayList<String>(str.length());
		StringBuilder buf = new StringBuilder();
		CharType bt = CharType.UNK;
		CharType t;
		for (int i = 0; i < str.length(); i++) {
			char c = str.charAt(i);

			t = StrTools.typeOfChar(c);
			if (buf.length() > 0 && bt != t) {
				tmp.add(buf.toString());
				buf.setLength(0);
				bt = CharType.UNK;
			}

			if (t == CharType.CJK) {
				tmp.add(c + "");
				continue;
			}
			buf.append(c);
			bt = t;

		}
		if (buf.length() > 0)
			tmp.add(buf.toString());

		return tmp;
	}

	private static CellType changeType(CharType bt) {
		if (bt == CharType.CJK)
			return CellType.CHW;

		if (bt == CharType.PUNC)
			return CellType.PUNC;

		if (bt == CharType.NUM)
			return CellType.NUM;

		if (bt == CharType.SPACE)
			return CellType.SPACE;
		if (bt == CharType.WORD)
			return CellType.ENG;

		return CellType.UNK;
	}
}