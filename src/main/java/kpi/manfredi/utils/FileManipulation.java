package kpi.manfredi.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.file.FileTypeDirectory;
import javafx.collections.FXCollections;
import javafx.scene.image.Image;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.URI;
import java.net.URL;
import java.nio.file.FileAlreadyExistsException;
import java.nio.file.FileSystemException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static kpi.manfredi.utils.DialogsUtil.showFileNotFoundAlert;
import static kpi.manfredi.utils.MessageUtil.formatMessage;

public abstract class FileManipulation {
    private static final Logger logger = LoggerFactory.getLogger(FileManipulation.class);

    public static List<File> removeDuplicates(List<File> list) {
        return list.stream().distinct().collect(Collectors.toList());
    }

    public static List<File> filterImages(List<File> files) {
        return files.stream().filter(FileManipulation::isImage).collect(Collectors.toList());
    }

    private static boolean isImage(File file) {
        try {
            Metadata metadata = ImageMetadataReader.readMetadata(file);
            FileTypeDirectory fileTypeDirectory = metadata.getFirstDirectoryOfType(FileTypeDirectory.class);
            ArrayList<Tag> tags = new ArrayList<>(fileTypeDirectory.getTags());
            String description = tags.get(0).getDescription();
            switch (description) {
                case "PNG":
                    logger.debug("PNG file. {}", file);
                    break;
                case "JPEG":
                    logger.debug("JPEG file. {}", file);
                    break;
                default:
                    logger.debug("File is not image! {}", file);
                    return false;
            }
            return true;
        } catch (Exception ex) {
            logger.warn(ex.getMessage());
            return false;
        }
    }

    public static List<File> deleteFiles(List<File> fileArrayList) {
        List<File> notDeletedFiles = new ArrayList<>();
        fileArrayList.forEach(file -> {
            if (file.exists()) {
                logger.debug("-- Deleting file " + file);
                boolean isDeleted = file.delete();
                if (isDeleted) {
                    logger.debug("---- File was deleted successfully");
                } else {
                    logger.error("---- Error. The file was not deleted!");
                    notDeletedFiles.add(file);
                }
            } else {
                logger.error("-- File does not exist! " + file);
            }
        });
        return notDeletedFiles;
    }

    public static File convertToFile(Image image) {
        try {
            URL url = new URL(image.getUrl());
            URI uri = url.toURI();
            return new File(uri);
        } catch (Exception e) {
            logger.error(e.getMessage());
            return null;
        }
    }

    /**
     * This method is used to return a format of file
     *
     * @param path path to file
     * @return format of file
     * @throws IOException File format is undefined
     */
    private static String getFormatOfFile(String path) throws IOException {
        int indexOfLastDot = path.lastIndexOf('.');
        if (indexOfLastDot == -1) {
            throw new IOException("File format is undefined! " + path);
        } else {
            return path.substring(indexOfLastDot);
        }
    }

    /**
     * This method is used to rename file
     *
     * @param file    file to rename
     * @param newName new name of file
     * @return renamed file
     * @throws FileAlreadyExistsException file with name 'newName' already exists
     * @throws FileNotFoundException      file not found
     * @throws FileSystemException        file renaming failed
     */
    public static File renameFile(File file, String newName) throws IOException {
        if (file.exists()) {
            String path = file.getPath();
            String format = getFormatOfFile(path);
            String dir = path.replace(file.getName(), "");
            File newFile = new File(dir + newName + format);
            if (newFile.exists()) {
                throw new FileAlreadyExistsException("File with name '" + newName + format + "' already exists!");
            }

            boolean success = file.renameTo(newFile);
            if (!success) {
                throw new FileSystemException("File: " + file + " was not successfully renamed!");
            } else {
                logger.debug("File '{}' was successfully renamed to '{}'", file.getName(), newFile.getName());
                return newFile;
            }
        } else {
            throw new FileNotFoundException("File '" + file + "' not found!");
        }
    }

    /**
     * This method is used to rename a file until it is successfully renamed. If the file with the passed name exists,
     * the method tries to add a number at the end of the name until its name becomes free
     *
     * @param file file to rename
     * @param name new name of file
     * @return renamed file
     * @throws FileNotFoundException file not found
     * @throws FileSystemException   file renaming failed
     */
    public static File renameFileUntilSuccessful(File file, String name) throws IOException {
        if (file.exists()) {
            File resultFile = null;
            int i = 0;
            while (resultFile == null) {
                try {
                    resultFile = FileManipulation.renameFile(
                            file,
                            i == 0 ? name : name + String.format(" %03d", i));
                } catch (FileAlreadyExistsException existsException) {
                    i++;
                }
            }
            return resultFile;
        } else {
            throw new FileNotFoundException("File '" + file + "' not found!");
        }
    }

    public static List<File> renameFilesByTemplate(
            List<File> itemsToRename, String prefix, Integer zeroPad, String postfix) {

        int i = 1;
        List<File> renamedItems = FXCollections.observableArrayList();
        for (File item : itemsToRename) {
            if (item.exists()) {
                try {
                    String path = item.getPath();
                    String format = getFormatOfFile(path);
                    path = path.replace(item.getName(), "");
                    File newFile;
                    do {
                        newFile = new File(path
                                + prefix
                                + String.format("%0" + zeroPad + "d", i++)
                                + postfix
                                + format);
                    } while (newFile.exists());

                    boolean success = item.renameTo(newFile);
                    if (!success) {
                        throw new IOException("File:\n" + item + "\nrenaming was failed!");
                    } else {
                        renamedItems.add(newFile);
                        logger.debug("File \"{}\" was successfully renamed to \"{}\"",
                                item.getName(),
                                newFile.getName());
                    }
                } catch (IOException e) {
                    logger.error(e.getMessage());
                }
            } else {
                showFileNotFoundAlert(item);
            }
        }
        return renamedItems;
    }

    /**
     * This method is used to return copy of resource file (temporary file) by relative path
     * (like {@code '/path/to/file.txt'})
     *
     * @param path path to resource file
     * @return <ui>
     * <li>A temporary copy of the resource extracted from this file if a program is called from
     * {@code *.jar} file</li>
     * <li>Otherwise, the method returns a resource file.</li>
     * </ui>
     * @throws FileNotFoundException file not found
     */
    public static File getResourceFile(String path) throws FileNotFoundException {
        File file;

        URL res = FileManipulation.class.getResource(path);
        if (res == null) throw new FileNotFoundException(formatMessage("file.not.found", path));

        if (res.getProtocol().equals("jar")) {
            file = getCopyOfFile(path);
        } else {
            file = new File(res.getFile()); // it will work from IDE, but not from a JAR
        }

        if (file != null && !file.exists()) {
            throw new FileNotFoundException(formatMessage("file.not.found", file));
        }
        return file;
    }

    /**
     * This method is used to return copy of file (temporary file) by relative path
     *
     * @param path path to resource file
     * @return copy of file
     */
    private static File getCopyOfFile(String path) {
        File file = null;
        try {
            InputStream input = FileManipulation.class.getResourceAsStream(path);
            file = File.createTempFile("tempfile", ".tmp");
            OutputStream out = new FileOutputStream(file);

            int read;
            byte[] bytes = new byte[1024];
            while ((read = input.read(bytes)) != -1) {
                out.write(bytes, 0, read);
            }

            out.close();
            file.deleteOnExit();
        } catch (IOException ex) {
            logger.error(ex.getMessage());
        }
        return file;
    }

    /**
     * This method is used to exclude extension from filename.
     *
     * @param filename filename
     * @return filename without extension
     */
    public static String excludeExtension(String filename) {
        if (filename != null && filename.contains(".")) {
            return filename.substring(0, filename.lastIndexOf('.'));
        } else {
            return filename;
        }
    }
}
