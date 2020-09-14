package kpi.manfredi.scanning;

import kpi.manfredi.tags.TagsAdapter;
import kpi.manfredi.tags.TagsCustodian;
import kpi.manfredi.utils.FileManipulation;
import kpi.manfredi.utils.WrongArgumentsException;

import javax.xml.bind.JAXBException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
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
     * @return collection of tags
     * @throws IOException if an I/O error is thrown when accessing the starting file
     */
    public static Collection<String> getTagsFromDirectory(Path path, boolean recursively) throws IOException {
        List<String> files;
        try (Stream<Path> walk = Files.walk(path, recursively ? MAX_VALUE : 1)) {
            files = walk.filter(Files::isRegularFile)
                    .map(x -> x.getFileName().toString()).collect(Collectors.toList());
        }
        return parseFilenames(files);
    }

    /**
     * This method is used to parse filenames and collect a set of unique tags
     *
     * @param filenames collection of filenames
     * @return collection of tags
     */
    public static Collection<String> parseFilenames(Collection<String> filenames) {
        if (filenames == null) {
            return new HashSet<>();
        } else {
            return filenames.stream().flatMap(filename -> parseFilename(filename).stream()).collect(Collectors.toSet());
        }
    }

    /**
     * This method is used to parse the filename into tags
     *
     * @param filename name of file
     * @return collection of tags
     */
    public static Collection<String> parseFilename(String filename) {
        if (filename != null) {
            return Set.of(FileManipulation.excludeExtension(filename).split(" ")).stream()
                    .filter(e -> e.matches("#[a-zA-Z_\\d]+")).collect(Collectors.toSet());
        } else {
            return new HashSet<>();
        }
    }

    /**
     * This method is used to invoke tags scanner. It scan directory, collect tags and save results into file
     *
     * @param args input arguments (-s [-r] &lt;dir&gt; &lt;file&gt;):
     *             <br>-s - scan
     *             <br>-r - recursive
     *             <br>&lt;dir&gt; - path to directory
     *             <br>&lt;file&gt; - path to output file
     * @return file with results of scanning
     * @throws IOException             if an I/O error is thrown when accessing the file
     * @throws WrongArgumentsException wrong arguments
     * @throws JAXBException           validation failed
     */
    public static File scan(String[] args) throws IOException, WrongArgumentsException, JAXBException {
        handleArguments(args);
        Collection<String> tags;
        tags = TagsScanner.getTagsFromDirectory(dir.toPath(), recursive);
        TagsCustodian.saveTags(TagsAdapter.convertToTagsMap(tags), resultFile);
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
        if (args == null || args.length < 3 || !args[0].equals("-s")) {
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
        if (!dir.exists()) {
            throw new FileNotFoundException("Directory \"" + dir + "\" not found");
        }
        if (!dir.isDirectory()) {
            throw new WrongArgumentsException("File \"" + dir + "\" is not directory");
        }
    }

}
