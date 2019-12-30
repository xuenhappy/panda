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
	 * 中文数字
	 */
	CCNUM,

	/**
	 * 日期
	 */
	DATE,
	/**
	 * 各种节日
	 */
	FESTIVAL,

	/**
	 * 时间
	 */
	TIME,
	/**
	 * 姓名或人物，名人等
	 */
	PERSON,

	/**
	 * 地址，行政区划，景点等
	 */
	PLACE,
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
	ORDER,
	/**
	 * 动物
	 */
	ANIMALS, 
	/**
	 * 植物
	 */
	PLANTS, 
	/**
	 * 食物
	 */
	FOOD,
	/**
	 * 器具，电器等
	 */
	APPLIANCE,
	/**
	 * 疾病
	 */
	DISEASE,
	/**
	 * 法律术语
	 */
	LEGAL,
	/**
	 * 天体
	 */
	CELESTIAL,
	/**
	 * 星座
	 */
	ZODIAC;

}
