import { Link, useRouterState } from "@tanstack/react-router";
import {
  Bell,
  Calendar,
  Compass,
  Flame,
  Gamepad2,
  Heart,
  Home,
  Layers,
  LayoutGrid,
  Library,
  MessageSquare,
  Menu,
  Newspaper,
  Search,
  Settings,
  ShoppingBag,
  Sparkles,
  Star,
  Tag,
  Users,
} from "lucide-react";
import { useState, type ReactNode } from "react";

import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Sheet, SheetContent, SheetTitle, SheetTrigger } from "@/components/ui/sheet";
import { cn } from "@/lib/utils";

type NavItem = { label: string; to: string; icon: typeof Home; badge?: string };

const sections: { heading: string; items: NavItem[] }[] = [
  {
    heading: "Discover",
    items: [
      { label: "Home", to: "/", icon: Home },
      { label: "Browse Games", to: "/browse", icon: Compass },
      { label: "Upcoming", to: "/browse", icon: Calendar },
      { label: "Genres", to: "/browse", icon: LayoutGrid },
      { label: "Platforms", to: "/browse", icon: Layers },
      { label: "Top Rated", to: "/browse", icon: Star },
      { label: "New Releases", to: "/browse", icon: Sparkles },
      { label: "Deals", to: "/browse", icon: Tag },
    ],
  },
  {
    heading: "Library",
    items: [
      { label: "Wishlist", to: "/browse", icon: Heart },
      { label: "Favorites", to: "/browse", icon: Flame },
      { label: "My Collection", to: "/browse", icon: Library },
    ],
  },
  {
    heading: "Community",
    items: [
      { label: "Chat", to: "/community", icon: MessageSquare, badge: "New" },
      { label: "Forums", to: "/community", icon: Users },
      { label: "Events", to: "/community", icon: Calendar },
      { label: "News", to: "/community", icon: Newspaper },
      { label: "Marketplace", to: "/marketplace", icon: ShoppingBag },
    ],
  },
];

function SidebarNav({ onNavigate }: { onNavigate?: () => void }) {
  const pathname = useRouterState({ select: (s) => s.location.pathname });

  return (
    <nav aria-label="Main" className="flex h-full flex-col gap-6 overflow-y-auto px-3 py-4">
      {sections.map((section) => (
        <div key={section.heading}>
          <p className="px-3 pb-2 text-[11px] font-semibold uppercase tracking-widest text-muted-foreground">
            {section.heading}
          </p>
          <ul className="space-y-1">
            {section.items.map((item) => {
              const active = pathname === item.to && item.label !== "Upcoming";
              return (
                <li key={item.label}>
                  <Link
                    to={item.to}
                    onClick={onNavigate}
                    className={cn(
                      "flex items-center gap-3 rounded-xl px-3 py-2.5 text-sm font-medium text-muted-foreground transition-colors hover:bg-sidebar-accent hover:text-foreground",
                      active && "bg-primary/15 text-foreground ring-1 ring-primary/40",
                    )}
                  >
                    <item.icon className="size-4 shrink-0" aria-hidden />
                    <span className="truncate">{item.label}</span>
                    {item.badge && (
                      <Badge className="ml-auto bg-primary/20 text-[10px] text-primary-foreground">
                        {item.badge}
                      </Badge>
                    )}
                  </Link>
                </li>
              );
            })}
          </ul>
        </div>
      ))}

      <div className="glass mt-auto rounded-2xl p-4 text-center">
        <p className="text-sm font-semibold gradient-text">Join GameSphere Premium</p>
        <p className="mt-1 text-xs text-muted-foreground">
          Exclusive perks, no ads and early access to upcoming releases.
        </p>
        <Button className="mt-3 w-full" size="sm">
          Go Premium
        </Button>
      </div>
    </nav>
  );
}

export function AppShell({ children }: { children: ReactNode }) {
  const [open, setOpen] = useState(false);

  return (
    <div className="min-h-dvh">
      <header className="sticky top-0 z-40 border-b border-border/60 bg-background/70 backdrop-blur-xl">
        <div className="grid grid-cols-[auto_minmax(0,1fr)_auto] items-center gap-3 px-3 py-3 sm:px-5">
          <div className="flex min-w-0 items-center gap-2">
            <Sheet open={open} onOpenChange={setOpen}>
              <SheetTrigger asChild>
                <Button variant="ghost" size="icon" className="lg:hidden" aria-label="Open menu">
                  <Menu className="size-5" />
                </Button>
              </SheetTrigger>
              <SheetContent side="left" className="w-72 bg-sidebar p-0">
                <SheetTitle className="px-5 pt-5 text-base">GameSphere</SheetTitle>
                <SidebarNav onNavigate={() => setOpen(false)} />
              </SheetContent>
            </Sheet>
            <Link to="/" className="flex items-center gap-2">
              <span
                className="grid size-9 shrink-0 place-items-center rounded-xl"
                style={{ background: "var(--gradient-primary)" }}
              >
                <Gamepad2 className="size-5 text-primary-foreground" aria-hidden />
              </span>
              <span className="hidden text-lg font-bold tracking-tight sm:inline">
                Game<span className="gradient-text">Sphere</span>
              </span>
            </Link>
          </div>

          <div className="relative min-w-0">
            <Search
              className="pointer-events-none absolute left-3 top-1/2 size-4 -translate-y-1/2 text-muted-foreground"
              aria-hidden
            />
            <Input
              type="search"
              aria-label="Search games, genres, developers"
              placeholder="Search games, genres, developers..."
              className="h-10 rounded-xl border-border/70 bg-secondary/60 pl-9"
            />
          </div>

          <div className="flex shrink-0 items-center gap-1 sm:gap-2">
            <Button variant="ghost" size="icon" aria-label="Notifications" className="relative">
              <Bell className="size-5" />
              <span className="absolute right-1.5 top-1.5 size-2 rounded-full bg-primary" />
            </Button>
            <Button variant="ghost" size="icon" aria-label="Messages" className="hidden sm:inline-flex">
              <MessageSquare className="size-5" />
            </Button>
            <Button variant="ghost" size="icon" aria-label="Settings" className="hidden md:inline-flex">
              <Settings className="size-5" />
            </Button>
            <Button variant="outline" size="sm" className="hidden rounded-xl md:inline-flex">
              Log in
            </Button>
            <Button size="sm" className="hidden rounded-xl md:inline-flex">
              Register
            </Button>
            <Avatar className="size-9 ring-1 ring-primary/40">
              <AvatarFallback className="bg-secondary text-xs">GX</AvatarFallback>
            </Avatar>
          </div>
        </div>
      </header>

      <div className="flex">
        <aside className="sticky top-[68px] hidden h-[calc(100dvh-68px)] w-64 shrink-0 border-r border-border/60 bg-sidebar/60 lg:block">
          <SidebarNav />
        </aside>
        <main className="min-w-0 flex-1">{children}</main>
      </div>

      <footer className="border-t border-border/60 px-5 py-8 text-sm text-muted-foreground lg:pl-72">
        <div className="flex flex-wrap items-center justify-between gap-4">
          <p>© {new Date().getFullYear()} GameSphere — a gaming discovery & community platform.</p>
          <div className="flex flex-wrap gap-4">
            <a href="https://discord.com" className="hover:text-foreground">
              Discord
            </a>
            <a href="https://x.com" className="hover:text-foreground">
              X
            </a>
            <a href="https://youtube.com" className="hover:text-foreground">
              YouTube
            </a>
            <a href="https://twitch.tv" className="hover:text-foreground">
              Twitch
            </a>
          </div>
        </div>
      </footer>
    </div>
  );
}
