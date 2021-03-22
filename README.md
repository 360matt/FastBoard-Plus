# FastBoard-Plus (1.7 - 1.16)

Forked from: FastBoard from MrMicky (https://github.com/MrMicky-FR/FastBoard)   
Please read the original project documentation before this one.  

## Why a fork ?
* I thought my additions might be useful for a few people, I personally use it on my server.  
* it would be too cumbersome to add it to the original project  
* This fork still has some utility  

# What additional features  ?
The goal of this library is to be able to quickly create a dynamic scoreboard.  
* Each line can have its own refresh times defined by you.
* So you avoid overly repetitive placeholder calls if it's not worth the uptime so many times in such a short time. 
* There is a concept which is "change scoreboard".  
For example, let's say that when players join the server, the main scoreboard is displayed to the players,  
but let's say there's an event or something like that: you can switch to another scoreboard.  
  
# Structure of project:
* ``ScoreBoardAPI.java`` : concerns the API in general (initialization) 
* ``BoardPlus.java`` : represents and relates to a scoreboard  
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
  
### To unregister the service:  
>  I do not recommend attempting to re-enable the service if it has been unregistered this way!  
>  This method will be more useful when shutting down the plugin / server.  
```java
public void onDisable() {
  ScoreBoardAPI.unregisterPlugin();
}
```  
  
### Activate/Deactivate securely:
```java
ScoreBoardAPI.disable();
ScoreBoardAPI.enable();
```  
## Init one instance of BoardPlus:  
```java
public class ExampleBoardTutorial extends BoardPlus {
    @Override
    public void init (final FastBoard board) {
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
    @Override public void onMove (final FastBoard board) { }
    @Override public void onOnlineChange (final FastBoard board) { }
    /* optionnaly, basic events */
}
```
## Use BoardPlus:  
### Get existing instance:
```java
BoardPlus.getInstance( "SomeInstance" );
```  
### Statically get the BoardPlus Instance FastBoard:
```java
ScoreBoardAPI.updateAllBoards(" NameOfTheBoard ", (board) -> {
    // some stuff with board:
    board.updateTitle("Welcome to Server");
});
```  
### Add player to BoardPlus:  
```java
BoardPlus jaaj = ...;
jaaj.addPlayer ( Player );
```
### Add a task at any time:
```java
BoardPlus jaaj = ...;
jaaj.schedule( int TICK, FastBoard );
```
### Temporarily hide this scoreboard by another:  
I thought that for a server with jobs,  
you can display the job scoreboard when a task is completed for X seconds and as long as the values change let it display,  
then once the values don't change go back to the scoreboard main.  
  
Here is an example:  
```java
public class SomeTest implements Listener {
    BoardPlus.SchedulingBascule basculeData = new BoardPlus.SchedulingBascule();

    @EventHandler
    protected void onBlockBreak (BlockBreakEvent event) {
        basculeData.setValue(" new value ");
        // As long as there is at least one call
        // on this method below 20 * 10 ticks,
        // the special scoreboard will not disappear.
    }


    public void someMethod (Player player) {
        BoardPlus defaultSco = BoardPlus.getInstance(" someInstance ");
        BoardPlus jobsSco = BoardPlus.getInstance(" someInstance ");


        // add player to special scoreboard
        jobsSco.addPlayer( player );

        // switch to the main scoreboard
        // if the special scoreboard no longer evolves

        // In our example,
        // if the event has not been invoked for 20*10 ticks
        jobsSco.basculeIfUnchanging(20*10, player, defaultSco, basculeData);
    }
}
```
### Unregister instance of BoardPlus:  
```java
BoardPlus jaaj = ...;
jaaj.unregister();
```
## BoardPlayer:
### Get instance of BoardPlayer:
```java
BoardPlayer bp = BoardPlayer.getPlayer(player);
```
### Remove player from all mechanism:
```java
BoardPlayer.deletePlayer(player);
```
