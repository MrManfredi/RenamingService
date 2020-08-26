package kpi.manfredi.tags;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.lang.Integer.MAX_VALUE;

public abstract class TagsAnalyzer {

    /**
     * This method is used to parse the names of files in a directory and return a list of tags.
     *
     * @param path        path to directory
     * @param recursively include sub-folders when {@code true}
     * @return list of tags
     */
    public static List<String> getTagsByPath(Path path, boolean recursively) {
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
     * This method is used to parse filenames and collect a list of tags
     *
     * @param files list of files
     * @return list of tags
     */
    private static List<String> parseFiles(List<String> files) {
        if (files == null) return null;
        HashSet<String> tags = new HashSet<>();
        for (String filename : files) {
            tags.addAll(parseFilename(filename));
        }
        return new ArrayList<>(tags);
    }

    /**
     * This method is used to parse the filename into tags
     *
     * @param filename name of file
     * @return list of tags
     */
    private static List<String> parseFilename(String filename) {
        filename = filename.substring(0, filename.lastIndexOf('.'));
        List<String> tags = Arrays.asList(filename.split(" "));
        tags = tags.stream().filter(e -> e.matches("#[a-zA-Z_\\d]+")).collect(Collectors.toList());
        return tags;
    }
}
