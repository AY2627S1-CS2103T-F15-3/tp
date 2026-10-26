package seedu.address.commons.util;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static seedu.address.testutil.Assert.assertThrows;

import java.io.IOException;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import seedu.address.commons.exceptions.DataLoadingException;
import seedu.address.testutil.SerializableTestClass;
import seedu.address.testutil.TestUtil;

/**
 * Tests JSON Read and Write
 */
public class JsonUtilTest {

    private static final Path SERIALIZATION_FILE = TestUtil.getFilePathInSandboxFolder("serialize.json");

    @TempDir
    public Path temporaryFolder;

    @Test
    public void readJsonFile_nullRoot_throwsDataLoadingExceptionWithoutChangingFile() throws IOException {
        Path filePath = temporaryFolder.resolve("null.json");
        FileUtil.writeToFile(filePath, "null");

        assertThrows(DataLoadingException.class, () -> JsonUtil.readJsonFile(filePath, SerializableTestClass.class));
        assertEquals("null", FileUtil.readFromFile(filePath));
    }

    @Test
    public void readJsonFile_missingFile_returnsEmpty() throws DataLoadingException {
        assertFalse(JsonUtil.readJsonFile(temporaryFolder.resolve("missing.json"), SerializableTestClass.class)
                .isPresent());
    }

    @Test
    public void readJsonFile_validObject_returnsObject() throws Exception {
        Path filePath = temporaryFolder.resolve("valid.json");
        FileUtil.writeToFile(filePath, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass result = JsonUtil.readJsonFile(filePath, SerializableTestClass.class).orElseThrow();

        assertEquals(SerializableTestClass.getNameTestValue(), result.getName());
        assertEquals(SerializableTestClass.getListTestValues(), result.getListOfLocalDateTimes());
        assertEquals(SerializableTestClass.getHashMapTestValues(), result.getMapOfIntegerToString());
    }

    @Test
    public void serializeObjectToJsonFile_noExceptionThrown() throws IOException {
        SerializableTestClass serializableTestClass = new SerializableTestClass();
        serializableTestClass.setTestValues();

        JsonUtil.serializeObjectToJsonFile(SERIALIZATION_FILE, serializableTestClass);

        assertEquals(FileUtil.readFromFile(SERIALIZATION_FILE), SerializableTestClass.JSON_STRING_REPRESENTATION);
    }

    @Test
    public void deserializeObjectFromJsonFile_noExceptionThrown() throws IOException {
        FileUtil.writeToFile(SERIALIZATION_FILE, SerializableTestClass.JSON_STRING_REPRESENTATION);

        SerializableTestClass serializableTestClass = JsonUtil
                .deserializeObjectFromJsonFile(SERIALIZATION_FILE, SerializableTestClass.class);

        assertEquals(serializableTestClass.getName(), SerializableTestClass.getNameTestValue());
        assertEquals(serializableTestClass.getListOfLocalDateTimes(), SerializableTestClass.getListTestValues());
        assertEquals(serializableTestClass.getMapOfIntegerToString(), SerializableTestClass.getHashMapTestValues());
    }

    //TODO: @Test jsonUtil_readJsonStringToObjectInstance_correctObject()

    //TODO: @Test jsonUtil_writeThenReadObjectToJson_correctObject()
}
