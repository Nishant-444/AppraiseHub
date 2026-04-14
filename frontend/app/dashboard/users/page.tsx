"use client";

import { useEffect, useState } from "react";
import { useAuth } from "@/lib/auth-context";
import { apiClient } from "@/lib/api/client";
import type { User, Department } from "@/lib/api/types";
import { Button } from "@/components/ui/button";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";
import { Input } from "@/components/ui/input";
import { Label } from "@/components/ui/label";
import { Switch } from "@/components/ui/switch";
import {
	Dialog,
	DialogContent,
	DialogDescription,
	DialogFooter,
	DialogHeader,
	DialogTitle,
	DialogTrigger,
} from "@/components/ui/dialog";
import {
	Select,
	SelectContent,
	SelectItem,
	SelectTrigger,
	SelectValue,
} from "@/components/ui/select";
import {
	Table,
	TableBody,
	TableCell,
	TableHead,
	TableHeader,
	TableRow,
} from "@/components/ui/table";
import { toast } from "sonner";
import { Plus, Search, Loader2, Users, UserCheck, UserX } from "lucide-react";

const roleColors: Record<string, string> = {
	HR: "bg-purple-100 text-purple-800",
	MANAGER: "bg-blue-100 text-blue-800",
	EMPLOYEE: "bg-green-100 text-green-800",
};

export default function UsersPage() {
	const { user: currentUser } = useAuth();
	const [users, setUsers] = useState<User[]>([]);
	const [departments, setDepartments] = useState<Department[]>([]);
	const [managers, setManagers] = useState<User[]>([]);
	const [isLoading, setIsLoading] = useState(true);
	const [searchQuery, setSearchQuery] = useState("");
	const [roleFilter, setRoleFilter] = useState<string>("all");
	const [isCreateOpen, setIsCreateOpen] = useState(false);
	const [isCreating, setIsCreating] = useState(false);
	const [togglingUserId, setTogglingUserId] = useState<number | null>(null);

	// Create user form state
	const [fullName, setFullName] = useState("");
	const [email, setEmail] = useState("");
	const [password, setPassword] = useState("");
	const [role, setRole] = useState<string>("EMPLOYEE");
	const [jobTitle, setJobTitle] = useState("");
	const [departmentId, setDepartmentId] = useState<string>("");
	const [managerId, setManagerId] = useState<string>("");

	useEffect(() => {
		if (currentUser?.role === "HR") {
			loadUsers();
			loadDepartments();
		}
	}, [currentUser]);

	const loadUsers = async () => {
		setIsLoading(true);
		try {
			const res = await apiClient.getUsers();
			if (res.success && res.data) {
				setUsers(res.data);
				setManagers(res.data.filter((u) => u.role === "MANAGER"));
			}
		} catch {
			toast.error("Failed to load users");
		}
		setIsLoading(false);
	};

	const loadDepartments = async () => {
		const res = await apiClient.getDepartments();
		if (res.success && res.data) {
			setDepartments(res.data);
		}
	};

	const handleCreateUser = async () => {
		if (
			!fullName ||
			!email ||
			!password ||
			!role ||
			!jobTitle ||
			!departmentId
		) {
			toast.error("Please fill all required fields");
			return;
		}

		const normalizedManagerId =
			managerId && managerId !== "none" ? managerId : "";
		if (role === "EMPLOYEE" && !normalizedManagerId) {
			toast.error("Employees must be assigned a manager");
			return;
		}
		if (role === "HR" && normalizedManagerId) {
			toast.error("HR users cannot have a manager assigned");
			return;
		}

		setIsCreating(true);
		try {
			const res = await apiClient.createUser({
				fullName,
				email,
				password,
				role: role as User["role"],
				jobTitle,
				departmentId: parseInt(departmentId),
				managerId: normalizedManagerId
					? parseInt(normalizedManagerId)
					: undefined,
			});
			if (res.success) {
				toast.success("User created successfully");
				setIsCreateOpen(false);
				resetForm();
				loadUsers();
			} else {
				toast.error(res.message || "Failed to create user");
			}
		} catch {
			toast.error("Failed to create user");
		}
		setIsCreating(false);
	};

	const resetForm = () => {
		setFullName("");
		setEmail("");
		setPassword("");
		setRole("EMPLOYEE");
		setJobTitle("");
		setDepartmentId("");
		setManagerId("");
	};

	useEffect(() => {
		if (role === "HR") {
			setManagerId("none");
		}
	}, [role]);

	const handleToggleUserStatus = async (user: User, nextActive: boolean) => {
		setTogglingUserId(user.id);
		try {
			const res = await apiClient.updateUser(user.id, { isActive: nextActive });
			if (res.success) {
				toast.success(`User ${nextActive ? "activated" : "deactivated"}`);
				loadUsers();
			} else {
				toast.error(res.message || "Failed to update user");
			}
		} catch {
			toast.error("Failed to update user");
		}
		setTogglingUserId(null);
	};

	const filteredUsers = users.filter((u) => {
		const matchesSearch =
			u.fullName.toLowerCase().includes(searchQuery.toLowerCase()) ||
			u.email.toLowerCase().includes(searchQuery.toLowerCase()) ||
			u.jobTitle.toLowerCase().includes(searchQuery.toLowerCase());
		const matchesRole = roleFilter === "all" || u.role === roleFilter;
		return matchesSearch && matchesRole;
	});

	if (currentUser?.role !== "HR") {
		return (
			<div className="text-center py-12">
				<p className="text-muted-foreground">
					You don&apos;t have access to user management
				</p>
			</div>
		);
	}

	const activeUsers = users.filter((u) => u.isActive).length;
	const totalManagers = users.filter((u) => u.role === "MANAGER").length;

	return (
		<div className="space-y-6">
			<div className="flex items-center justify-between">
				<div>
					<h1 className="text-3xl font-bold tracking-tight">Users</h1>
					<p className="text-muted-foreground">
						Manage employees and their roles
					</p>
				</div>
				<Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
					<DialogTrigger asChild>
						<Button>
							<Plus className="mr-2 h-4 w-4" />
							Add User
						</Button>
					</DialogTrigger>
					<DialogContent className="max-w-md">
						<DialogHeader>
							<DialogTitle>Add New User</DialogTitle>
							<DialogDescription>
								Create a new employee account
							</DialogDescription>
						</DialogHeader>
						<div className="space-y-4 py-4">
							<div className="space-y-2">
								<Label htmlFor="fullName">Full Name</Label>
								<Input
									id="fullName"
									value={fullName}
									onChange={(e) => setFullName(e.target.value)}
									placeholder="John Doe"
								/>
							</div>
							<div className="space-y-2">
								<Label htmlFor="email">Email</Label>
								<Input
									id="email"
									type="email"
									value={email}
									onChange={(e) => setEmail(e.target.value)}
									placeholder="john@example.com"
								/>
							</div>
							<div className="space-y-2">
								<Label htmlFor="password">Password</Label>
								<Input
									id="password"
									type="password"
									value={password}
									onChange={(e) => setPassword(e.target.value)}
									placeholder="Enter password"
								/>
							</div>
							<div className="grid grid-cols-2 gap-4">
								<div className="space-y-2">
									<Label htmlFor="role">Role</Label>
									<Select value={role} onValueChange={setRole}>
										<SelectTrigger>
											<SelectValue />
										</SelectTrigger>
										<SelectContent>
											<SelectItem value="EMPLOYEE">Employee</SelectItem>
											<SelectItem value="MANAGER">Manager</SelectItem>
											<SelectItem value="HR">HR</SelectItem>
										</SelectContent>
									</Select>
								</div>
								<div className="space-y-2">
									<Label htmlFor="jobTitle">Job Title</Label>
									<Input
										id="jobTitle"
										value={jobTitle}
										onChange={(e) => setJobTitle(e.target.value)}
										placeholder="Developer"
									/>
								</div>
							</div>
							<div className="space-y-2">
								<Label htmlFor="department">Department</Label>
								<Select value={departmentId} onValueChange={setDepartmentId}>
									<SelectTrigger>
										<SelectValue placeholder="Select department" />
									</SelectTrigger>
									<SelectContent>
										{departments.map((d) => (
											<SelectItem key={d.id} value={d.id.toString()}>
												{d.name}
											</SelectItem>
										))}
									</SelectContent>
								</Select>
							</div>
							<div className="space-y-2">
								<Label htmlFor="manager">
									Manager{role === "EMPLOYEE" ? " (Required)" : " (Optional)"}
								</Label>
								<Select
									value={managerId}
									onValueChange={setManagerId}
									disabled={role === "HR"}
								>
									<SelectTrigger>
										<SelectValue placeholder="Select manager" />
									</SelectTrigger>
									<SelectContent>
										<SelectItem value="none">No Manager</SelectItem>
										{managers.map((m) => (
											<SelectItem key={m.id} value={m.id.toString()}>
												{m.fullName} ({m.role})
											</SelectItem>
										))}
									</SelectContent>
								</Select>
							</div>
						</div>
						<DialogFooter>
							<Button variant="outline" onClick={() => setIsCreateOpen(false)}>
								Cancel
							</Button>
							<Button onClick={handleCreateUser} disabled={isCreating}>
								{isCreating && (
									<Loader2 className="mr-2 h-4 w-4 animate-spin" />
								)}
								Create User
							</Button>
						</DialogFooter>
					</DialogContent>
				</Dialog>
			</div>

			{/* Stats */}
			<div className="grid gap-4 md:grid-cols-3">
				<Card>
					<CardContent className="flex items-center gap-4 p-6">
						<div className="rounded-lg bg-blue-100 p-3 text-blue-600">
							<Users className="h-5 w-5" />
						</div>
						<div>
							<p className="text-2xl font-bold">{users.length}</p>
							<p className="text-sm text-muted-foreground">Total Users</p>
						</div>
					</CardContent>
				</Card>
				<Card>
					<CardContent className="flex items-center gap-4 p-6">
						<div className="rounded-lg bg-green-100 p-3 text-green-600">
							<UserCheck className="h-5 w-5" />
						</div>
						<div>
							<p className="text-2xl font-bold">{activeUsers}</p>
							<p className="text-sm text-muted-foreground">Active Users</p>
						</div>
					</CardContent>
				</Card>
				<Card>
					<CardContent className="flex items-center gap-4 p-6">
						<div className="rounded-lg bg-purple-100 p-3 text-purple-600">
							<UserX className="h-5 w-5" />
						</div>
						<div>
							<p className="text-2xl font-bold">{totalManagers}</p>
							<p className="text-sm text-muted-foreground">Managers</p>
						</div>
					</CardContent>
				</Card>
			</div>

			<Card>
				<CardHeader>
					<div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
						<div>
							<CardTitle>All Users</CardTitle>
							<CardDescription>
								{filteredUsers.length} users found
							</CardDescription>
						</div>
						<div className="flex flex-col gap-2 sm:flex-row">
							<div className="relative">
								<Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
								<Input
									placeholder="Search users..."
									value={searchQuery}
									onChange={(e) => setSearchQuery(e.target.value)}
									className="pl-9"
								/>
							</div>
							<Select value={roleFilter} onValueChange={setRoleFilter}>
								<SelectTrigger className="w-37.5">
									<SelectValue placeholder="Filter by role" />
								</SelectTrigger>
								<SelectContent>
									<SelectItem value="all">All Roles</SelectItem>
									<SelectItem value="HR">HR</SelectItem>
									<SelectItem value="MANAGER">Manager</SelectItem>
									<SelectItem value="EMPLOYEE">Employee</SelectItem>
								</SelectContent>
							</Select>
						</div>
					</div>
				</CardHeader>
				<CardContent>
					{isLoading ? (
						<div className="flex items-center justify-center py-8">
							<Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
						</div>
					) : filteredUsers.length === 0 ? (
						<div className="flex flex-col items-center justify-center py-12 text-center">
							<Users className="mb-4 h-12 w-12 text-muted-foreground" />
							<h3 className="text-lg font-medium">No users found</h3>
							<p className="text-sm text-muted-foreground">
								Add a new user to get started.
							</p>
						</div>
					) : (
						<Table>
							<TableHeader>
								<TableRow>
									<TableHead>Name</TableHead>
									<TableHead>Email</TableHead>
									<TableHead>Job Title</TableHead>
									<TableHead>Manager</TableHead>
									<TableHead>Role</TableHead>
									<TableHead>Department</TableHead>
									<TableHead>Status</TableHead>
								</TableRow>
							</TableHeader>
							<TableBody>
								{filteredUsers.map((user) => (
									<TableRow key={user.id}>
										<TableCell>
											<p className="font-medium">{user.fullName}</p>
										</TableCell>
										<TableCell>{user.email}</TableCell>
										<TableCell>{user.jobTitle}</TableCell>
										<TableCell>{user.managerName || "-"}</TableCell>
										<TableCell>
											<Badge
												className={roleColors[user.role]}
												variant="secondary"
											>
												{user.role}
											</Badge>
										</TableCell>
										<TableCell>{user.departmentName || "-"}</TableCell>
										<TableCell>
											<div className="flex items-center justify-between gap-3">
												<Badge
													variant={user.isActive ? "default" : "secondary"}
												>
													{user.isActive ? "Active" : "Inactive"}
												</Badge>
												<Switch
													checked={user.isActive}
													onCheckedChange={(checked) =>
														handleToggleUserStatus(user, checked)
													}
													disabled={togglingUserId === user.id}
												/>
											</div>
										</TableCell>
									</TableRow>
								))}
							</TableBody>
						</Table>
					)}
				</CardContent>
			</Card>
		</div>
	);
}
