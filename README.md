# United Core

**United Core** is a custom-made plugin for United Lands, which has most of our custom made features spread out over multiple 'nodes'. Info detailing each node is provided below. The current working nodes are:
  1. UnitedPvP
  2. UnitedChat
  3. UnitedItems
  4. UnitedUpkeep
  5. UnitedProtection

# Building and Development
Unfortunately, due to how these plugins are configured, they are very hard to replicate and use on a development server outside of United Lands. The code is purely available for accessibility and anyone who's curious. However, if you still want to try and build the plugin(s), you will need:

- A local maven repository of SafariNet, which is used for UnitedSkills capturing skills. You may google how to set up a local maven repository from a jar file, once you have bought the plugin from [here](https://www.spigotmc.org/resources/9732/). You should then add the info of the local repo to the pom.xml of the UnitedSkills module

- Access to [ItemsAdder](https://www.spigotmc.org/resources/73355/). If you have not bought the plugin and have it running on your server, UnitedSkills and UnitedItems will not enable properly.

- Access to [PlayerBlockTracker](https://github.com/Flo0/PlayerBlockTracker). This is a free open source plugin that we use as a library to track blocks without pulling down our own plugins with extra code. You need to have it installed in order to run UnitedSkills. 

- Access to [Towny](https://github.com/TownyAdvanced/Towny). Most of our plugins rely on towny for various reasons, so if you don't have a version of that up and running nothing will work. 

Again, it is not recommended to try and use these plugins as is, since chances are they will not work as you expect or not work at all. No support will be provided or any help on how to set them up beyond the details in this section. The code is purely open-sourced for accessibility, better tracking, and if anyone wants to learn or use parts of it in their projects.


# Plugin info

## UnitedPvP
A custom-made pvp manager that handles the pvp status of players. It also has a few additions, such as force-enabling pvp for outlaws when they enter an outlawed town. 

### Commands
- `/pvp on` - Sets your pvp status to ON, meaning you can engage in combat with other players
- `/pvp off` - Sets your pvp status to off, meaning you can no longer engage in combat
- `/pvp status` - Returns your current pvp status (Either enabled, or disabled) 
- `/pvp on/off <player>` - Admin command, force sets the pvp status of <player>

### Permissions
  - `united.pvp.toggle` Gives permissions to use the `/pvp on/off` command
  - `united.pvp.cooldown.ignore` Allows you to ignore the pvp toggle command cooldown (120 seconds by default)
  - `united.pvp.admin` Allows you to set the status of other players. 
  
### Placeholders
  - `%unitedpvp_status%` - Returns either `⚔` if the pvp is enabled, or `🛡` if it's disabled
  - `%unitedpvp_status-string%` - Returns either `enabled` or `disabled`

## UnitedChat
  Used to manage most custom chat aspects. Most notably, the auto broadcast messages, gradientchat, player pings, and join MOTD 
  
  ### Commands
  #### Gradient Commands
  - `/gradient on/off` - Toggles your gradient on/off
  - `/gradient [preset]` - Sets your current gradient to on of the pre-made presets 
  - `/gradient [Hex 1] [Hex 2]` - Sets your gradient to a custom gradient with start and end hex codes
  #### ClearChat
  - `/cc` - Clears the current chat.
  
  ### Permissions
  - `united.chat.gradient` - Gives access to the primary /gradient command
  - `united.chat.gradient.<preset>` - Gives access to a specific preset defined in the config.yml
  - `united.chat.gradient.all` - Gives access to all presets, and the ability to set custom hex codes
  - `united.chat.admin` - Gives access to the `/cc` command 

  ## UnitedItems
  A plugin which handles custom items, trees, saplings, and more! Info about all the custom items can be found [here](https://github.com/UnitedLands/community/wiki/Custom-Items), this mainly details the plugin usage
  
  
  ### Commands
  #### Custom Item Commands
  - `/customitem help` - Shows a help message
  - `/customitem list` - Lists all available custom items
  - `/customitem listfqn` - Lists all custom items with their colored names
  - `/customitem give <name>` - Gives an item depending on how similar the name is to an existing item.
  #### Tree Commands
  - `/tree help` - Shows a help message
  - `/tree seed <name>` - Gives a seed/sapling of the requested tree
  - `/tree info <name>` - Gives information for the specified tree
  - `/tree list` - Lists all available trees 
  - `/tree give <player> <name>` - Gives the player a tree seed
  
  ### Permissions
  - `united.custom.admin` - Gives access to all commands above 

  ## UnitedUpkeep
  It's the sole plugin handling our custom upkeep system. This plugin calculates the upkeep needed per-town through a sophisticated formula to help ensure balance. As of now there are no commands or permissions needed to set this plugin up, and it simply just works out of the box. 
  
  ## UnitedProtection
  This plugin is built to protect the Earth world in UnitedLands. This is mainly to perserve the value of ores, since the Earth world is way too rich in ores. Its essentially just a simple listener that checks for placing/breaking blocks. If you're in the Earth world, below y45 (configurable), and not in a town of yours, then you're not allowed to break or place anything. There's also a blacklist for specific blocks (found in config.yml) which bypass these protections. 
  
  Currently there are no commands, but there's 1 permission. 
  
  ### Permissions
  - `united.protection.bypass` - Bypasses Earth protection rules everywhere. 
  
  ## UnitedBrands
  This is perhaps one of the more sophisticated plugins in the list, coming in right behind UnitedItems. This plugin allows you to create Brands, which are pretty similar to towns in the sense that you can invite and manage players in your brand. Brands are currently only limited to one type — Breweries. 
  
  Creating a brewery allows you to add a custom branding on any alcohol that you brew, as well as a custom slogan. Breweries also grant the ability to create 'abnormal' star levels for any drinks you make via buying brewery upgrades. You can get drinks with up to 10 stars (also known as Master Brews) if you properly upgrade your brewery. 
  
  ### Commands
  **Player Commands** 
  Intended to be used by your average player. 
  - `/brewery help` - shows a list of all commands
  - `/brewery create <name>` - Creates a new brewery with the specified name (can have multiple words)
  - `/brewery slogan <slogan>` - Sets your brewery''s slogan to a new slogan (multiple words supported)
  - `/brewery delete` - Deletes your current brewery, if it exists
  - `/brewery invite <player>` - Invites a player to your brewery
  - `/brewery kick <player>` - Kicks a player from your brewery
  - `/brewery accept` - Accepts any incoming brewery join requests
  - `/brewery deny` - Denies any incoming brewery join requests
  - `/brewery leave` - Makes you leave from your current brewery
  - `/brewery info [name]` - Shows stats and info of your current brewery. If a name is specified, it shows info for that brewery if it exists.
  - `/brewery list [page]` - Shows a list of all breweries. The list supports pages so you can go back and forth between them just by clicking arrows in chat!
  - `/brewery upgrade` - Upgrades your brewery to the next level. This command is not intended for usage, but is there more of a utility command. it is locked behind a permission specified below. 
  
  **Admin Commands**
  Used by administrators and mods to help manage any issues that come with breweries. 
  - `/breweryadmin help` - shows a list of all commands
  - `/breweryadmin reload` - reloads the plugin configuration
  - `/breweryadmin clearslogan <brewery>` - clears a brewery''s slogan
  - `/breweryadmin delete <brewery>` - Deletes specified brewery completely
  - `/breweryadmin addmember <member> <brewery>` - adds a member to a brewery
  - `/breweryadmin removemember <member> <brewery>` - removes a member from a brewery
  
  ### Permissions
  - `united.brands.admin` - Grants access to the `/breweryadmin` command
  - `united.brewery.upgrade` - Grants access to the `/brewery upgrade` command.
  
  ## Archived Plugins

  Unused plugins can be found in a separate repository [here](https://github.com/United-Lands/United-Archive).
