package kpi.manfredi.monitoring;

import kpi.manfredi.tags.TagsAdapter;
import kpi.manfredi.tags.map.Tag;
import kpi.manfredi.tags.map.TagsMap;
import kpi.manfredi.utils.FileManipulation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.*;
import java.util.stream.Collectors;

public class FilenameHandler {
    private final HashMap<String, Tag> reversedTagsMap;

    public FilenameHandler(TagsMap tagsMap) {
        reversedTagsMap = TagsAdapter.getReversedTagsMap(tagsMap);
    }

    /**
     * This method is used to rename file by changing certain words or phrases into tags in the correct order
     *
     * @param file file to rename
     * @throws FileNotFoundException file not found
     * @throws FileSystemException   file renaming failed
     */
    public File handleFile(File file) throws IOException {
        String name = file.getName();
        if (file.exists()) {
            name = name.substring(0, name.lastIndexOf('.'));
            name = handleFilename(name);
            return FileManipulation.renameFileUntilSuccessful(file, name);
        } else {
            throw new FileNotFoundException("File " + name + " not found!");
        }
    }

    /**
     * This method is used to transform filename by changing certain words or phrases into tags in the correct order
     *
     * @param filename name of file
     * @return transformed filename
     */
    public String handleFilename(String filename) {

        List<Tag> resultList = new ArrayList<>();
        List<String> elements = new ArrayList<>(List.of(filename.split("[ _+\\-().,#]+")));

        while (!elements.isEmpty()) {
            Tag resultTag = extractLongestTag(elements);
            if (resultTag != null) {
                resultList.add(resultTag);
            } else {
                elements.remove(0);
            }
        }

        return assembleString(resultList);
    }

    /**
     * This method is used to find longer tag which consist from elements with indices ranging from {@code 0} to
     * {@code indexOfLastElement}, remove those elements and return that tag.
     *
     * @param elements list of elements
     * @return longest tag
     */
    private Tag extractLongestTag(List<String> elements) {
        Integer indexOfLastElement = null;
        int j = 1;
        Tag resultTag = null;
        while (j <= elements.size()) {
            Tag tempTag = reversedTagsMap.get(String.join("_", elements.subList(0, j)));
            if (tempTag != null) {
                resultTag = tempTag;
                indexOfLastElement = j;
            }
            j++;
        }
        if (indexOfLastElement != null) {
            elements.removeAll(elements.subList(0, indexOfLastElement));
        }
        return resultTag;
    }

    /**
     * This method is used to retrieve a string from a list of tags in the correct order
     *
     * @param tags list of tags
     * @return string from tags; "{@code #tagme}" string when list is empty
     */
    private String assembleString(List<Tag> tags) {
        if (tags == null || tags.isEmpty()) {
            return "#tagme";
        }

        HashSet<Tag> uniqueTags = new HashSet<>(tags);
        List<Tag> sortedTags = uniqueTags.stream()
                .sorted(Comparator.comparingInt(Tag::getPriority))
                .collect(Collectors.toList());
        return sortedTags.stream().map(Tag::getName).collect(Collectors.joining(" "));
    }
}
