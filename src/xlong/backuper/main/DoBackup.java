package xlong.backuper.main;

import xlong.backuper.manager.BackupManager;

/**
 * Create new backup. Remember create or modify setting/backup file
 * before run DoBackup. The sample backup file is setting/backup_sample.
 * Set the path to change the backup setting path.
 * Set the message to change the backup message.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 *
 */
public final class DoBackup {
	/**
	 * Constructor.
	 */
	private DoBackup() {
		
	}
	
	/**
	 * Backup setting path.
	 * set null to use default.
	 */
	private static String path = "setting/backup_sample";
	
	/** Message. */
	private static String message = "Sample backup";
	
	/**
	 * run.
	 * @param args args
	 */
	public static void main(final String[] args) {
		BackupManager bm = BackupManager.getManager();
		if (path == null) {
			bm.loadBackupSetting();
		} else {
			bm.loadBackupSetting(path);
		}
		bm.newBackup(message);
	}
}
