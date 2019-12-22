package org.bamboo.nlp.panda.tools;

import java.io.Closeable;

/**
 * 查找词向量的词典
 * @author xuen
 *
 */
public interface WordVecDic extends Closeable{
	
	/**
	 * 向量维度
	 * @return
	 */
	public int dimSize();
	
	
	/**
	 * 支持的字符个数
	 * @return
	 */
	public long wordNum();
	
	/**
	 * 是否支持获取词
	 * @param str
	 * @return
	 */
	public boolean hasWord(CharSequence str);
	
	
	
	/**
	 * 获取词向量
	 * @param seq
	 * @return
	 */
	public float[] embeding(CharSequence seq);

}
