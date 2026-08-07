import { createFileRoute } from "@tanstack/react-router";
import { ArrowBigDown, ArrowBigUp, Pin } from "lucide-react";

import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import { Badge } from "@/components/ui/badge";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Tabs, TabsContent, TabsList, TabsTrigger } from "@/components/ui/tabs";
import { chatMessages, eventsList, groupsList, newsItems } from "@/data/games";

export const Route = createFileRoute("/community")({
  head: () => ({
    meta: [
      { title: "Community — Chat, Forums, Groups & Events | GameSphere" },
      {
        name: "description",
        content:
          "Join global and game-specific chat rooms, discuss in the forums, find gaming groups and track upcoming community events on GameSphere.",
      },
      { property: "og:title", content: "GameSphere Community" },
      {
        property: "og:description",
        content: "Chat rooms, forums, gaming groups and community events.",
      },
    ],
  }),
  component: Community,
});

const threads = [
  { title: "What's your favourite boss fight of the year?", replies: 214, votes: 512, pinned: true },
  { title: "Iron Vanguard ranked: best mech loadouts for season 3", replies: 88, votes: 301 },
  { title: "Underrated indies you wish more people played", replies: 156, votes: 274 },
  { title: "Controller vs mouse & keyboard for souls-likes", replies: 63, votes: 119 },
];

function Community() {
  return (
    <div className="space-y-6 p-4 sm:p-6">
      <header className="space-y-2">
        <h1 className="text-2xl font-black sm:text-3xl">Community</h1>
        <p className="text-sm text-muted-foreground">
          Chat live, debate in the forums, join groups and plan events with other players.
        </p>
      </header>

      <Tabs defaultValue="chat">
        <TabsList className="flex-wrap rounded-xl">
          <TabsTrigger value="chat">Live Chat</TabsTrigger>
          <TabsTrigger value="forums">Forums</TabsTrigger>
          <TabsTrigger value="groups">Groups</TabsTrigger>
          <TabsTrigger value="events">Events & News</TabsTrigger>
        </TabsList>

        <TabsContent value="chat" className="mt-5 grid gap-4 lg:grid-cols-[220px_minmax(0,1fr)]">
          <div className="glass rounded-2xl p-4">
            <p className="text-xs font-semibold uppercase tracking-widest text-muted-foreground">
              Rooms
            </p>
            <ul className="mt-3 space-y-1 text-sm">
              {["Global", "Ashen Crown", "Neon Drift", "Trade", "Groups"].map((r, i) => (
                <li key={r}>
                  <button
                    className={`w-full rounded-xl px-3 py-2 text-left transition-colors hover:bg-secondary ${
                      i === 0 ? "bg-primary/15 ring-1 ring-primary/40" : ""
                    }`}
                  >
                    #{r.toLowerCase().replace(/\s+/g, "-")}
                  </button>
                </li>
              ))}
            </ul>
          </div>
          <div className="glass flex min-h-[420px] flex-col rounded-2xl p-4">
            <ul className="flex-1 space-y-4">
              {[...chatMessages, ...chatMessages].map((m, i) => (
                <li key={`${m.user}-${i}`} className="flex gap-3">
                  <Avatar className="size-8 shrink-0">
                    <AvatarFallback className="bg-secondary text-[10px]">
                      {m.user.slice(0, 2).toUpperCase()}
                    </AvatarFallback>
                  </Avatar>
                  <div className="min-w-0">
                    <p className="text-xs">
                      <span className="font-semibold">{m.user}</span>{" "}
                      <span className="text-muted-foreground">{m.time}</span>
                    </p>
                    <p className="text-sm text-muted-foreground">{m.text}</p>
                  </div>
                </li>
              ))}
            </ul>
            <p className="py-2 text-xs italic text-muted-foreground">PixelNinja is typing…</p>
            <form className="flex gap-2" onSubmit={(e) => e.preventDefault()}>
              <Input aria-label="Message" placeholder="Message #global" className="rounded-xl" />
              <Button className="rounded-xl">Send</Button>
            </form>
          </div>
        </TabsContent>

        <TabsContent value="forums" className="mt-5 space-y-3">
          {threads.map((t) => (
            <div key={t.title} className="glass flex items-center gap-4 rounded-2xl p-4">
              <div className="flex shrink-0 flex-col items-center">
                <button aria-label="Upvote" className="text-muted-foreground hover:text-primary">
                  <ArrowBigUp className="size-5" />
                </button>
                <span className="text-sm font-bold">{t.votes}</span>
                <button aria-label="Downvote" className="text-muted-foreground hover:text-destructive">
                  <ArrowBigDown className="size-5" />
                </button>
              </div>
              <div className="min-w-0">
                <p className="flex items-center gap-2 text-sm font-semibold">
                  {t.pinned && <Pin className="size-3.5 text-primary" aria-hidden />}
                  <span className="truncate">{t.title}</span>
                </p>
                <p className="text-xs text-muted-foreground">{t.replies} replies</p>
              </div>
            </div>
          ))}
        </TabsContent>

        <TabsContent value="groups" className="mt-5 grid gap-4 sm:grid-cols-2 lg:grid-cols-3">
          {groupsList.map((g) => (
            <div key={g.name} className="glass surface-hover rounded-2xl p-5">
              <Avatar className="size-12">
                <AvatarFallback className="bg-secondary text-xs">
                  {g.name.slice(0, 2).toUpperCase()}
                </AvatarFallback>
              </Avatar>
              <p className="mt-3 font-semibold">{g.name}</p>
              <p className="text-xs text-muted-foreground">{g.members}</p>
              <Button className="mt-4 w-full rounded-xl" variant="outline">
                Join group
              </Button>
            </div>
          ))}
        </TabsContent>

        <TabsContent value="events" className="mt-5 grid gap-4 md:grid-cols-2">
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
          <div className="glass rounded-2xl p-5">
            <h2 className="text-lg font-bold">Latest News</h2>
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
        </TabsContent>
      </Tabs>
    </div>
  );
}
