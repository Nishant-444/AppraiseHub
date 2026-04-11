// API Response wrapper
export interface ApiResponse<T> {
	success: boolean;
	message: string;
	data: T | null;
}

// User & Auth
export type Role = "HR" | "MANAGER" | "EMPLOYEE";

export interface User {
	id: number;
	fullName: string;
	email: string;
	role: Role;
	jobTitle: string;
	departmentName?: string;
	managerId?: number;
	managerName?: string;
	isActive: boolean;
	createdAt: string;
}

export interface CreateUserRequest {
	fullName: string;
	email: string;
	password: string;
	role: Role;
	jobTitle: string;
	departmentId: number;
	managerId?: number;
}

export interface UpdateUserRequest {
	fullName?: string;
	email?: string;
	role?: Role;
	jobTitle?: string;
	departmentId?: number;
	managerId?: number;
	isActive?: boolean;
}

export interface LoginRequest {
	email: string;
	password: string;
}

export interface LoginResponse {
	token: string;
	user?: User;
	userId?: number;
	fullName?: string;
	email?: string;
	role?: Role;
	jobTitle?: string;
	departmentName?: string;
	managerId?: number | null;
	managerName?: string | null;
	isActive?: boolean;
	createdAt?: string;
}

// Department
export interface Department {
	id: number;
	name: string;
	description?: string;
}

// Appraisal
export type AppraisalStatus =
	| "PENDING"
	| "EMPLOYEE_DRAFT"
	| "SELF_SUBMITTED"
	| "MANAGER_DRAFT"
	| "MANAGER_REVIEWED"
	| "APPROVED"
	| "ACKNOWLEDGED";

export type CycleStatus = "DRAFT" | "ACTIVE" | "CLOSED";

export interface Appraisal {
	id: number;
	cycleName: string;
	cycleStartDate: string;
	cycleEndDate: string;
	cycleStatus: CycleStatus;
	employeeId: number;
	employeeName: string;
	employeeJobTitle?: string;
	employeeDepartment?: string;
	managerId: number;
	managerName: string;
	appraisalStatus: AppraisalStatus;
	// Self Assessment
	whatWentWell?: string;
	whatToImprove?: string;
	achievements?: string;
	selfRating?: number;
	// Manager Review
	managerStrengths?: string;
	managerImprovements?: string;
	managerComments?: string;
	managerRating?: number;
	submittedAt?: string;
	approvedAt?: string;
	createdAt: string;
}

export interface BulkCycleRequest {
	cycleName: string;
	cycleStartDate: string;
	cycleEndDate: string;
	departmentId?: number | null;
}

export interface CreateAppraisalRequest {
	cycleName: string;
	cycleStartDate: string;
	cycleEndDate: string;
	employeeId: number;
	managerId: number;
}

export interface BulkCycleResponse {
	cycleName: string;
	totalEmployees: number;
	created: number;
	skippedAlreadyExists: number;
	skippedNoManager: number;
}

export interface SelfAssessmentRequest {
	whatWentWell: string;
	whatToImprove: string;
	achievements: string;
	selfRating: number;
}

export interface ManagerReviewRequest {
	managerStrengths: string;
	managerImprovements: string;
	managerComments: string;
	managerRating: number;
}

// Goal
export type GoalStatus =
	| "NOT_STARTED"
	| "IN_PROGRESS"
	| "COMPLETED"
	| "CANCELLED";

export interface Goal {
	id: number;
	appraisalId: number;
	employeeId: number;
	employeeName: string;
	title: string;
	description?: string;
	dueDate: string;
	status: GoalStatus;
}

export interface CreateGoalRequest {
	appraisalId: number;
	title: string;
	description?: string;
	dueDate: string;
}

export interface UpdateGoalProgressRequest {
	status: GoalStatus;
}

// Notification
export interface Notification {
	id: number;
	title: string;
	message: string;
	type: string;
	isRead: boolean;
	createdAt: string;
}

// Reports
export interface CycleSummary {
	cycleName: string;
	totalAppraisals: number;
	pending: number;
	employeeDraft: number;
	selfSubmitted: number;
	managerDraft: number;
	managerReviewed: number;
	approved: number;
	acknowledged: number;
	completionPercentage: number;
	averageManagerRating: number | null;
}

export interface DepartmentBreakdown {
	departmentName: string;
	totalEmployees: number;
	completed: number;
	pending: number;
	averageRating: number | null;
}

export interface RatingDistribution {
	cycleName: string;
	totalRated: number;
	distribution: Record<number, number>;
	averageRating: number | null;
}

export interface TeamReport {
	managerName: string;
	cycleName: string;
	totalTeamMembers: number;
	teamAverageRating: number | null;
	members: {
		employeeId: number;
		employeeName: string;
		jobTitle: string;
		status: AppraisalStatus;
		selfRating?: number;
		managerRating?: number;
		goalsCompleted: number;
		totalGoals: number;
	}[];
}

export interface EmployeeHistory {
	employeeId: number;
	employeeName: string;
	cycles: {
		cycleName: string;
		cycleStartDate: string;
		cycleEndDate: string;
		selfRating?: number;
		managerRating?: number;
		status: AppraisalStatus;
		managerName: string;
	}[];
}
