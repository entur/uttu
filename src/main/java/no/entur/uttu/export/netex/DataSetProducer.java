/*
 * Licensed under the EUPL, Version 1.2 or – as soon they will be approved by
 * the European Commission - subsequent versions of the EUPL (the "Licence");
 * You may not use this work except in compliance with the Licence.
 * You may obtain a copy of the Licence at:
 *
 *   https://joinup.ec.europa.eu/software/page/eupl
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the Licence is distributed on an "AS IS" basis,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the Licence for the specific language governing permissions and
 * limitations under the Licence.
 */

package no.entur.uttu.export.netex;

import no.entur.uttu.export.model.ExportException;
import org.apache.commons.io.FileUtils;

import java.io.Closeable;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardOpenOption;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Build zip file.
 */
public class DataSetProducer implements Closeable {

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
            File datasetFile = File.createTempFile("dataset", ".zip", contentFolder.toFile());
            zipFilesInFolder(contentFolder, datasetFile);
            return Files.newInputStream(datasetFile.toPath(), StandardOpenOption.DELETE_ON_CLOSE);
        } catch (IOException ioe) {
            throw new ExportException("Failed to build data set: " + ioe.getMessage(), ioe);
        }
    }


    // Find impl in lib?
    private void zipFilesInFolder(Path folder, File targetFile) throws IOException {
        FileOutputStream out = new FileOutputStream(targetFile);
        ZipOutputStream outZip = new ZipOutputStream(out);

        Files.walk(folder).filter(Files::isRegularFile).forEach(path -> addToZipFile(path, outZip));

        outZip.close();
        out.close();
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
