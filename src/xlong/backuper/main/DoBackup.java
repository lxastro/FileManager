package xlong.backuper.main;

import xlong.backuper.manager.BackupManager;

/**
 * Create new backup. Remember create or modify setting/backup file
 * before run DoBackup. The sample backup file is setting/backup_sample.
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
	 * run.
	 * @param args args
	 */
	public static void main(final String[] args) {
		BackupManager bm = BackupManager.getManager();
		bm.loadBackupSetting();
		bm.newBackup("New Backup");
		bm.createRecoverSetting();
	}
}
