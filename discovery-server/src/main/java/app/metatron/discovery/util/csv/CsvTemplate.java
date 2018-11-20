package app.metatron.discovery.util.csv;

import com.univocity.parsers.common.processor.RowListProcessor;
import com.univocity.parsers.csv.CsvParser;
import com.univocity.parsers.csv.CsvParserSettings;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class CsvTemplate {

  private File targetFile;

  public CsvTemplate(File targetFile) {
    this.targetFile = targetFile;
  }

  public <C,R> CsvData<C, R> getData(String lineSep, String delimiter,
                                     CsvFirstRowMapper<C> firstRowMapper,
                                     CsvRowMapper<C, R> rowMapper,
                                     boolean skipFirstRow) {
    CsvParserSettings settings = new CsvParserSettings();
    settings.getFormat().setLineSeparator(lineSep);
    settings.getFormat().setDelimiter(delimiter.charAt(0));

    RowListProcessor rowProcessor = new RowListProcessor();
    settings.setProcessor(rowProcessor);

    CsvParser parser = new CsvParser(settings);
    parser.beginParsing(targetFile);

    C headers = null;
    List<R> rows = new ArrayList<>();
    boolean firstRowFlag = true;
    String[] row;
    while ((row = parser.parseNext()) != null) {
      if(firstRowFlag) {
        headers = firstRowMapper.mapRow(row);
        firstRowFlag = false;
        if(skipFirstRow) {
          continue;
        }
      }
      rows.add(rowMapper.mapRow(headers, row));
    }

    parser.stopParsing();

    return new CsvData<>(headers, rows);
  }
}
