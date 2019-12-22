package org.bamboo.nlp.panda.tools;

/**
 * 一些常用的字符串处理工具
 * 
 * @author xuen
 *
 */
public final class StrTools {

	/**
	 * 全角转半角
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
			} else if (ch >= ' ' && ch <= 65374) {
				ch = (char) (ch - 65248);
			}
			b.append(ch);
		}
		return b.toString();
	}

}
