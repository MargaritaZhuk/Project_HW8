package tests;

import com.codeborne.pdftest.PDF;
import com.codeborne.xlstest.XLS;
import com.opencsv.CSVReader;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

public class FilesTest {
    private final ClassLoader cl = FilesTest.class.getClassLoader();
    private static final String ARCHIVE_NAME = "Archive.zip";

    private InputStream openFileInZip(String archiveName, String fileName) throws Exception {
        ZipInputStream zis = new ZipInputStream(Objects.requireNonNull(cl.getResourceAsStream(archiveName)));
        ZipEntry entry;
        while ((entry = zis.getNextEntry()) != null) {
            String name = entry.getName();
            if (name.startsWith("__MACOSX/") || name.contains("/._") || entry.isDirectory()) continue;
            if (fileName.equals(name)) {
                return zis;
                /** вызывая данный метод нужно обязательно закрыть stream */
            }
        }
        zis.close();
        throw new FileNotFoundException("Файл " + fileName + " не найден в архиве " + archiveName);
    }

    @Test
    @DisplayName("Архив не пустой")
    void zipFileNotEmptyTest() throws Exception {

        try (ZipInputStream zis = new ZipInputStream(Objects.requireNonNull(cl.getResourceAsStream(ARCHIVE_NAME)))) {

            ZipEntry entry;

            int numberOfFiles = 0;

            while ((entry = zis.getNextEntry()) != null) {
                String name = entry.getName();
                if (name.startsWith("__MACOSX/") || name.contains("/._")) continue;
                numberOfFiles++;
            }
            Assertions.assertTrue(numberOfFiles > 0);
        }
    }


    @Test
    @DisplayName("Искомые файлы содержатся в архиве")
    void zipFileContainsSearchedFilesTest() {
        Set<String> searchedFiles = Set.of("FileXLS.xls", "realFileCsv.csv", "someInstructionPDF.pdf");

        Set<String> notFound = new HashSet<>();

        for (String fileName : searchedFiles) {
            try (InputStream is = openFileInZip(ARCHIVE_NAME, fileName)) {
            } catch (FileNotFoundException e) {
                notFound.add(fileName);
            } catch (Exception e) {
                Assertions.fail("Ошибка при поиске файла " + fileName + ": " + e.getMessage());
            }
        }
        Assertions.assertTrue(notFound.isEmpty(), "В архиве не найдены файлы: " + notFound);
    }

    @Test
    @DisplayName("Проверяем количество страниц в PDF")
    void pdfFileFromZipTest() throws Exception {
        try (InputStream is = openFileInZip(ARCHIVE_NAME, "someInstructionPDF.pdf")) {
            PDF pdf = new PDF(is);
            Assertions.assertEquals(4, pdf.numberOfPages, "Число страниц не совпадает");
            Assertions.assertTrue(pdf.text.contains("Руководство по установке"), "Текст не найден");
        }
    }

    @Test
    @DisplayName("Проверяем содержимое Excel")
    void excelFileFromZipTest() throws Exception {
        try (InputStream is = openFileInZip(ARCHIVE_NAME, "FileXLS.xls")) {
            XLS xls = new XLS(is);
            String actualValue = xls.excel.getSheetAt(0).getRow(0).getCell(0).getStringCellValue();
            String actualValue2 = xls.excel.getSheetAt(0).getRow(1).getCell(1).getStringCellValue();
            Assertions.assertEquals("column A row 1", actualValue);
            Assertions.assertEquals("info 2", actualValue2);
        }
    }

    @Test
    @DisplayName("Проверяем содержимое CSV")
    void csvFileFromZipTest() throws Exception {
        try (InputStream is = openFileInZip(ARCHIVE_NAME, "realFileCsv.csv")) {

            CSVReader csvReader = new CSVReader(new InputStreamReader(is, StandardCharsets.UTF_8));
            List<String[]> data = csvReader.readAll();
            String[] actual = data.get(0);
            for (int i = 0; i < actual.length; i++) {
                actual[i] = actual[i].replace("\uFEFF", "");
            }
            String[] expected = {"2025-10-12", "2025-10-14", "ЧеПогнали?", "6896", "щапотестим", "007237", "nt_take_charge_item", "1", "5", "500", "false"};
            Assertions.assertArrayEquals(expected, data.get(0), "Строка из CSV не соответствует ожидаемой");
        }
    }
}

