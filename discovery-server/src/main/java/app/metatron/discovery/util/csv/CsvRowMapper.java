package app.metatron.discovery.util.csv;

public interface CsvRowMapper<C, T> {
  T mapRow(C headers, String[] row);
}
