package kpi.manfredi.tags;

import kpi.manfredi.tags.map.Tag;
import kpi.manfredi.tags.map.TagsMap;
import kpi.manfredi.utils.FileManipulation;
import org.junit.Test;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

public class TagsHandlerTest {

    @Test
    public void assembleString() {
        List<Tag> tags = getTestTagsForAssembleString();
        TagsHandler tagsHandler = new TagsHandler(getTagsMapForAssembleString());
        try {
            Method method = TagsHandler.class.getDeclaredMethod("assembleString", List.class);
            method.setAccessible(true);
            String result = (String) method.invoke(tagsHandler, tags);
            assertEquals("#fruit #test #OrdinalTag #the_word", result);
        } catch (NoSuchMethodException | IllegalAccessException | InvocationTargetException e) {
            fail(e.getMessage());
        }
    }

    @Test
    public void handleFilename() {
        TagsMap tagsMap = getTagsMapForHandleFilename();
        TagsHandler tagsHandler = new TagsHandler(tagsMap);
        String result = tagsHandler.handleFilename(
                "first_dog_s+e-+c(_on)d.cat w_o r -l)d_ign.ore-this.text+_third(test)bird.txt");
        assertEquals("#animal #test #NiceOrdinalTag #yare_yare_daze", result);
    }

    @Test
    public void handleFile() {
        TagsMap tagsMap = getTagsMapForHandleFilename();

        // todo improve - create file just here
        String name = "/first_s+e-+c(_on)d.cat w_o r -l)d_igno.re-this.text+_third(test)bird.txt";
        URL resource = TagsHandlerTest.class.getResource(
                name);
        if (resource == null) fail("Test file not found");
        File file = null;
        try {
            file = new File(resource.toURI());
        } catch (URISyntaxException e) {
            fail(e.getMessage());
        }


        TagsHandler tagsHandler = new TagsHandler(tagsMap);
        File newFile = null;
        try {
            newFile = tagsHandler.handleFile(file);
        } catch (IOException e) {
            fail(e.getMessage());
        }

        assertEquals("#animal #test #NiceOrdinalTag #yare_yare_daze.txt", newFile.getName());

        // undo renaming
        try {
            FileManipulation.renameFile(newFile, name.substring(1, name.lastIndexOf('.')));
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


        return Arrays.asList(tag1, tag2, tag3, tag4);
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
}
