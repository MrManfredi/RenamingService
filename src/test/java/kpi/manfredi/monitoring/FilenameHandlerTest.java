package kpi.manfredi.monitoring;

import kpi.manfredi.tags.map.Tag;
import kpi.manfredi.tags.map.TagsMap;
import org.junit.Test;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.*;

public class FilenameHandlerTest {

    @Test
    public void assembleString() {
        List<Tag> tags = getTestTagsForAssembleString();
        FilenameHandler filenameHandler = new FilenameHandler(getTagsMapForAssembleString());
        try {
            Method method = FilenameHandler.class.getDeclaredMethod("assembleString", List.class);
            method.setAccessible(true);
            String result1 = (String) method.invoke(filenameHandler, tags);
            assertEquals("#fruit #test #OrdinalTag #the_word", result1);

            // when tags list is empty
            String result2 = (String) method.invoke(filenameHandler, new ArrayList<Tag>());
            assertEquals("#tagme", result2);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void handleFilename() {
        TagsMap tagsMap = getTagsMapForHandleFilename(); // #animal #animal_ears #test #NiceOrdinalTag #yare_yare_daze
        FilenameHandler filenameHandler = new FilenameHandler(tagsMap);

        // when the text consists of ordinary words
        String result1 = filenameHandler.handleFilename(
                "first_dog_s+e-+c(_on)d.cat w_o r -l)d_ign.ore-this.text+_third(bird)test");
        assertEquals("#animal #test #NiceOrdinalTag #yare_yare_daze", result1);

        // when name doesn't contain any alias
        String result2 = filenameHandler.handleFilename("This text has no any alias");
        assertEquals("#tagme", result2);

        // when the text consists of both tags and ordinary words
        String result3 = filenameHandler.handleFilename("first #test word #nonexistent_cat");
        assertEquals("#animal #test #NiceOrdinalTag", result3);

        // when the text consists of complex aliases
        String result4 = filenameHandler.handleFilename("test cat_ears");
        assertEquals("#animal_ears #test", result4);

    }

    @Test
    public void handleFile() {
        TagsMap tagsMap = getTagsMapForHandleFilename();
        FilenameHandler filenameHandler = new FilenameHandler(tagsMap);
        String startName = "first_s+e-+c(_on)d.cat w_o r -l)d_igno.re-this.text+_third(test)bird_";
        String expectedName = "#animal #test #NiceOrdinalTag #yare_yare_daze";
        String expectedType = ".txt";

        // test when file with expected name doesn't exists
        File file = getTempFile(startName, expectedType);
        deleteFileWithExpectedNameIfExists(
                file.getPath().replace(file.getName(), ""),
                expectedName,
                expectedType); // delete file with expected name so that the method handleFile() can create it
        File handledFile = getHandledFile(filenameHandler, file);
        assertEquals(expectedName + expectedType, handledFile.getName());

        // test when file with expected name already exists
        File secondFile = getTempFile(startName, expectedType);
        File secondHandledFile = getHandledFile(filenameHandler, secondFile);
        String secondHandledFilename =
                secondHandledFile.getName().substring(0, secondHandledFile.getName().lastIndexOf('.'));
        assertTrue(secondHandledFilename.matches(expectedName + "[ \\d]*"));

        // test when file to handle doesn't exists
        File nonExistentFile = new File("This file should not exist");
        if (nonExistentFile.exists()) {
            if (!nonExistentFile.delete()) {
                fail("Unable to delete a file that should not exist");
            }
        }
        try {
            filenameHandler.handleFile(nonExistentFile);
        } catch (FileNotFoundException e) {
            // as expected
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    private TagsMap getTagsMapForHandleFilename() {
        TagsMap tagsMap = new TagsMap();
        tagsMap.getTag().addAll(getTestTagsForHandleFilename());
        return tagsMap;
    }

    private List<Tag> getTestTagsForHandleFilename() {

        Tag tag1 = new Tag();
        tag1.setName("#NiceOrdinalTag");
        tag1.getAlias().addAll(Arrays.asList("first", "s_e_c_on_d", "third"));
        tag1.setPriority((byte) 82);

        Tag tag2 = new Tag();
        tag2.setName("#yare_yare_daze");
        tag2.getAlias().add("w_o_r_l_d");
        tag2.setPriority((byte) 90);

        Tag tag3 = new Tag();
        tag3.setName("#test");
        tag3.getAlias().add("test");

        Tag tag4 = new Tag();
        tag4.setName("#animal");
        tag4.getAlias().addAll(Arrays.asList("dog", "cat", "bird"));
        tag4.setPriority((byte) 17);

        Tag tag5 = new Tag();
        tag5.setName("#animal_ears");
        tag5.getAlias().addAll(Arrays.asList("dog_ears", "cat_ears"));
        tag5.setPriority((byte) 25);

        return Arrays.asList(tag1, tag2, tag3, tag4, tag5);
    }

    private TagsMap getTagsMapForAssembleString() {
        TagsMap tagsMap = new TagsMap();
        tagsMap.getTag().addAll(new HashSet<>(getTestTagsForAssembleString()));
        return tagsMap;
    }

    private List<Tag> getTestTagsForAssembleString() {
        Tag tag1 = new Tag();
        tag1.setName("#OrdinalTag");
        tag1.getAlias().addAll(Arrays.asList("first", "s_e_c_on_d", "third"));
        tag1.setPriority((byte) 90);

        Tag tag2 = new Tag();
        tag2.setName("#the_word");
        tag2.getAlias().add("w_o_r_d");
        tag2.setPriority((byte) 100);

        Tag tag3 = new Tag();
        tag3.setName("#test");
        tag3.getAlias().add("test");

        Tag tag4 = new Tag();
        tag4.setName("#fruit");
        tag4.getAlias().add("cherry");
        tag4.setPriority((byte) 10);

        return Arrays.asList(tag1, tag2, tag3, tag2, tag1, tag4, tag3);
    }

    /**
     * This method is used to delete file with expected name if it exists
     *
     * @param dir          directory
     * @param expectedName expected name of file
     * @param expectedType expected type of file
     */
    private void deleteFileWithExpectedNameIfExists(String dir, String expectedName, String expectedType) {
        try {
            Files.deleteIfExists(Paths.get(dir + expectedName + expectedType));
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    /**
     * This method is used to create temp file
     *
     * @param prefix       The prefix string to be used in generating the file's name;
     *                     must be at least three characters long
     * @param expectedType The suffix string to be used in generating the file's name;
     *                     may be null, in which case the suffix ".tmp" will be used
     * @return An abstract pathname denoting a newly-created empty file
     */
    private File getTempFile(String prefix, String expectedType) {
        File file = null;
        try {
            file = File.createTempFile(prefix, expectedType);
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return file;
    }

    private File getHandledFile(FilenameHandler filenameHandler, File file) {
        File newFile = null;
        try {
            newFile = filenameHandler.handleFile(file);
            newFile.deleteOnExit();
        } catch (IOException e) {
            fail(e.getMessage());
        }
        return newFile;
    }
}
