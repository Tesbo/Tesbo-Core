package ExtCode;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface ExtCode {
    String value();
}

