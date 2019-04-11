package distributeblocks.io;

import java.io.File;

/**
 * The DirectoryManager provides helper functions
 * for correctly formatting file paths (directories)
 * and Files so that the code may run smoothly across
 * differing systems.
 */
public class DirectoryManager {
	
	/**
	 * Given a String path that represents a directory 
	 * accessible from the current working directory, this 
	 * method will build a full path from the root of the
	 * file system up to the current working directory and
	 * including the path given.
	 * 
	 * @param path		A directory accessible from the current working directory
	 * @return			A full path from the file system root to the path given
	 */
	public static String fullPathToDir(String path) {
		if (path.length() == 0)
			return path;
		
		String firstChar = String.valueOf(path.charAt(0));
		String lastChar = String.valueOf(path.charAt(path.length() - 1));
		
		// add any required forward or backwar slashes
		String seperator = (!File.separator.equals(firstChar))? File.separator: "";
		String suffix = (!File.separator.equals(lastChar))? File.separator: "";
		
		return System.getProperty("user.dir") + seperator + path + suffix;
	}
	
	/**
	 * Given a String path that represents a file 
	 * accessible from the current working directory, this 
	 * method will build a full path from the root of the
	 * file system up to the current working directory and
	 * including the path given.
	 * 
	 * @param path		A file accessible from the current working directory
	 * @return			A full path from the file system root to the path given
	 */
	public static String fullPathToFile(String path) {
		if (path.length() == 0)
			return path;
		
		String firstChar = String.valueOf(path.charAt(0));
		
		// add any required forward or backwar slashes
		String seperator = (!File.separator.equals(firstChar))? File.separator: "";
		
		return System.getProperty("user.dir") + seperator + path;
	}
	
	/**
	 * Checks whether the local path is the path to a directory, and if its
	 * empty. This method expects a full path, so it may be required to call
	 * fullPathTo() first.
	 * 
	 * @param path		A directory accessible from the current working directory
	 * @param create	Whether or not to create an empty dir if none was found
	 * @return			true if it is an empty dir. If create was true, this will
	 * 					return whether an empty dir resulted (created or already there)
	 */
	public static boolean isEmptyDir(String path, boolean create) {
		File file = new File(path);
		if(!file.isDirectory()) {
			// Fail if it is not a directory and creation was not requested
			if(create == false)
				return false;
			file.mkdir();	
		}
		try {
			// try to count files inside
			if(file.list().length != 0){
				return false;
			}
		} catch (NullPointerException e) {
			return false;
		}
		// if all else passed, it is an empty dir
		return true;
	}

}
