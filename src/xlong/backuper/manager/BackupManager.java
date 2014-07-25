package xlong.backuper.manager;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;

import xlong.backuper.object.Tree;
import xlong.backuper.object.TreeBuilder;
import xlong.backuper.object.TreeRestorer;


/** Creates, deletes and manages backups. */
public final class BackupManager implements Serializable {
		
	/** for serialization. */
	private static final long serialVersionUID = 4562832167527098377L;
	
	/** the directory to store backups. */
	private static String backupDir = "backup";
	
	/** the directory to store settings. */
	private static String settingDir = "setting";
	
	/** default backup setting file. */
	private static String backupSetting = "backup";

	/** default recover setting file. */
	private static String recoverSetting = "recover";
	
	/** the name of the file to store manager. */
	private static String managerFileName = "manager";
	
	/** backup list. */
	private ArrayList<String> backups;
	
	/** current treeBuilder. */
	private TreeBuilder treeBuilder;
	
	/** current treeRestorer. */
	private TreeRestorer treeRestorer;
	
	/**
	 * get treeBuilder.
	 * @return treeBuilder
	 */
	public TreeBuilder getTreeBuilder() {
		return treeBuilder;
	}
	
	/**
	 * set treeBuilder.
	 * @param tb treeBuilder
	 */
	public void setTreeBuilder(final TreeBuilder tb) {
		treeBuilder = tb;
	}
	
	/**
	 * set treeRestorer.
	 * @param tr treeRestorer
	 */
	public void setTreeRestorer(final TreeRestorer tr) {
		treeRestorer = tr;
	}
	
	/** get file path. 
	 * @return the path
	 */
	public static Path getPath() {
		return Paths.get(backupDir + "/" + managerFileName);
	}
	
	/** get file path of backup.
	 * 
	 * @param name backup name
	 * @return the path
	 */
	private static Path getPath(final String name) {
		return Paths.get(backupDir + "/" + name);
	}
	
	/**
	 * Constructor.
	 */
	private BackupManager() {
		try {
			Files.createDirectories(Paths.get(backupDir));
			Files.createDirectories(Paths.get(settingDir));
		} catch (IOException e) {
			e.printStackTrace();
		}
		backups = new ArrayList<String>();
		treeBuilder = new TreeBuilder();
		treeRestorer = treeBuilder.getRestorer();
		save();
	}
	
	/**
	 * load a backup setting file.
	 * @param path filepath
	 * @return success or not
	 */
	public boolean loadBackupSetting(final String path) {
		try {
			treeBuilder = TreeBuilder.create(Paths.get(path));
			treeRestorer = treeBuilder.getRestorer();
			save();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}

	/**
	 * load a backup setting file.
	 * @return success or not
	 */
	public boolean loadBackupSetting() {
		return loadBackupSetting(settingDir + "/" + backupSetting);
	}
	
	/**
	 * create a backup setting file.
	 * @param path filepath
	 * @return success or not
	 */
	public boolean createBackupSetting(final String path) {
		try {
			treeBuilder.restore(Paths.get(path));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * create a backup setting file.
	 * @return success or not
	 */
	public boolean createBackupSetting() {
		return createBackupSetting(settingDir + "/" + backupSetting);
	}
	
	/**
	 * load a recover setting file.
	 * @param path filepath
	 * @return success or not
	 */
	public boolean loadRecoverSetting(final String path) {
		try {
			treeRestorer = TreeRestorer.create(Paths.get(path));
			save();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * load a recover setting file.
	 * @return success or not
	 */
	public boolean loadRecoverSetting() {
		return loadRecoverSetting(settingDir + "/" + recoverSetting);
	}
	
	/**
	 * create a recover setting file.
	 * @param path filepath
	 * @return success or not
	 */
	public boolean createRecoverSetting(final String path) {
		try {
			treeRestorer.restore(Paths.get(path));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		return true;
	}
	
	/**
	 * create a recover setting file.
	 * @return success or not
	 */
	public boolean createRecoverSetting() {
		return createRecoverSetting(settingDir + "/" + recoverSetting);
	}
	
	/**
	 * add new Backup Map.
	 * @param path filepath
	 * @param nick nickpath
	 */
	public void addBackupMap(final String path, final String nick) {
		treeBuilder.add(Paths.get(path), Paths.get(nick));
		treeRestorer.add(Paths.get(nick), Paths.get(path));
		save();
	}
	
	/**
	 * remove a backup map.
	 * @param path filepath
	 */
	public void delBackupMap(final String path) {
		String nick = treeBuilder.delPath(Paths.get(path));
		if (nick != null) {
			treeRestorer.delNick(Paths.get(nick));
		}
		save();
	}
	
	/**
	 * add new default backup Map.
	 * @param path filepath
	 */
	public void addBackupMap(final String path) {
		Path p = Paths.get(path);
		p = p.toAbsolutePath();
		String root = p.getRoot().toString().substring(0, 1) + "/";
		addBackupMap(path, root + p.subpath(1, p.getNameCount()));
		save();
	}
	
	/**
	 * Create new Backup.
	 * @param message the message
	 * @return success or not
	 */
	public boolean newBackup(final String message) {
		Tree tree = null;
		try {
			tree = Tree.create(treeBuilder);
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		try {
			treeBuilder.save();
			tree.save();
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		
		Backup backup = new Backup(treeBuilder.getChecksum(), 
				tree.getChecksum(), message);
		try {
			backup.save(getPath(backup.getDate()));
		} catch (IOException e) {
			e.printStackTrace();
			return false;
		}
		backups.add(backup.getDate());
		save();
		return true;
	}
	
	/**
	 * Gets the manager.
	 * @return the manager
	 */
	public static BackupManager getManager() {
		Path path = getPath();
		if (Files.exists(path)) {
			return load();
		} else {
			return new BackupManager();
		}
	}
	
	/**
	 * gets backup list.
	 * @return backup list
	 */
	public ArrayList<String> getBackupList() {
		return backups;
	}
	
	/**
	 * gets the backup.
	 * @param date the date of the backup
	 * @return the backup
	 */
	public Backup getBackup(final String date) {
		try {
			return Backup.load(getPath(date));
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
			return null;
		}
	}
	
	/**
	 * Show backup in console.
	 * @param date the date of the backup
	 */
	public void showBackup(final String date) {
		System.out.println(getBackup(date).toString());
	}
	
	/**
	 * Recover backup.
	 * @param date the date of the backup
	 * @return success or not
	 */
	public boolean recover(final String date) {
		return getBackup(date).recover(treeRestorer);
	}
	
	/**
	 * Saves the manager to file.
	 * If the directory not exist, this method will create it.
	 * 
	 * @param path the file save to
	 * @throws IOException if an I/O error occurs
	 */
	public void save(final Path path) throws IOException {
		Files.createDirectories(path.getParent());
		FileOutputStream bs = new FileOutputStream(path.toString());
        ObjectOutputStream os = new ObjectOutputStream(bs);   
        os.writeObject(this);
        bs.close();
	}
	
	/**
	 * Loads the manager.
	 * @param path the file to load
	 * @return the manger
	 * @throws IOException if an I/O error occurs
	 * @throws ClassNotFoundException 
	 * the file does not contains a backup instance 
	 */
	public static BackupManager load(final Path path)
			throws IOException, ClassNotFoundException {
		FileInputStream fi = new FileInputStream(path.toString());
        ObjectInputStream oi = new ObjectInputStream(fi);   
        BackupManager bc = (BackupManager) oi.readObject();
        fi.close();
        return bc;
	}
	
	/**
	 * Saves the manager.
	 */
	public void save() {
		try {
			save(getPath());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Loads the manager.
	 * @return the manager
	 */
	public static BackupManager load() {
		try {
			return load(getPath());
		} catch (IOException | ClassNotFoundException e) {
			e.printStackTrace();
			return null;
		}
	}
	
}
