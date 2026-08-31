# Board Game Platform Chatbot - Frontend

A modern React-based chatbot interface for the Board Game Platform. This chatbot helps users with game rules, platform navigation, and gameplay questions.

## Features

- Clean and modern UI with gradient design
- Real-time chat interface with smooth animations
- Loading indicators for better UX
- Quick suggestion chips for common questions
- Response caching indicators
- Fully responsive design

## Tech Stack

- React 18 with TypeScript
- TypeScript 5.6
- Vite (build tool)
- CSS Modules for styling
- Fetch API for backend communication

## Prerequisites

- Node.js (v16 or higher)
- npm or yarn
- Backend server running on `http://localhost:7777`

## Installation

1. Clone the repository:
```bash
cd existing_repo
git remote add origin https://gitlab.com/kdg-ti/inf-curriculum/domain-integration/integration-5/2025-2026/team10/chatbot-frontend.git
git branch -M main
git push -uf origin main
```

2. Install dependencies:
```bash
npm install
```

## Running the Application

1. Make sure your backend server is running on port 7777

2. Start the development server:
```bash
npm run dev
```

3. Open your browser and navigate to `http://localhost:3000`

## Building for Production

To create a production build:

```bash
npm run build
```

To preview the production build:

```bash
npm run preview
```

## Project Structure

```
chatbot-frontend/
├── src/
│   ├── components/
│   │   ├── Chatbot.jsx           # Main chatbot container
│   │   ├── Chatbot.module.css
│   │   ├── Message.jsx            # Individual message component
│   │   ├── Message.module.css
│   │   ├── ChatInput.jsx          # Input form with suggestions
│   │   ├── ChatInput.module.css
│   │   ├── LoadingIndicator.jsx  # Loading animation
│   │   └── LoadingIndicator.module.css
│   ├── App.jsx                    # Root component
│   ├── main.jsx                   # Application entry point
│   └── index.css                  # Global styles
├── index.html                     # HTML template
├── vite.config.js                 # Vite configuration
└── package.json                   # Dependencies and scripts
```

## API Integration

The chatbot communicates with a backend server at `http://localhost:7777/chat`. The API expects:

**Request:**
```json
{
  "question": "string",
  "use_cache": true,
  "n_results": 3
}
```

**Response:**
```json
{
  "response": "string",
  "cached": boolean,
  "processing_time": number
}
```

## Customization

### Changing the API URL

Edit the `API_URL` constant in `src/components/Chatbot.jsx`:

```javascript
const API_URL = 'http://your-backend-url:port/chat'
```

### Modifying Suggestions

Update the `suggestions` array in `src/components/ChatInput.jsx`:

```javascript
const suggestions = [
  'Your custom suggestion 1',
  'Your custom suggestion 2',
  'Your custom suggestion 3'
]
```

### Styling

The project uses CSS Modules for component-specific styling. Each component has its own `.module.css` file. Global styles are in `src/index.css`.

## Browser Support

- Chrome (latest)
- Firefox (latest)
- Safari (latest)
- Edge (latest)
