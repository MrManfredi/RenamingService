package kpi.manfredi.monitoring;

import kpi.manfredi.tags.map.Tag;
import kpi.manfredi.tags.map.TagsMap;
import org.junit.BeforeClass;
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
import java.util.List;

import static org.junit.Assert.*;

public class FilenameHandlerTest {

    private static TagsMap tagsMap;
    private static FilenameHandler filenameHandler;

    @BeforeClass
    public static void initTagsMap() {

        Tag tag0 = new Tag();
        tag0.setName("#animal");
        tag0.getAlias().addAll(Arrays.asList("dog", "cat", "bird"));
        tag0.setPriority((byte) 17);

        Tag tag1 = new Tag();
        tag1.setName("#OrdinalTag");
        tag1.getAlias().addAll(Arrays.asList("first", "s_e_c_on_d", "third"));
        tag1.setPriority((byte) 82);

        Tag tag2 = new Tag();
        tag2.setName("#animal_ears");
        tag2.getAlias().addAll(Arrays.asList("dog_ears", "cat_ears"));
        tag2.setPriority((byte) 25);

        Tag tag3 = new Tag();
        tag3.setName("#test");
        tag3.getAlias().add("test");

        Tag tag4 = new Tag();
        tag4.setName("#fruit");
        tag4.getAlias().add("cherry");
        tag4.setPriority((byte) 10);

        Tag tag5 = new Tag();
        tag5.setName("#yare_yare_daze");
        tag5.getAlias().add("w_o_r_l_d");
        tag5.setPriority((byte) 90);

        Tag tag6 = new Tag();
        tag6.setName("#the_word");
        tag6.getAlias().add("w_o_r_d");
        tag6.setPriority((byte) 100);

        Tag tag7 = new Tag();
        tag7.setName("#Word");
        tag7.getAlias().add("w_o_r_d_v_2");
        tag7.setPriority((byte) 58);

        tagsMap = new TagsMap();
        tagsMap.getTag().addAll(Arrays.asList(tag0, tag1, tag2, tag3, tag4, tag5, tag7));
        filenameHandler = new FilenameHandler(tagsMap);
    }

    @Test
    public void assembleString() {
        List<Tag> tags = getTestTagsForAssembleString();
        try {
            Method method = FilenameHandler.class.getDeclaredMethod("assembleString", List.class);
            method.setAccessible(true);
            String result1 = (String) method.invoke(filenameHandler, tags);
            assertEquals("#fruit #animal_ears #test #OrdinalTag", result1);

            // when tags list is empty
            String result2 = (String) method.invoke(filenameHandler, new ArrayList<Tag>());
            assertEquals("#tagme", result2);

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void extractLongestTag() {
        try {

            Method method = FilenameHandler.class.getDeclaredMethod("extractLongestTag", List.class);
            method.setAccessible(true);

            // elements {"cat", "ears"} -> elements {}, Tag (#animal_ears)
            List<String> elements1 = new ArrayList<>(List.of("cat", "ears"));
            Tag result1 = (Tag) method.invoke(filenameHandler, elements1);
            assertEquals(tagsMap.getTag().get(2), result1);
            assertEquals(0, elements1.size());

            // elements {"cat", "something", "ears"} -> elements {"something", "ears"}, Tag (#animal)
            List<String> elements2 = new ArrayList<>(List.of("cat", "something", "ears"));
            Tag result2 = (Tag) method.invoke(filenameHandler, elements2);
            assertEquals(tagsMap.getTag().get(0), result2);
            assertEquals(2, elements2.size());

            // elements {"something", "cat", "ears"} -> elements {"something", "cat", "ears"}, Tag (null)
            List<String> elements3 = new ArrayList<>(List.of("something", "cat", "ears"));
            Tag result3 = (Tag) method.invoke(filenameHandler, elements3);
            assertNull(result3);
            assertEquals(3, elements3.size());

        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void handleFilename() {

        // when the text consists of ordinary words
        String result1 = filenameHandler.handleFilename(
                "first_dog_s+e-+c(_on)d.cat w_o r -l)d_ign.ore-this.text+_third(bird)test");
        assertEquals("#animal #test #OrdinalTag #yare_yare_daze", result1);

        // when name doesn't contain any alias
        String result2 = filenameHandler.handleFilename("This text has no any alias");
        assertEquals("#tagme", result2);

        // when the text consists of both tags and ordinary words
        String result3 = filenameHandler.handleFilename("first #test word #nonexistent_cat");
        assertEquals("#animal #test #OrdinalTag", result3);

        // when the text consists of complex aliases
        String result4 = filenameHandler.handleFilename("test cat_ears");
        assertEquals("#animal_ears #test", result4);

    }

    @Test
    public void handleFile() {
        String startName = "first_s+e-+c(_on)d.cat w_o r -l)d_igno.re-this.text+_third(test)bird_";
        String expectedName = "#animal #test #OrdinalTag #yare_yare_daze";
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

    private List<Tag> getTestTagsForAssembleString() {
        Tag tag1 = tagsMap.getTag().get(1);
        Tag tag2 = tagsMap.getTag().get(2);
        Tag tag3 = tagsMap.getTag().get(3);
        Tag tag4 = tagsMap.getTag().get(4);
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

    /**
     * This method is used to call handleFile() method and ensure the deletion of the resulting file after the tests
     * @param filenameHandler filename handler instance
     * @param file file to be handled
     * @return handled file
     */
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
