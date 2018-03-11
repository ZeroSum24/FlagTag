# DATABASE USAGE

## Retrieving game settings

You want to start by getting the rules needed for creating a game instance from the database (zone bounds, score limit etc..)

```java
RuleSet rules = new RuleSetConnection().getRuleSet();
```
Have a look at the `RuleSet` class to see what fields it has.

## Creating a game instance

### See whether a game instance is live

You can check whether a game is in progress via the static method

```java
boolean inProgress = new GameCreatorConnection().isGameInProgress();
```

You can create a new game instance (this will reset the current state back to the initial empty state) via:

```java
GameCreatorConnection gc = new GameCreatorConnection();
gc.registerNewGame();
```
This will put the database in a state such that in can be (hopefully) correctly modified later.

## Connecting other users to the game instance
There can only be one game instace globally (for now).



You should create a `LiveGameConnection` object and keep it alive throughout the user's play session. The host is **automatically added** to the game instance when they create an instance, so no need to add them.

```java
LiveGameConnection playerConnection = new LiveGameConnection(new LatLng(userLat, userLong), deviceID, displayName);
```

To **add** a player to the game, you ***must*** call this method:
```java
playerConnection.addPlayerToGame(Team.redTeam);
```

You should also remove a user from a session when necessary:
```java
playerConnection.removePlayerFromGame();
```

There are many *set* methods for updating this player's state in the database. One example is:
```java
playerConnection.updatePos(new LatLng(currentLat, currentLong));
```
You can retrieve the list of all players in the database (as Player objects) via:
```java
ArrayList<Player> players = playerConnection.getPlayers();
```
