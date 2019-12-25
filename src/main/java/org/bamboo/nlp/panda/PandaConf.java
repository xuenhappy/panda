package org.bamboo.nlp.panda;

import org.json.JSONObject;

public final class PandaConf {
	/**
	 * conf data
	 */
	private JSONObject data;

	public JSONObject getConf(Class<?> cls) {
		return data.getJSONObject(cls.getSimpleName().toLowerCase());
	}

}
