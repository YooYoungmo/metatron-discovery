package app.metatron.discovery.domain.workbench.hive;

import org.junit.Test;

import static org.assertj.core.api.Assertions.assertThat;


public class HiveNamingRuleTest {

  @Test
  public void replaceNotAllowedCharacters() {
    // given
    String str = "private_test.user";

    // when
    String actual = HiveNamingRule.replaceNotAllowedCharacters(str);


    // then
    assertThat(actual).isEqualTo("private_test_user");
  }

  @Test
  public void replaceNotAllowedCharacters_when() {
    // given
    String str = "private_testuser";

    // when
    String actual = HiveNamingRule.replaceNotAllowedCharacters(str);


    // then
    assertThat(actual).isEqualTo("private_testuser");
  }


}