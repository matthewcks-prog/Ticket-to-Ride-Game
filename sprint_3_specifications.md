Sprint 3 - Specifications 
1. Project Requirements 
1.1 Required Extension 
1.2 Pick-From-The-List Extensions 
1.3 Self-Defined Extensions
2. Tech-based Software 
3. Assessment Guide 5 
4. General Project Technical Requirements and Expectations 10 


1. Project Requirements 
In Sprint 2,( how our repo is currently as we just finished sprint 2), we produced a Minimal Viable Product of the Ticket to Ride Board Game. We got as feedback “Great detail however could have talked more about the alternatives you considered and what would have been the pros and cons of that approach. Blind deck UI/complete logic not present ?” 
In Sprint 3, now, we will continue the work started in Sprint 2 and extensions to the Ticket to Ride Board game is now to be implemented. 
The extensions are divided into three categories: (i) required, (ii) pick-from-the-list (chose undo mechanism), and (iii) self-defined. For Sprint 3, you must implement one extension each from all three categories, respectively - so in total we have 3 extensions (those extensions cannot be functionality that already exists from your Sprint 2 implementation or the other two extensions in Sprint 3). 
1.1 Required Extension 
Introducing Ferries. Ferries are special routes linking two adjacent cities across a body of water. They are easily identified by the Locomotive icon(s) featured on at least one of the spaces making the route. To claim a Ferry Route, a player must play a Locomotive card for each locomotive symbol on the route, and the usual set of cards of the proper color for the remaining spaces of that Ferry Route. Players may also use extra Locomotive cards as wild cards to replace color cards. 
If a player does not have enough Locomotive cards, any 3 cards may be used in place of 1 required Locomotive card. 
 
The Ferry extension can be an addition to the London map (for example, by placing a ferry route across the Thames), or you may introduce it as part of a new map. 
1.2 Pick-From-The-List Extensions 
Introduce a new extension of the Ticket to Ride board game. We are choosing the undo mechanism 
Undo mechanism. The game facilitates the user to undo the last two turns (the turn of the previous player, plus the one of their own). 
1.3 Self-Defined Extensions 
We encourage you to show some creativity and come up with your own extensions. Any self-defined extensions must, in some specific form, alter the game play and must not be limited to minor UI modifications only (e.g., changing colour of game entities; adding sound/music; enabling UI resizing). We discourage a surface level implementation; deeper implementations are encouraged and rewarded. 
Chosen self-defined extension: Rush Hour Events. The game uses a turn-based event clock with a
Forecast round and a Peak round. Forecast shows affected routes with no rule effect. Peak requires
one extra detour Transportation card to claim affected routes and awards an immediate +2 Rush Hour
bonus when the affected route is claimed successfully.
Examples include (but these are only examples to stimulate curiosity)  BE CREATIVE AND THINK OF A GOOD EXTENSION
1. illegal moves, the game displays a clear, specific message explaining why the action was rejected (e.g. "You need 3 pink cards to claim this route" or "You cannot draw a face-up locomotive as your second card draw"). 
2. Tying up with a local charity organisation and allowing the player to make a donation to the charity every time they win 5 games in a row. 

We hope that you will have many great ideas on how to extend the game. 
Further details of each of these tasks are given below. 
2. Sprint 3 Deliverables 
All tasks are mandatory. 
1. Object-Oriented Design Deliverables 
Using the provided design template, you are required to submit the following: 
1. Updated class diagram (We will do that after)
2. Identified design antipatterns from Sprint 2 and applied refactoring techniques 
3. Design Rationales 

2. Tech-based Software Prototype 
Your working prototype must include the game functionalities as well as the chosen extensions. Deviations are OK as one rarely gets things 100% right in a design, but these deviations need to be documented. 
If specific artwork is created for your user interface (e.g., icons, images for game objects etc.), this can be created by Generative AI. You must, however, acknowledge the use of Generative AI and what specifically you have used it for as part of your submission. 
The repository should include a description of your executable, the platforms it runs on, how it needs to be run, as well as instructions on how it can be created from source code. 
Proper documentation for the 3 extensions we made and how they work 
Frequent commits of your code to your repository helps to establish proper use of a repository. 
As was the case for Sprint 2, for Java, we require the Google Java Style Guide (https://google.github.io/styleguide/javaguide.html) to be followed for any source code that you are creating. We strongly discourage applying the relevant coding standards at the very end and encourage the use of the style guide throughout the Sprint 3 development. 

3. General Project Technical Requirements and Expectations 
There are several expectations, conditions and restrictions that apply throughout the entirety of the project. 
1. The object-oriented programming languages you are approved to use for the project (throughout all of the sprints) are Java. We are further restricting the User Interface libraries to Java Swing. The reason for using is because they ship with Java, they can be assessed consistently, they force focus on OOP design, not UI frameworks and they avoid licensing and installation issues. 
2. The application you will eventually develop must be implemented as a standalone application that is able to run locally on a single device and does not require separate server-side code to be written. 
