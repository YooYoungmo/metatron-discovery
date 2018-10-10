package app.metatron.discovery.domain.datasource;

import app.metatron.discovery.common.exception.ErrorCodes;
import app.metatron.discovery.common.exception.MetatronException;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value= HttpStatus.BAD_REQUEST, reason="DataSource Incompatible Error")
public class DataSourceIncompatibleException extends MetatronException {
  public DataSourceIncompatibleException(ErrorCodes code, String message) {
    super(code, message);
  }

  public DataSourceIncompatibleException(ErrorCodes code, String message, Throwable cause) {
    super(code, message, cause);
  }

  public enum DataSourceIncompatibleErrorCodes implements ErrorCodes {
    STATUS_ERROR_CODE("DSI0001"),
    CONNECTION_TYPE_DIFFERENT_CODE("DSI0002"),
    GRANULARITY_DIFFERENT_CODE("DSI0003"),
    FIELD_ERROR_CODE("DSI0004");

    String errorCode;

    DataSourceIncompatibleErrorCodes(String errorCode) {
      this.errorCode = errorCode;
    }

    @Override
    public String getCode() {
      return errorCode;
    }
  }
}
