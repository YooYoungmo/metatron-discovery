package app.metatron.discovery.domain.workbook;

import app.metatron.discovery.domain.datasource.DataSource;
import app.metatron.discovery.domain.datasource.DataSourceIncompatibleException;
import org.junit.Test;

import java.util.Arrays;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;


public class WorkBookTest {

  @Test
  public void changeDataSource() {
    // given
    WorkBook workBook = new WorkBook();
    DashBoard mockDashBoard1 = mock(DashBoard.class);
    DashBoard mockDashBoard2 = mock(DashBoard.class);
    workBook.setDashBoards(Arrays.asList(mockDashBoard1, mockDashBoard2));

    DataSource changeFromDataSource = mock(DataSource.class);
    DataSource changeToDataSource = mock(DataSource.class);

    // when
    workBook.changeDataSource(changeFromDataSource, changeToDataSource);

    // then
    assertThat(workBook.getDashBoards()).hasSize(2);
    verify(mockDashBoard1).changeDataSource(changeFromDataSource, changeToDataSource);
    verify(mockDashBoard2).changeDataSource(changeFromDataSource, changeToDataSource);
  }

  @Test
  public void changeDataSource_when_dashboards_is_null() {
    // given
    WorkBook workBook = new WorkBook();

    DataSource changeFromDataSource = mock(DataSource.class);
    DataSource changeToDataSource = mock(DataSource.class);

    // when
    workBook.changeDataSource(changeFromDataSource, changeToDataSource);

    // then
    assertThat(workBook.getDashBoards()).isNull();
  }

  @Test(expected = DataSourceIncompatibleException.class)
  public void changeDataSource_when_incompatible_datasource() {
    // given
    WorkBook workBook = new WorkBook();

    DataSource changeFromDataSource = mock(DataSource.class);
    DataSource changeToDataSource = mock(DataSource.class);
    doThrow(DataSourceIncompatibleException.class)
        .when(changeFromDataSource)
        .validateCompatibleDatasource(changeToDataSource);

    // when
    workBook.changeDataSource(changeFromDataSource, changeToDataSource);

    // then
    // DataSourceIncompatibleException
  }
}