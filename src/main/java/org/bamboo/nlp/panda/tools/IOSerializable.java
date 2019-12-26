package org.bamboo.nlp.panda.tools;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

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
	public void load(InputStream in)throws IOException;
	
	
	
	/**
	 * save to output stream
	 * @param out
	 * @throws IOException
	 */
	public void save(OutputStream out)throws IOException;
	
	
}
