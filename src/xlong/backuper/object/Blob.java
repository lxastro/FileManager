package xlong.backuper.object;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

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
	
	/** for serialization. */
	private static final long serialVersionUID = -4807521920350645024L;

	/** the limit of compressing file. */
	private static final int COMPRESSLIM = 1000000000;
	
	/** the blob is compressed or not. */
	private boolean compressed;
	/**
	 * Constructor just available in this class.
	 * Set the checksum of the blob. 
	 * @param checksum the checksum of a file this blob stores.
	 */
	private Blob(final String checksum) {
		setChecksum(checksum);
	}


	/**
	 * Save the file to the file system, 
	 * if the file not exist in the file system.
	 * Creates a new blob contains the checksum of the file.
	 * 
	 * @param filePath the file to save.
	 * @return the created Blob
	 * @throws IOException if an I/O error occurs
	 */
	protected static Blob create(
			final Path filePath) 
					throws IOException {
		System.out.println("Create Blob for " + filePath);
		String checksum = SHA1Util.sha1Checksum(filePath);
		Blob blob = new Blob(checksum);
		
		Path outFilePath = blob.getPath();

		Files.createDirectories(outFilePath.getParent());
		
		if (Files.size(filePath) < COMPRESSLIM) {
			// compress small file
			if (!Files.exists(outFilePath)) {
				System.out.println(
						"Compress " + filePath + " to " + outFilePath);
				CompressionUtil.compressFile(filePath, outFilePath); 
			}
			blob.compressed = true;
		} else {
			// copy big file
			if (!Files.exists(outFilePath)) {
				System.out.println(
						"Copy " + filePath + " to " + outFilePath);
				Files.copy(filePath, outFilePath);
			}
			blob.compressed = false;
		}
		return blob;
	}

	/**
	 * Restore this blob to the file with given path.
	 * If a file is already exist,
	 * this method will rewrite the file.
	 * If the output directory not exist,
	 * this method will create the directory.
	 * 
	 * @param outFilePath the path of the file restores to
	 * @return success or not
	 * @throws IOException if an I/O error occurs
	 */
	@Override
	public boolean restore(final Path outFilePath) 
			throws IOException {
		Path inFilePath = getPath();
		System.out.println("Restore Blob to " + outFilePath);
		
		Files.createDirectories(outFilePath.getParent());

		if (compressed) {
			System.out.println(
					"Decompress " + inFilePath + " to " + outFilePath);
			CompressionUtil.decompressToFile(inFilePath, outFilePath);
		} else {
			System.out.println(
					"Copy " + inFilePath + " to " + outFilePath);
			Files.copy(inFilePath, outFilePath,
					StandardCopyOption.REPLACE_EXISTING);
		}
		return true;
	}
	
	/**
	 * Converts blob to string.
	 * @return the string
	 */
	@Override
	public String toString() {
		if (compressed) {
			return "Blob compress " + getChecksum() + "\n";
		} else {
			return "Blob original " + getChecksum() + "\n";
		}	
	}
}
