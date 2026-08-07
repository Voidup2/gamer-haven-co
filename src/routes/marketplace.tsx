import { createFileRoute } from "@tanstack/react-router";
import { Flag, Heart, ShieldCheck, Star } from "lucide-react";
import { useState } from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { listings } from "@/data/games";

export const Route = createFileRoute("/marketplace")({
  head: () => ({
    meta: [
      { title: "Marketplace — Buy & Sell Gaming Items | GameSphere" },
      {
        name: "description",
        content:
          "Browse player listings for gaming keys, collectibles and accessories. Search, filter, message sellers and check seller ratings on the GameSphere marketplace.",
      },
      { property: "og:title", content: "GameSphere Marketplace" },
      {
        property: "og:description",
        content: "Player-to-player listings for gaming keys, collectibles and accessories.",
      },
    ],
  }),
  component: Marketplace,
});

const categories = ["All", "Digital Keys", "Subscriptions", "Accessories", "Collectibles"];

function Marketplace() {
  const [category, setCategory] = useState("All");
  const [query, setQuery] = useState("");

  const items = [...listings, ...listings].filter((l) =>
    l.title.toLowerCase().includes(query.trim().toLowerCase()),
  );

  return (
    <div className="space-y-6 p-4 sm:p-6">
      <header className="grid grid-cols-[minmax(0,1fr)_auto] items-center gap-4">
        <div className="min-w-0">
          <h1 className="text-2xl font-black sm:text-3xl">Marketplace</h1>
          <p className="text-sm text-muted-foreground">
            Player listings reviewed against platform policy before going live.
          </p>
        </div>
        <Button className="shrink-0 rounded-xl">Post an item</Button>
      </header>

      <div className="flex flex-wrap items-center gap-3">
        <Input
          value={query}
          onChange={(e) => setQuery(e.target.value)}
          aria-label="Search listings"
          placeholder="Search listings..."
          className="h-11 max-w-sm rounded-xl"
        />
        <div className="flex flex-wrap gap-2">
          {categories.map((c) => (
            <button
              key={c}
              onClick={() => setCategory(c)}
              className={`rounded-xl px-3 py-2 text-sm font-medium transition-colors ${
                category === c ? "bg-primary text-primary-foreground" : "bg-secondary/60 hover:bg-secondary"
              }`}
            >
              {c}
            </button>
          ))}
        </div>
      </div>

      <div className="grid gap-4 sm:grid-cols-2 xl:grid-cols-3">
        {items.map((l, i) => (
          <article key={`${l.title}-${i}`} className="glass surface-hover rounded-2xl p-5">
            <div className="flex items-start justify-between gap-3">
              <div className="min-w-0">
                <h2 className="truncate font-semibold">{l.title}</h2>
                <p className="truncate text-xs text-muted-foreground">{l.meta}</p>
              </div>
              <span className="shrink-0 text-lg font-black text-success">${l.price}</span>
            </div>
            <div className="mt-4 flex items-center gap-2 text-xs text-muted-foreground">
              <ShieldCheck className="size-4 text-neon" aria-hidden />
              <span>{l.seller}</span>
              <span className="inline-flex items-center gap-1 text-warning">
                <Star className="size-3 fill-warning" aria-hidden /> 4.8
              </span>
              <Badge variant="secondary" className="ml-auto rounded-md text-[10px]">
                Verified
              </Badge>
            </div>
            <div className="mt-4 flex gap-2">
              <Button className="flex-1 rounded-xl">Message seller</Button>
              <Button variant="outline" size="icon" className="rounded-xl" aria-label="Save listing">
                <Heart className="size-4" />
              </Button>
              <Button variant="outline" size="icon" className="rounded-xl" aria-label="Report listing">
                <Flag className="size-4" />
              </Button>
            </div>
          </article>
        ))}
      </div>
    </div>
  );
}
