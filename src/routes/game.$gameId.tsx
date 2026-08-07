import { createFileRoute, notFound } from "@tanstack/react-router";
import { Heart, ExternalLink, Star } from "lucide-react";

import { GameCard } from "@/components/GameCard";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Separator } from "@/components/ui/separator";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { games, getGame } from "@/data/games";

export const Route = createFileRoute("/game/$gameId")({
  loader: ({ params }) => {
    const game = getGame(params.gameId);
    if (!game) throw notFound();
    return { game };
  },
  head: ({ loaderData }) => {
    if (!loaderData) {
      return {
        meta: [{ title: "Game not found | GameSphere" }, { name: "robots", content: "noindex" }],
      };
    }
    const { game } = loaderData;
    return {
      meta: [
        { title: `${game.title} — Reviews, Price & Store Links | GameSphere` },
        { name: "description", content: game.description.slice(0, 155) },
        { property: "og:title", content: `${game.title} | GameSphere` },
        { property: "og:description", content: game.description.slice(0, 155) },
      ],
    };
  },
  component: GameDetail,
});

const reviews = [
  {
    user: "ShadowHunter",
    rating: 9.5,
    text: "The combat feels heavy in the best way. Every encounter reads like a puzzle you solve with muscle memory.",
  },
  {
    user: "PixelNinja",
    rating: 8.0,
    text: "Gorgeous world and a great soundtrack. Performance on mid-range hardware needs another patch though.",
  },
  {
    user: "LunaXP",
    rating: 9.0,
    text: "Around 60 hours in and still finding hidden areas. The exploration payoff is genuinely excellent.",
  },
];

function GameDetail() {
  const { game } = Route.useLoaderData();
  const finalPrice = game.discount ? game.price * (1 - game.discount / 100) : game.price;
  const similar = games.filter((g) => g.id !== game.id).slice(0, 5);

  return (
    <article>
      <div className="relative h-56 w-full overflow-hidden sm:h-80">
        <img
          src={game.banner}
          alt={`${game.title} banner art`}
          width={1600}
          height={912}
          className="size-full object-cover"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-background via-background/60 to-transparent" />
      </div>

      <div className="mx-auto -mt-24 max-w-7xl space-y-8 px-4 pb-10 sm:px-6">
        <header className="grid gap-5 sm:grid-cols-[200px_minmax(0,1fr)]">
          <img
            src={game.cover}
            alt={`${game.title} cover art`}
            width={640}
            height={860}
            className="hidden w-full rounded-2xl object-cover shadow-lg sm:block"
          />
          <div className="min-w-0 space-y-3">
            <h1 className="text-3xl font-black sm:text-4xl">{game.title}</h1>
            <p className="text-muted-foreground">{game.tagline}</p>
            <div className="flex flex-wrap items-center gap-2">
              <span className="inline-flex items-center gap-1 rounded-lg bg-secondary px-2 py-1 text-sm font-semibold">
                <Star className="size-4 fill-warning text-warning" aria-hidden />
                {game.rating.toFixed(1)}
              </span>
              <span className="text-sm text-muted-foreground">
                {game.reviews.toLocaleString()} reviews
              </span>
              <Badge variant="secondary" className="rounded-md">
                {game.esrb}
              </Badge>
            </div>
            <div className="flex flex-wrap gap-1.5">
              {[...game.genres, ...game.tags].map((t) => (
                <Badge key={t} variant="secondary" className="rounded-md text-[11px]">
                  {t}
                </Badge>
              ))}
            </div>
            <div className="flex flex-wrap items-center gap-3 pt-2">
              <span className="text-2xl font-black">
                {game.freeToPay ? "Free to Play" : `$${finalPrice.toFixed(2)}`}
              </span>
              {game.discount && (
                <span className="rounded-md bg-success/90 px-2 py-1 text-xs font-bold text-accent-foreground">
                  -{game.discount}%
                </span>
              )}
              <Button className="rounded-xl">Buy on Steam</Button>
              <Button variant="outline" className="rounded-xl">
                <Heart className="size-4" aria-hidden /> Wishlist
              </Button>
            </div>
          </div>
        </header>

        <Tabs defaultValue="overview">
          <TabsList className="flex-wrap rounded-xl">
            <TabsTrigger value="overview">Overview</TabsTrigger>
            <TabsTrigger value="specs">System Requirements</TabsTrigger>
            <TabsTrigger value="stores">Where to Buy</TabsTrigger>
            <TabsTrigger value="reviews">Reviews</TabsTrigger>
          </TabsList>

          <TabsContent value="overview" className="mt-5 grid gap-6 lg:grid-cols-[minmax(0,1fr)_300px]">
            <div className="glass space-y-4 rounded-2xl p-5">
              <h2 className="text-lg font-bold">About this game</h2>
              <p className="text-sm leading-relaxed text-muted-foreground">{game.description}</p>
              <Separator />
              <h3 className="font-semibold">Features</h3>
              <ul className="grid gap-2 text-sm text-muted-foreground sm:grid-cols-2">
                {game.features.map((f) => (
                  <li key={f}>• {f}</li>
                ))}
              </ul>
              <Separator />
              <h3 className="font-semibold">Supported languages</h3>
              <p className="text-sm text-muted-foreground">{game.languages.join(", ")}</p>
            </div>
            <dl className="glass space-y-3 rounded-2xl p-5 text-sm">
              {[
                ["Developer", game.developer],
                ["Publisher", game.publisher],
                ["Release date", game.releaseDate],
                ["Platforms", game.platforms.join(", ")],
                ["Controller", game.controller ? "Full support" : "Not supported"],
                ["Multiplayer", game.multiplayer ? "Online" : "Single player only"],
                ["VR", game.vr ? "Supported" : "No"],
              ].map(([k, v]) => (
                <div key={k} className="flex justify-between gap-4">
                  <dt className="text-muted-foreground">{k}</dt>
                  <dd className="text-right font-medium">{v}</dd>
                </div>
              ))}
            </dl>
          </TabsContent>

          <TabsContent value="specs" className="mt-5">
            <div className="glass overflow-x-auto rounded-2xl p-5">
              <table className="w-full min-w-lg text-left text-sm">
                <thead>
                  <tr className="text-muted-foreground">
                    <th className="pb-3 font-medium">Component</th>
                    <th className="pb-3 font-medium">Minimum</th>
                    <th className="pb-3 font-medium">Recommended</th>
                  </tr>
                </thead>
                <tbody>
                  {game.requirements.map((r) => (
                    <tr key={r.label} className="border-t border-border/60">
                      <td className="py-3 font-medium">{r.label}</td>
                      <td className="py-3 text-muted-foreground">{r.min}</td>
                      <td className="py-3 text-muted-foreground">{r.rec}</td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </TabsContent>

          <TabsContent value="stores" className="mt-5">
            <div className="grid gap-3 sm:grid-cols-2 lg:grid-cols-3">
              {game.stores.map((s) => (
                <a
                  key={s.store}
                  href={s.url}
                  target="_blank"
                  rel="noopener noreferrer"
                  className="glass surface-hover flex items-center justify-between rounded-2xl p-4"
                >
                  <span className="font-medium">{s.store}</span>
                  <ExternalLink className="size-4 text-muted-foreground" aria-hidden />
                </a>
              ))}
            </div>
            <p className="mt-4 text-xs text-muted-foreground">
              GameSphere links to official storefronts only. All purchases are completed on the
              retailer's site.
            </p>
          </TabsContent>

          <TabsContent value="reviews" className="mt-5 space-y-3">
            {reviews.map((r) => (
              <div key={r.user} className="glass flex gap-3 rounded-2xl p-4">
                <Avatar className="size-10 shrink-0">
                  <AvatarFallback className="bg-secondary text-xs">
                    {r.user.slice(0, 2).toUpperCase()}
                  </AvatarFallback>
                </Avatar>
                <div className="min-w-0">
                  <p className="flex items-center gap-2 text-sm font-semibold">
                    {r.user}
                    <span className="inline-flex items-center gap-1 text-xs text-warning">
                      <Star className="size-3 fill-warning" aria-hidden />
                      {r.rating}
                    </span>
                  </p>
                  <p className="mt-1 text-sm text-muted-foreground">{r.text}</p>
                </div>
              </div>
            ))}
          </TabsContent>
        </Tabs>

        <section className="space-y-4">
          <h2 className="text-lg font-bold sm:text-xl">Similar Games</h2>
          <div className="grid grid-cols-2 gap-4 sm:grid-cols-3 xl:grid-cols-5">
            {similar.map((g) => (
              <GameCard key={g.id} game={g} />
            ))}
          </div>
        </section>
      </div>
    </article>
  );
}
