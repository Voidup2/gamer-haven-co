import { Link } from "@tanstack/react-router";
import { Heart, Star } from "lucide-react";

import { Badge } from "@/components/ui/badge";
import { Skeleton } from "@/components/ui/skeleton";
import type { Game } from "@/data/games";
import { cn } from "@/lib/utils";

export function GameCard({ game, className }: { game: Game; className?: string }) {
  const finalPrice = game.discount ? game.price * (1 - game.discount / 100) : game.price;

  return (
    <Link
      to="/game/$gameId"
      params={{ gameId: game.id }}
      className={cn(
        "glass surface-hover group block overflow-hidden rounded-2xl focus-visible:ring-2 focus-visible:ring-ring",
        className,
      )}
    >
      <div className="relative aspect-[3/4] overflow-hidden">
        <img
          src={game.cover}
          alt={`${game.title} cover art`}
          loading="lazy"
          width={640}
          height={860}
          className="size-full object-cover transition-transform duration-500 group-hover:scale-105"
        />
        <div className="absolute inset-0 bg-gradient-to-t from-background via-background/20 to-transparent" />
        <span className="absolute left-2 top-2 inline-flex items-center gap-1 rounded-lg bg-background/80 px-2 py-1 text-xs font-semibold backdrop-blur">
          <Star className="size-3 fill-warning text-warning" aria-hidden />
          {game.rating.toFixed(1)}
        </span>
        {game.discount && (
          <span className="absolute right-2 top-2 rounded-lg bg-success/90 px-2 py-1 text-xs font-bold text-accent-foreground">
            -{game.discount}%
          </span>
        )}
        <span className="absolute bottom-2 right-2 grid size-8 place-items-center rounded-lg bg-background/70 opacity-0 backdrop-blur transition-opacity group-hover:opacity-100">
          <Heart className="size-4" aria-hidden />
        </span>
      </div>
      <div className="space-y-2 p-3">
        <h3 className="truncate text-sm font-semibold">{game.title}</h3>
        <div className="flex flex-wrap gap-1">
          {game.genres.slice(0, 2).map((g) => (
            <Badge key={g} variant="secondary" className="rounded-md text-[10px] font-medium">
              {g}
            </Badge>
          ))}
        </div>
        <div className="flex items-center justify-between pt-1">
          <span className="text-xs text-muted-foreground">{game.platforms[0]}</span>
          <span className="text-sm font-bold">
            {game.freeToPay ? "Free" : `$${finalPrice.toFixed(2)}`}
          </span>
        </div>
      </div>
    </Link>
  );
}

export function GameCardSkeleton() {
  return (
    <div className="glass overflow-hidden rounded-2xl">
      <Skeleton className="aspect-[3/4] w-full rounded-none" />
      <div className="space-y-2 p-3">
        <Skeleton className="h-4 w-3/4" />
        <Skeleton className="h-3 w-1/2" />
      </div>
    </div>
  );
}
