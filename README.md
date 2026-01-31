# GateKeeper
A plugin adding a basic whitelist system to velocity, with a floodgate integration to add direct bedrock player support.
It stores the players in a simple SQLite database.

---
### Commands and Permissions:
---
`/vwhitelist` (aliases: `/velocitywhitelist`, `/gatekeeper`)
- `add <username or uuid>` : Adds a new player to the whitelist
- `remove <username or uuid>` : Removes the specified player from the whitelist
- `on` : Enables the whitelist
- `off` : Disables the whitelist
- `list` : Lists all players currently on the whitelist
- `clear` : Clears the entire whitelist

Permissions are given based on the commands:

`whitelist`: base command node
- `whitelist.add` for the add-command
- `whitelist.clear` for the clear-command
- `whitelist.list`  for the list-command
- `whitelist.off`   for the off-command
- `whitelist.on`    for the on-command
- `whitelist.remove` for the remove-command

> [!IMPORTANT]
> If you want to add a bedrock player to the whitelist by their username and not UUID, please prefix it with your configured bedrock player prefix (default: `.`) first.
> Example: `Tim203` -> `.Tim203`
---
### config.yml
---
 ```yaml
enabled: true # if the whitelist should be enabled or not. This will change with the use of `/whitelist on|off`.
prefix: "<dark_gray>[<light_purple>Whitelist<dark_gray>]<reset> " # The prefix for every message except the disconnect reason.
cache:
  enabled: true # if caching should be enabled
  ttl-minutes: 5 # ttl (time to live) defining for how long the cache should be considered up-to-date
  refresh-interval-minutes: 10 # interval in which to refresh the cache
messages:
  disconnect-reason: | # the message shown to the player when he is not on the whitelist
    <red>You are not whitelisted on this server.</red>
    <gray>Please contact an administrator if you believe this is a mistake.</gray>
  errors:
    player-not-found: "<red>Player '{0}' not found.</red>"
    already-whitelisted: "<red>Player is already whitelisted.</red>"
    not-whitelisted: "<red>Player is not whitelisted.</red>"
    incorrect-confirmation: "<red>Incorrect confirmation number."
    whitelist-already-on: "<red>Whitelist is already enabled.</red>"
    whitelist-already-off: "<red>Whitelist is already disabled.</red>"
  usage:
    add: "<red>Usage: /whitelist add <player></red>"
    remove: "<red>Usage: /whitelist remove <player></red>"
    whitelist: "<red>Usage: /whitelist <on|off|add|remove|list></red>"
  info:
    added-to-whitelist: "Added <red>{0}</red> to the whitelist."
    removed-from-whitelist: "Removed <red>{0}</red> from the whitelist."
    whitelist-on: "Whitelist has been <green>enabled</green>."
    whitelist-off: "Whitelist has been <red>disabled</red>."
    list: "Whitelisted players: <yellow>{0}</yellow>"
    list-empty: "<gray>The whitelist is currently empty."
    whitelist-clear: "Please type in <red>/whitelist clear {0}</red> in the next 15 seconds to confirm. <gray>Note that this action cannot be undone."
    whitelist-clear-confirmed: "Whitelist cleared successfully."
```