   package com.booklibrary;

   /**
    * Author: Farid Rahmani
    * Contribution: Implemented a plain launcher class so the application can be started from Eclipse.
    * Purpose: starts MainApp without the launcher itself extending
    * javafx.application.Application, which avoids the "JavaFX runtime
    * components are missing" error when run from an IDE.
    */
   public class Launcher {

       /**
        * Delegates to {@link MainApp#main(String[])}.
        *
        * @param args command-line arguments, passed on unchanged
        */
       public static void main(String[] args) {
           MainApp.main(args);
       }
   }