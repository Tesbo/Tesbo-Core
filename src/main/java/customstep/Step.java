package customstep;

import java.lang.annotation.*;

@Retention(RetentionPolicy.RUNTIME)
public @interface Step {
    String value();
}

