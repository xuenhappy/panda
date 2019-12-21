package org.bamboo.nlp.panda.tools;

/**
 * 
 * @author xuen
 *
 */
public interface ItemSequence{
	/**
	 * 找到第i个位置的item
	 * @param index
	 * @return
	 */
	public Item ItemAt(int index);
	/**
	 * 长度
	 * @return
	 */
	public int length();
	/**
	 * 转为数组
	 * @return
	 */
	public Item[] toItemArray();
}
