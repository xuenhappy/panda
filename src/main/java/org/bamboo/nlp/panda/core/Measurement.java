package org.bamboo.nlp.panda.core;

public interface Measurement {
	/**
	 * measure the distance of f1 and f2
	 * @param f1
	 * @param f2 f1.length==f2.length
	 * @return distance must >0
	 */
	public double measure(float[] f1,float[] f2);

}
