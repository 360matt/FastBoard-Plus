# :sparkles: FastBoard-Plus (1.7 - 1.16)

Forked from: FastBoard from MrMicky (https://github.com/MrMicky-FR/FastBoard)   
Please read the original project documentation before this one.  

## :question: Why a fork ?
* I thought my additions might be useful for a few people, I personally use it on my server.  
* it would be too cumbersome to add it to the original project  
* This fork still has some utility  
  
My Discord: ``Matteow#6953``

# :fire: What additional features  ?
The goal of this library is to be able to quickly create a dynamic scoreboard.  
* Each line can have its own refresh times defined by you.
* So you avoid overly repetitive placeholder calls if it's not worth the uptime so many times in such a short time. 
* There is a concept which is "change scoreboard".  
For example, let's say that when players join the server, the main scoreboard is displayed to the players,  
but let's say there's an event or something like that: you can switch to another scoreboard.  
  
# Structure of project:
* ``ScoreBoardAPI.java`` : concerns the API in general (initialization) 
* ``BoardView.java`` : represents and relates to a scoreboard's view 
* ``BoardPlayer.java`` : concerns the scoreboard linked to the player 
  
# How to use ?
## Initializer:
### To activate the service:  
```java
@Override
public void onEnable() {
  ScoreBoardAPI.registerPlugin(this);
}
```  
  
### To enable / disable the server:  
```java
public void onDisable() {
  ScoreBoardAPI.enable();
  ScoreBoardAPI.disable();
}
```  

## Init one instance of BoardPlus:  
```java
public class ExampleBoardTutorial extends BoardView {
    
    public ExampleBoardTutorial () {
        super("name");
        
        /* 
        super("name", true);
        If you want to define thiw view as default                
         */
    }
    
    @Override
    public void init (final BoardPlayer board) {
        // this will be a lambda looped for every player
    }

    @Override
    public void registerYourSchedulersHere () {
        // will only be called once, used for register schedulers.

        schedule(10, board -> {
            // do stuff every 10 ticks, like change line with a external placeholder:
            board.updateLine(5, ExampleEconomyPlugin.get(board.getPlayer()) );
        });
    }
    
    /* optionnaly, basic events */
    @Override public void onMove (final BoardPlayer board) { }
    @Override public void onOnlineChange (final BoardPlayer board) { }
    /* optionnaly, basic events */
}
```  
And now, register it:
```java
ExampleBoardTutorial inst = new ExampleBoardTutorial();
// Instantiate the newly created class.
```   
## Use BoardView:  
### Get existing instance:
```java
BoardView.getInstance( "SomeInstance" );
```  
### Statically all BoardPlayer of a BoardView:
```java
BoardView.updateAllBoards(" NameOfTheBoard ", (board) -> {
    // some stuff with board:
    board.updateTitle("Welcome to Server");
});
```  
### Add player to BoardPlus:  
```java
BoardView jaaj = ...;
jaaj.addPlayer ( Player );
```
### Add a task at any time:
```java
BoardView jaaj = ...;
jaaj.schedule( int TICK, BoardView );
```
### Temporarily hide this scoreboard by another:  
I thought that for a server with jobs,  
you can display the job scoreboard when a task is completed for X seconds and as long as the values change let it display,  
then once the values don't change go back to the scoreboard main.  
  
Here is an example:

```java
public class SomeTest implements Listener {
    BoardView.SchedulingBascule basculeData = new BoardView.SchedulingBascule();

    @EventHandler
    protected void onBlockBreak (final BlockBreakEvent event) {
        basculeData.setValue(" new value ");
        // As long as there is at least one call
        // on this method below 20 * 10 ticks,
        // the special scoreboard will not disappear.
    }


    public void someMethod (final Player player) {
        final BoardView defaultSco = BoardView.getInstance(" someInstance ");
        final BoardView jobsSco = BoardView.getInstance(" someInstance ");


        // add player to special scoreboard
        jobsSco.addPlayer(player);

        // switch to the main scoreboard
        // if the special scoreboard no longer evolves

        // In our example,
        // if the event has not been invoked for 20*10 ticks
        jobsSco.basculeIfUnchanging(20 * 10, player, defaultSco, basculeData);
    }
}
```
### Unregister instance of BoardView:  
```java
BoardView jaaj = ...;
jaaj.unregister();
```
## BoardPlayer:
### Get instance of BoardPlayer:
```java
BoardView bp = BoardView.getPlayer(player);
```
### Remove player from all mechanism:
```java
BoardView.deletePlayer(player);
```
