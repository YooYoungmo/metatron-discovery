package app.metatron.discovery.util.csv;

public interface CsvFirstRowMapper<T> {
  T mapRow(String[] firstRow);
}
