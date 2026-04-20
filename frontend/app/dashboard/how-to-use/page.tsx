"use client";

import { useAuth } from "@/lib/auth-context";
import {
	Card,
	CardContent,
	CardDescription,
	CardHeader,
	CardTitle,
} from "@/components/ui/card";
import { Badge } from "@/components/ui/badge";

const quickSteps = [
	{
		title: "Start at Dashboard",
		body: "See your pending items, goals progress, and recent activity at a glance.",
	},
	{
		title: "Complete Appraisals",
		body: "Open an appraisal, fill the required sections, then submit it for review.",
	},
	{
		title: "Track Goals",
		body: "Create or update goals and keep progress current for each review cycle.",
	},
	{
		title: "Check Notifications",
		body: "Use Notifications for reminders, feedback requests, and approvals.",
	},
];

const roleGuides = [
	{
		role: "Employee",
		steps: [
			"Use Appraisals to submit self-assessments and view feedback.",
			"Update Goals with progress notes and status changes.",
			"Watch Notifications for review requests and next steps.",
		],
	},
	{
		role: "Manager",
		steps: [
			"Review team Appraisals and add manager feedback.",
			"Create or adjust Goals for direct reports.",
			"Use Reports to track team outcomes and trends.",
		],
	},
	{
		role: "HR",
		steps: [
			"Create Appraisal cycles and monitor completion rates.",
			"Manage Users and Departments to keep org data current.",
			"Use Reports for organization-wide summaries.",
		],
	},
];

const formTips = [
	"Fill required fields first; they block submission.",
	"Save drafts if you are not ready to submit.",
	"Use clear, specific examples in comments and ratings.",
];

export default function HowToUsePage() {
	const { user } = useAuth();

	if (!user) return null;
	if (user.role !== "EMPLOYEE") {
		return (
			<div className="py-12 text-center">
				<p className="text-muted-foreground">
					How to Use is available for employees only.
				</p>
			</div>
		);
	}

	return (
		<div className="space-y-8">
			<div>
				<h1 className="text-3xl font-bold tracking-tight">How to Use</h1>
				<p className="text-muted-foreground">
					A quick guide to where things live and how to complete common tasks.
				</p>
			</div>

			<div className="grid gap-4 md:grid-cols-2">
				{quickSteps.map((step) => (
					<Card key={step.title}>
						<CardHeader>
							<CardTitle className="text-lg">{step.title}</CardTitle>
							<CardDescription>{step.body}</CardDescription>
						</CardHeader>
					</Card>
				))}
			</div>

			<Card>
				<CardHeader>
					<CardTitle className="text-lg">Role-based guidance</CardTitle>
					<CardDescription>
						What each role typically does most often.
					</CardDescription>
				</CardHeader>
				<CardContent className="grid gap-4 md:grid-cols-3">
					{roleGuides.map((guide) => (
						<div key={guide.role} className="space-y-3">
							<Badge variant="secondary" className="text-xs">
								{guide.role}
							</Badge>
							<ul className="space-y-2 text-sm text-muted-foreground">
								{guide.steps.map((step) => (
									<li key={step}>- {step}</li>
								))}
							</ul>
						</div>
					))}
				</CardContent>
			</Card>

			<Card>
				<CardHeader>
					<CardTitle className="text-lg">Form tips</CardTitle>
					<CardDescription>
						Make submissions smooth and avoid missing info.
					</CardDescription>
				</CardHeader>
				<CardContent>
					<ul className="space-y-2 text-sm text-muted-foreground">
						{formTips.map((tip) => (
							<li key={tip}>- {tip}</li>
						))}
					</ul>
				</CardContent>
			</Card>
		</div>
	);
}
