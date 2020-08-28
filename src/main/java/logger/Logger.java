package logger;

import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi;


public class Logger {

    /**
     *
     * @param step
     */
    public void stepLog(String step)

    {
      ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
                .foreground(Ansi.FColor.GREEN)   //setting format
                .build();
        cp.println(step);
        cp.clear();

    }

    /**
     *
     * @param step
     */
    public void titleLog(String step)

    {
        ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
                  //setting format
                .build();
        cp.println(step, Ansi.Attribute.BOLD, Ansi.FColor.GREEN, Ansi.BColor.NONE);
        cp.clear();

    }

    /**
     *
     * @param step
     */
    public void testLog(String step)
    {
        ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
                .foreground(Ansi.FColor.GREEN)
                .build();
        cp.print(step + "\n",Ansi.Attribute.BOLD, Ansi.FColor.GREEN, Ansi.BColor.NONE);
    }

    /**
     *
     * @param msg
     */
    public void testPassed(String msg)
    {
       ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
                .foreground(Ansi.FColor.GREEN)   //setting format
                .build();
        cp.println(msg,Ansi.Attribute.BOLD, Ansi.FColor.GREEN, Ansi.BColor.NONE);

    }


    /**
     *
     * @param msg
     */
    public void testFailed(String msg)
    {
      ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
                .foreground(Ansi.FColor.RED)   //setting format
                .build();
        cp.println(msg,Ansi.Attribute.BOLD, Ansi.FColor.RED, Ansi.BColor.NONE);

        cp.clear();
    }

    /**
     *
     * @param msg
     * @param fg
     */
    public void customeLog(String msg,Ansi.FColor fg )

    {
       ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
                .foreground(fg)   //setting format
                .build();
        cp.print(msg,Ansi.Attribute.BOLD, fg, Ansi.BColor.NONE);

        cp.clear();
    }


}
