package no.entur.uttu.export.netex;

import no.entur.uttu.export.model.ExportException;
import org.apache.commons.io.FileUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Build zip file.
 */
public class DataSetProducer implements Closeable {

    private static final String DATA_SET_FILE = "dataset.zip";

    private static final String DATA_SET_CONTENT_FOLDER = "content";

    private Path tmpFolder;

    private Path contentFolder;

    public DataSetProducer(String workingFolder) {
        try {


            tmpFolder = Files.createDirectories(Paths.get(workingFolder, String.valueOf(System.currentTimeMillis())));
            contentFolder = Files.createDirectory(tmpFolder.resolve(DATA_SET_CONTENT_FOLDER));
        } catch (IOException ioe) {
            throw new ExportException("Failed to create working folder for producing data set: " + ioe.getMessage(), ioe);
        }
    }

    public OutputStream addFile(String fileName) {
        try {
            return Files.newOutputStream(contentFolder.resolve(fileName));
        } catch (IOException ioe) {
            throw new ExportException("Failed add file to working folder: " + fileName + ", msg: " + ioe.getMessage(), ioe);
        }
    }

    public InputStream buildDataSet() {
        try {

            File dataSetFile = zipFilesInFolder(contentFolder, DATA_SET_FILE);
            return new FileInputStream(dataSetFile);
        } catch (IOException ioe) {
            throw new ExportException("Failed to build data set: " + ioe.getMessage(), ioe);
        }
    }


    // Find impl in lib?
    private File zipFilesInFolder(Path folder, String targetFilePath) throws IOException {
        FileOutputStream out = new FileOutputStream(new File(targetFilePath));
        ZipOutputStream outZip = new ZipOutputStream(out);

        Files.walk(folder).filter(Files::isRegularFile).forEach(path -> addToZipFile(path, outZip));

        outZip.close();
        out.close();

        return new File(targetFilePath);
    }

    private void addToZipFile(Path file, ZipOutputStream zos) {
        try {
            InputStream fis = Files.newInputStream(file);
            ZipEntry zipEntry = new ZipEntry(file.getFileName().toString());
            zos.putNextEntry(zipEntry);

            byte[] bytes = new byte[1024];
            int length;
            while ((length = fis.read(bytes)) >= 0) {
                zos.write(bytes, 0, length);
            }

            zos.closeEntry();
            fis.close();
        } catch (IOException ioe) {
            throw new ExportException("Failed to add file to zip: " + ioe.getMessage(), ioe);
        }
    }

    @Override
    public void close() throws IOException {
        FileUtils.deleteDirectory(tmpFolder.toFile());
    }
}
