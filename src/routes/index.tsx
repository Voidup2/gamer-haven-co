import { createFileRoute, Link } from "@tanstack/react-router";
import { ArrowRight, Flame, Sparkles, Star, TrendingUp } from "lucide-react";
import { type ReactNode } from "react";

import { GameCard } from "@/components/GameCard";
import { HeroSlider } from "@/components/HeroSlider";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import {
  chatMessages,
  eventsList,
  games,
  genres,
  groupsList,
  listings,
  newsItems,
  platforms,
} from "@/data/games";

export const Route = createFileRoute("/")({
  head: () => ({
    meta: [
      { title: "GameSphere — Discover Games, Deals & Gaming Community" },
      {
        name: "description",
        content:
          "Discover trending, upcoming and top-rated games, compare official store prices, join the community chat and browse the gaming marketplace on GameSphere.",
      },
      { property: "og:title", content: "GameSphere — Discover Games, Deals & Gaming Community" },
      {
        property: "og:description",
        content:
          "Discover trending, upcoming and top-rated games, compare official store prices and join the gaming community.",
      },
    ],
  }),
  component: Home,
});

function Row({
  title,
  icon,
  children,
}: {
  title: string;
  icon?: ReactNode;
  children: ReactNode;
}) {
  return (
    <section className="space-y-4">
      <div className="grid grid-cols-[minmax(0,1fr)_auto] items-center gap-3">
        <h2 className="flex min-w-0 items-center gap-2 truncate text-lg font-bold sm:text-xl">
          {icon}
          {title}
        </h2>
        <Button asChild variant="ghost" size="sm" className="shrink-0 rounded-xl">
          <Link to="/browse">
            View all <ArrowRight className="size-4" aria-hidden />
          </Link>
        </Button>
      </div>
      {children}
    </section>
  );
}

function Grid({ items }: { items: typeof games }) {
  return (
    <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 xl:grid-cols-5">
      {items.map((g) => (
        <GameCard key={g.id} game={g} />
      ))}
    </div>
  );
}

function Home() {
  const trending = [...games].sort((a, b) => b.reviews - a.reviews);
  const topRated = [...games].sort((a, b) => b.rating - a.rating);
  const upcoming = games.filter((g) => g.year >= 2026);
  const free = games.filter((g) => g.freeToPay);
  const deals = games.filter((g) => g.discount);

  return (
    <div className="grid gap-6 p-4 sm:p-6 xl:grid-cols-[minmax(0,1fr)_340px]">
      <div className="min-w-0 space-y-10">
        <HeroSlider />

        <Row title="Trending Now" icon={<TrendingUp className="size-5 text-primary" aria-hidden />}>
          <Grid items={trending} />
        </Row>

        <Row title="Top Rated" icon={<Star className="size-5 text-warning" aria-hidden />}>
          <Grid items={topRated.slice(0, 5)} />
        </Row>

        <Row title="Upcoming Releases" icon={<Sparkles className="size-5 text-neon" aria-hidden />}>
          <Grid items={upcoming} />
        </Row>

        <Row title="Deals & Discounts" icon={<Flame className="size-5 text-destructive" aria-hidden />}>
          <Grid items={deals} />
        </Row>

        {free.length > 0 && (
          <Row title="Free to Play">
            <Grid items={free} />
          </Row>
        )}

        <section className="space-y-4">
          <h2 className="text-lg font-bold sm:text-xl">Browse by Genre</h2>
          <div className="flex flex-wrap gap-2">
            {genres.map((g) => (
              <Link
                key={g}
                to="/browse"
                search={{ genre: g }}
                className="glass surface-hover rounded-xl px-4 py-2 text-sm font-medium"
              >
                {g}
              </Link>
            ))}
          </div>
          <h2 className="pt-2 text-lg font-bold sm:text-xl">Browse by Platform</h2>
          <div className="flex flex-wrap gap-2">
            {platforms.map((p) => (
              <Link
                key={p}
                to="/browse"
                className="glass surface-hover rounded-xl px-4 py-2 text-sm font-medium"
              >
                {p}
              </Link>
            ))}
          </div>
        </section>

        <section className="grid gap-4 md:grid-cols-2">
          <div className="glass rounded-2xl p-5">
            <h2 className="text-lg font-bold">Latest Gaming News</h2>
            <ul className="mt-4 space-y-4">
              {newsItems.map((n) => (
                <li key={n.title} className="flex gap-3">
                  <Badge variant="secondary" className="h-fit rounded-md text-[10px]">
                    {n.tag}
                  </Badge>
                  <div className="min-w-0">
                    <p className="truncate text-sm font-medium">{n.title}</p>
                    <p className="text-xs text-muted-foreground">{n.time}</p>
                  </div>
                </li>
              ))}
            </ul>
          </div>
          <div className="glass rounded-2xl p-5">
            <h2 className="text-lg font-bold">Upcoming Events</h2>
            <ul className="mt-4 space-y-4">
              {eventsList.map((e) => (
                <li key={e.title} className="flex items-center gap-3">
                  <span className="grid size-11 shrink-0 place-items-center rounded-xl bg-primary/15 text-xs font-bold text-primary">
                    {e.date}
                  </span>
                  <div className="min-w-0">
                    <p className="truncate text-sm font-medium">{e.title}</p>
                    <p className="text-xs text-muted-foreground">{e.place}</p>
                  </div>
                </li>
              ))}
            </ul>
          </div>
        </section>
      </div>

      <aside className="space-y-6">
        <div className="glass rounded-2xl p-4">
          <div className="flex items-center justify-between">
            <h2 className="font-bold">Live Chat</h2>
            <Link to="/community" className="text-xs text-primary hover:underline">
              See all
            </Link>
          </div>
          <ul className="mt-4 space-y-4">
            {chatMessages.map((m) => (
              <li key={m.user} className="flex gap-3">
                <Avatar className="size-8 shrink-0">
                  <AvatarFallback className="bg-secondary text-[10px]">
                    {m.user.slice(0, 2).toUpperCase()}
                  </AvatarFallback>
                </Avatar>
                <div className="min-w-0">
                  <p className="text-xs">
                    <span className="font-semibold text-foreground">{m.user}</span>{" "}
                    <span className="text-muted-foreground">{m.time}</span>
                  </p>
                  <p className="text-sm text-muted-foreground">{m.text}</p>
                </div>
              </li>
            ))}
          </ul>
          <form
            className="mt-4 flex gap-2"
            onSubmit={(e) => {
              e.preventDefault();
            }}
          >
            <Input aria-label="Message" placeholder="Type a message..." className="rounded-xl" />
            <Button type="submit" className="rounded-xl">
              Send
            </Button>
          </form>
        </div>

        <div className="glass rounded-2xl p-4">
          <div className="flex items-center justify-between">
            <h2 className="font-bold">Marketplace</h2>
            <Link to="/marketplace" className="text-xs text-primary hover:underline">
              View all
            </Link>
          </div>
          <ul className="mt-4 space-y-3">
            {listings.map((l) => (
              <li key={l.title} className="flex items-center gap-3 rounded-xl bg-secondary/40 p-3">
                <div className="min-w-0 flex-1">
                  <p className="truncate text-sm font-medium">{l.title}</p>
                  <p className="truncate text-xs text-muted-foreground">
                    {l.meta} · {l.seller}
                  </p>
                </div>
                <span className="shrink-0 text-sm font-bold text-success">${l.price}</span>
              </li>
            ))}
          </ul>
          <Button asChild className="mt-4 w-full rounded-xl">
            <Link to="/marketplace">+ Post an item</Link>
          </Button>
        </div>

        <div className="glass rounded-2xl p-4">
          <h2 className="font-bold">Active Groups</h2>
          <ul className="mt-4 space-y-3">
            {groupsList.map((g) => (
              <li key={g.name} className="flex items-center gap-3">
                <Avatar className="size-9">
                  <AvatarFallback className="bg-secondary text-[10px]">
                    {g.name.slice(0, 2).toUpperCase()}
                  </AvatarFallback>
                </Avatar>
                <div className="min-w-0">
                  <p className="truncate text-sm font-medium">{g.name}</p>
                  <p className="text-xs text-muted-foreground">{g.members}</p>
                </div>
              </li>
            ))}
          </ul>
        </div>
      </aside>
    </div>
  );
}
