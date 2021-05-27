/*
 * Licensed Materials - Property of IBM
 * 
 * (c) Copyright IBM Corp. 2019.
 */
package dev.galasa.zos.manager.ivt;

import static org.assertj.core.api.Assertions.*;
import java.io.IOException;
import java.nio.file.attribute.PosixFilePermission;
import java.nio.file.attribute.PosixFilePermissions;
import java.util.Set;
import java.util.SortedMap;

import org.apache.commons.logging.Log;

import dev.galasa.ICredentials;
import dev.galasa.ICredentialsUsernamePassword;
import dev.galasa.Test;
import dev.galasa.artifact.BundleResources;
import dev.galasa.artifact.IBundleResources;
import dev.galasa.core.manager.CoreManager;
import dev.galasa.core.manager.CoreManagerException;
import dev.galasa.core.manager.ICoreManager;
import dev.galasa.core.manager.Logger;
import dev.galasa.zos.IZosImage;
import dev.galasa.zos.ZosImage;
import dev.galasa.zosfile.IZosFileHandler;
import dev.galasa.zosfile.ZosFileHandler;
import dev.galasa.zosfile.ZosUNIXFileException;
import dev.galasa.zosfile.IZosUNIXFile;
import dev.galasa.zosfile.IZosUNIXFile.UNIXFileDataType;
import dev.galasa.zosunixcommand.IZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommand;
import dev.galasa.zosunixcommand.ZosUNIXCommandException;

@Test
public class ZosManagerFileIVT {
    
    private final String IMG_TAG = "PRIMARY";
    
    @Logger
    public Log logger;
    
    @ZosImage(imageTag =  IMG_TAG)
    public IZosImage imagePrimary;
        
    @ZosFileHandler
    public IZosFileHandler fileHandler;

    @BundleResources
    public IBundleResources resources; 
    
    @CoreManager
    public ICoreManager coreManager;
    
    @ZosUNIXCommand(imageTag = IMG_TAG)
    public IZosUNIXCommand zosUNIXCommand;

    @Test
    public void preFlightTests() throws Exception {
        // Ensure we have the resources we need for testing
        assertThat(imagePrimary).isNotNull();
        assertThat(fileHandler).isNotNull();
        assertThat(coreManager).isNotNull();
        assertThat(resources).isNotNull();
        assertThat(logger).isNotNull();
        assertThat(imagePrimary.getDefaultCredentials()).isNotNull();
    }
    
    @Test
    public void unixFileCreate() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests file creation using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String userName = ((ICredentialsUsernamePassword) coreManager.getCredentials("ZOS")).getUsername();
        String filePath = "/u/" + userName + "/GalasaTests/fileTest/" + coreManager.getRunName() + "/createMe";
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);

        String commandTestExist = "test -f " + filePath + " && echo \"File Exists\"";
        
        // Check file doesn't exist
        assertThat(unixFile.exists()).isFalse(); // Using fileManager
        assertThat(zosUNIXCommand.issueCommand(commandTestExist)).isEqualTo(""); // Using commandManager
        
        // Create File
        unixFile.create();
        
        // Check file was created
        assertThat(unixFile.exists()).isTrue(); // Using fileManager
        assertThat(zosUNIXCommand.issueCommand(commandTestExist))
            .isEqualToIgnoringWhitespace("File Exists"); // Using commandManager
    }
    
    @Test
    public void unixFileCreateWithPermissions() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests file creation (with Specified Access Permissions) using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String userName = ((ICredentialsUsernamePassword) coreManager.getCredentials("ZOS")).getUsername();
        String fileName = "createMeWithPermissions";
        String filePath = "/u/" + userName + "/GalasaTests/fileTest/" + coreManager.getRunName() + "/" + fileName;
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);

        String commandTestExist = "test -f " + filePath + " && echo \"File Exists\"";
        String commandCheckPermissions = "ls -l " + filePath + " | grep " + fileName;
        
        // Check file doesn't exist
        assertThat(unixFile.exists()).isFalse(); // Using fileManager
        assertThat(zosUNIXCommand.issueCommand(commandTestExist)).isEqualTo(""); // Using commandManager
        
        // Create File With Permissions
        unixFile.create(PosixFilePermissions.fromString("rwxrwxrwx"));
        
        // Check file was created
        assertThat(unixFile.exists()).isTrue(); // Using fileManager
        assertThat(zosUNIXCommand.issueCommand(commandTestExist))
            .isEqualToIgnoringWhitespace("File Exists"); // Using commandManager
        
        // Check file has relevant permissions
        assertThat(unixFile.getAttributesAsString()).contains("Mode=-rwxrwxrwx"); // Using fileManager
        Set<PosixFilePermission> permissions = unixFile.getFilePermissions();
        assertThat(permissions).contains(PosixFilePermission.OWNER_READ);
        assertThat(permissions).contains(PosixFilePermission.OWNER_WRITE);
        assertThat(permissions).contains(PosixFilePermission.OWNER_EXECUTE);
        assertThat(permissions).contains(PosixFilePermission.GROUP_READ);
        assertThat(permissions).contains(PosixFilePermission.GROUP_WRITE);
        assertThat(permissions).contains(PosixFilePermission.GROUP_EXECUTE);
        assertThat(permissions).contains(PosixFilePermission.OTHERS_READ);
        assertThat(permissions).contains(PosixFilePermission.OTHERS_WRITE);
        assertThat(permissions).contains(PosixFilePermission.OTHERS_EXECUTE);
        assertThat(zosUNIXCommand.issueCommand(commandCheckPermissions))
            .startsWith("-rwxrwxrwx"); // Using commandManager
    }
    
    @Test
    public void unixFileDelete() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests file deletion using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String userName = ((ICredentialsUsernamePassword) coreManager.getCredentials("ZOS")).getUsername();
        String filePath = "/u/" + userName + "/GalasaTests/fileTest/" + coreManager.getRunName() + "/deleteMe";
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);

        String commandTestExist = "test -f " + filePath + " && echo \"File Exists\"";
        
        // Create File
        unixFile.create();
        
        // Check file was created
        assertThat(unixFile.exists()).isTrue(); // Using fileManager
        assertThat(zosUNIXCommand.issueCommand(commandTestExist))
            .isEqualToIgnoringWhitespace("File Exists"); // Using commandManager
        
        // Delete File
        unixFile.delete();
        
        // Check file doesn't exist / was deleted
        assertThat(unixFile.exists()).isFalse(); // Using fileManager
        assertThat(zosUNIXCommand.issueCommand(commandTestExist)).isEqualTo(""); // Using commandManager
    }
    
    @Test
    public void unixFileDeleteNonEmptyDirectory() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests directory deletion using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String userName = ((ICredentialsUsernamePassword) coreManager.getCredentials("ZOS")).getUsername();
        String filePath = "/u/" + userName + "/GalasaTests/fileTest/" + coreManager.getRunName() 
            + "/deleteThisDir/ThisToo";
        String deletePath = filePath.substring(0, filePath.indexOf("ThisToo"));
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);
        IZosUNIXFile unixDirectoryToDelete = fileHandler.newUNIXFile(deletePath, imagePrimary);
        
        String commandTestFileExists = "test -f " + filePath + " && echo \"File Exists\"";
        String commandTestDirExists = "test -f " + deletePath + " && echo \"File Exists\"";
        
        // Create File
        unixFile.create();
        
        // Check file was created
        assertThat(unixFile.exists()).isTrue(); // Using fileManager
        assertThat(zosUNIXCommand.issueCommand(commandTestFileExists))
            .isEqualToIgnoringWhitespace("File Exists"); // Using commandManager
        
        // Delete Directory
        unixDirectoryToDelete.directoryDeleteNonEmpty();
        
        // Check that the directory doesn't exist / was deleted
        assertThat(unixFile.exists()).isFalse(); // Using fileManager
        assertThat(unixDirectoryToDelete.exists()).isFalse(); // Using fileManager
        assertThat(zosUNIXCommand.issueCommand(commandTestFileExists)).isEqualTo(""); // Using commandManager
        assertThat(zosUNIXCommand.issueCommand(commandTestDirExists)).isEqualTo(""); // Using commandManager
    }
    
    @Test
    public void unixFileListDirectories() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests directory listing using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String userName = ((ICredentialsUsernamePassword) coreManager.getCredentials("ZOS")).getUsername();
        String dirPath = "/u/" + userName + "/GalasaTests/fileTest/" + coreManager.getRunName() + "/testDir/";
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(dirPath, imagePrimary);
        
        String commandCreateDirectories = "mkdir -p " + dirPath + "dirFoo";
        String commandCreateFiles = "touch " + dirPath + "fileBar";
        
        // Create Directory / File
        assertThat(zosUNIXCommand.issueCommand(commandCreateDirectories)).isEmpty();
        assertThat(zosUNIXCommand.issueCommand(commandCreateFiles)).isEmpty();

        // List files
        SortedMap<String, IZosUNIXFile> fileMap = unixFile.directoryList();
        assertThat(fileMap.containsKey(dirPath + "dirFoo"));
        assertThat(fileMap.containsKey(dirPath + "fileBar"));
        assertThat(fileMap.get(dirPath + "dirFoo").isDirectory()).isTrue();
        assertThat(fileMap.get(dirPath + "dirFoo").getUnixPath()).isEqualTo(dirPath + "dirFoo");
        assertThat(fileMap.get(dirPath + "fileBar").isDirectory()).isFalse();
        assertThat(fileMap.get(dirPath + "fileBar").getUnixPath()).isEqualTo(dirPath + "fileBar");
    }
    
    @Test
    public void unixFileListDirectoriesRecursively() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests recursive directory listing using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String userName = ((ICredentialsUsernamePassword) coreManager.getCredentials("ZOS")).getUsername();
        String dirPath = "/u/" + userName + "/GalasaTests/fileTest/" + coreManager.getRunName() + "/testRecDir/";
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(dirPath, imagePrimary);
        
        String commandCreateDirectories = "mkdir -p " + dirPath + "dirFoo " + dirPath + "dirBar";
        String commandCreateFiles = "touch " + dirPath + "dirFoo/fileFoo "  + dirPath + "dirBar/fileBar" ;
        
        // Create Directory / File
        zosUNIXCommand.issueCommand(commandCreateDirectories);
        zosUNIXCommand.issueCommand(commandCreateFiles);
                
        // List files
        SortedMap<String, IZosUNIXFile> fileMap = unixFile.directoryListRecursive();
        
        assertThat(fileMap.containsKey(dirPath + "dirFoo")).isTrue();
        assertThat(fileMap.containsKey(dirPath + "dirFoo/fileFoo")).isTrue();
        assertThat(fileMap.containsKey(dirPath + "dirBar")).isTrue();
        assertThat(fileMap.containsKey(dirPath + "dirBar/fileBar")).isTrue();
        
        assertThat(fileMap.get(dirPath + "dirFoo").getUnixPath()).isEqualTo(dirPath + "dirFoo");
        assertThat(fileMap.get(dirPath + "dirFoo/fileFoo").getUnixPath()).isEqualTo(dirPath + "dirFoo/fileFoo");
        assertThat(fileMap.get(dirPath + "dirBar").getUnixPath()).isEqualTo(dirPath + "dirBar");
        assertThat(fileMap.get(dirPath + "dirBar/fileBar").getUnixPath()).isEqualTo(dirPath + "dirBar/fileBar");
    }
    
    @Test
    public void unixFileGetAttributes() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests reading file attributes using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String userName = ((ICredentialsUsernamePassword) coreManager.getCredentials("ZOS")).getUsername();
        String filePath = "/u/" + userName + "/GalasaTests/fileTest/" + coreManager.getRunName() + "/attributes";
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);
        
        // Create File With Permissions (to ensure permissions are known)
        unixFile.create(PosixFilePermissions.fromString("rwxrwxrwx"));
        
        // Test Attributes
        String attributes = unixFile.getAttributesAsString();
        assertThat(attributes).isNotEmpty();
        assertThat(attributes).contains("Name=" + filePath);
        assertThat(attributes).contains("Type=file");
        assertThat(attributes).contains("Mode=-rwxrwxrwx");
        assertThat(attributes).contains("Size=0");
        assertThat(attributes).containsIgnoringCase("User=" + userName);
    }
    
    @Test
    public void unixFileTypeIsText() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests file type using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String userName = ((ICredentialsUsernamePassword) coreManager.getCredentials("ZOS")).getUsername();
        String filePath = "/u/" + userName + "/GalasaTests/fileTest/" + coreManager.getRunName() + "/textFile";
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);
        
        // Create File
        unixFile.create();
        unixFile.store("Hello World"); // Text
        
        // Test file type
        assertThat(unixFile.getDataType()).isEqualTo(UNIXFileDataType.TEXT);
    }
    
    @Test
    public void unixFileGetDirectoryPath() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests directory path using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String userName = ((ICredentialsUsernamePassword) coreManager.getCredentials("ZOS")).getUsername();
        String filePath = "/u/" + userName + "/GalasaTests/fileTest/" + coreManager.getRunName() + "/childFile";
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);
        
        // Create File
        unixFile.create();
        
        // Test Directory Path
        assertThat(unixFile.getDirectoryPath()).isEqualTo(filePath.substring(0, filePath.indexOf("/childFile")));
    }
    
    @Test
    public void unixFileGetFileName() throws ZosUNIXFileException, ZosUNIXCommandException, CoreManagerException {
        // Tests file name retrieval using ZosFileHandler and UNIX File(s)
        // Establish file name and location
        String userName = ((ICredentialsUsernamePassword) coreManager.getCredentials("ZOS")).getUsername();
        String filePath = "/u/" + userName + "/GalasaTests/fileTest/" + coreManager.getRunName() + "/uniqueFileName";
        IZosUNIXFile unixFile = fileHandler.newUNIXFile(filePath, imagePrimary);
        
        // Create File
        unixFile.create();
        
        // Test file name
        assertThat(unixFile.getFileName()).isEqualTo("uniqueFileName");
    }
}
