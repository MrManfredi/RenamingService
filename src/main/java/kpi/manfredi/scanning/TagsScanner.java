package kpi.manfredi.scanning;

import kpi.manfredi.tags.TagsAdapter;
import kpi.manfredi.tags.TagsCustodian;
import kpi.manfredi.utils.WrongArgumentsException;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.MAX_VALUE;

public abstract class TagsScanner {

    private static boolean recursive;
    private static File dir;
    private static File resultFile;

    /**
     * This method is used to parse the names of files in a directory and return a list of tags.
     *
     * @param path        path to directory
     * @param recursively include sub-folders when {@code true}
     * @return list of tags
     */
    public static Set<String> getTagsFromDirectory(Path path, boolean recursively) {
        List<String> files = null;
        try (Stream<Path> walk = Files.walk(path, recursively ? MAX_VALUE : 1)) {

            files = walk.filter(Files::isRegularFile)
                    .map(x -> x.getFileName().toString()).collect(Collectors.toList());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return parseFiles(files);
    }

    /**
     * This method is used to parse filenames and collect a set of unique tags
     *
     * @param files list of files
     * @return list of tags
     */
    private static Set<String> parseFiles(List<String> files) {
        if (files == null) return null;
        HashSet<String> tags = new HashSet<>();
        for (String filename : files) {
            tags.addAll(parseFilename(filename));
        }
        return tags;
    }

    /**
     * This method is used to parse the filename into tags
     *
     * @param filename name of file
     * @return set of tags
     */
    private static Set<String> parseFilename(String filename) {
        filename = filename.substring(0, filename.lastIndexOf('.'));
        Set<String> tags = Set.of(filename.split(" "));
        tags = tags.stream().filter(e -> e.matches("#[a-zA-Z_\\d]+")).collect(Collectors.toSet());
        return tags;
    }

    /**
     * This method is used to invoke tags scanner. It scan directory, collect tags and save results into file
     *
     * @param args input arguments: <br>
     *             -s [-r] &lt;dir&gt; &lt;file&gt;
     * @return file with results of scanning
     * @throws FileNotFoundException   file not found
     * @throws WrongArgumentsException wrong arguments
     * @throws JAXBException           validation failed
     */
    public static File scan(String[] args) throws FileNotFoundException, WrongArgumentsException, JAXBException {
        handleArguments(args);
        Set<String> tags;

        tags = TagsScanner.getTagsFromDirectory(dir.toPath(), recursive);

        if (tags != null) {
            TagsCustodian.saveTags(TagsAdapter.convertToTagsMap(tags), resultFile);
        }

        return resultFile;
    }

    /**
     * This method is used to parse list of arguments and init this class fields
     * <br><br>
     * Valid input parameters: -a [-r] &lt;dir&gt; &lt;file-save-into&gt;
     *
     * @param args list of arguments
     * @throws FileNotFoundException   file not found
     * @throws WrongArgumentsException file is not directory
     */
    private static void handleArguments(String[] args) throws FileNotFoundException, WrongArgumentsException {
        if (args.length < 3 || !args[0].equals("-s")) {
            throw new WrongArgumentsException();
        } else if (args.length == 3) {
            recursive = false;
            handleDirectory(args[1]);
            resultFile = new File(args[2]);
        } else if (args.length == 4 && args[1].equals("-r")) {
            recursive = true;
            handleDirectory(args[2]);
            resultFile = new File(args[3]);
        } else {
            throw new WrongArgumentsException();
        }
    }

    /**
     * This method is used to init directory and validate it
     *
     * @param path path to directory
     * @throws FileNotFoundException   file not found
     * @throws WrongArgumentsException file is not directory
     */
    private static void handleDirectory(String path) throws FileNotFoundException, WrongArgumentsException {
        dir = new File(path);
        if (dir.exists()) {
            throw new FileNotFoundException("Directory " + dir + " not found");
        }
        if (!dir.isDirectory()) {
            throw new WrongArgumentsException("File " + dir + " is not directory");
        }
    }

}
