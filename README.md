<a name="readme-top"></a>

[![Java](https://a11ybadges.com/badge?logo=java)](https://www.java.com/it/)
[![IntelliJ](https://a11ybadges.com/badge?logo=intellijidea)](https://www.jetbrains.com/idea/)


<!-- PROJECT LOGO -->
<br />
<div align="center">
  <a href="http://www.craniocreations.it/">
    <img src="src/main/resources/Publisher_material/Publisher.png" alt="Cranio Creations logo" width="192">
  </a>

<h3 align="center">My Shelfie Board Game Implementation</h3>

  <p align="center">
     My Shelfie Board Game - Software Engineering Final Project 2023 - Politecnico di Milano
  </p>
  <br />
</div>

<!-- ABOUT THE PROJECT -->
## My Shelfie Board Game :books:

For the final project of the Software Engineering course of the Bachelor’s degree at Politecnico di Milano we were tasked to reproduce the board game My Shelfie, created by Cranio Creations.
We had to implement a distributed system with client-server technology using the Model-View-Controller (MVC) pattern. 

<img src="src/main/resources/Publisher_material/Display_1.jpg" align="center" alt="MyShelfyDisplay"/>

We chose to implement the complete rule set of the game, effectively translating the whole board game into its digital copy , plus we added 3 advanced functionalities:
<br />
- Persistency: the server will periodically save the game state on a file to enable resuming a game after a server crash
- Resiliency to disconnections: the player that disconnect from a game will be able to rejoin the same game they left while the others will keep on playing. If only one player remains a countdown will start and if by the end of it that player is still alone he will win the game by default
- Chat: player will be able to talk to each other or whisper to one another via private messages

<p align="right">(<a href="#readme-top">back to top</a>)</p>

## Implementation Details

***Descrizione della soluzione***

### Model

As it’s clear from the simplified UML diagram, the object “Game” is the primary class composed by the objects Board, Player in a playerList and commonGoalCards. About the latter, we implemented a Strategy design pattern, to enable extendibility of the code in the future allowing to add more common goal cards. Extendibility was also the reason we implemented a map for personal goal cards.
Shelf and Board are both represented as Tile matrix, TilePlacingSpot is used to manage tiles in the board, for example by checking if a tile is available in a certain position.
Each player has its own personal goal card and its own shelf which calculates in real time the score of the adjacent tiles. The tiles are contained by the object Bag before being drawn and positioned on the board.
Each Tile has the attribute “type”, which is an enumeration of the six types that a tile can assume (cat, book, game, trophy, plant, and frames).

<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Client-Server Communication

We implemented both RMI and Socket connection type. The player choses which connection type to use at the start of the game, every connection is compatible with each other since the server manages all the messages to send to each client knowing which type of connection it uses. 

<p align="right">(<a href="#readme-top">back to top</a>)</p>

### View

We implemented both CLI and GUI, but ,since we wanted both to feel similar, the only two command that a CLI user can input are /chat (followed by an @user  and a message, or simply 
the message if he wants to send it to all the players) and /quit to quit the game (this action ends the game for all players as per requirements). To play the game through CLI the 
player answers to server queries by choosing from which line and column he wants to extract a tile from the board, how many and in which directions, then he chooses in which column 
of its shelfie to insert them and in which order. After every turn the server sends an updated board to every player, regardless if a player is using GUI or CLI, as every client with 
the same connection technology is treated equally by the server. Any other input of a player in the cli is ignored.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

### Known issues

Persistency is not stable for disconnection of 2 or more player at the same time

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- RUN -->
## How to play

run 'java -jar server.java' to run the server, then connect to it by running 'java -jar cli.jaror java -jar gui.jar' to either play with CLI or GUI.

<p align="right">(<a href="#readme-top">back to top</a>)</p>

<!-- LICENSE -->
## License 📄

Distributed under the ` AGPL-3.0 license` License. See [LICENSE](https://github.com/dre-droid/ing-sw-2023-mastroberti-lecchi-maggese-martellosio/blob/main/LICENSE) for more information.

<p align="right">(<a href="#readme-top">back to top</a>)</p>



<!-- CONTACT -->
## Group Members 👥
* [Diego Lecchi](https://github.com/DiegoLecchi)
* [Saverio Maggese](https://github.com/SaverioMaggese99)
* [Francesco Martellosio](https://github.com/FrancescoMartellosio)
* [Andrea Mastroberti](https://github.com/dre-droid)

<p align="right">(<a href="#readme-top">back to top</a>)</p>
