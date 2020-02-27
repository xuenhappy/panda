package org.bamboo.nlp.panda.tools;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;

/**
 * can load and save from stream
 * @author xuen
 *
 */
public interface IOSerializable {

	/**
	 * load from input stream
	 * @param in
	 * @throws IOException
	 */
	public void load(DataInputStream in)throws IOException;
	
	
	
	/**
	 * save to output stream
	 * @param out
	 * @throws IOException
	 */
	public void save(DataOutputStream out)throws IOException;
	
	
}
