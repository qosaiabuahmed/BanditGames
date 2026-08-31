# Prompt Log - Connect Four Game Platform

**Session Date:** 2025-11-17
**Project:** Integration 5

---

## Prompt History

### Prompt 1: Initial Game Refactoring

**Prompt:**
```
You are an expert Python developer and software engineer. Refactor the following
Connect Four code, originally translated from C, to meet modern software engineering
standards.

* Use clear function and variable names, modularize logic, and adhere to PEP 8 style.
* Organize code for readability, maintainability, and scalability.
* Ensure the code is ready for unit testing and future enhancements.

Here is the code: 
```

**Prompt Engineering Techniques:**
- **Role Definition:** Sets expertise level and context
- **Clear Requirements:** Bullet-pointed specifications
- **Explicit Constraints:** Modern engineering standards
- **Context Provision:** Explains legacy code origin

**Strategy:**
Establish clean code foundation before adding complex features.


### Prompt 2: Gameplay Logging Implementation

**Prompt:**
```
Design and implement the following components for the game system:

Database integration: Establish a PostgreSQL database for persistent storage.​

Schema and ERD: Develop and document a full database schema/Entity-Relationship Diagram (ERD) tailored to tracking games, players, moves, and heuristic evaluations.​

Move-by-move logging: Implement detailed logging for every move, ensuring each record captures:

Player ID

Game ID

Current game state snapshot

Move details (coordinates, action type, etc.)

Timestamp of the move

Heuristic evaluation metrics​

All components must be designed for scalability, auditability, and future analytic queries.
```


**Prompt:**
```

 implement a player scoring system for this Connect Four game with the following requirements:

  1. **Player Score Tracking**
     - Add a score attribute to each player that persists across multiple games
     - Initialize new players with a starting score (e.g., 1000 or 0)
     - Store player scores in the database so they persist between sessions

  2. **Score Updates Based on Game Results**
     - When a player wins a game: increase their score (e.g., +10 points)
     - When a player loses a game: decrease their score (e.g., -5 points)
     - For draw/tie games: optionally award a small amount or no change (e.g., +2 points each)

  3. **Score Display**
     - Display each player's current score in the game interface
     - Show score changes after each game ends
     - Consider adding a leaderboard or player statistics view

  4. **Database Integration**
     - Update the database schema to include the score field for players
     - Save score updates to the database after each game concludes
     - Ensure scores are loaded correctly when players reconnect

  Please maintain the existing game logic and only add the scoring system on top of the current functionality. Make sure the implementation is clean, well-documented, and follows the
  existing code structure.
```



---
