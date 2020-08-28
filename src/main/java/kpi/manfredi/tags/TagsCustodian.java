package kpi.manfredi.tags;

import kpi.manfredi.tags.map.TagsMap;
import kpi.manfredi.tags.tree.TagsTree;
import kpi.manfredi.utils.FileManipulation;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import java.io.File;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

import static kpi.manfredi.utils.MessageUtil.formatMessage;

/**
 * This class is used to provide methods to save tags in XML file and read it
 */
public abstract class TagsCustodian {
    private static final String TAGS_TREE_XSD = "/tagsTree.xsd";
    private static final String TAGS_MAP_XSD = "/tagsMap.xsd";
    private static final String TAGS_XML = "tags.xml";

    /**
     * This method is used to parse data from XML file
     *
     * @param xmlFile     file to read from
     * @param targetClass the class of the object to be returned
     * @return {@code targetClass} instance (Empty, if {@code xmlFile} not exists)
     * @throws FileNotFoundException  schema file not found
     * @throws JAXBException          validation failed
     * @throws IllegalAccessException the target class does not have a public default constructor
     */
    public static Object getTags(File xmlFile, Class<?> targetClass)
            throws FileNotFoundException, JAXBException, IllegalAccessException {
        Object tags;
        File schemaFile = FileManipulation.getResourceFile(getSchemaLocation(targetClass));

        if (xmlFile.exists()) {
            try {

                JAXBContext jaxbContext = JAXBContext.newInstance(targetClass);

                Unmarshaller jaxbUnmarshaller = jaxbContext.createUnmarshaller();
                Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                        .newSchema(schemaFile);
                jaxbUnmarshaller.setSchema(schema);
                tags = targetClass.cast(jaxbUnmarshaller.unmarshal(xmlFile));

            } catch (JAXBException | SAXException e) {
                throw new JAXBException(formatExceptionMessage(e, xmlFile.getName()));
            }
        } else {
            tags = getNewInstance(targetClass);
        }

        return tags;
    }

    /**
     * This method is used to parse {@value TAGS_XML} file (that contains categories and tags)
     * and convert to object view
     *
     * @return {@code TagsStorage} instance which is a container of categories and tags.
     * @throws FileNotFoundException schema file not found
     * @throws JAXBException         validation failed
     */
    public static TagsTree getTags() throws FileNotFoundException, JAXBException {
        TagsTree tags;
        try {
            tags = (TagsTree) getTags(new File(TAGS_XML), TagsTree.class);
        } catch (IllegalAccessException iae) {
            // It will never come because TagsStorage class has a public default constructor
            tags = null;
        }
        return tags;
    }

    /**
     * This method is used to save tags to XML file
     *
     * @param tags    instance that contains data
     * @param xmlFile name of file to save into
     * @throws FileNotFoundException schema file not found
     */
    public static void saveTags(Object tags, String xmlFile) throws FileNotFoundException, JAXBException {
        try {

            File file = new File(xmlFile);
            File schemaFile = FileManipulation.getResourceFile(getSchemaLocation(tags.getClass()));

            JAXBContext jaxbContext = JAXBContext.newInstance(tags.getClass());
            Marshaller jaxbMarshaller = jaxbContext.createMarshaller();

            // output pretty printed
            jaxbMarshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            Schema schema = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI)
                    .newSchema(schemaFile);
            jaxbMarshaller.setSchema(schema);

            jaxbMarshaller.marshal(tags, file);

        } catch (JAXBException | SAXException e) {
            throw new JAXBException(formatExceptionMessage(e, xmlFile));
        }
    }

    /**
     * This method is used to return appropriate schema location
     *
     * @param tags instance that contains data
     * @return schema location
     */
    private static String getSchemaLocation(Class<?> tags) {
        String schemaLocation = null;
        if (tags == TagsTree.class) {
            schemaLocation = TAGS_TREE_XSD;
        } else if (tags == TagsMap.class) {
            schemaLocation = TAGS_MAP_XSD;
        }
        return schemaLocation;
    }

    /**
     * This method is used to show alert about exception cause
     *
     * @param e exception
     */
    private static String formatExceptionMessage(Exception e, String xmlFile) {
        String constraintViolation = e.getCause().getMessage().
                substring(e.getCause().getMessage().indexOf(':') + 2);
        return formatMessage("tags.validation.error.header", xmlFile, constraintViolation);
    }

    /**
     * This method is used to create a new {@code targetClass} instance
     *
     * @param targetClass the class of the object to be returned
     * @return new {@code targetClass} instance
     * @throws IllegalAccessException the target class does not have a public default constructor
     */
    private static Object getNewInstance(Class<?> targetClass) throws IllegalAccessException {
        Object tags;
        try {
            tags = targetClass.getDeclaredConstructor().newInstance();
        } catch (InstantiationException | InvocationTargetException
                | NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalAccessException("Target class " + targetClass
                    + "does not have a public default constructor");
        }
        return tags;
    }

}
