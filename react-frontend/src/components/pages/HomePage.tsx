import { useNavigate } from 'react-router-dom';
import { useAuth } from '../../context/AuthContext';
import { useEffect, useState } from 'react';
import { Gamepad2, Users, Trophy, Sparkles, ArrowRight, Play, Shield, Zap, X } from 'lucide-react';
import type { Game } from '../../types/game';
import { GameMetadataService } from '../../services/gameMetadataService';

export default function HomePage() {
  const navigate = useNavigate();
  const { isAuthenticated, login } = useAuth();
  const [showGamesModal, setShowGamesModal] = useState(false);
  const [games, setGames] = useState<Game[]>([]);
  const [loadingGames, setLoadingGames] = useState(false);

  const handleLogin = () => {
    login();
  };

  const handleBrowseGames = async () => {
    setShowGamesModal(true);
    if (games.length === 0) {
      setLoadingGames(true);
      try {
        const allGames = await GameMetadataService.listGames();
        setGames(allGames);
      } catch (error) {
        console.error('Failed to load games:', error);
      } finally {
        setLoadingGames(false);
      }
    }
  };

  // If user is already authenticated, redirect to dashboard
  useEffect(() => {
    if (isAuthenticated) {
      navigate('/dashboard');
    }
  }, [isAuthenticated, navigate]);

  const features = [
    {
      icon: Gamepad2,
      title: 'Classic Board Games',
      description: 'Play timeless favorites like Chess, Connect Four, and more with friends or AI opponents.',
      gradient: 'from-blue-600 to-purple-600',
    },
    {
      icon: Users,
      title: 'Multiplayer Lobbies',
      description: 'Create private lobbies, invite friends, or join public matches with players worldwide.',
      gradient: 'from-purple-600 to-pink-600',
    },
    {
      icon: Trophy,
      title: 'Achievements & Stats',
      description: 'Track your progress, unlock achievements, and climb the leaderboards.',
      gradient: 'from-pink-600 to-red-600',
    },
    {
      icon: Sparkles,
      title: 'AI-Powered Opponents',
      description: 'Challenge yourself against intelligent AI with adjustable difficulty levels.',
      gradient: 'from-orange-600 to-yellow-600',
    },
  ];

  const steps = [
    {
      icon: Shield,
      title: 'Sign In',
      description: 'Secure login with Keycloak SSO',
    },
    {
      icon: Play,
      title: 'Choose a Game',
      description: 'Browse our catalog of board games',
    },
    {
      icon: Zap,
      title: 'Start Playing',
      description: 'Join a lobby or challenge the AI',
    },
  ];

  return (
    <div className="min-h-screen bg-slate-950">
      {/* Navigation */}
      <nav className="border-b border-slate-800">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-between h-16">
            {/* Logo */}
            <div className="flex items-center gap-2">
              <Gamepad2 className="w-8 h-8 text-purple-400" />
              <span className="text-xl font-semibold text-slate-100">BanditGames</span>
            </div>

            {/* Auth Buttons */}
            <div className="flex items-center gap-3">
              <button
                onClick={handleLogin}
                className="px-4 py-2 text-slate-300 hover:text-slate-100 transition-colors"
              >
                Sign In
              </button>
              <button
                onClick={handleLogin}
                className="px-5 py-2 bg-purple-600 hover:bg-purple-700 text-white rounded-lg transition-colors font-medium"
              >
                Get Started
              </button>
            </div>
          </div>
        </div>
      </nav>

      {/* Hero Section */}
      <section className="relative overflow-hidden">
        {/* Background Gradient */}
        <div className="absolute inset-0 bg-gradient-to-br from-purple-950 via-slate-950 to-slate-950 opacity-50"></div>

        <div className="relative max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-24 sm:py-32">
          <div className="text-center">
            {/* Badge */}
            <div className="inline-flex items-center gap-2 px-4 py-2 bg-purple-500/10 border border-purple-500/20 rounded-full mb-8">
              <Sparkles className="w-4 h-4 text-purple-400" />
              <span className="text-purple-300 text-sm font-medium">Play Board Games Online</span>
            </div>

            {/* Main Heading */}
            <h1 className="text-5xl sm:text-6xl lg:text-7xl font-bold mb-6">
              <span className="text-slate-100">Your Ultimate</span>
              <br />
              <span className="bg-gradient-to-r from-purple-400 via-pink-400 to-purple-400 bg-clip-text text-transparent">
                Board Game Platform
              </span>
            </h1>

            {/* Subheading */}
            <p className="text-xl sm:text-2xl text-slate-400 mb-10 max-w-3xl mx-auto">
              Challenge friends, compete with AI, and master classic strategy games.
              All in one place.
            </p>

            {/* CTA Buttons */}
            <div className="flex flex-col sm:flex-row gap-4 justify-center">
              <button
                onClick={handleLogin}
                className="group px-8 py-4 bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 text-white rounded-lg transition-all font-semibold text-lg shadow-lg shadow-purple-500/25 hover:shadow-purple-500/40 flex items-center justify-center gap-2"
              >
                Start Playing Now
                <ArrowRight className="w-5 h-5 group-hover:translate-x-1 transition-transform" />
              </button>
              <button
                onClick={handleBrowseGames}
                className="px-8 py-4 bg-slate-800 hover:bg-slate-700 border border-slate-700 text-slate-200 rounded-lg transition-colors font-semibold text-lg"
              >
                Browse Games
              </button>
            </div>
          </div>
        </div>
      </section>

      {/* Features Section */}
      <section className="py-24 bg-slate-900/50">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* Section Header */}
          <div className="text-center mb-16">
            <h2 className="text-3xl sm:text-4xl font-bold text-slate-100 mb-4">
              Why Choose BanditGames?
            </h2>
            <p className="text-lg text-slate-400 max-w-2xl mx-auto">
              Everything you need for an amazing board gaming experience
            </p>
          </div>

          {/* Features Grid */}
          <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-4 gap-6">
            {features.map((feature) => {
              const Icon = feature.icon;
              return (
                <div
                  key={feature.title}
                  className="bg-slate-900 border border-slate-800 rounded-xl p-6 hover:border-purple-500/50 transition-all group"
                >
                  {/* Icon */}
                  <div className={`w-12 h-12 bg-gradient-to-br ${feature.gradient} rounded-lg flex items-center justify-center mb-4 group-hover:scale-110 transition-transform`}>
                    <Icon className="w-6 h-6 text-white" />
                  </div>

                  {/* Content */}
                  <h3 className="text-lg font-semibold text-slate-100 mb-2">
                    {feature.title}
                  </h3>
                  <p className="text-slate-400 text-sm leading-relaxed">
                    {feature.description}
                  </p>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* How It Works Section */}
      <section className="py-24">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          {/* Section Header */}
          <div className="text-center mb-16">
            <h2 className="text-3xl sm:text-4xl font-bold text-slate-100 mb-4">
              Get Started in 3 Easy Steps
            </h2>
            <p className="text-lg text-slate-400 max-w-2xl mx-auto">
              From sign-up to your first game in minutes
            </p>
          </div>

          {/* Steps */}
          <div className="grid grid-cols-1 md:grid-cols-3 gap-8 relative">
            {/* Connection Lines (hidden on mobile) */}
            <div className="hidden md:block absolute top-16 left-1/3 right-1/3 h-0.5 bg-gradient-to-r from-purple-600 via-pink-600 to-purple-600"></div>

            {steps.map((step, index) => {
              const Icon = step.icon;
              return (
                <div key={step.title} className="relative">
                  {/* Step Number */}
                  <div className="flex flex-col items-center text-center">
                    <div className="relative mb-6">
                      <div className="w-16 h-16 bg-gradient-to-br from-purple-600 to-pink-600 rounded-full flex items-center justify-center relative z-10">
                        <Icon className="w-8 h-8 text-white" />
                      </div>
                      <div className="absolute top-0 left-0 w-16 h-16 bg-purple-400 rounded-full animate-ping opacity-20"></div>
                    </div>

                    <div className="inline-block px-3 py-1 bg-purple-500/10 border border-purple-500/20 rounded-full mb-4">
                      <span className="text-purple-300 text-sm font-medium">Step {index + 1}</span>
                    </div>

                    <h3 className="text-xl font-semibold text-slate-100 mb-2">
                      {step.title}
                    </h3>
                    <p className="text-slate-400">
                      {step.description}
                    </p>
                  </div>
                </div>
              );
            })}
          </div>
        </div>
      </section>

      {/* Final CTA Section */}
      <section className="py-24 bg-gradient-to-br from-purple-900/20 via-slate-900/50 to-pink-900/20 border-y border-slate-800">
        <div className="max-w-4xl mx-auto px-4 sm:px-6 lg:px-8 text-center">
          <h2 className="text-3xl sm:text-4xl font-bold text-slate-100 mb-4">
            Ready to Start Your Journey?
          </h2>
          <p className="text-lg text-slate-400 mb-8">
            Join thousands of players enjoying classic board games online
          </p>
          <button
            onClick={handleLogin}
            className="group px-10 py-5 bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 text-white rounded-lg transition-all font-semibold text-xl shadow-lg shadow-purple-500/25 hover:shadow-purple-500/40 inline-flex items-center gap-3"
          >
            Get Started Free
            <ArrowRight className="w-6 h-6 group-hover:translate-x-1 transition-transform" />
          </button>
        </div>
      </section>

      {/* Footer */}
      <footer className="border-t border-slate-800 py-12">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex items-center justify-center gap-2">
            <Gamepad2 className="w-6 h-6 text-purple-400" />
            <span className="text-slate-400">© 2025 BanditGames. All rights reserved.</span>
          </div>
        </div>
      </footer>

      {/* Games Modal */}
      {showGamesModal && (
        <div
          className="fixed inset-0 bg-black/70 backdrop-blur-sm z-50 flex items-center justify-center p-4"
          onClick={() => setShowGamesModal(false)}
        >
          <div
            className="bg-slate-900 border border-slate-800 rounded-xl max-w-6xl w-full max-h-[80vh] overflow-hidden shadow-2xl"
            onClick={(e) => e.stopPropagation()}
          >
            {/* Modal Header */}
            <div className="flex items-center justify-between p-6 border-b border-slate-800">
              <div>
                <h2 className="text-2xl font-bold text-slate-100">Our Games</h2>
                <p className="text-slate-400 mt-1">Explore our collection of classic board games</p>
              </div>
              <button
                onClick={() => setShowGamesModal(false)}
                className="p-2 hover:bg-slate-800 rounded-lg transition-colors"
              >
                <X className="w-6 h-6 text-slate-400" />
              </button>
            </div>

            {/* Modal Content */}
            <div className="p-6 overflow-y-auto max-h-[calc(80vh-120px)]">
              {loadingGames ? (
                <div className="flex items-center justify-center py-12">
                  <div className="flex flex-col items-center gap-4">
                    <div className="w-12 h-12 border-4 border-purple-600 border-t-transparent rounded-full animate-spin"></div>
                    <p className="text-slate-400">Loading games...</p>
                  </div>
                </div>
              ) : games.length === 0 ? (
                <div className="text-center py-12">
                  <p className="text-slate-400">No games available</p>
                </div>
              ) : (
                <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4">
                  {games.map((game) => (
                    <div
                      key={game.id}
                      className="bg-slate-800 border border-slate-700 rounded-lg p-4 hover:border-purple-500 transition-all group cursor-pointer"
                      onClick={() => {
                        setShowGamesModal(false);
                        navigate(`/games/${game.id}`);
                      }}
                    >
                      {/* Game Icon/Image */}
                      <div className="w-full h-32 bg-gradient-to-br from-purple-600 to-pink-600 rounded-lg flex items-center justify-center mb-4">
                        <Gamepad2 className="w-12 h-12 text-white" />
                      </div>

                      {/* Game Info */}
                      <h3 className="text-lg font-semibold text-slate-100 mb-2 group-hover:text-purple-400 transition-colors">
                        {game.name}
                      </h3>
                      <p className="text-slate-400 text-sm mb-3 line-clamp-2">
                        {game.description || 'No description available'}
                      </p>

                      {/* Game Details */}
                      <div className="flex items-center justify-between text-xs text-slate-500">
                        <span>{game.playerConfiguration.minPlayers}-{game.playerConfiguration.maxPlayers} Players</span>
                        <span className="px-2 py-1 bg-slate-700 rounded">
                          {game.metaData?.category || 'Board Game'}
                        </span>
                      </div>
                    </div>
                  ))}
                </div>
              )}

              {/* Sign up CTA */}
              <div className="mt-8 p-6 bg-gradient-to-br from-purple-900/20 to-pink-900/20 border border-purple-500/20 rounded-lg text-center">
                <h3 className="text-xl font-semibold text-slate-100 mb-2">
                  Ready to Play?
                </h3>
                <p className="text-slate-400 mb-4">
                  Sign up now to start playing these amazing games!
                </p>
                <button
                  onClick={() => {
                    setShowGamesModal(false);
                    handleLogin();
                  }}
                  className="px-6 py-3 bg-gradient-to-r from-purple-600 to-pink-600 hover:from-purple-700 hover:to-pink-700 text-white rounded-lg transition-all font-semibold inline-flex items-center gap-2"
                >
                  Get Started Free
                  <ArrowRight className="w-5 h-5" />
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
}
