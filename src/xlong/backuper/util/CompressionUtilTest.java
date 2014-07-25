package xlong.backuper.util;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;

import org.junit.Test;

/**
 * JUnit test class for CompressionUtil class.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 */
public class CompressionUtilTest {

	/**
	 * Test method for 
	 * {@link xlong.backuper.util.CompressionUtil#compress(Path, Path)} 
	 * and
	 * {@link xlong.backuper.util.CompressionUtil#decompress(Path, Path)}.
	 */
	@Test
	public final void test() {
		String oriFile = "data/test";
		Path oriPath = Paths.get(oriFile);
		Path zipPath = Paths.get(oriFile + ".zlib");
		Path newPath = Paths.get(oriFile + "_new");
		String checksumOri = null;
		String checksumNew = null;
		try {
			CompressionUtil.compressFile(oriPath, zipPath);
			CompressionUtil.decompressToFile(zipPath, newPath);
			checksumOri = SHA1Util.sha1Checksum(oriPath);
			checksumNew = SHA1Util.sha1Checksum(newPath);
		} catch (IOException e) {
			org.junit.Assert.fail();
			e.printStackTrace();
		}
		assertEquals(checksumOri, checksumNew);
	}

}
