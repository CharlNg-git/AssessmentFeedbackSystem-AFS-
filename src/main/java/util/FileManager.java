package util;

import java.io.File;

public class FileManager implements PathProvider {

    private final String basePath;
    private final String databasePath;

    public FileManager() {
        this.basePath = System.getProperty("user.dir");

        this.databasePath = basePath
                + File.separator + "src"
                + File.separator + "main"
                + File.separator + "resources"
                + File.separator + "Database"
                + File.separator;
    }

    @Override
    public String getDatabasePath() {
        return databasePath;
    }

    @Override
    public String getFilePath(String fileName) {
        return databasePath + fileName;
    }
}
