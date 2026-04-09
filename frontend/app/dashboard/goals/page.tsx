"use client"

import { useEffect, useState } from "react"
import { useAuth } from "@/lib/auth-context"
import { apiClient } from "@/lib/api/client"
import type { Goal, Appraisal, GoalStatus } from "@/lib/api/types"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import {
  Dialog,
  DialogContent,
  DialogDescription,
  DialogFooter,
  DialogHeader,
  DialogTitle,
  DialogTrigger,
} from "@/components/ui/dialog"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { toast } from "sonner"
import { Plus, Loader2, Target, Calendar, CheckCircle2, XCircle, Clock, PlayCircle } from "lucide-react"

const statusColors: Record<string, string> = {
  NOT_STARTED: "bg-gray-100 text-gray-800",
  IN_PROGRESS: "bg-blue-100 text-blue-800",
  COMPLETED: "bg-green-100 text-green-800",
  CANCELLED: "bg-red-100 text-red-800",
}

const statusIcons: Record<string, React.ElementType> = {
  NOT_STARTED: Clock,
  IN_PROGRESS: PlayCircle,
  COMPLETED: CheckCircle2,
  CANCELLED: XCircle,
}

export default function GoalsPage() {
  const { user } = useAuth()
  const [goals, setGoals] = useState<Goal[]>([])
  const [appraisals, setAppraisals] = useState<Appraisal[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [isCreating, setIsCreating] = useState(false)
  const [isUpdating, setIsUpdating] = useState<number | null>(null)

  // Create goal form state
  const [title, setTitle] = useState("")
  const [description, setDescription] = useState("")
  const [dueDate, setDueDate] = useState("")
  const [appraisalId, setAppraisalId] = useState<string>("")

  useEffect(() => {
    if (!user) return
    if (user.role === "MANAGER") {
      loadManagerGoals()
    } else {
      loadGoals()
    }
  }, [user])

  const loadGoals = async () => {
    if (!user) return
    setIsLoading(true)
    try {
      const res = await apiClient.getGoalsByEmployee(user.id)
      if (res.success && res.data) {
        setGoals(res.data)
      }
    } catch {
      toast.error("Failed to load goals")
    }
    setIsLoading(false)
  }

  const loadTeamAppraisals = async () => {
    if (!user) return
    try {
      const res = await apiClient.getAppraisalsByManager(user.id)
      if (res.success && res.data) {
        setAppraisals(
          res.data.filter((a) => a.appraisalStatus !== "ACKNOWLEDGED"),
        )
      }
    } catch {
      console.error("Failed to load appraisals")
    }
  }

  const loadManagerGoals = async () => {
    if (!user) return
    setIsLoading(true)
    try {
      const appraisalsRes = await apiClient.getAppraisalsByManager(user.id)
      if (appraisalsRes.success && appraisalsRes.data) {
        const activeAppraisals = appraisalsRes.data.filter(
          (a) => a.appraisalStatus !== "ACKNOWLEDGED",
        )
        setAppraisals(activeAppraisals)
        const goalsResponses = await Promise.all(
          activeAppraisals.map((a) => apiClient.getGoalsByAppraisal(a.id)),
        )
        const allGoals = goalsResponses
          .filter((res) => res.success && res.data)
          .flatMap((res) => res.data ?? [])
        setGoals(allGoals)
      } else {
        setGoals([])
      }
    } catch {
      toast.error("Failed to load team goals")
    }
    setIsLoading(false)
  }

  const handleCreateGoal = async () => {
    if (!user || !title || !dueDate || !appraisalId) {
      toast.error("Please fill all required fields")
      return
    }

    setIsCreating(true)
    try {
      const res = await apiClient.createGoal(user.id, {
        appraisalId: parseInt(appraisalId),
        title,
        description,
        dueDate,
      })
      if (res.success) {
        toast.success("Goal created successfully")
        setIsCreateOpen(false)
        setTitle("")
        setDescription("")
        setDueDate("")
        setAppraisalId("")
        if (user.role === "MANAGER") {
          loadManagerGoals()
        } else {
          loadGoals()
        }
      } else {
        toast.error(res.message || "Failed to create goal")
      }
    } catch {
      toast.error("Failed to create goal")
    }
    setIsCreating(false)
  }

  const handleUpdateStatus = async (goalId: number, status: GoalStatus) => {
    if (!user) return
    setIsUpdating(goalId)
    try {
      const res = await apiClient.updateGoalProgress(goalId, user.id, { status })
      if (res.success) {
        toast.success("Goal updated")
        loadGoals()
      } else {
        toast.error(res.message || "Failed to update goal")
      }
    } catch {
      toast.error("Failed to update goal")
    }
    setIsUpdating(null)
  }

  const handleDeleteGoal = async (goalId: number) => {
    if (!user) return
    try {
      const res = await apiClient.deleteGoal(goalId, user.id)
      if (res.success) {
        toast.success("Goal deleted")
        if (user.role === "MANAGER") {
          loadManagerGoals()
        } else {
          loadGoals()
        }
      } else {
        toast.error(res.message || "Failed to delete goal")
      }
    } catch {
      toast.error("Failed to delete goal")
    }
  }

  const filteredGoals = goals.filter((g) => {
    if (statusFilter === "all") return true
    if (statusFilter === "overdue") {
      return (
        g.status !== "COMPLETED" &&
        g.status !== "CANCELLED" &&
        new Date(g.dueDate) < new Date()
      )
    }
    return g.status === statusFilter
  })

  const stats = {
    total: goals.length,
    notStarted: goals.filter((g) => g.status === "NOT_STARTED").length,
    inProgress: goals.filter((g) => g.status === "IN_PROGRESS").length,
    completed: goals.filter((g) => g.status === "COMPLETED").length,
  }

  if (!user) return null

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Goals</h1>
          <p className="text-muted-foreground">
            {user.role === "MANAGER"
              ? "Track and manage employee goals across your team"
              : "Track your performance objectives"}
          </p>
        </div>
        {user.role === "MANAGER" && (
          <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="mr-2 h-4 w-4" />
                Create Goal
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Create Goal</DialogTitle>
                <DialogDescription>
                  Create a new performance goal for a team member
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4 py-4">
                <div className="space-y-2">
                  <Label htmlFor="appraisal">Assign to Appraisal</Label>
                  <Select value={appraisalId} onValueChange={setAppraisalId}>
                    <SelectTrigger>
                      <SelectValue placeholder="Select an appraisal" />
                    </SelectTrigger>
                    <SelectContent>
                      {appraisals.map((a) => (
                        <SelectItem key={a.id} value={a.id.toString()}>
                          {a.employeeName} - {a.cycleName}
                        </SelectItem>
                      ))}
                    </SelectContent>
                  </Select>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="title">Goal Title</Label>
                  <Input
                    id="title"
                    value={title}
                    onChange={(e) => setTitle(e.target.value)}
                    placeholder="e.g., Improve code quality"
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="description">Description (Optional)</Label>
                  <Textarea
                    id="description"
                    value={description}
                    onChange={(e) => setDescription(e.target.value)}
                    placeholder="Describe the goal in detail..."
                    rows={3}
                  />
                </div>
                <div className="space-y-2">
                  <Label htmlFor="dueDate">Due Date</Label>
                  <Input
                    id="dueDate"
                    type="date"
                    value={dueDate}
                    onChange={(e) => setDueDate(e.target.value)}
                  />
                </div>
              </div>
              <DialogFooter>
                <Button variant="outline" onClick={() => setIsCreateOpen(false)}>
                  Cancel
                </Button>
                <Button onClick={handleCreateGoal} disabled={isCreating}>
                  {isCreating && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  Create Goal
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        )}
      </div>

      {/* Stats */}
      <div className="grid gap-4 md:grid-cols-4">
        <Card>
          <CardContent className="flex items-center gap-4 p-6">
            <div className="rounded-lg bg-blue-100 p-3 text-blue-600">
              <Target className="h-5 w-5" />
            </div>
            <div>
              <p className="text-2xl font-bold">{stats.total}</p>
              <p className="text-sm text-muted-foreground">Total Goals</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-4 p-6">
            <div className="rounded-lg bg-gray-100 p-3 text-gray-600">
              <Clock className="h-5 w-5" />
            </div>
            <div>
              <p className="text-2xl font-bold">{stats.notStarted}</p>
              <p className="text-sm text-muted-foreground">Not Started</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-4 p-6">
            <div className="rounded-lg bg-blue-100 p-3 text-blue-600">
              <PlayCircle className="h-5 w-5" />
            </div>
            <div>
              <p className="text-2xl font-bold">{stats.inProgress}</p>
              <p className="text-sm text-muted-foreground">In Progress</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-4 p-6">
            <div className="rounded-lg bg-green-100 p-3 text-green-600">
              <CheckCircle2 className="h-5 w-5" />
            </div>
            <div>
              <p className="text-2xl font-bold">{stats.completed}</p>
              <p className="text-sm text-muted-foreground">Completed</p>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <CardTitle>{user.role === "MANAGER" ? "Employee Goals" : "My Goals"}</CardTitle>
              <CardDescription>
                {user.role === "MANAGER"
                  ? "Goals assigned to employees on your team"
                  : `${filteredGoals.length} goals`}
              </CardDescription>
            </div>
            <Select value={statusFilter} onValueChange={setStatusFilter}>
              <SelectTrigger className="w-[180px]">
                <SelectValue placeholder="Filter by status" />
              </SelectTrigger>
              <SelectContent>
                <SelectItem value="all">All Statuses</SelectItem>
                <SelectItem value="overdue">Overdue</SelectItem>
                <SelectItem value="NOT_STARTED">Not Started</SelectItem>
                <SelectItem value="IN_PROGRESS">In Progress</SelectItem>
                <SelectItem value="COMPLETED">Completed</SelectItem>
              </SelectContent>
            </Select>
          </div>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex items-center justify-center py-8">
              <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            </div>
          ) : filteredGoals.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <Target className="mb-4 h-12 w-12 text-muted-foreground" />
              <h3 className="text-lg font-medium">No goals found</h3>
              <p className="text-sm text-muted-foreground">
                {user.role === "MANAGER"
                  ? "Create goals for your team members."
                  : "Your manager will assign goals to you."}
              </p>
            </div>
          ) : (
            <div className="space-y-4">
              {filteredGoals.map((goal) => {
                const StatusIcon = statusIcons[goal.status] ?? Target
                const isOverdue =
                  goal.status !== "COMPLETED" &&
                  goal.status !== "CANCELLED" &&
                  new Date(goal.dueDate) < new Date()

                return (
                  <div
                    key={goal.id}
                    className="rounded-lg border p-4 transition-colors hover:bg-muted/30"
                  >
                    <div className="flex items-start justify-between gap-4">
                      <div className="flex items-start gap-3 flex-1">
                        <div
                          className={`mt-1 rounded-lg p-2 ${goal.status === "COMPLETED"
                            ? "bg-green-100 text-green-600"
                            : goal.status === "IN_PROGRESS"
                              ? "bg-blue-100 text-blue-600"
                              : goal.status === "CANCELLED"
                                ? "bg-red-100 text-red-600"
                                : "bg-gray-100 text-gray-600"
                            }`}
                        >
                          <StatusIcon className="h-4 w-4" />
                        </div>
                        <div className="flex-1">
                          <div className="flex items-center gap-2">
                            <h4 className="font-medium">{goal.title}</h4>
                          </div>
                          {goal.description && (
                            <p className="text-sm text-muted-foreground mt-1">
                              {goal.description}
                            </p>
                          )}
                          {goal.employeeName && (
                            <p className="text-sm text-muted-foreground mt-1">
                              Assigned To: <span className="font-medium text-foreground">{goal.employeeName}</span>
                            </p>
                          )}
                          <div className="flex flex-wrap items-center gap-2 mt-2 text-sm text-muted-foreground">
                            <span className="flex items-center gap-1">
                              <Calendar className="h-4 w-4" />
                              Due: {new Date(goal.dueDate).toLocaleDateString()}
                            </span>
                            {isOverdue && (
                              <Badge variant="destructive" className="text-xs">
                                Overdue
                              </Badge>
                            )}
                          </div>

                        </div>
                      </div>
                      <div className="flex flex-col gap-2">
                        <Badge
                          className={statusColors[goal.status] ?? "bg-gray-100 text-gray-800"}
                          variant="secondary"
                        >
                          {(goal.status ?? "UNKNOWN").replace(/_/g, " ")}
                        </Badge>
                        {user.role === "EMPLOYEE" &&
                          goal.status !== "COMPLETED" &&
                          goal.status !== "CANCELLED" && (
                            <Select
                              value={goal.status}
                              onValueChange={(value) =>
                                handleUpdateStatus(goal.id, value as GoalStatus)
                              }
                              disabled={isUpdating === goal.id}
                            >
                              <SelectTrigger className="w-[140px] text-xs">
                                <SelectValue placeholder="Update status" />
                              </SelectTrigger>
                              <SelectContent>
                                <SelectItem value="NOT_STARTED">Not Started</SelectItem>
                                <SelectItem value="IN_PROGRESS">In Progress</SelectItem>
                                <SelectItem value="COMPLETED">Completed</SelectItem>
                              </SelectContent>
                            </Select>
                          )}
                        {user.role === "MANAGER" && (
                          <Button
                            variant="ghost"
                            size="sm"
                            className="text-destructive"
                            onClick={() => handleDeleteGoal(goal.id)}
                          >
                            Delete
                          </Button>
                        )}
                      </div>
                    </div>
                  </div>
                )
              })}
            </div>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
