# Tic Tac Toe Frontend

A React-based frontend for the Tic Tac Toe game that connects to a Python FastAPI backend.

## Features

- Interactive 3x3 Tic Tac Toe game board
- Real-time game state updates via API
- Beautiful, responsive UI with animations
- Player turn indicators
- Win/draw detection
- Game controls (New Game, Reset)

## Prerequisites

- Node.js (v16 or higher)
- npm or yarn
- Running Tic Tac Toe API backend on `http://localhost:8189`

## Installation

1. Install dependencies:
```bash
npm install
```

## Running the Application

### Development Mode

Start the development server:
```bash
npm run dev
```

The application will be available at `http://localhost:5173`

### Build for Production

```bash
npm run build
```

The built files will be in the `dist` directory.

### Preview Production Build

```bash
npm run preview
```

## Backend API

The frontend connects to the Tic Tac Toe API running on `http://localhost:8189`.

Make sure the backend is running before starting the frontend:

```bash
cd C:\School\tic-tac-toe\src
python api.py
```

## API Endpoints Used

- `POST /api/game/new` - Create a new game
- `GET /api/game/{game_id}` - Get current game state
- `POST /api/game/{game_id}/move` - Make a move
- `DELETE /api/game/{game_id}` - Delete a game

## Project Structure

```
tic-tac-toe-frontend/
├── src/
│   ├── api/
│   │   └── gameApi.js          # API client for backend communication
│   ├── components/
│   │   ├── Board.jsx            # Game board component
│   │   └── Cell.jsx             # Individual cell component
│   ├── App.jsx                  # Main application component
│   ├── App.css                  # Application styles
│   ├── main.jsx                 # Application entry point
│   └── index.css                # Global styles
├── index.html                   # HTML template
├── vite.config.js              # Vite configuration
└── package.json                 # Project dependencies

```

## Technologies Used

- React 19
- Vite (Build tool)
- Axios (HTTP client)
- CSS3 (Styling with animations)

## How to Play

1. Click "New Game" to start a new game
2. Players take turns clicking on empty cells
3. Player X always goes first
4. The first player to get 3 marks in a row (horizontally, vertically, or diagonally) wins
5. If all cells are filled and no player has won, the game is a draw
6. Click "Restart Game" to start a new game or "Reset" to clear the current game

## Development

The application uses React hooks for state management and Axios for API communication. All game logic is handled by the backend API.
