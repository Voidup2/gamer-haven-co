import { Link } from "@tanstack/react-router";
import { ChevronLeft, ChevronRight, Heart } from "lucide-react";
import { useCallback, useEffect, useState } from "react";

import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { heroSlides } from "@/data/games";
import { cn } from "@/lib/utils";

export function HeroSlider() {
  const [index, setIndex] = useState(0);
  const count = heroSlides.length;

  const go = useCallback((next: number) => setIndex(((next % count) + count) % count), [count]);

  useEffect(() => {
    const timer = setInterval(() => setIndex((i) => (i + 1) % count), 7000);
    return () => clearInterval(timer);
  }, [count]);

  return (
    <section
      aria-roledescription="carousel"
      aria-label="Featured games"
      className="glass relative overflow-hidden rounded-3xl"
    >
      <div className="relative aspect-[16/10] w-full sm:aspect-[21/9]">
        {heroSlides.map((slide, i) => (
          <div
            key={slide.id}
            aria-hidden={i !== index}
            className={cn(
              "absolute inset-0 transition-opacity duration-700",
              i === index ? "opacity-100" : "pointer-events-none opacity-0",
            )}
          >
            <img
              src={slide.image}
              alt={`${slide.title} key art`}
              width={1600}
              height={912}
              className="size-full object-cover"
            />
            <div className="absolute inset-0 bg-gradient-to-r from-background via-background/70 to-transparent" />
            <div className="absolute inset-0 flex flex-col justify-end gap-3 p-5 sm:justify-center sm:gap-4 sm:p-10 md:max-w-xl">
              <Badge className="w-fit rounded-md bg-primary/90 uppercase tracking-wider">
                {slide.label}
              </Badge>
              <h2 className="text-3xl font-black uppercase leading-none tracking-tight sm:text-5xl">
                {slide.title}
              </h2>
              <p className="text-sm font-medium text-muted-foreground sm:text-lg">{slide.subtitle}</p>
              <p className="hidden text-sm text-muted-foreground sm:block">{slide.blurb}</p>
              <div className="flex flex-wrap gap-1.5">
                {slide.tags.map((t) => (
                  <Badge key={t} variant="secondary" className="rounded-md text-[11px]">
                    {t}
                  </Badge>
                ))}
              </div>
              <div className="flex flex-wrap gap-2 pt-1">
                <Button asChild className="rounded-xl">
                  <Link to="/game/$gameId" params={{ gameId: slide.id }}>
                    View details
                  </Link>
                </Button>
                <Button variant="outline" className="rounded-xl">
                  <Heart className="size-4" aria-hidden /> Add to wishlist
                </Button>
              </div>
            </div>
          </div>
        ))}
      </div>

      <div className="absolute bottom-4 right-4 flex items-center gap-2">
        <Button
          variant="outline"
          size="icon"
          aria-label="Previous slide"
          className="rounded-full bg-background/60"
          onClick={() => go(index - 1)}
        >
          <ChevronLeft className="size-4" />
        </Button>
        <Button
          variant="outline"
          size="icon"
          aria-label="Next slide"
          className="rounded-full bg-background/60"
          onClick={() => go(index + 1)}
        >
          <ChevronRight className="size-4" />
        </Button>
      </div>

      <div className="absolute bottom-6 left-5 flex gap-1.5 sm:left-10">
        {heroSlides.map((s, i) => (
          <button
            key={s.id}
            aria-label={`Go to slide ${i + 1}`}
            onClick={() => go(i)}
            className={cn(
              "h-1.5 rounded-full transition-all",
              i === index ? "w-6 bg-primary" : "w-1.5 bg-muted-foreground/50",
            )}
          />
        ))}
      </div>
    </section>
  );
}
