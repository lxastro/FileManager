package xlong.backuper.main;

import java.util.ArrayList;

import xlong.backuper.manager.BackupManager;

/**
 * Recover latest backup. Remember modify setting/recover file
 * before run DoRecover.
 * Set the toRecover to to change the backup to recover.
 * Set the path to change the recover setting path.
 * 
 * @author Xiang Long (longx13@mails.tsinghua.edu.cn)
 *
 */
public final class DoRecover {
	/**
	 * Constructor.
	 */
	private DoRecover() {
		
	}
	
	/**
	 * Recover setting path.
	 * set null to use default.
	 */
	private static String path = "setting/recover_sample";
	
	/** the date of backup to recover.
	 *  set null to get the lastone.  
	 */
	private static String toRecover = null;
	
	/**
	 * run.
	 * @param args args
	 */
	public static void main(final String[] args) {
		BackupManager bm = BackupManager.getManager();
		if (path == null) {
			bm.loadRecoverSetting();
		} else {
			bm.loadRecoverSetting(path);
		}
		ArrayList<String> blist = bm.getBackupList();
		
		if (blist.size() > 0) {
			if (toRecover == null) {
				String last = blist.get(blist.size() - 1);
				bm.recover(last);
			} else {
				bm.recover(toRecover);
			}
		}
	}

}
