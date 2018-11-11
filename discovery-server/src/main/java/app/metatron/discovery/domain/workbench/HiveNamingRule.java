package app.metatron.discovery.domain.workbench;

public class HiveNamingRule {
  public static String replaceNotAllowedCharacters(String str) {
    return str.replaceAll("[^A-Za-z0-9]", "_");
  }
}
