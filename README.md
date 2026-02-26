# Scallywag

Scallywag is a simple database-backed authentication plugin for Minecraft servers. It allows players to register and log in, making it useful for both offline mode servers and those requiring additional security measures (e.g., private event servers). <br>
The plugin supports both external and native database authentication types. The native authentication encrypts player passwords and operates asynchronously, preventing server blocking during operations. Basic timeouts and password validation are also implemented. External authentication can be used to use preexisting databases or APIs.

## Table of Contents

- [Setup](#setup)
- [Usage](#usage)
- [Admin Commands](#admin-commands)
- [Configuration](#configuration)
- [Notes](#notes)
- [API](#api)
- [API Example](#example)

## Setup

First, choose the authentication source. If you wish to use native database authentication, make sure that you have access to a mysql server (if you are using something like pterodactyl, it usually comes bundled with the software). If you wish to use external authentication, you would need to either find a plugin that implements it or write one yourself.

For native authentication: to get started, set up the database connection in the configuration file (`config.yml` located in `plugins/Scallywag`. Search for the `databaseConnection` section and fill out the fields:

- **`url`**: The JDBC connection URL (should look like `jdbc:mysql://host:port/databaseName`). The database must be MySQL.
- **`user`**: The database username.
- **`password`**: The corresponding password.

*Note*: The user and password can also be included in the JDBC URL, so be attentive if it is provided by a server host.

## Usage

Before logging in, players must register. Admins can also add registrations manually using the command `/registration add`. Upon registration, players specify a password and can log in with it.<br>
Player registration can be disabled in the configuration, in which case all registration will need to be performed by admins (good for private servers or event servers).

### Player Commands

- **`/register <password>`**, **`/r <password>`**: Allows players to register on the server. This can be disabled by setting the `allowPlayerRegistration` flag to `false`.
- **`/login <password>`**, **`/l <password>`**: Allows players to log in.
- **`/password <oldPassword> <newPassword>`**: Lets logged-in players change their password if the old password matches. This can be disabled by setting the `allowPlayerPasswordChanging` flag to `false`.

### Admin Commands

*Permission: `scallywag.admin`*

- **`/registration add <UUID> <username> <password>`**: Admins can add a registration for the specified UUID, username, and password (password validation rules apply). Use caution with offline mode servers, as UUIDs may not match.
- **`/registration remove <UUID or username>`**: Removes a registration for the specified UUID or username.
- **`/registration modify <UUID or username> <property> <value>`**: Modifies a registration's property (either `password` or `username`) to the specified value.

## Notes

- Only tested with a MySQL database.
- By default, there is no notification prompting players to log in upon joining the server. You can implement this notification using the API or another plugin.
- The logged-in player list resets when the server restarts.
- If external authentication is used, Scallywag is only responsible for keeping players in a limbo state. If external authentication is insecure, Scallywag will not assist with the problem. Only use external authentication if you know what you are doing.

## Configuration

The following configuration options are available:

- **`authenticationSource`**: The authentication source to use. Use NATIVE_DATABASE for native and EXTERNAL for external.
- **`allowPlayerPasswordChanging`**: Whether to allows players to update their passwords using `/password`.
- **`allowPlayerRegistration`**: Enables or disables player registration using `/register`.
- **`applyDarknessToUnauthorisedPlayers`**: Whether to apply an infinite darkness effect to players who are not logged in.
- **`automaticallyLogInUponRegistration`**: Whether to automatically log players in after registration using `/register`.
- **`databaseConnection`**: Database connection information (see [Setup](#setup)).
- **`doSetUnauthorisedInvulnerable`**: Whether to cancel all damage to unauthorised players.
- **`enableDefaultCommandFeedback`**: Whether to enables default messages in chat to communicate operation information.
- **`freezeUnauthorisedPlayers`**: Whether to freeze non-logged-in players in place, preventing movement and interactions.
- **`keepQuittersLoggedIn`**: Whether to retain a player’s logged-in status after they quit. If set to true, they won't need to log in again upon rejoining.
- **`limboLocation`**: The location where unauthorised players are teleported after they join the server (and are brought back from after they log in).
- **`timeOutSeconds`**: Duration (in seconds) until a non-logged-in player is kicked from the server. Set to `-1` to disable this feature.
- **`useLimboLocation`**: Whether to teleport unauthorised players to the limboLocation when they join the server.
 
## API

The plugin includes a simple API. The `Scallywag` interface provides an abstraction for common operations. Synchronous Bukkit events, `ScallywagLogInEvent` and `ScallywagLogOutEvent`, are triggered on the next tick after a player logs in or logs out, respectively. You can also use `ScallywagUnauthorisedPlayerJoinEvent` to manage what happens to unauthorised players when they join your server.<br>

To use external authentication, you must provide your own implementation of the `LoginManager` and/or `RegistrationManager` interfaces depending on the functionality you need. You should then assign them using `Scallywag.setLoginManager(loginManager)` and `Scallywag.setRegistrationManager(registrationManager)` respectivelly. It is important to note that this is only allowed once, so make sure that your plugin list contains only one plugin that provides Scallywag with external authentication.<br>

Refer to the Javadocs for more information.

### Example

```java
public class APITester extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new ListenerExample(), this);
        // Set the password validator to the function defined below
        Scallywag.setPasswordValidator(this, this::isPasswordValid);
    }

    private boolean isPasswordValid(String password) {
        return password.contains("$"); // Return true if the password is strong enough, false otherwise
    }
}

public class ListenerExample implements Listener {

    @EventHandler
    private void onUnauthorisedPlayerJoined(ScallywagUnauthorisedPlayerJoinEvent event)
    {
        var player = event.getPlayer();
        player.sendMessage("Please log in to authorise on the server");
        player.setHealth(20.0f);
    }

    @EventHandler
    private void onPlayerLoggedIn(ScallywagLogInEvent event) {
        // Show a message to a newly logged-in player
        final var player = event.getPlayer();
        if (player != null) {
            player.sendMessage(Component.text("You have logged in successfully, hip hip hurray!")
                    .color(NamedTextColor.GOLD)
                    .decorate(TextDecoration.BOLD));
        }
    }

    @EventHandler
    private void onPlayerLoggedOut(ScallywagLogOutEvent event) {
        // Show a message to a newly logged-out player
        final var player = event.getPlayer();
        if (player != null) {
            player.sendMessage(Component.text("You have logged out!")
                    .color(NamedTextColor.DARK_RED)
                    .decorate(TextDecoration.BOLD));
        }
    }

    @EventHandler
    private void onPlayerMountsEntity(EntityMountEvent event) {

        // Cancel the event if the mounter is not logged in

        if (event.getEntity() instanceof Player player && !Scallywag.isLoggedIn(player)) {
            event.setCancelled(true);
        }
    }
}
