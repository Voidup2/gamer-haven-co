import { useEffect, useState } from "react";
import { Link, createFileRoute, useNavigate } from "@tanstack/react-router";

import { verifyEmail } from "@/api/auth";
import { ApiError } from "@/api/client";
import { Button } from "@/components/ui/button";

export const Route = createFileRoute("/verify-email")({
  head: () => ({
    meta: [
      { title: "Verify email | GameSphere" },
      { name: "description", content: "Verify your GameSphere email address." },
    ],
  }),
  component: VerifyEmailPage,
});

function VerifyEmailPage() {
  const navigate = useNavigate();
  const [status, setStatus] = useState<"verifying" | "success" | "error">("verifying");
  const [message, setMessage] = useState("Verifying your email...");

  useEffect(() => {
    const token = new URLSearchParams(window.location.search).get("token");

    if (!token) {
      setStatus("error");
      setMessage("Verification token is missing.");
      return;
    }

    verifyEmail(token)
      .then(() => {
        setStatus("success");
        setMessage("Your email has been verified. You can now log in.");
      })
      .catch((error) => {
        setStatus("error");
        setMessage(error instanceof ApiError ? error.message : "Unable to verify your email.");
      });
  }, []);

  return (
    <main className="flex min-h-[calc(100dvh-68px)] items-center justify-center px-4 py-12">
      <div className="w-full max-w-md rounded-3xl border border-border/60 bg-card/70 p-8 text-center shadow-xl backdrop-blur">
        <h1 className="text-3xl font-black tracking-tight">
          {status === "verifying" ? "Verifying email" : status === "success" ? "Email verified" : "Verification failed"}
        </h1>
        <p className="mt-3 text-sm text-muted-foreground">{message}</p>

        {status !== "verifying" && (
          <Button asChild className="mt-6 w-full rounded-xl">
            <Link to={status === "success" ? "/login" : "/register"}>
              {status === "success" ? "Go to login" : "Back to register"}
            </Link>
          </Button>
        )}
      </div>
    </main>
  );
}
