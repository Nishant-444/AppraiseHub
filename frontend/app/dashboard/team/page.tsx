"use client";

import { useEffect, useMemo, useState } from "react";
import { useAuth } from "@/lib/auth-context";
import { apiClient } from "@/lib/api/client";
import type { User } from "@/lib/api/types";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import {
	Table,
	TableBody,
	TableCell,
	TableHead,
	TableHeader,
	TableRow,
} from "@/components/ui/table";
import { Loader2, Users } from "lucide-react";
import { toast } from "sonner";

export default function TeamPage() {
	const { user } = useAuth();
	const [team, setTeam] = useState<User[]>([]);
	const [isLoading, setIsLoading] = useState(true);

	useEffect(() => {
		if (!user || user.role === "HR") return;
		const loadTeam = async () => {
			setIsLoading(true);
			try {
				const res = await apiClient.getMyTeam();
				if (res.success && res.data) {
					setTeam(res.data);
				} else {
					setTeam([]);
				}
			} catch {
				toast.error("Failed to load team");
			}
			setIsLoading(false);
		};

		loadTeam();
	}, [user]);

	const teamMembers = useMemo(() => {
		if (!user) return [];
		return team.filter((member) => member.id !== user.id);
	}, [team, user]);

	if (!user) return null;
	if (user.role === "HR") {
		return (
			<div className="py-12 text-center">
				<p className="text-muted-foreground">
					Team view is available for managers and employees only.
				</p>
			</div>
		);
	}

	const managerLabel = user.role === "MANAGER" ? "Your Leader" : "Your Manager";
	const managerName = user.managerName ?? "No manager assigned";

	return (
		<div className="space-y-6">
			<div>
				<h1 className="text-3xl font-bold tracking-tight">Team</h1>
				<p className="text-muted-foreground">
					See your manager and team members in one place.
				</p>
			</div>

			<div className="grid gap-6 lg:grid-cols-2">
				<Card>
					<CardHeader>
						<CardTitle className="text-lg">{managerLabel}</CardTitle>
						<CardDescription>
							Your reporting line and primary contact.
						</CardDescription>
					</CardHeader>
					<CardContent className="space-y-3">
						<div className="text-lg font-semibold">{managerName}</div>
						{user.managerName ? (
							<div className="text-sm text-muted-foreground">
								Department: {user.departmentName ?? "Not assigned"}
							</div>
						) : (
							<div className="text-sm text-muted-foreground">
								No manager assigned yet.
							</div>
						)}
					</CardContent>
				</Card>

				<Card>
					<CardHeader className="flex flex-row items-center justify-between">
						<div>
							<CardTitle className="text-lg">Team Members</CardTitle>
							<CardDescription>
								{user.role === "MANAGER"
									? "Direct reports who work with you."
									: "Colleagues who share your manager."}
							</CardDescription>
						</div>
						<Badge variant="secondary" className="text-xs">
							{teamMembers.length} total
						</Badge>
					</CardHeader>
					<CardContent>
						{isLoading ? (
							<div className="flex items-center gap-2 text-sm text-muted-foreground">
								<Loader2 className="h-4 w-4 animate-spin" />
								Loading team...
							</div>
						) : teamMembers.length === 0 ? (
							<div className="flex items-center gap-2 text-sm text-muted-foreground">
								<Users className="h-4 w-4" />
								No team members available.
							</div>
						) : (
							<Table>
								<TableHeader>
									<TableRow>
										<TableHead>Name</TableHead>
										<TableHead>Job Title</TableHead>
									</TableRow>
								</TableHeader>
								<TableBody>
									{teamMembers.map((member) => (
										<TableRow key={member.id}>
											<TableCell className="font-medium">
												{member.fullName}
											</TableCell>
											<TableCell>{member.jobTitle}</TableCell>
										</TableRow>
									))}
								</TableBody>
							</Table>
						)}
					</CardContent>
				</Card>
			</div>
		</div>
	);
}
