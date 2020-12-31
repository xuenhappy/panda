package org.bamboo.nlp.panda.tools;

import java.io.File;

/**
 * some class tools
 * 
 * @author xuen
 *
 */
public final class ClassTools {
	/**
	 * 获取jar包所在的路径
	 * 
	 * @param cls
	 * @return
	 */
	public static String getJarPath(Class<?> cls) {
		String path = cls.getProtectionDomain().getCodeSource().getLocation().getPath();
		if (System.getProperty("os.name").contains("dows")) {
			path = path.substring(1, path.length());
		}
		if (path.contains("jar")) {
			path = path.substring(0, path.lastIndexOf("."));
			return path.substring(0, path.lastIndexOf("/"));
		}
		return path.replace("target/classes/", "");
	}

	/**
	 * get a path
	 * @param cls
	 * @param path
	 * @return
	 */
	public static File getRelativeFile(Class<?> cls, String path) {
		File file = new File(path);
		if (file.exists() && file.isFile())
			return file;
		return new File(getJarPath(cls), path);
	}

}
