package xlong.backuper.object;

import java.io.IOException;
import java.util.zip.DataFormatException;


/**
 * The abstract class of all kinds of object classes.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 *
 */
public abstract class BackupObject implements Comparable<BackupObject> {
	
	/** the directory to store objects. */
	private static String objectDir = "object";
	
	/**
	 * Sets the object directory.
	 * @param dir the object directory wants to set.
	 */
	public static final void setObjectDir(final String dir) {
		objectDir = dir;
	}
	
	/**
	 * Gets the object directory. 
	 * @return the object directory.
	 */
	public static final String getObjectDir() {
		return objectDir;
	}
	
	/** the name of this object. */
	private String name;
	
	/**
	 * Gets the name of this object.
	 * @return the name of this blob.
	 */
	public final String getName() {
		return name;
	}

	/**
	 * Sets the name of this object.
	 * @param inName name to set
	 */
	public final void setName(final String inName) {
		name = inName;
	}
	
	/**
	 * Restore the object to the given directory.
	 * If a file is already exist,
	 * this method will rewrite the file.
	 * 
	 * @param dir the directory to restore this tree
	 * @return totally success or not.
	 * @throws IOException if the file is not found or the cannot be read.
	 * @throws DataFormatException  if the file in the blob is not a blob.
	 */
	public abstract boolean restore(String dir) 
			throws IOException, DataFormatException;
	
	/**
	 * Compares one object to another. 
	 * 
	 * @param o the object compare to
	 * @return the compare result.
	 */
	@Override
	public final int compareTo(final BackupObject o) {
		return name.compareTo(o.name);
	}
	
	/**
	 * Converts checksum to name.
	 * @param checksum checksum
	 * @return name
	 */
	public static final String checksumToName(final String checksum) {
		return (checksum.substring(0, 2) + "/" + checksum.substring(2));
	}
	
	/**
	 * Converts checksum to path.
	 * @param checksum checksum
	 * @return path
	 */
	public static final String checksumToPath(final String checksum) {
		return getObjectDir() + "/" + checksumToName(checksum);
	}

}
