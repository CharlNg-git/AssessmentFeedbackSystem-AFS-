package util;

public interface PathProvider {

    // Return the base database directory path
    String getDatabasePath();

    // Return full path for a specific database file
    String getFilePath(String fileName);
}
