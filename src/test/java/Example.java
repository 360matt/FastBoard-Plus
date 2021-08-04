import fr.i360matt.fastboardplus.BoardView;
import fr.i360matt.fastboardplus.BoardPlayer;

public class Example extends BoardView {

    // init test
    public static void main (final String[] args) {
        new Example();
    }


    public Example () {
        super("event");
    }


    @Override
    public void init (final BoardPlayer board) {
        // init line for first time to a board

        board.updateTitle("Hiii Kyyyyyyylle !!");

        board.updateLine(0, "kenny's fortune: 0$"); // rip kenny

    }

    @Override
    public void registerYourSchedulersHere () {
        // simply execute Consumer lambda every x defined ticks

        // you can also declare variable here

        schedule(10, (board) -> {
            final int KennyFortuneFromDatabase = 0; // example

            board.updateLine(0, "kenny's fortune: " + KennyFortuneFromDatabase + "$");
        });

    }
}
