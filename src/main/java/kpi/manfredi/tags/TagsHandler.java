package kpi.manfredi.tags;

import kpi.manfredi.tags.map.Tag;
import kpi.manfredi.tags.map.TagsMap;
import kpi.manfredi.utils.FileManipulation;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.FileSystemException;
import java.util.*;
import java.util.stream.Collectors;

public class TagsHandler {
    private final TagsMap tagsMap;
    private final HashMap<String, Tag> reversedTagsMap;

    public TagsHandler(TagsMap tagsMap) {
        this.tagsMap = tagsMap;
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

        String[] elements = filename.split("[ _+\\-().,]+");

        // todo update algorithm of tag matching
        for (int i = 0; i < elements.length; i++) {
            if (!elements[i].isEmpty()) {
                if (reversedTagsMap.containsKey(elements[i])) {
                    resultList.add(reversedTagsMap.get(elements[i]));
                } else if (i + 1 < elements.length) {
                    StringBuilder temp = new StringBuilder(elements[i]);
                    int j = i + 1;
                    do {
                        temp.append('_').append(elements[j++]);
                        if (reversedTagsMap.containsKey(temp.toString())) {
                            resultList.add(reversedTagsMap.get(temp.toString()));
                            i = j - 1;
                            break;
                        }
                    } while (j < elements.length);
                }
            }
        }

        return assembleString(resultList);
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
