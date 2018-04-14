package logger;

import com.diogonunes.jcdp.color.ColoredPrinter;
import com.diogonunes.jcdp.color.api.Ansi;


public class Logger {


    public void stepLog(String step)

    {

        ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
                .foreground(Ansi.FColor.MAGENTA)   //setting format
                .build();
        cp.println(step);
      //  cp.clear();

    }


    public void titleLog(String step)

    {

        ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
                .foreground(Ansi.FColor.WHITE).background(Ansi.BColor.BLACK)   //setting format
                .build();
        cp.print(step + "\n", Ansi.Attribute.BOLD, Ansi.FColor.YELLOW, Ansi.BColor.NONE);
    //    cp.clear();

    }

    public void testLog(String step)

    {

        ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
                   //setting format
                .build();
        cp.print(step + "\n", Ansi.Attribute.BOLD, Ansi.FColor.BLUE, Ansi.BColor.NONE);

        //cp.clear();
    }


    public void testPassed()

    {

        ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
                .foreground(Ansi.FColor.GREEN)   //setting format
                .build();
        cp.println("Passed");

       // cp.clear();
    }


    public void testFailed()

    {

        ColoredPrinter cp = new ColoredPrinter.Builder(1, false)
                .foreground(Ansi.FColor.RED)   //setting format
                .build();
        cp.println("Test Failed");

        cp.clear();
    }



}
