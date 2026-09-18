import { api } from "./client";

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

export async function getGames(params: GameSearchParams = {}) {
  const query = toQuery({
    page: 0,
    size: 24,
    sortBy: "title",
    direction: "asc",
    ...params,
  });

  return api.get<GamePage>(`/games?${query}`);
}

export async function getGame(id: string) {
  return api.get<ApiGame>(`/games/${encodeURIComponent(id)}`);
}
