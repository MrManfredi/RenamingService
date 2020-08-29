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
    TagsMap tags;
    HashMap<String, Tag> tagsMap;

    public TagsHandler(TagsMap tags) {
        this.tags = tags;
        tagsMap = TagsAdapter.getTagsMap(tags);
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

        for (int i = 0; i < elements.length; i++) {
            if (!elements[i].isEmpty()) {
                StringBuilder temp = new StringBuilder(elements[i]);
                for (int j = i + 1; j < elements.length; j++) {
                    if (tagsMap.containsKey(temp.toString())) {
                        resultList.add(tagsMap.get(temp.toString()));
                        i = j - 1;
                        break;
                    } else {
                        temp.append('_').append(elements[j]);
                    }
                }
            }
        }

        return assembleString(resultList);
    }

    /**
     * This method is used to retrieve a string from a list of tags in the correct order
     *
     * @param tags list of tags
     * @return string
     */
    private String assembleString(List<Tag> tags) {
        HashSet<Tag> uniqueTags = new HashSet<>(tags);
        List<Tag> sortedTags = uniqueTags.stream()
                .sorted(Comparator.comparingInt(Tag::getPriority))
                .collect(Collectors.toList());
        return sortedTags.stream().map(Tag::getName).collect(Collectors.joining(" "));
    }
}
