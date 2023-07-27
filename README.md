# ABOUT THIS PROJECT
this project is a part of the OOP (261200) course in Chiang Mai University.
It about a turn base game like _**Civilization**_, but it's not the same.

# How to play
1. First of all player need to make first construction plan that will be the first plan to win the game.
2. During the game player can change your construction plan by using the command that satisfy the grammar and parser of this game.
3. The game will be end when the player win the game or lose the game. (there is only one player left in the game)

# Grammar of the game
- Plan → Statement+
- Statement → Command | BlockStatement | IfStatement | WhileStatement
- Command → AssignmentStatement | ActionCommand
- AssignmentStatement → <identifier> = Expression
- ActionCommand → done | relocate | MoveCommand | RegionCommand | AttackCommand
- MoveCommand → move Direction
- RegionCommand → invest Expression | collect Expression
- AttackCommand → shoot Direction Expression
- Direction → up | down | upleft | upright | downleft | downright
- BlockStatement → { Statement* }
- IfStatement → if ( Expression ) then Statement else Statement
- WhileStatement → while ( Expression ) Statement
- Expression → Expression + Term | Expression - Term | Term
- Term → Term * Factor | Term / Factor | Term % Factor | Factor
- Factor → Power ^ Factor | Power
- Power → <number> | <identifier> | ( Expression ) | InfoExpression
- InfoExpression → opponent | nearby Direction

# Collaborator
