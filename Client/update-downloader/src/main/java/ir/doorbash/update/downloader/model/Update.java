package ir.doorbash.update.downloader.model;

/**
 * Created by Milad Doorbash on 3/15/16.
 */
public class Update {
    public int versionCode;
    public boolean forceInstall;
    public String lastChanges;
    public int fileSize;
    public String name;
    public String md5;

    public Update(int versionCode, boolean forceInstall, String lastChanges,int fileSize,String name,String md5)
    {
        this.versionCode = versionCode;
        this.forceInstall = forceInstall;
        this.lastChanges = lastChanges;
        this.fileSize = fileSize;
        this.name = name;
        this.md5 = md5;
    }
}
