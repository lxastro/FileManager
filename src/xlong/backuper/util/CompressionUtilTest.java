package xlong.backuper.util;

import static org.junit.Assert.assertEquals;
import java.io.IOException;
import java.util.zip.DataFormatException;

import org.junit.Test;

/**
 * JUnit test class for CompressionUtil class.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 */
public class CompressionUtilTest {

	/**
	 * Test method for 
	 * {@link xlong.backuper.util.CompressionUtil#compress(String, String)} 
	 * and
	 * {@link xlong.backuper.util.CompressionUtil#decompress(String, String)}.
	 */
	@Test
	public final void test() {
		String oriFile = "data/test";
		String zipFile = oriFile + ".zlib";
		String newFile = oriFile + "_new";
		String checksumOri = null;
		String checksumNew = null;
		try {
			CompressionUtil.compress(oriFile, zipFile);
			CompressionUtil.decompress(zipFile, newFile);
			checksumOri = SHA1Util.sha1Checksum(oriFile);
			checksumNew = SHA1Util.sha1Checksum(newFile);
		} catch (IOException | DataFormatException e) {
			org.junit.Assert.fail();
			e.printStackTrace();
		}
		assertEquals(checksumOri, checksumNew);
	}

}
