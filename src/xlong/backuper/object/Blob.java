package xlong.backuper.object;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.zip.DataFormatException;

import xlong.backuper.util.CompressionUtil;
import xlong.backuper.util.SHA1Util;

/**
 * A blob object only contains the content of a file.
 * <p>
 * A blob object use the SHA-1 checksum of a file as its name.
 * To be specific, the name a blob is "checksum[0-1]/checksum[2-39]".
 * If the blob is compressed, the field compressed is true.
 * <p>
 * A blob object only contains the content of a file.
 * To be specific, blob stores the compressed content of a small file
 * and stores the original content of a big file.
 * Uses ZLIB compression in this class.
 * <p>
 * This blob class provide static methods for
 * creating a blob for a file or restoring a blot to a file.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 */
public final class Blob extends BackupObject {
	
	/** the limit of compressing file. */
	private static final int COMPRESSLIM = 1000000000;
	
	/** the blob is compressed or not. */
	private boolean compressed;
	/**
	 * Constructor just available in this class.
	 * Change the checksum to the name of this blob.
	 * @param checksum the checksum of a file this blob stores.
	 */
	private Blob(final String checksum) {
		setName(checksumToName(checksum));
	}
	

	
	/**
	 * Creates a blob contains the content of a file.
	 * If the blob already exist, just return the exist one.
	 * 
	 * @param fileName the file whose content the blob contains.
	 * @return the created Blob
	 * @throws IOException if the file is not found or the cannot be read.
	 */
	public static Blob create(
			final String fileName) 
					throws IOException {
		String checksum = SHA1Util.sha1Checksum(fileName);
		Blob blob = new Blob(checksum);
		
		Path inFilePath = Paths.get(fileName);
		Path outFilePath = Paths.get(getObjectDir() + "/" + blob.getName());
	
		
		// Create output file directory.
		Files.createDirectories(outFilePath.getParent());

		
		if (Files.size(inFilePath) < COMPRESSLIM) {
			if (!Files.exists(outFilePath)) {
				System.out.println(
						"Compress " + inFilePath + " to " + outFilePath);
				CompressionUtil.compress(fileName, 
						outFilePath.toString());
			}
			blob.compressed = true;
		} else {	
			if (!Files.exists(outFilePath)) {
				System.out.println("Copy " + inFilePath + " to " + outFilePath);
				Files.copy(inFilePath, outFilePath);
			}
			blob.compressed = false;
		}
		
		return blob;
	}
	
	/**
	 * Restore a blob to the file with given fileName.
	 * If the file already exist, this method will rewrite the file.
	 * 
	 * @param blob the blob to restore
	 * @param fileName the fileName of the file restores to
	 * @return success or not.
	 * @throws DataFormatException if the file in the blob is not a blob.
	 * @throws IOException if the file is not found or the cannot be read.
	 */
	public static boolean restore(
			final Blob blob, 
			final String fileName)
					throws IOException, DataFormatException {
		Path inFilePath = Paths.get(getObjectDir() + "/" + blob.getName());
		Path outFilePath = Paths.get(fileName);
		if (!Files.isDirectory(outFilePath.getParent())) {
			Files.createDirectories(outFilePath.getParent());
		}

		if (blob.compressed) {
			System.out.println(
					"Decompress " + inFilePath + " to " + fileName);
			CompressionUtil.decompress(inFilePath.toString(), fileName);
		} else {
			System.out.println("Copy " + inFilePath + " to " + fileName);
			Files.copy(inFilePath, Paths.get(fileName),
					StandardCopyOption.REPLACE_EXISTING);
		}
		return true;
	}

	/**
	 * Restore this blob to the file with given fileName.
	 * If the file already exist, this method will rewrite the file.
	 * 
	 * @param fileName the fileName of the file restores to
	 * @return success or not.
	 * @throws DataFormatException if the file in the blob is not a blob.
	 * @throws IOException if the file is not found or the cannot be read.
	 */
	public boolean restore(final String fileName) 
			throws IOException, DataFormatException {
		return Blob.restore(this, fileName);
	}
	
	
	/**
	 * Gets the blob from its string representation.
	 * @param string the string representation of blob
	 * @return the blob
	 */
	public static Blob get(final String string) {
		Blob blob = new Blob(string.substring(2));
		if (string.startsWith("c")) {
			blob.compressed = true;
		} else {
			blob.compressed = false;
		}
		return blob;
	}
	/**
	 * Converts blob to string.
	 * @return the string
	 */
	@Override
	public String toString() {
		if (compressed) {
			return "c_" + getName().substring(0, 2) 
					+ getName().substring(2 + 1);
		} else {
			return "o_" + getName().substring(0, 2) 
					+ getName().substring(2 + 1);
		}	
	}
}
