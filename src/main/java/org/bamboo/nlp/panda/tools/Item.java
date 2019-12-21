package org.bamboo.nlp.panda.tools;

/**
 * 可比较对象
 * @author xuen
 *
 */
public interface Item  extends Comparable<Item>{
	public int hashCode() ;
	public boolean equals(Object obj);
}
