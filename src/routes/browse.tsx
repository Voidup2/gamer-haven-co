import { createFileRoute } from "@tanstack/react-router";
import { SlidersHorizontal } from "lucide-react";
import { useMemo, useState } from "react";

import { GameCard } from "@/components/GameCard";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Checkbox } from "@/components/ui/checkbox";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select";
import { games, genres, platforms } from "@/data/games";

type Search = { genre?: string | undefined; q?: string | undefined };

export const Route = createFileRoute("/browse")({
  validateSearch: (search: Record<string, unknown>): Search => ({
    genre: typeof search["genre"] === "string" ? search["genre"] : undefined,
    q: typeof search["q"] === "string" ? search["q"] : undefined,
  }),

  head: () => ({
    meta: [
      { title: "Browse Games — Filter by Genre, Platform & Price | GameSphere" },
      {
        name: "description",
        content:
          "Search and filter thousands of games by genre, platform, rating, price, multiplayer support and more on GameSphere.",
      },
      { property: "og:title", content: "Browse Games | GameSphere" },
      {
        property: "og:description",
        content: "Filter games by genre, platform, rating, price and features.",
      },
    ],
  }),
  component: Browse,
});

function Browse() {
  const search = Route.useSearch();
  const [query, setQuery] = useState(search.q ?? "");
  const [genre, setGenre] = useState(search.genre ?? "all");
  const [platform, setPlatform] = useState("all");
  const [sort, setSort] = useState("popular");
  const [multiplayer, setMultiplayer] = useState(false);
  const [coop, setCoop] = useState(false);
  const [freeOnly, setFreeOnly] = useState(false);
  const [showFilters, setShowFilters] = useState(false);

  const results = useMemo(() => {
    const q = query.trim().toLowerCase();
    let list = games.filter((g) => {
      const haystack = [g.title, g.developer, g.publisher, ...g.genres, ...g.tags, ...g.platforms]
        .join(" ")
        .toLowerCase();
      if (q && !haystack.includes(q)) return false;
      if (genre !== "all" && !g.genres.includes(genre)) return false;
      if (platform !== "all" && !g.platforms.includes(platform)) return false;
      if (multiplayer && !g.multiplayer) return false;
      if (coop && !g.coop) return false;
      if (freeOnly && !g.freeToPay) return false;
      return true;
    });
    list = [...list].sort((a, b) => {
      if (sort === "rating") return b.rating - a.rating;
      if (sort === "recent") return b.releaseDate.localeCompare(a.releaseDate);
      if (sort === "alpha") return a.title.localeCompare(b.title);
      return b.reviews - a.reviews;
    });
    return list;
  }, [query, genre, platform, sort, multiplayer, coop, freeOnly]);

  return (
    <div className="space-y-6 p-4 sm:p-6">
      <header className="space-y-2">
        <h1 className="text-2xl font-black sm:text-3xl">Browse Games</h1>
        <p className="text-sm text-muted-foreground">
          {results.length} game{results.length === 1 ? "" : "s"} matching your filters
        </p>
      </header>

      <div className="grid grid-cols-[minmax(0,1fr)_auto] gap-3 sm:flex sm:items-center">
        <Input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          placeholder="Search by title, developer, publisher, tag..."
          aria-label="Search games"
          className="h-11 rounded-xl sm:max-w-md"
        />
        <Button
          variant="outline"
          className="rounded-xl lg:hidden"
          onClick={() => setShowFilters((v) => !v)}
          aria-expanded={showFilters}
        >
          <SlidersHorizontal className="size-4" aria-hidden /> Filters
        </Button>
        <Select value={sort} onValueChange={setSort}>
          <SelectTrigger className="hidden h-11 w-48 rounded-xl sm:flex" aria-label="Sort by">
            <SelectValue />
          </SelectTrigger>
          <SelectContent>
            <SelectItem value="popular">Most Popular</SelectItem>
            <SelectItem value="rating">Highest Rated</SelectItem>
            <SelectItem value="recent">Recently Released</SelectItem>
            <SelectItem value="alpha">Alphabetical</SelectItem>
          </SelectContent>
        </Select>
      </div>

      <div className="grid gap-6 lg:grid-cols-[260px_minmax(0,1fr)]">
        <aside className={`${showFilters ? "block" : "hidden"} lg:block`}>
          <div className="glass space-y-5 rounded-2xl p-4">
            <div className="space-y-2">
              <Label>Genre</Label>
              <Select value={genre} onValueChange={setGenre}>
                <SelectTrigger className="rounded-xl">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All genres</SelectItem>
                  {genres.map((g) => (
                    <SelectItem key={g} value={g}>
                      {g}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <div className="space-y-2">
              <Label>Platform</Label>
              <Select value={platform} onValueChange={setPlatform}>
                <SelectTrigger className="rounded-xl">
                  <SelectValue />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All platforms</SelectItem>
                  {platforms.map((p) => (
                    <SelectItem key={p} value={p}>
                      {p}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            <fieldset className="space-y-3">
              <legend className="text-sm font-medium">Features</legend>
              <div className="flex items-center gap-2">
                <Checkbox
                  id="mp"
                  checked={multiplayer}
                  onCheckedChange={(v) => setMultiplayer(v === true)}
                />
                <Label htmlFor="mp" className="font-normal">
                  Multiplayer
                </Label>
              </div>
              <div className="flex items-center gap-2">
                <Checkbox id="coop" checked={coop} onCheckedChange={(v) => setCoop(v === true)} />
                <Label htmlFor="coop" className="font-normal">
                  Co-op
                </Label>
              </div>
              <div className="flex items-center gap-2">
                <Checkbox
                  id="free"
                  checked={freeOnly}
                  onCheckedChange={(v) => setFreeOnly(v === true)}
                />
                <Label htmlFor="free" className="font-normal">
                  Free to play
                </Label>
              </div>
            </fieldset>
            <Button
              variant="outline"
              className="w-full rounded-xl"
              onClick={() => {
                setGenre("all");
                setPlatform("all");
                setMultiplayer(false);
                setCoop(false);
                setFreeOnly(false);
                setQuery("");
              }}
            >
              Reset filters
            </Button>
          </div>
        </aside>

        <div>
          {results.length === 0 ? (
            <div className="glass rounded-2xl p-10 text-center">
              <p className="font-semibold">No games match those filters</p>
              <p className="mt-1 text-sm text-muted-foreground">
                Try removing a filter or searching a different term.
              </p>
            </div>
          ) : (
            <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 2xl:grid-cols-4">
              {results.map((g) => (
                <GameCard key={g.id} game={g} />
              ))}
            </div>
          )}
          <div className="mt-6 flex flex-wrap gap-2">
            {genres.slice(0, 6).map((g) => (
              <Badge
                key={g}
                variant="secondary"
                className="cursor-pointer rounded-lg"
                onClick={() => setGenre(g)}
              >
                {g}
              </Badge>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
}
