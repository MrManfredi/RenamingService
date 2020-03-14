package kpi.manfredi.utils;

import com.drew.imaging.ImageMetadataReader;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;
import com.drew.metadata.file.FileTypeDirectory;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.scene.image.Image;
import kpi.manfredi.gui.Controller;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import static kpi.manfredi.utils.Dialogs.showFileNotFoundAlert;

public abstract class FileManipulation {
    private static final Logger logger = LoggerFactory.getLogger(Controller.class);

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
                    logger.info("PNG file. {}", file);
                    break;
                case "JPEG":
                    logger.info("JPEG file. {}", file);
                    break;
                default:
                    logger.info("File is not image! {}", file);
                    return false;
            }
            return true;
        } catch (Exception ex) {
            logger.warn(ex.getMessage());
            return false;
        }
    }

    public static ArrayList<File> deleteFiles(ArrayList<File> fileArrayList) {
        ArrayList<File> notDeletedFiles = new ArrayList<>();
        fileArrayList.forEach(file -> {
            if (file.exists()) {
                logger.info("-- Deleting file " + file);
                boolean isDeleted = file.delete();
                if (isDeleted) {
                    logger.info("---- File was deleted successfully");
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

    private static String getFormatOfFile(String path) throws IOException {
        int indexOfLastDot = path.lastIndexOf('.');
        if (indexOfLastDot == -1) {
            throw new IOException("File format is undefined! " + path);
        } else {
            return path.substring(indexOfLastDot);
        }
    }

    public static File renameFile(File item, String newName) throws IOException {
        if (item.exists()) {
            String path = item.getPath();
            String format = getFormatOfFile(path);
            path = path.replace(item.getName(), "");
            File newFile = new File(path + newName + format);
            if (newFile.exists()) {
                throw new IOException("File with name \"" + newName + format + "\" already exists!");
            }

            boolean success = item.renameTo(newFile);
            if (!success) {
                throw new IOException("File:\n" + item + "\nwas not successfully renamed!");
            } else {
                logger.info("File \"{}\" was successfully renamed to \"{}\"", item.getName(), newFile.getName());
                return newFile;
            }
        } else {
            throw new FileNotFoundException("File with name \"" + item + "\" not found!");
        }
    }

    public static ObservableList<File> renameFilesByTemplate(ObservableList<File> itemsToRename, String prefix, String postfix) {
        int i = 1;
        ObservableList<File> renamedItems = FXCollections.observableArrayList();
        for (File item : itemsToRename) {
            if (item.exists()) {
                try {
                    String path = item.getPath();
                    String format = getFormatOfFile(path);
                    path = path.replace(item.getName(), "");
                    File newFile;
                    do {
                        newFile = new File(path + prefix + i++ + postfix + format);
                    } while (newFile.exists());

                    boolean success = item.renameTo(newFile);
                    if (!success) {
                        throw new IOException("File:\n" + item + "\nwas not successfully renamed!");
                    } else {
                        renamedItems.add(newFile);
                        logger.info("File \"{}\" was successfully renamed to \"{}\"", item.getName(), newFile.getName());
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

}
