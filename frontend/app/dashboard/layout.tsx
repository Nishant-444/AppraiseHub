"use client";

import { useEffect, type ReactNode } from "react";
import { useRouter, usePathname } from "next/navigation";
import Link from "next/link";
import { useAuth } from "@/lib/auth-context";
import { apiClient } from "@/lib/api/client";
import { Button } from "@/components/ui/button";
import { Avatar, AvatarFallback } from "@/components/ui/avatar";
import {
	DropdownMenu,
	DropdownMenuContent,
	DropdownMenuItem,
	DropdownMenuLabel,
	DropdownMenuSeparator,
	DropdownMenuTrigger,
} from "@/components/ui/dropdown-menu";
import {
	Loader2,
	ClipboardCheck,
	LayoutDashboard,
	Users,
	Target,
	Bell,
	BarChart3,
	LogOut,
	Building2,
	BookOpen,
} from "lucide-react";
import { cn } from "@/lib/utils";

interface NavItem {
	href: string;
	label: string;
	icon: React.ElementType;
	roles: ("HR" | "MANAGER" | "EMPLOYEE")[];
}

const navItems: NavItem[] = [
	{
		href: "/dashboard",
		label: "Dashboard",
		icon: LayoutDashboard,
		roles: ["HR", "MANAGER", "EMPLOYEE"],
	},
	{
		href: "/dashboard/appraisals",
		label: "Appraisals",
		icon: ClipboardCheck,
		roles: ["HR", "MANAGER", "EMPLOYEE"],
	},
	{
		href: "/dashboard/goals",
		label: "Goals",
		icon: Target,
		roles: ["MANAGER", "EMPLOYEE"],
	},
	{
		href: "/dashboard/reports",
		label: "Reports",
		icon: BarChart3,
		roles: ["HR", "MANAGER"],
	},
	{ href: "/dashboard/users", label: "Users", icon: Users, roles: ["HR"] },
	{
		href: "/dashboard/departments",
		label: "Departments",
		icon: Building2,
		roles: ["HR"],
	},
];

export default function DashboardLayout({ children }: { children: ReactNode }) {
	const router = useRouter();
	const pathname = usePathname();
	const { user, isLoading, isAuthenticated, logout } = useAuth();

	useEffect(() => {
		if (isLoading) return;

		if (!isAuthenticated) {
			const token = apiClient.getToken();
			if (token) {
				logout();
			}
			router.replace("/");
		}
	}, [isLoading, isAuthenticated, router, logout]);

	if (isLoading) {
		return (
			<div className="flex min-h-screen items-center justify-center">
				<Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
			</div>
		);
	}

	if (!user) {
		return null;
	}

	const filteredNavItems = navItems.filter((item) =>
		item.roles.includes(user.role),
	);
	const showHowToUse = ["HR", "MANAGER", "EMPLOYEE"].includes(user.role);
	const navIconClassName = "mr-2 flex h-5 w-5 items-center justify-center";

	const handleLogout = () => {
		logout();
		router.push("/");
	};

	const getInitials = (name: string) => {
		return name
			.split(" ")
			.map((n) => n[0])
			.join("")
			.toUpperCase()
			.slice(0, 2);
	};

	return (
		<div className="flex min-h-screen">
			{/* Sidebar */}
			<aside className="fixed left-0 top-0 z-40 h-screen w-64 border-r bg-card">
				<div className="flex h-full flex-col">
					{/* Logo */}
					<div className="flex h-16 items-center gap-2 border-b px-6">
						<div className="flex h-8 w-8 items-center justify-center rounded-md bg-primary text-primary-foreground">
							<ClipboardCheck className="h-4 w-4" />
						</div>
						<span className="font-semibold">AppraiseHub</span>
					</div>

					{/* Profile */}
					<div className="border-b px-4 py-3">
						<DropdownMenu>
							<DropdownMenuTrigger asChild>
								<Button
									variant="ghost"
									className="h-auto w-full justify-start gap-3 rounded-lg border border-border/60 bg-muted/40 p-2 hover:bg-accent"
								>
									<Avatar className="h-9 w-9">
										<AvatarFallback className="text-xs">
											{getInitials(user.fullName)}
										</AvatarFallback>
									</Avatar>
									<div className="flex flex-col items-start text-left leading-tight">
										<span className="text-sm font-medium">{user.fullName}</span>
										<span className="text-xs text-muted-foreground">
											{user.jobTitle || user.role}
										</span>
									</div>
								</Button>
							</DropdownMenuTrigger>
							<DropdownMenuContent align="start" className="w-56">
								<DropdownMenuLabel>
									<div className="flex flex-col">
										<span>{user.fullName}</span>
										<span className="text-xs font-normal text-muted-foreground">
											{user.email}
										</span>
									</div>
								</DropdownMenuLabel>
								<DropdownMenuSeparator />
								<DropdownMenuItem
									onClick={handleLogout}
									className="text-destructive"
								>
									<LogOut className="mr-2 h-4 w-4" />
									Sign out
								</DropdownMenuItem>
							</DropdownMenuContent>
						</DropdownMenu>
					</div>

					{/* Navigation */}
					<nav className="flex-1 space-y-1 p-4">
						{filteredNavItems.map((item) => {
							const isActive =
								pathname === item.href ||
								(item.href !== "/dashboard" && pathname.startsWith(item.href));
							return (
								<Link key={item.href} href={item.href}>
									<Button
										variant={isActive ? "secondary" : "ghost"}
										className={cn(
											"w-full justify-start",
											isActive && "bg-secondary",
										)}
									>
										<span className={navIconClassName}>
											<item.icon className="h-4 w-4" />
										</span>
										{item.label}
									</Button>
								</Link>
							);
						})}
					</nav>

					{/* Footer */}
					<div className="border-t p-4">
						{showHowToUse && (
							<Link href="/dashboard/how-to-use">
								<Button variant="ghost" className="w-full justify-start">
									<span className={navIconClassName}>
										<BookOpen className="h-4 w-4" />
									</span>
									How to Use
								</Button>
							</Link>
						)}
						<Link href="/dashboard/notifications">
							<Button variant="ghost" className="w-full justify-start">
								<span className={navIconClassName}>
									<Bell className="h-4 w-4" />
								</span>
								Notifications
							</Button>
						</Link>
					</div>
				</div>
			</aside>

			{/* Main Content */}
			<main className="ml-64 flex-1 p-8">{children}</main>
		</div>
	);
}
