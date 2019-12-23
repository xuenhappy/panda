package org.bamboo.nlp.panda.tools;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

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

	
	/**
	 * 将流转为字符串
	 * 
	 * @param ins
	 * @param code
	 * @return
	 * @throws IOException
	 */
	public static String readfromStream(InputStream ins, String code) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		byte[] buffer = new byte[1024];
		int length = -1;
		while ((length = ins.read(buffer)) != -1) {
			bos.write(buffer, 0, length);
		}
		bos.close();
		ins.close();
		return bos.toString(code);
	}
}
