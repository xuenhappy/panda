package org.bamboo.nlp.panda.core;

import java.util.List;

/**
 * 标注词性使用的工具
 * @author xuen
 *
 */
public interface PosTagger {
	
	/**
	 * tag of the split sequence
	 * @param tokens
	 */
	public void tag(List<WordCell> tokens);
	
	
	/**
	 * explain the featue data
	 * @param feature
	 * @return
	 */
	public String explain(int feature);
	
	

}
