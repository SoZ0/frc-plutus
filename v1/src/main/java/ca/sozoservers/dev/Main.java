package ca.sozoservers.dev;

import ca.sozoservers.dev.core.ErrorManager;
import ca.sozoservers.dev.gui.GUIManager;

public class Main 
{
    public static void main( String[] args )
    {
        //h3PHA3stus{#6390/21}
        try {
            Thread.setDefaultUncaughtExceptionHandler(ErrorManager::throwError);
            GUIManager.run(args);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }   
}
    