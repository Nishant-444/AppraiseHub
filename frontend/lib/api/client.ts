import type {
	ApiResponse,
	LoginRequest,
	LoginResponse,
	User,
	CreateUserRequest,
	UpdateUserRequest,
	Department,
	Appraisal,
	BulkCycleRequest,
	BulkCycleResponse,
	SelfAssessmentRequest,
	ManagerReviewRequest,
	Goal,
	CreateGoalRequest,
	UpdateGoalProgressRequest,
	Notification,
	CycleSummary,
	DepartmentBreakdown,
	RatingDistribution,
	TeamReport,
} from './types';

const API_BASE = process.env.NEXT_PUBLIC_API_URL || 'http://localhost:8080/api';

class ApiClient {
	private token: string | null = null;

	setToken(token: string | null) {
		this.token = token;
		if (typeof window !== 'undefined') {
			if (token) {
				localStorage.setItem('appraisal_token', token);
			} else {
				localStorage.removeItem('appraisal_token');
			}
		}
	}

	getToken(): string | null {
		if (this.token) return this.token;
		if (typeof window !== 'undefined') {
			this.token = localStorage.getItem('appraisal_token');
		}
		return this.token;
	}

	private async request<T>(
		endpoint: string,
		options: RequestInit = {},
	): Promise<ApiResponse<T>> {
		const token = this.getToken();
		const headers: HeadersInit = {
			'Content-Type': 'application/json',
			...options.headers,
		};

		if (token) {
			(headers as Record<string, string>)['Authorization'] = `Bearer ${token}`;
		}

		const response = await fetch(`${API_BASE}${endpoint}`, {
			...options,
			headers,
		});

		const data = await response.json();
		return data as ApiResponse<T>;
	}

	// Auth
	async login(credentials: LoginRequest): Promise<ApiResponse<LoginResponse>> {
		return this.request<LoginResponse>('/auth/login', {
			method: 'POST',
			body: JSON.stringify(credentials),
		});
	}

	async getProfile(): Promise<ApiResponse<LoginResponse>> {
		return this.request<LoginResponse>('/auth/me');
	}

	// Users (HR only)
	async getUsers(): Promise<ApiResponse<User[]>> {
		return this.request<User[]>('/users');
	}

	async createUser(user: CreateUserRequest): Promise<ApiResponse<User>> {
		return this.request<User>('/users', {
			method: 'POST',
			body: JSON.stringify(user),
		});
	}

	async updateUser(
		id: number,
		user: UpdateUserRequest,
	): Promise<ApiResponse<User>> {
		return this.request<User>(`/users/${id}`, {
			method: 'PUT',
			body: JSON.stringify(user),
		});
	}

	async deleteUser(id: number): Promise<ApiResponse<void>> {
		return this.request<void>(`/users/${id}`, {
			method: 'DELETE',
		});
	}

	// Departments (HR only)
	async getDepartments(): Promise<ApiResponse<Department[]>> {
		return this.request<Department[]>('/departments');
	}

	async createDepartment(
		dept: Partial<Department>,
	): Promise<ApiResponse<Department>> {
		return this.request<Department>('/departments', {
			method: 'POST',
			body: JSON.stringify(dept),
		});
	}

	// Appraisals
	async getAppraisalsByEmployee(
		employeeId: number,
	): Promise<ApiResponse<Appraisal[]>> {
		return this.request<Appraisal[]>(`/appraisals/my?employeeId=${employeeId}`);
	}

	async getAppraisalsByManager(
		managerId: number,
	): Promise<ApiResponse<Appraisal[]>> {
		return this.request<Appraisal[]>(`/appraisals/team?managerId=${managerId}`);
	}

	async getAllAppraisals(): Promise<ApiResponse<Appraisal[]>> {
		return this.request<Appraisal[]>('/appraisals');
	}

	async getAppraisalById(
		id: number,
		requesterId: number,
	): Promise<ApiResponse<Appraisal>> {
		return this.request<Appraisal>(
			`/appraisals/${id}?requesterId=${requesterId}`,
		);
	}

	async createBulkCycle(
		data: BulkCycleRequest,
	): Promise<ApiResponse<BulkCycleResponse>> {
		return this.request<BulkCycleResponse>('/appraisals/cycle/bulk-create', {
			method: 'POST',
			body: JSON.stringify(data),
		});
	}

	async saveSelfAssessmentDraft(
		appraisalId: number,
		employeeId: number,
		data: SelfAssessmentRequest,
	): Promise<ApiResponse<Appraisal>> {
		return this.request<Appraisal>(
			`/appraisals/${appraisalId}/self-assessment/draft?employeeId=${employeeId}`,
			{
				method: 'PUT',
				body: JSON.stringify(data),
			},
		);
	}

	async submitSelfAssessment(
		appraisalId: number,
		employeeId: number,
		data: SelfAssessmentRequest,
	): Promise<ApiResponse<Appraisal>> {
		return this.request<Appraisal>(
			`/appraisals/${appraisalId}/self-assessment/submit?employeeId=${employeeId}`,
			{
				method: 'PUT',
				body: JSON.stringify(data),
			},
		);
	}

	async saveManagerReviewDraft(
		appraisalId: number,
		managerId: number,
		data: ManagerReviewRequest,
	): Promise<ApiResponse<Appraisal>> {
		return this.request<Appraisal>(
			`/appraisals/${appraisalId}/manager-review/draft?managerId=${managerId}`,
			{
				method: 'PUT',
				body: JSON.stringify(data),
			},
		);
	}

	async submitManagerReview(
		appraisalId: number,
		managerId: number,
		data: ManagerReviewRequest,
	): Promise<ApiResponse<Appraisal>> {
		return this.request<Appraisal>(
			`/appraisals/${appraisalId}/manager-review/submit?managerId=${managerId}`,
			{
				method: 'PUT',
				body: JSON.stringify(data),
			},
		);
	}

	async approveAppraisal(appraisalId: number): Promise<ApiResponse<Appraisal>> {
		return this.request<Appraisal>(`/appraisals/${appraisalId}/approve`, {
			method: 'PATCH',
		});
	}

	async acknowledgeAppraisal(
		appraisalId: number,
		employeeId: number,
	): Promise<ApiResponse<Appraisal>> {
		return this.request<Appraisal>(
			`/appraisals/${appraisalId}/acknowledge?employeeId=${employeeId}`,
			{
				method: 'PATCH',
			},
		);
	}

	// Goals
	async getGoalsByEmployee(employeeId: number): Promise<ApiResponse<Goal[]>> {
		return this.request<Goal[]>(`/goals/employee/${employeeId}`);
	}

	async getGoalsByAppraisal(appraisalId: number): Promise<ApiResponse<Goal[]>> {
		return this.request<Goal[]>(`/goals/appraisal/${appraisalId}`);
	}

	async createGoal(
		managerId: number,
		data: CreateGoalRequest,
	): Promise<ApiResponse<Goal>> {
		return this.request<Goal>(`/goals?managerId=${managerId}`, {
			method: 'POST',
			body: JSON.stringify(data),
		});
	}

	async updateGoalProgress(
		goalId: number,
		employeeId: number,
		data: UpdateGoalProgressRequest,
	): Promise<ApiResponse<Goal>> {
		return this.request<Goal>(
			`/goals/${goalId}/progress?employeeId=${employeeId}`,
			{
				method: 'PATCH',
				body: JSON.stringify(data),
			},
		);
	}

	async deleteGoal(
		goalId: number,
		managerId: number,
	): Promise<ApiResponse<void>> {
		return this.request<void>(`/goals/${goalId}?managerId=${managerId}`, {
			method: 'DELETE',
		});
	}

	// Notifications
	async getNotifications(userId: number): Promise<ApiResponse<Notification[]>> {
		return this.request<Notification[]>(`/notifications?userId=${userId}`);
	}

	async getUnreadCount(userId: number): Promise<ApiResponse<number>> {
		return this.request<number>(`/notifications/unread-count?userId=${userId}`);
	}

	async markNotificationRead(
		notificationId: number,
		userId: number,
	): Promise<ApiResponse<void>> {
		return this.request<void>(
			`/notifications/${notificationId}/read?userId=${userId}`,
			{
				method: 'PATCH',
			},
		);
	}

	async markAllNotificationsRead(userId: number): Promise<ApiResponse<void>> {
		return this.request<void>(`/notifications/read-all?userId=${userId}`, {
			method: 'PATCH',
		});
	}

	// Reports (HR)
	async getCycleSummary(cycleName: string): Promise<ApiResponse<CycleSummary>> {
		return this.request<CycleSummary>(
			`/reports/cycle/${encodeURIComponent(cycleName)}/summary`,
		);
	}

	async getDepartmentBreakdown(
		cycleName: string,
	): Promise<ApiResponse<DepartmentBreakdown[]>> {
		return this.request<DepartmentBreakdown[]>(
			`/reports/cycle/${encodeURIComponent(cycleName)}/departments`,
		);
	}

	async getRatingDistribution(
		cycleName: string,
	): Promise<ApiResponse<RatingDistribution>> {
		return this.request<RatingDistribution>(
			`/reports/cycle/${encodeURIComponent(cycleName)}/ratings`,
		);
	}

	// Reports (Manager)
	async getTeamReport(
		managerId: number,
		cycleName: string,
	): Promise<ApiResponse<TeamReport>> {
		return this.request<TeamReport>(
			`/reports/manager/${managerId}/team/${encodeURIComponent(cycleName)}`,
		);
	}
}

export const apiClient = new ApiClient();
