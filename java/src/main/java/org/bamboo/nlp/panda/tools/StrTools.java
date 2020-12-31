package org.bamboo.nlp.panda.tools;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Pattern;

/**
 * 一些常用的字符串处理工具
 * 
 * @author xuen
 *
 */
public final class StrTools {

	public static enum CharType {
		/**
		 * unknown char
		 */
		UNK,

		/**
		 * number char
		 */
		NUM,

		/**
		 * english char
		 */
		WORD,

		/**
		 * a space char
		 */
		SPACE,
		/**
		 * no visual
		 */
		NOVIS,
		/**
		 * a punch char
		 */
		PUNC,
		/**
		 * Chinese or korean or japanese characters
		 */
		CJK

	}

	private static final Pattern PUNC_REGEX = Pattern.compile("\\p{P}");
	private static final Map<Character.UnicodeBlock, Integer> CHAR_TYPE = new HashMap<Character.UnicodeBlock, Integer>();
	static {
		// chinese
		CHAR_TYPE.put(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS, 2);
		CHAR_TYPE.put(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A, 2);
		CHAR_TYPE.put(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_B, 2);
		CHAR_TYPE.put(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_C, 2);
		CHAR_TYPE.put(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_D, 2);
		CHAR_TYPE.put(Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS, 2);
		CHAR_TYPE.put(Character.UnicodeBlock.CJK_COMPATIBILITY_IDEOGRAPHS_SUPPLEMENT, 2);
		// chinese punc
		CHAR_TYPE.put(Character.UnicodeBlock.GENERAL_PUNCTUATION, 3);
		CHAR_TYPE.put(Character.UnicodeBlock.CJK_SYMBOLS_AND_PUNCTUATION, 3);
		CHAR_TYPE.put(Character.UnicodeBlock.HALFWIDTH_AND_FULLWIDTH_FORMS, 3);
		CHAR_TYPE.put(Character.UnicodeBlock.CJK_COMPATIBILITY_FORMS, 3);
		CHAR_TYPE.put(Character.UnicodeBlock.VERTICAL_FORMS, 3);
		// jp
		CHAR_TYPE.put(Character.UnicodeBlock.HIRAGANA, 2);
		CHAR_TYPE.put(Character.UnicodeBlock.KATAKANA, 2);
		CHAR_TYPE.put(Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS, 2);
	}

	/**
	 * Determine the type of a given character
	 * 
	 * @param ch
	 * @return
	 */
	public static CharType typeOfChar(char c) {
		if (c < 256) {
			if (c == ' ' || c == '\t' || c == '\n')
				return CharType.SPACE;
			if ('0' <= c && c <= '9')
				return CharType.NUM;
			if ('A' <= c && c <= 'Z')
				return CharType.WORD;
			if ('a' <= c && c <= 'z')
				return CharType.WORD;
			if (PUNC_REGEX.matcher(c + "").matches())
				return CharType.PUNC;
			return CharType.NOVIS;
		}
		Character.UnicodeBlock ub = Character.UnicodeBlock.of(c);
		if (c == '　')
			return CharType.SPACE;
		if ('０' <= c && c <= '９')
			return CharType.NUM;
		if (PUNC_REGEX.matcher(c + "").matches())
			return CharType.PUNC;
		if (CHAR_TYPE.containsKey(ub)) {
			int ct = CHAR_TYPE.get(ub);
			if (ct == 2)
				return CharType.CJK;
			if (ct == 3)
				return CharType.PUNC;
		}
		if ((0x3130 < c & c < 0x318F) || (0xAC00 <= c && c <= 0xD7A3))
			return CharType.CJK;
		return CharType.UNK;
	}

	/**
	 * 全角转半角
	 * 
	 * @param string
	 * @return
	 */
	public static String full2Half(CharSequence string) {
		if (string.length() == 0)
			return "";
		StringBuilder b = new StringBuilder(string.length());
		for (int i = 0; i < string.length(); i++) {
			char ch = string.charAt(i);
			if (ch == 12288) {
				ch = ' ';
			} else if (ch > '\uFF00' && ch < '\uFF5F') {
				ch = (char) (ch - 65248);
			}
			b.append(ch);
		}
		return b.toString();
	}

	/**
	 * 将流转为字符串
	 * 
	 * @param ins
	 * @param code
	 * @return
	 * @throws IOException
	 */
	public static String readfromStream(InputStream ins, String code) throws IOException {
		StringBuilder buf = new StringBuilder();
		BufferedReader reader = new BufferedReader(new InputStreamReader(ins, code));
		try {
			String line;
			while ((line = reader.readLine()) != null)
				buf.append(line).append('\n');
		} finally {
			reader.close();
		}
		return buf.toString();
	}
}
