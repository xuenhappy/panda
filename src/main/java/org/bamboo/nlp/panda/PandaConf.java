package org.bamboo.nlp.panda;

import java.io.IOException;
import java.io.InputStream;

import org.bamboo.nlp.panda.tools.StrTools;
import org.json.JSONException;
import org.json.JSONObject;

public final class PandaConf {
	/**
	 * conf data
	 */
	private JSONObject data;
	
	/**
	 * get some conf by id
	 * @param id
	 * @return
	 */

	public JSONObject getConf(String id) {
		return data.getJSONObject(id);
	}

	/**
	 * load data
	 * @param in
	 * @throws JSONException
	 * @throws IOException
	 */
	public void loadConf(InputStream in) throws JSONException, IOException {
		this.data = new JSONObject(StrTools.readfromStream(in, "utf-8"));
	}

}
