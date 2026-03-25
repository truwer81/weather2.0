package com.example;

public enum Cl {
    RED("\u001B[38;5;52m"),
    GREEN("\u001B[38;5;28m "),
    YELLOW("\u001B[38;5;154m "),
    BLUE("\u001B[38;5;12m "),

    REDs(     //tak zapisana wartość ENUM która posiada zdefiniowane argumenty wymaga stworzenia konstruktora
            //który da dostęp do argumentów, konstruktor w ENUM jest zawsze prywatny i jego użycie zachodzi właśnie przy
            //tworzeniu obiektu za pomocą konstruktora
            new String[]{
                    "\u001B[0m",
                    "\u001B[38;5;1m",
                    "\u001B[38;5;9m",
                    "\u001B[38;5;52m",
                    "\u001B[38;5;88m",
                    "\u001B[38;5;124m ",
                    "\u001B[38;5;160m ",
                    "\u001B[38;5;196m ",
                    "\u001B[38;5;167m ",
                    "\u001B[38;5;203m ",
            }
    ),


    GREENs(
            new String[]{
                    "\u001B[0m",
                    "\u001B[38;5;2m ",
                    "\u001B[38;5;10m ",
                    "\u001B[38;5;22m ",
                    "\u001B[38;5;28m ",
                    "\u001B[38;5;34m ",
                    "\u001B[38;5;40m ",
                    "\u001B[38;5;46m ",
                    "\u001B[38;5;76m ",
                    "\u001B[38;5;112m "}
    ),

    YELLOWs(
            new String[]{
                    "\u001B[0m",
                    "\u001B[38;5;3m ",
                    "\u001B[38;5;11m ",
                    "\u001B[38;5;58m ",
                    "\u001B[38;5;154m ",
                    "\u001B[38;5;184m ",
                    "\u001B[38;5;190m ",
                    "\u001B[38;5;226m ",
                    "\u001B[38;5;220m ",
                    "\u001B[38;5;227m "
            }
    ),

    BLUEs(
            new String[]{
                    "\u001B[0m",
                    "\u001B[38;5;4m ",
                    "\u001B[38;5;12m ",
                    "\u001B[38;5;24m ",
                    "\u001B[38;5;27m ",
                    "\u001B[38;5;31m ",
                    "\u001B[38;5;33m ",
                    "\u001B[38;5;37m ",
                    "\u001B[38;5;39m ",
                    "\u001B[38;5;44m "}
    ),

    DBLUEs(
            new String[]{
                    "\u001B[0m",
                    "\u001B[38;5;16m ",
                    "\u001B[38;5;17m ",
                    "\u001B[38;5;18m ",
                    "\u001B[38;5;19m ",
                    "\u001B[38;5;20m ",
                    "\u001B[38;5;21m "}
    ),

    VIOLETs(
            new String[]{
                    "\u001B[0m",
                    "\u001B[38;5;5m ",
                    "\u001B[38;5;53m ",
                    "\u001B[38;5;93m ",
                    "\u001B[38;5;129m ",
                    "\u001B[38;5;162m ",
                    "\u001B[38;5;164m ",
                    "\u001B[38;5;198m ",
                    "\u001B[38;5;201m ",
                    "\u001B[38;5;207m "}
    ),

    GREYs(
            new String[]{
                    "\u001B[0m",
                    "\u001B[38;5;0m ",
                    "\u001B[38;5;232m ",
                    "\u001B[38;5;233m ",
                    "\u001B[38;5;235m ",
                    "\u001B[38;5;239m ",
                    "\u001B[38;5;8m ",
                    "\u001B[38;5;7m ",
                    "\u001B[38;5;242m ",
                    "\u001B[38;5;244m "}
    ),
    RESET(new String[]{"\u001B[0m"});

    private final String[] kod;

    //konstruktor typu String... pozwala użyć tablicy z dowolną liczbą argumentów
    Cl(String... kod) {
        this.kod = kod;
    }   //kod jest tablicą String[] o zmiennej długości (TYP varchar?)

    //metoda get, gettter pola kolor o wartości indeksu index
    public String get(int index) {
        if (index < 0 || index >= kod.length) {
            throw new IndexOutOfBoundsException("Nie ma takiego koloru w palecie");
        }
        return kod[index];
    }

    public String[] getShades() {
        return kod;
    }


    public static void printlnC(Cl Color, int shade, String text) {
        System.out.println(Color.get(shade) + text + Color.get(0));
    }

    public static void printC(Cl Color, int shade, String text) {
        System.out.print(Color.get(shade) + text + Cl.RESET.get(0));
    }
}




