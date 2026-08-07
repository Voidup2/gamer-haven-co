import cover1 from "@/assets/cover-1.jpg";
import cover2 from "@/assets/cover-2.jpg";
import cover3 from "@/assets/cover-3.jpg";
import cover4 from "@/assets/cover-4.jpg";
import cover5 from "@/assets/cover-5.jpg";
import cover6 from "@/assets/cover-6.jpg";
import hero1 from "@/assets/hero-1.jpg";
import hero2 from "@/assets/hero-2.jpg";
import hero3 from "@/assets/hero-3.jpg";

export type StoreLink = { store: string; url: string };

export type Game = {
  id: string;
  title: string;
  tagline: string;
  description: string;
  cover: string;
  banner: string;
  rating: number;
  reviews: number;
  price: number;
  discount?: number;
  releaseDate: string;
  year: number;
  developer: string;
  publisher: string;
  genres: string[];
  platforms: string[];
  tags: string[];
  esrb: string;
  multiplayer: boolean;
  coop: boolean;
  freeToPay: boolean;
  vr: boolean;
  earlyAccess: boolean;
  controller: boolean;
  languages: string[];
  features: string[];
  stores: StoreLink[];
  requirements: { label: string; min: string; rec: string }[];
};

const req = [
  { label: "OS", min: "Windows 10 64-bit", rec: "Windows 11 64-bit" },
  { label: "Processor", min: "Intel i5-8400", rec: "Intel i7-12700K" },
  { label: "Memory", min: "12 GB RAM", rec: "16 GB RAM" },
  { label: "Graphics", min: "GTX 1060 6GB", rec: "RTX 3070" },
  { label: "Storage", min: "70 GB SSD", rec: "70 GB NVMe SSD" },
];

const stores: StoreLink[] = [
  { store: "Steam", url: "https://store.steampowered.com/" },
  { store: "Epic Games Store", url: "https://store.epicgames.com/" },
  { store: "GOG", url: "https://www.gog.com/" },
  { store: "PlayStation Store", url: "https://store.playstation.com/" },
  { store: "Microsoft Store", url: "https://www.xbox.com/games" },
  { store: "Nintendo eShop", url: "https://www.nintendo.com/store/" },
];

export const games: Game[] = [
  {
    id: "ashen-crown",
    title: "Ashen Crown",
    tagline: "Shadow of the Ember Throne",
    description:
      "A punishing dark-fantasy action RPG set across a shattered kingdom. Forge your own legend through deliberate, weighty combat and a world that rewards the curious.",
    cover: cover1,
    banner: hero1,
    rating: 9.4,
    reviews: 128420,
    price: 59.99,
    discount: 25,
    releaseDate: "2025-11-14",
    year: 2025,
    developer: "Emberlight Studios",
    publisher: "Northgate Interactive",
    genres: ["Action", "RPG", "Souls-like"],
    platforms: ["PC", "PS5", "Xbox Series X"],
    tags: ["Open World", "Difficult", "Atmospheric", "Story Rich"],
    esrb: "M (Mature 17+)",
    multiplayer: true,
    coop: true,
    freeToPay: false,
    vr: false,
    earlyAccess: false,
    controller: true,
    languages: ["English", "French", "German", "Japanese", "Spanish"],
    features: ["Single Player", "Online Co-op", "Achievements", "Cloud Saves", "Ultrawide Support"],
    stores,
    requirements: req,
  },
  {
    id: "verdant-ruin",
    title: "Verdant Ruin",
    tagline: "Reclaim the wild city",
    description:
      "A cinematic survival-adventure through a reclaimed metropolis. Craft, hunt and negotiate your way through fractured factions in a living, breathing overgrown world.",
    cover: cover2,
    banner: hero1,
    rating: 8.9,
    reviews: 74210,
    price: 49.99,
    releaseDate: "2026-02-20",
    year: 2026,
    developer: "Wildline Games",
    publisher: "Aurora Publishing",
    genres: ["Action", "Adventure", "Survival"],
    platforms: ["PC", "PS5"],
    tags: ["Open World", "Crafting", "Post-apocalyptic", "Female Protagonist"],
    esrb: "M (Mature 17+)",
    multiplayer: false,
    coop: false,
    freeToPay: false,
    vr: false,
    earlyAccess: false,
    controller: true,
    languages: ["English", "Portuguese", "Italian", "Korean"],
    features: ["Single Player", "Photo Mode", "Achievements", "HDR"],
    stores,
    requirements: req,
  },
  {
    id: "neon-drift",
    title: "Neon Drift 2077",
    tagline: "Own every rain-slick street",
    description:
      "High-velocity street racing in a sprawling neon megacity. Tune, drift, and outrun rival crews across a persistent open world with seamless online lobbies.",
    cover: cover3,
    banner: hero2,
    rating: 8.7,
    reviews: 96140,
    price: 39.99,
    discount: 50,
    releaseDate: "2025-06-05",
    year: 2025,
    developer: "Vertex Motion",
    publisher: "Hypergrid",
    genres: ["Racing", "Open World"],
    platforms: ["PC", "PS5", "Xbox Series X"],
    tags: ["Cyberpunk", "Multiplayer", "Customization", "Fast-Paced"],
    esrb: "T (Teen)",
    multiplayer: true,
    coop: true,
    freeToPay: false,
    vr: true,
    earlyAccess: false,
    controller: true,
    languages: ["English", "Japanese", "Chinese", "German"],
    features: ["Online Multiplayer", "Split Screen", "VR Support", "Achievements"],
    stores,
    requirements: req,
  },
  {
    id: "lumen-hollow",
    title: "Lumen Hollow",
    tagline: "A tiny lantern against a vast night",
    description:
      "A hand-painted cozy exploration game about mapping a glowing forest, befriending its creatures, and slowly bringing light back to a forgotten valley.",
    cover: cover4,
    banner: hero1,
    rating: 9.1,
    reviews: 31280,
    price: 0,
    releaseDate: "2026-01-09",
    year: 2026,
    developer: "Paper Fox Collective",
    publisher: "Paper Fox Collective",
    genres: ["Indie", "Adventure", "Puzzle"],
    platforms: ["PC", "Switch"],
    tags: ["Cozy", "Hand-drawn", "Relaxing", "Exploration"],
    esrb: "E (Everyone)",
    multiplayer: false,
    coop: true,
    freeToPay: true,
    vr: false,
    earlyAccess: false,
    controller: true,
    languages: ["English", "Spanish", "Japanese"],
    features: ["Single Player", "Local Co-op", "Controller Support"],
    stores,
    requirements: req,
  },
  {
    id: "iron-vanguard",
    title: "Iron Vanguard",
    tagline: "Pilot the storm",
    description:
      "Command a customizable mech squad in tactical, destructible arenas. Layered loadouts, ranked seasons and a full campaign of desert warfare.",
    cover: cover5,
    banner: hero3,
    rating: 8.4,
    reviews: 58730,
    price: 34.99,
    releaseDate: "2026-05-22",
    year: 2026,
    developer: "Redshift Works",
    publisher: "Northgate Interactive",
    genres: ["Action", "Shooter", "Strategy"],
    platforms: ["PC", "Xbox Series X"],
    tags: ["Mechs", "Competitive", "Sci-fi", "Destruction"],
    esrb: "T (Teen)",
    multiplayer: true,
    coop: true,
    freeToPay: false,
    vr: false,
    earlyAccess: true,
    controller: true,
    languages: ["English", "Russian", "Chinese"],
    features: ["Online Multiplayer", "Ranked Play", "Early Access", "Achievements"],
    stores,
    requirements: req,
  },
  {
    id: "crimson-blade",
    title: "Crimson Blade",
    tagline: "Two swords, one moon",
    description:
      "A stylish samurai duelling game built on precise parries and readable tells. Feudal landscapes rendered in painterly detail with a haunting original score.",
    cover: cover6,
    banner: hero1,
    rating: 9.2,
    reviews: 87120,
    price: 44.99,
    discount: 15,
    releaseDate: "2025-09-30",
    year: 2025,
    developer: "Kagerou Interactive",
    publisher: "Aurora Publishing",
    genres: ["Action", "Fighting"],
    platforms: ["PC", "PS5", "Switch"],
    tags: ["Samurai", "Difficult", "Stylized", "PvP"],
    esrb: "M (Mature 17+)",
    multiplayer: true,
    coop: false,
    freeToPay: false,
    vr: false,
    earlyAccess: false,
    controller: true,
    languages: ["English", "Japanese", "French"],
    features: ["Single Player", "Online PvP", "Achievements", "Photo Mode"],
    stores,
    requirements: req,
  },
];

export const heroSlides = [
  {
    id: "ashen-crown",
    label: "Featured",
    title: "Ashen Crown",
    subtitle: "Shadow of the Ember Throne",
    blurb: "An all-new adventure in a shattered kingdom. Rise, ash-born.",
    image: hero1,
    tags: ["Action", "RPG", "Open World", "Souls-like"],
  },
  {
    id: "neon-drift",
    label: "Trending",
    title: "Neon Drift 2077",
    subtitle: "Own every rain-slick street",
    blurb: "Tune, drift and outrun rival crews in a persistent neon megacity.",
    image: hero2,
    tags: ["Racing", "Cyberpunk", "Multiplayer"],
  },
  {
    id: "iron-vanguard",
    label: "Early Access",
    title: "Iron Vanguard",
    subtitle: "Pilot the storm",
    blurb: "Command a customizable mech squad across destructible arenas.",
    image: hero3,
    tags: ["Shooter", "Mechs", "Sci-fi"],
  },
];

export const genres = [
  "Action",
  "RPG",
  "Adventure",
  "Shooter",
  "Strategy",
  "Racing",
  "Indie",
  "Puzzle",
  "Survival",
  "Fighting",
];

export const platforms = ["PC", "PS5", "Xbox Series X", "Switch"];

export const allTags = [
  "Open World",
  "Story Rich",
  "Cozy",
  "Difficult",
  "Cyberpunk",
  "Multiplayer",
  "Atmospheric",
  "Crafting",
  "Competitive",
  "Stylized",
];

export const chatMessages = [
  { user: "ShadowHunter", time: "2 min ago", text: "Anyone up for raiding tonight in Iron Vanguard?" },
  { user: "PixelNinja", time: "1 min ago", text: "Yeah! I'm in, add me." },
  { user: "GameKnight", time: "just now", text: "Verdant Ruin's photo mode is unreal." },
  { user: "LunaXP", time: "just now", text: "Check out these deals on Crimson Blade!" },
];

export const listings = [
  { title: "Ashen Crown (PC) — Steam Key", meta: "New | Global", seller: "GameStore", price: 32 },
  { title: "Retro Controller — Limited Edition", meta: "Used | Like New", seller: "KratosFan", price: 48 },
  { title: "Game Pass Ultimate — 3 Months", meta: "Digital subscription", seller: "GameHub", price: 22 },
  { title: "Crimson Blade Artbook", meta: "Sealed | Collector", seller: "ProGamer", price: 45 },
];

export const groupsList = [
  { name: "PC Gamers", members: "12.3K members" },
  { name: "PlayStation Community", members: "8.7K members" },
  { name: "Indie Games Hub", members: "5.2K members" },
  { name: "Speedrun Lab", members: "3.1K members" },
];

export const newsItems = [
  { title: "Ashen Crown patch 1.4 adds a brutal new boss rush", tag: "Update", time: "2h ago" },
  { title: "Neon Drift free weekend starts Friday", tag: "Event", time: "5h ago" },
  { title: "Indie Spotlight: five hand-drawn worlds worth your time", tag: "Feature", time: "1d ago" },
  { title: "Iron Vanguard ranked season 3 rewards revealed", tag: "Esports", time: "2d ago" },
];

export const eventsList = [
  { title: "GameSphere Summer Showcase", date: "Jun 12", place: "Online" },
  { title: "Crimson Blade Community Tournament", date: "Jun 20", place: "Discord" },
  { title: "Indie Dev AMA: Paper Fox Collective", date: "Jul 02", place: "Forums" },
];

export const getGame = (id: string) => games.find((g) => g.id === id);
