"use client";

import { useEffect, useState } from "react";
import { useRouter } from "next/navigation";
import { useAuth } from "@/lib/auth-context";
import { Button } from "@/components/ui/button";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import {
	Card,
	CardContent,
	CardDescription,
	CardTitle,
} from "@/components/ui/card";
import { Alert, AlertDescription } from "@/components/ui/alert";
import { Loader2, ClipboardCheck } from "lucide-react";

export default function LoginPage() {
	const router = useRouter();
	const { login, isAuthenticated, user, isLoading: authLoading } = useAuth();
	const [email, setEmail] = useState("");
	const [password, setPassword] = useState("");
	const [error, setError] = useState("");
	const [isLoading, setIsLoading] = useState(false);

	useEffect(() => {
		if (!authLoading && isAuthenticated && user) {
			router.replace("/dashboard");
		}
	}, [authLoading, isAuthenticated, user, router]);

	// Redirect if already authenticated
	if (authLoading) {
		return (
			<div className="flex min-h-screen items-center justify-center">
				<Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
			</div>
		);
	}

	const handleSubmit = async (e: React.FormEvent) => {
		e.preventDefault();
		setError("");
		setIsLoading(true);

		const result = await login(email, password);
		if (result.success) {
			router.push("/dashboard");
		} else {
			setError(result.message);
		}
		setIsLoading(false);
	};

	return (
		<div className="flex min-h-screen items-center justify-center bg-muted/30 p-4">
			<Card className="w-full max-w-4xl">
				<CardContent className="p-0">
					<div className="flex flex-col md:flex-row">
						<div className="flex flex-col items-center gap-4 p-6 text-center md:flex-1 md:justify-center md:p-8">
							<div className="flex h-14 w-14 items-center justify-center rounded-lg bg-primary text-primary-foreground">
								<ClipboardCheck className="h-7 w-7" />
							</div>
							<div className="space-y-2">
								<CardTitle className="text-3xl">AppraiseHub</CardTitle>
								<CardDescription>
									A focused hub for performance reviews and goals in one place.
								</CardDescription>
							</div>
						</div>

						<div className="hidden w-px bg-border md:block" />

						<div className="border-t border-border p-6 md:flex-1 md:border-l md:border-t-0 md:p-8">
							<div className="mb-6 text-center md:text-left">
								<CardTitle className="text-2xl">Sign in</CardTitle>
								<CardDescription>
									Use your work email to continue.
								</CardDescription>
							</div>

							<form onSubmit={handleSubmit} className="space-y-4">
								{error && (
									<Alert variant="destructive">
										<AlertDescription>{error}</AlertDescription>
									</Alert>
								)}

								<div className="space-y-2">
									<Label htmlFor="email">Email</Label>
									<Input
										id="email"
										type="email"
										placeholder="you@example.com"
										value={email}
										onChange={(e) => setEmail(e.target.value)}
										required
										disabled={isLoading}
									/>
								</div>

								<div className="space-y-2">
									<Label htmlFor="password">Password</Label>
									<Input
										id="password"
										type="password"
										placeholder="Enter your password"
										value={password}
										onChange={(e) => setPassword(e.target.value)}
										required
										disabled={isLoading}
									/>
								</div>

								<Button type="submit" className="w-full" disabled={isLoading}>
									{isLoading ? (
										<>
											<Loader2 className="mr-2 h-4 w-4 animate-spin" />
											Signing in...
										</>
									) : (
										"Sign In"
									)}
								</Button>
							</form>
						</div>
					</div>
				</CardContent>
			</Card>
		</div>
	);
}
