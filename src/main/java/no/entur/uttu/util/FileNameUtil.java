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

package no.entur.uttu.util;

import no.entur.uttu.export.netex.producer.NetexIdProducer;
import no.entur.uttu.model.FlexibleLine;
import no.entur.uttu.model.Provider;
import no.entur.uttu.model.job.Export;
import org.springframework.util.StringUtils;

import java.text.StringCharacterIterator;
import java.time.format.DateTimeFormatter;

public class FileNameUtil {


    private static final String COMMON_FILE_NAME_SUFFIX = "_flexible_shared_data.xml";

    private static final String MAIN_SEPARATOR = "_";
    private static final String SECONDARY_SEPARATOR = "-";

    private static final String DATE_PATTERN = "yyyyMMdd";

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern(DATE_PATTERN);

    public static String createExportedDataSetFilename(Provider provider) {
        return "rb_" + provider.getCode().toLowerCase() + "-flexible-lines.zip";
    }

    public static String createBackupDataSetFilename(Export export) {
        StringBuilder fileNameBuilder = new StringBuilder(export.getProvider().getCode().toLowerCase());
        fileNameBuilder.append(MAIN_SEPARATOR);

        if (!StringUtils.isEmpty(export.getName())) {
            fileNameBuilder.append(export.getName());
            fileNameBuilder.append(MAIN_SEPARATOR);
        }
        fileNameBuilder.append(export.getFromDate().format(DATE_FORMATTER));
        fileNameBuilder.append(SECONDARY_SEPARATOR);
        fileNameBuilder.append(export.getToDate().format(DATE_FORMATTER));
        fileNameBuilder.append(MAIN_SEPARATOR);
        fileNameBuilder.append(export.getPk());

        fileNameBuilder.append(".zip");
        return fileNameBuilder.toString();
    }

    public static String createCommonFileFilename(Provider provider) {
        return "_" + provider.getCodespace().getXmlns().toUpperCase() + COMMON_FILE_NAME_SUFFIX;
    }

    public static String createLineFilename(FlexibleLine line) {
        StringBuilder b = new StringBuilder();
        b.append(NetexIdProducer.getObjectIdPrefix(line.getNetexId()));
        b.append(MAIN_SEPARATOR);
        b.append(line.getNetexId().replaceAll(":", SECONDARY_SEPARATOR));
        b.append(MAIN_SEPARATOR);
        if (line.getPrivateCode() != null) {
            b.append(line.getPrivateCode().replaceAll(MAIN_SEPARATOR, SECONDARY_SEPARATOR));
            b.append(MAIN_SEPARATOR);
        }
        if (line.getName() != null) {
            b.append(line.getName());
        } else if (line.getPublicCode() != null) {
            b.append(line.getPublicCode());
        }

        return utftoasci(b.toString()).replaceAll("/", SECONDARY_SEPARATOR).replace(" ", SECONDARY_SEPARATOR).replaceAll("\\.", SECONDARY_SEPARATOR) + ".xml";
    }

    // Convert string to ascii, replacing common non ascii chars with replacements (Å => A etc) and omitting the rest.
    private static String utftoasci(String s) {
        final StringBuffer sb = new StringBuffer(s.length() * 2);

        final StringCharacterIterator iterator = new StringCharacterIterator(s);

        char ch = iterator.current();

        while (ch != StringCharacterIterator.DONE) {
            if (Character.getNumericValue(ch) > 0) {
                sb.append(ch);
            } else {

                if (Character.toString(ch).equals("Ê")) {
                    sb.append("E");
                } else if (Character.toString(ch).equals("È")) {
                    sb.append("E");
                } else if (Character.toString(ch).equals("ë")) {
                    sb.append("e");
                } else if (Character.toString(ch).equals("é")) {
                    sb.append("e");
                } else if (Character.toString(ch).equals("è")) {
                    sb.append("e");
                } else if (Character.toString(ch).equals("è")) {
                    sb.append("e");
                } else if (Character.toString(ch).equals("Â")) {
                    sb.append("A");
                } else if (Character.toString(ch).equals("ä")) {
                    sb.append("a");
                } else if (Character.toString(ch).equals("ß")) {
                    sb.append("ss");
                } else if (Character.toString(ch).equals("Ç")) {
                    sb.append("C");
                } else if (Character.toString(ch).equals("Ö")) {
                    sb.append("O");
                } else if (Character.toString(ch).equals("º")) {
                    sb.append("");
                } else if (Character.toString(ch).equals("Ó")) {
                    sb.append("O");
                } else if (Character.toString(ch).equals("ª")) {
                    sb.append("");
                } else if (Character.toString(ch).equals("º")) {
                    sb.append("");
                } else if (Character.toString(ch).equals("Ñ")) {
                    sb.append("N");
                } else if (Character.toString(ch).equals("É")) {
                    sb.append("E");
                } else if (Character.toString(ch).equals("Ä")) {
                    sb.append("A");
                } else if (Character.toString(ch).equals("Å")) {
                    sb.append("A");
                } else if (Character.toString(ch).equals("å")) {
                    sb.append("a");
                } else if (Character.toString(ch).equals("ä")) {
                    sb.append("a");
                } else if (Character.toString(ch).equals("Ü")) {
                    sb.append("U");
                } else if (Character.toString(ch).equals("ö")) {
                    sb.append("o");
                } else if (Character.toString(ch).equals("ü")) {
                    sb.append("u");
                } else if (Character.toString(ch).equals("á")) {
                    sb.append("a");
                } else if (Character.toString(ch).equals("Ó")) {
                    sb.append("O");
                } else if (Character.toString(ch).equals("É")) {
                    sb.append("E");
                } else if (Character.toString(ch).equals("Æ")) {
                    sb.append("E");
                } else if (Character.toString(ch).equals("æ")) {
                    sb.append("e");
                } else if (Character.toString(ch).equals("Ø")) {
                    sb.append("O");
                } else if (Character.toString(ch).equals("ø")) {
                    sb.append("o");
                } else {
                    sb.append(ch);
                }
            }
            ch = iterator.next();
        }
        return sb.toString().replaceAll("[^\\p{ASCII}]", "");
    }
}
