package xlong.backuper.main;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import xlong.backuper.manager.BackupManager;

/**
 * Initialize.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 *
 */
public final class Init {
	/**
	 * Constructor.
	 */
	private Init() {
		
	}
	/**
	 * run.
	 * @param args args
	 */
	public static void main(final String[] args) {
		Path path = BackupManager.getPath();
		try {
			Files.deleteIfExists(path);
		} catch (IOException e) {
			e.printStackTrace();
		}
		BackupManager.getManager();

	}

}
