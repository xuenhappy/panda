package org.bamboo.nlp.panda.core;

public enum CellType {
	/**
	 * 未知的类型
	 */
	UNK,
	/**
	 * 空白
	 */
	SPACE,
	/**
	 * 普通中文字符串
	 */
	CHW,
	/**
	 * 标点符号
	 */
	PUNC,
	/**
	 * 表情符号
	 */
	EMOJI,
	/**
	 * 英文字符串
	 */
	ENG,
	/**
	 * 数字
	 */
	NUM,
	/**
	 * 日期
	 */
	DATE,

	/**
	 * 时间
	 */
	TIME,
	/**
	 * 姓名
	 */
	NAME,

	/**
	 * 地址
	 */
	ADDRESS,
	/**
	 * 机构
	 */
	ORG,
	/**
	 * 量词
	 */
	QUANT,
	/**
	 * 编号与序数
	 */
	ORDER;

}
