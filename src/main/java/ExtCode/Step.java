package ExtCode;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface Step {
    String value();
}

