package kpi.manfredi.scanning;

import kpi.manfredi.tags.TagsAdapter;
import kpi.manfredi.tags.TagsCustodian;
import kpi.manfredi.tags.map.TagsMap;
import kpi.manfredi.utils.WrongArgumentsException;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class TagsScannerTest {
    private static Path testDir1;
    private static Path testDir2;
    private static Path testDir3;
    private static Path testDir4;
    private static Path testFile1_1;
    private static Path testFile1_2;
    private static Path testFile1_3;
    private static Path testFile2_1;
    private static Path testFile3_1;
    private static String resultFilePathname;

    /**
     * This method is used to create temporary files and directories for tests
     */
    @BeforeClass
    public static void initTestData() {
        try {
            testDir1 = Files.createTempDirectory("scanning_test_dir ");
            testDir1.toFile().deleteOnExit();

            testFile1_1 = Files.createTempFile(testDir1, "Fly little #bird fly ", ".txt");
            testFile1_2 = Files.createTempFile(testDir1, "Fly into the #sky ", ".jpg");
            testFile1_3 = Files.createTempFile(testDir1, "One, Two, Three You are #free ", ".tmp");

            testDir2 = Files.createTempDirectory(testDir1, "scanning_testDir2 ");
            testFile2_1 = Files.createTempFile(testDir2, "Tag in #second_layer ", ".png");

            testDir3 = Files.createTempDirectory(testDir2, "scanning_testDir3 ");
            testFile3_1 = Files.createTempFile(testDir3, "Tag in #third_layer ", ".tmp");
            testFile3_1.toFile().deleteOnExit();

            testDir4 = Files.createTempDirectory(testDir3, "scanning_testDir4 ");

            resultFilePathname = testDir1.getParent().toString() + File.separator + "resultFile.xml";

        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void parseFilename_filenameWithoutExtensionConsistsOnlyFromTags_returnCollectionOfTags() {
        Collection<String> expected = Set.of("#tag", "#longer_than_others", "#four");
        Collection<String> result = TagsScanner.parseFilename("#tag #longer_than_others #four");
        assertEquals(expected, result);
    }

    @Test
    public void parseFilename_filenameWithExtensionConsistsOnlyFromTags_returnCollectionOfTags() {
        Collection<String> expected = Set.of("#tag", "#longer_than_others", "#four");
        Collection<String> result = TagsScanner.parseFilename("#tag #longer_than_others #four.png");
        assertEquals(expected, result);
    }

    @Test
    public void parseFilename_filenameWithoutExtensionConsistsFromTagsAndPlainText_returnCollectionOfTags() {
        Collection<String> expected = Set.of("#test", "#four", "#frog", "#flying_fish");
        Collection<String> result = TagsScanner.parseFilename("#flying_fish #test other #frog test_text #four");
        assertEquals(expected, result);
    }

    @Test
    public void parseFilename_filenameWithExtensionConsistsOnlyFromPlainText_returnEmptyCollection() {
        Collection<String> result = TagsScanner.parseFilename("tag longer_than_others four.png");
        assertTrue(result.isEmpty());
    }

    @Test
    public void parseFilename_null_returnEmptyCollection() {
        Collection<String> result = TagsScanner.parseFilename(null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void parseFilenames_filenamesContainsTags_returnCollectionOfTags() {
        Collection<String> expected = Set.of("#test", "#four", "#frog", "#flying_fish");
        List<String> filenames = List.of(
                "text #test file.txt",
                "#flying_fish and #frog image.png",
                "other file 2.tmp",
                "file without extension",
                "bug number #four.kek");
        Collection<String> result = TagsScanner.parseFilenames(filenames);
        assertEquals(expected, result);
    }

    @Test
    public void parseFilenames_filenamesDoesNotContainsTags_returnEmptyCollection() {
        List<String> filenames = List.of(
                "text file.txt",
                "image.png",
                "other file_2.tmp",
                "file without extension");
        Collection<String> result = TagsScanner.parseFilenames(filenames);
        assertTrue(result.isEmpty());
    }

    @Test
    public void parseFilenames_emptyFilenamesList_returnEmptyCollection() {
        List<String> emptyList = new ArrayList<>();
        Collection<String> result = TagsScanner.parseFilenames(emptyList);
        assertTrue(result.isEmpty());
    }

    @Test
    public void parseFilenames_null_returnEmptyCollection() {
        Collection<String> result = TagsScanner.parseFilenames(null);
        assertTrue(result.isEmpty());
    }

    @Test
    public void getTagsFromDirectory_dirWithoutRecursion_returnCollectionOfTags() {
        try {
            Collection<String> expected1 = Set.of("#free", "#sky", "#bird");
            Collection<String> result1 = TagsScanner.getTagsFromDirectory(testDir1, false);
            assertEquals(expected1, result1);

            Collection<String> expected2 = Set.of("#second_layer");
            Collection<String> result2 = TagsScanner.getTagsFromDirectory(testDir2, false);
            assertEquals(expected2, result2);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getTagsFromDirectory_dirWithRecursion_returnCollectionOfTags() {
        try {
            Collection<String> expected1 = Set.of("#free", "#sky", "#bird", "#second_layer", "#third_layer");
            Collection<String> result1 = TagsScanner.getTagsFromDirectory(testDir1, true);
            assertEquals(expected1, result1);

            Collection<String> expected2 = Set.of("#second_layer", "#third_layer");
            Collection<String> result2 = TagsScanner.getTagsFromDirectory(testDir2, true);
            assertEquals(expected2, result2);
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void getTagsFromDirectory_emptyDir_returnEmptyCollection() {
        try {
            Collection<String> result = TagsScanner.getTagsFromDirectory(testDir4, true);
            assertNotNull(result);
            assertTrue(result.isEmpty());
        } catch (IOException e) {
            fail(e.getMessage());
        }
    }

    @Test(expected = IOException.class)
    public void getTagsFromDirectory_nonExistentDir_throwIOException() throws IOException {
        TagsScanner.getTagsFromDirectory(Paths.get("some non existent dir"), true);
    }

    @Test
    public void scan_dirWithoutRecursion_returnFile() {
        try {
            TagsMap expectedMap1 = TagsAdapter.convertToTagsMap(Set.of("#free", "#sky", "#bird"));
            File resultFile1 = TagsScanner.scan(new String[]{"-s", testDir1.toString(), resultFilePathname});
            assertEqualsAdapter(expectedMap1, resultFile1);

            TagsMap expectedMap2 = TagsAdapter.convertToTagsMap(Set.of("#second_layer"));
            File resultFile2 = TagsScanner.scan(new String[]{"-s", testDir2.toString(), resultFilePathname});
            assertEqualsAdapter(expectedMap2, resultFile2);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void scan_dirWithRecursion_returnFile() {
        try {
            TagsMap expectedMap1 = TagsAdapter.convertToTagsMap(
                    Set.of("#free", "#sky", "#bird", "#second_layer", "#third_layer"));
            File resultFile1 = TagsScanner.scan(new String[]{"-s", "-r", testDir1.toString(), resultFilePathname});
            assertEqualsAdapter(expectedMap1, resultFile1);

            TagsMap expectedMap2 = TagsAdapter.convertToTagsMap(Set.of("#second_layer", "#third_layer"));
            File resultFile2 = TagsScanner.scan(new String[]{"-s", "-r", testDir2.toString(), resultFilePathname});
            assertEqualsAdapter(expectedMap2, resultFile2);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void scan_emptyDir_returnFileWithEmptyBody() {
        try {
            File resultFile1 = TagsScanner.scan(new String[]{"-s", "-r", testDir4.toString(), resultFilePathname});
            assertEqualsAdapter(new TagsMap(), resultFile1);
        } catch (Exception e) {
            fail(e.getMessage());
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void scan_nonExistentDir_throwException() throws JAXBException, IOException, WrongArgumentsException {
        TagsScanner.scan(new String[]{"-s", "-r", "some dir", resultFilePathname});
    }

    @Test(expected = IOException.class)
    public void scan_wrongDir_throwException() throws JAXBException, IOException, WrongArgumentsException {
        TagsScanner.scan(new String[]{"-s", "wrong^%;?", resultFilePathname});
    }

    @Test(expected = IOException.class)
    public void scan_wrongFile_throwException() throws JAXBException, IOException, WrongArgumentsException {
        TagsScanner.scan(new String[]{"-s", "some dir", "wrong^%;?"});
    }

    @Test(expected = WrongArgumentsException.class)
    public void scan_wrongFirstArgument_throwException() throws JAXBException, IOException, WrongArgumentsException {
        TagsScanner.scan(new String[]{"-a", "-r", "some dir", resultFilePathname});
    }

    @Test(expected = WrongArgumentsException.class)
    public void scan_wrongRecursionArgument_throwException() throws JAXBException, IOException, WrongArgumentsException {
        TagsScanner.scan(new String[]{"-s", "-t", testDir1.toString(), resultFilePathname});
    }

    @Test(expected = WrongArgumentsException.class)
    public void scan_extraArgument_throwException() throws JAXBException, IOException, WrongArgumentsException {
        TagsScanner.scan(new String[]{"-s", testDir1.toString(), resultFilePathname, "extraArg"});
    }

    @Test(expected = WrongArgumentsException.class)
    public void scan_notEnoughArguments_throwException() throws JAXBException, IOException, WrongArgumentsException {
        TagsScanner.scan(new String[]{"-s", testDir1.toString()});
    }

    @Test(expected = WrongArgumentsException.class)
    public void scan_null_throwException() throws JAXBException, IOException, WrongArgumentsException {
        TagsScanner.scan(null);
    }

    @Test(expected = FileNotFoundException.class)
    public void handleDirectory_nonExistentDir_throwException() throws Exception {
        Method method = TagsScanner.class.getDeclaredMethod("handleDirectory", String.class);
        method.setAccessible(true);
        try {
            method.invoke(TagsScanner.class, "non existent dir");
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw (FileNotFoundException) e.getCause();
            } else {
                throw e;
            }
        }
    }

    @Test(expected = WrongArgumentsException.class)
    public void handleDirectory_fileIsNotDir_throwException() throws Exception {
        Method method = TagsScanner.class.getDeclaredMethod("handleDirectory", String.class);
        method.setAccessible(true);
        try {
            method.invoke(TagsScanner.class, testFile1_1.toString());
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof WrongArgumentsException) {
                throw (WrongArgumentsException) e.getCause();
            } else {
                throw e;
            }
        }
    }

    @Test(expected = FileNotFoundException.class)
    public void handleDirectory_wrongName_throwException() throws Exception {
        Method method = TagsScanner.class.getDeclaredMethod("handleDirectory", String.class);
        method.setAccessible(true);
        try {
            method.invoke(TagsScanner.class, "wrong%name;?");
        } catch (InvocationTargetException e) {
            if (e.getCause() instanceof FileNotFoundException) {
                throw (FileNotFoundException) e.getCause();
            } else {
                throw e;
            }
        }
    }

    /**
     * This method is used to delete temporary files and directories created for tests
     */
    @SuppressWarnings("ResultOfMethodCallIgnored")
    @AfterClass
    public static void deleteTestData() {
        testDir4.toFile().delete();
        testFile3_1.toFile().delete();
        testDir3.toFile().delete();
        testFile2_1.toFile().delete();
        testDir2.toFile().delete();
        testFile1_3.toFile().delete();
        testFile1_2.toFile().delete();
        testFile1_1.toFile().delete();
        testDir1.toFile().delete();

        File resultFile = new File(resultFilePathname);
        resultFile.deleteOnExit();
    }

    /**
     * This method is used to compare expected map with map from file
     *
     * @param expected   expected tags map
     * @param resultFile file that contains tags map
     * @throws Exception fail
     */
    private void assertEqualsAdapter(TagsMap expected, File resultFile)
            throws Exception {
        assertNotNull(resultFile);
        assertTrue(resultFile.exists());
        TagsMap result1 = (TagsMap) TagsCustodian.getTags(resultFile, TagsMap.class);
        assertEquals(expected, result1);
    }
}
