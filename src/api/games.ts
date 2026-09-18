import cover1 from "@/assets/cover-1.jpg";
import cover2 from "@/assets/cover-2.jpg";
import cover3 from "@/assets/cover-3.jpg";
import cover4 from "@/assets/cover-4.jpg";
import cover5 from "@/assets/cover-5.jpg";
import cover6 from "@/assets/cover-6.jpg";
import hero1 from "@/assets/hero-1.jpg";
import hero2 from "@/assets/hero-2.jpg";
import hero3 from "@/assets/hero-3.jpg";

import { api } from "./client";
import type { Game } from "@/data/games";

export type ApiGame = {
  id: string;
  title: string;
  tagline: string;
  description: string;
  coverUrl: string;
  bannerUrl: string;
  rating: number;
  reviewCount: number;
  price: number;
  discount?: number | null;
  releaseDate: string;
  releaseYear: number;
  developer: string;
  publisher: string;
  esrb: string;
  multiplayer: boolean;
  coop: boolean;
  freeToPlay: boolean;
  vr: boolean;
  earlyAccess: boolean;
  controller: boolean;
  genres: string[];
  platforms: string[];
  tags: string[];
  languages: string[];
  features: string[];
  stores: { store: string; url: string }[];
  requirements: { label: string; min: string; rec: string }[];
};

export type GamePage = {
  content: ApiGame[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
  first: boolean;
  last: boolean;
};

export type GameSearchParams = {
  search?: string;
  genre?: string;
  platform?: string;
  tag?: string;
  multiplayer?: boolean;
  coop?: boolean;
  freeToPlay?: boolean;
  page?: number;
  size?: number;
  sortBy?: string;
  direction?: "asc" | "desc";
};

function toQuery(params: GameSearchParams) {
  const query = new URLSearchParams();

  for (const [key, value] of Object.entries(params)) {
    if (value !== undefined && value !== null && value !== "") {
      query.set(key, String(value));
    }
  }

  return query.toString();
}

const coverFallbacks: Record<string, string> = {
  "ashen-crown": cover1,
  "verdant-ruin": cover2,
  "neon-drift": cover3,
  "lumen-hollow": cover4,
  "iron-vanguard": cover5,
  "crimson-blade": cover6,
};

const bannerFallbacks: Record<string, string> = {
  "ashen-crown": hero1,
  "verdant-ruin": hero1,
  "neon-drift": hero2,
  "lumen-hollow": hero1,
  "iron-vanguard": hero3,
  "crimson-blade": hero1,
};

function mapGame(game: ApiGame): Game {
  return {
    id: game.id,
    title: game.title,
    tagline: game.tagline,
    description: game.description,
    cover: game.coverUrl ?? coverFallbacks[game.id] ?? cover1,
    banner: game.bannerUrl ?? bannerFallbacks[game.id] ?? hero1,
    rating: game.rating,
    reviews: game.reviewCount,
    price: game.price,
    discount: game.discount ?? undefined,
    releaseDate: game.releaseDate,
    year: game.releaseYear,
    developer: game.developer,
    publisher: game.publisher,
    genres: game.genres ?? [],
    platforms: game.platforms ?? [],
    tags: game.tags ?? [],
    esrb: game.esrb,
    multiplayer: game.multiplayer,
    coop: game.coop,
    freeToPay: game.freeToPlay,
    vr: game.vr,
    earlyAccess: game.earlyAccess,
    controller: game.controller,
    languages: game.languages ?? [],
    features: game.features ?? [],
    stores: game.stores ?? [],
    requirements: game.requirements ?? [],
  };
}

export async function getGames(params: GameSearchParams = {}) {
  const query = toQuery({
    page: 0,
    size: 24,
    sortBy: "title",
    direction: "asc",
    ...params,
  });

  const response = await api.get<GamePage>(`/games?${query}`);
  return { ...response, content: response.content.map(mapGame) };
}

export async function getGame(id: string) {
  const response = await api.get<ApiGame>(`/games/${encodeURIComponent(id)}`);
  return mapGame(response);
}
