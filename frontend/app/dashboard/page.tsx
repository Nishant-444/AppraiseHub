"use client"

import { useEffect, useState } from "react"
import { useAuth } from "@/lib/auth-context"
import { apiClient } from "@/lib/api/client"
import type { Appraisal, Goal, Notification } from "@/lib/api/types"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Button } from "@/components/ui/button"
import Link from "next/link"
import {
  ClipboardCheck,
  Target,
  Bell,
  Users,
  ArrowRight,
  CheckCircle2,
  Clock,
  AlertCircle,
} from "lucide-react"

const statusColors: Record<string, string> = {
  PENDING: "bg-yellow-100 text-yellow-800",
  EMPLOYEE_DRAFT: "bg-amber-100 text-amber-800",
  SELF_SUBMITTED: "bg-blue-100 text-blue-800",
  MANAGER_DRAFT: "bg-indigo-100 text-indigo-800",
  MANAGER_REVIEWED: "bg-purple-100 text-purple-800",
  APPROVED: "bg-green-100 text-green-800",
  ACKNOWLEDGED: "bg-gray-100 text-gray-800",
}

const goalStatusColors: Record<string, string> = {
  NOT_STARTED: "bg-gray-100 text-gray-800",
  IN_PROGRESS: "bg-blue-100 text-blue-800",
  COMPLETED: "bg-green-100 text-green-800",
  CANCELLED: "bg-red-100 text-red-800",
}

export default function DashboardPage() {
  const { user } = useAuth()
  const [appraisals, setAppraisals] = useState<Appraisal[]>([])
  const [goals, setGoals] = useState<Goal[]>([])
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [teamCount, setTeamCount] = useState(0)
  const [isLoading, setIsLoading] = useState(true)
  const [goalTotals, setGoalTotals] = useState({ total: 0, completed: 0 })

  useEffect(() => {
    if (!user) return

    const loadData = async () => {
      setIsLoading(true)
      try {
        // Load appraisals based on role
        let appraisalRes
        if (user.role === "HR") {
          appraisalRes = await apiClient.getAllAppraisals()
        } else if (user.role === "MANAGER") {
          appraisalRes = await apiClient.getAppraisalsByManager(user.id)
        } else {
          appraisalRes = await apiClient.getAppraisalsByEmployee(user.id)
        }
        if (appraisalRes.success && appraisalRes.data) {
          setAppraisals(appraisalRes.data.slice(0, 5))
          if (user.role === "MANAGER") {
            setTeamCount(appraisalRes.data.length)
          }
        }

        // Load goals (HR does not access goals)
        if (user.role === "HR") {
          setGoals([])
          setGoalTotals({ total: 0, completed: 0 })
        } else if (user.role === "MANAGER") {
          const teamAppraisalsRes = await apiClient.getAppraisalsByManager(user.id)
          if (teamAppraisalsRes.success && teamAppraisalsRes.data) {
            const goalsResponses = await Promise.all(
              teamAppraisalsRes.data.map((a) => apiClient.getGoalsByAppraisal(a.id)),
            )
            const allGoals = goalsResponses
              .filter((res) => res.success && res.data)
              .flatMap((res) => res.data ?? [])
              .sort((a, b) => (b.id ?? 0) - (a.id ?? 0))
            setGoals(allGoals.slice(0, 5))
            setGoalTotals({
              total: allGoals.length,
              completed: allGoals.filter((g) => g.status === "COMPLETED").length,
            })
          } else {
            setGoals([])
            setGoalTotals({ total: 0, completed: 0 })
          }
        } else {
          const goalsRes = await apiClient.getGoalsByEmployee(user.id)
          if (goalsRes.success && goalsRes.data) {
            setGoals(goalsRes.data.slice(0, 5))
            setGoalTotals({
              total: goalsRes.data.length,
              completed: goalsRes.data.filter((g) => g.status === "COMPLETED").length,
            })
          } else {
            setGoalTotals({ total: 0, completed: 0 })
          }
        }

        // Load notifications
        const notifRes = await apiClient.getNotifications(user.id)
        if (notifRes.success && notifRes.data) {
          setNotifications(notifRes.data.filter(n => !n.isRead).slice(0, 5))
        }
      } catch (error) {
        console.error("Failed to load dashboard data:", error)
      }
      setIsLoading(false)
    }

    loadData()
  }, [user])

  if (!user) return null

  const pendingAppraisals = appraisals.filter((a) => {
    const status = a.appraisalStatus
    return (
      status === "PENDING" ||
      status === "EMPLOYEE_DRAFT" ||
      status === "SELF_SUBMITTED" ||
      status === "MANAGER_DRAFT"
    )
  }).length

  const completedGoals =
    user.role === "MANAGER" ? goalTotals.completed : goals.filter((g) => g.status === "COMPLETED").length
  const totalGoals = user.role === "MANAGER" ? goalTotals.total : goals.length

  const stats = [
    {
      label: user.role === "HR" ? "Total Appraisals" : user.role === "MANAGER" ? "Team Appraisals" : "My Appraisals",
      value: appraisals.length,
      icon: ClipboardCheck,
      color: "text-blue-600",
    },
    {
      label: "Pending Action",
      value: pendingAppraisals,
      icon: Clock,
      color: "text-yellow-600",
    },
    {
      label: "Unread Notifications",
      value: notifications.length,
      icon: Bell,
      color: "text-purple-600",
    },
  ]

  if (user.role !== "HR") {
    stats.splice(2, 0, {
      label: "Goals Progress",
      value: `${completedGoals}/${totalGoals}`,
      icon: Target,
      color: "text-green-600",
    })
  }

  if (user.role === "MANAGER") {
    stats.push({
      label: "Team Members",
      value: teamCount,
      icon: Users,
      color: "text-indigo-600",
    })
  }

  return (
    <div className="space-y-8">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">
          Welcome back, {user.fullName.split(" ")[0]}
        </h1>
        <p className="text-muted-foreground">
          {user.role === "HR"
            ? "Manage appraisal cycles and view organization performance"
            : user.role === "MANAGER"
              ? "Review your team and manage performance goals"
              : "Track your appraisals and goals"}
        </p>
      </div>

      {/* Stats Grid */}
      <div
        className={`grid gap-4 md:grid-cols-2 ${user.role === "HR" ? "lg:grid-cols-3" : "lg:grid-cols-4"
          }`}
      >
        {stats.map((stat) => (
          <Card key={stat.label}>
            <CardContent className="flex items-center gap-4 p-6">
              <div className={`rounded-lg bg-muted p-3 ${stat.color}`}>
                <stat.icon className="h-5 w-5" />
              </div>
              <div>
                <p className="text-2xl font-bold">{stat.value}</p>
                <p className="text-sm text-muted-foreground">{stat.label}</p>
              </div>
            </CardContent>
          </Card>
        ))}
      </div>

      <div
        className={`grid gap-6 ${user.role === "HR" ? "lg:grid-cols-1" : "lg:grid-cols-2"}`}
      >
        {/* Recent Appraisals */}
        <Card>
          <CardHeader className="flex flex-row items-center justify-between">
            <div>
              <CardTitle className="text-lg">
                {user.role === "EMPLOYEE" ? "My Appraisals" : "Recent Appraisals"}
              </CardTitle>
              <CardDescription>
                {user.role === "HR"
                  ? "Latest appraisals across the organization"
                  : user.role === "MANAGER"
                    ? "Your team members' appraisals"
                    : "Your performance reviews"}
              </CardDescription>
            </div>
            <Link href="/dashboard/appraisals">
              <Button variant="ghost" size="sm">
                View all <ArrowRight className="ml-1 h-4 w-4" />
              </Button>
            </Link>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="space-y-3">
                {[1, 2, 3].map((i) => (
                  <div key={i} className="h-16 animate-pulse rounded-lg bg-muted" />
                ))}
              </div>
            ) : appraisals.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-8 text-center">
                <ClipboardCheck className="mb-2 h-8 w-8 text-muted-foreground" />
                <p className="text-sm text-muted-foreground">No appraisals found</p>
              </div>
            ) : (
              <div className="space-y-3">
                {appraisals.map((appraisal) => (
                  <Link
                    key={appraisal.id}
                    href={`/dashboard/appraisals/${appraisal.id}`}
                    className="flex items-center justify-between rounded-lg border p-3 transition-colors hover:bg-muted/50"
                  >
                    <div>
                      <p className="font-medium">
                        {user.role === "EMPLOYEE" ? appraisal.cycleName : appraisal.employeeName}
                      </p>
                      <p className="text-sm text-muted-foreground">
                        {user.role === "EMPLOYEE"
                          ? `Manager: ${appraisal.managerName}`
                          : appraisal.cycleName}
                      </p>
                    </div>
                    <Badge
                      className={statusColors[appraisal.appraisalStatus] ?? "bg-gray-100 text-gray-800"}
                      variant="secondary"
                    >
                      {(appraisal.appraisalStatus ?? "UNKNOWN").replace(/_/g, " ")}
                    </Badge>
                  </Link>
                ))}
              </div>
            )}
          </CardContent>
        </Card>

        {/* Goals */}
        {user.role !== "HR" && (
          <Card>
            <CardHeader className="flex flex-row items-center justify-between">
              <div>
                <CardTitle className="text-lg">
                  {user.role === "MANAGER" ? "Employee Goals" : "My Goals"}
                </CardTitle>
                <CardDescription>
                  {user.role === "MANAGER"
                    ? "Goals assigned to your team"
                    : "Track your performance objectives"}
                </CardDescription>
              </div>
              <Link href="/dashboard/goals">
                <Button variant="ghost" size="sm">
                  View all <ArrowRight className="ml-1 h-4 w-4" />
                </Button>
              </Link>
            </CardHeader>
            <CardContent>
              {isLoading ? (
                <div className="space-y-3">
                  {[1, 2, 3].map((i) => (
                    <div key={i} className="h-16 animate-pulse rounded-lg bg-muted" />
                  ))}
                </div>
              ) : goals.length === 0 ? (
                <div className="flex flex-col items-center justify-center py-8 text-center">
                  <Target className="mb-2 h-8 w-8 text-muted-foreground" />
                  <p className="text-sm text-muted-foreground">No goals set</p>
                </div>
              ) : (
                <div className="space-y-3">
                  {goals.map((goal) => (
                    <div
                      key={goal.id}
                      className="rounded-lg border p-3"
                    >
                      <div className="flex items-start justify-between gap-2">
                        <div className="flex-1">
                          <p className="font-medium">{goal.title}</p>
                          <p className="text-sm text-muted-foreground">
                            Due: {new Date(goal.dueDate).toLocaleDateString()}
                          </p>
                        </div>
                        <Badge
                          className={goalStatusColors[goal.status] ?? "bg-gray-100 text-gray-800"}
                          variant="secondary"
                        >
                          {(goal.status ?? "UNKNOWN").replace(/_/g, " ")}
                        </Badge>
                      </div>

                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        )}

        {/* Notifications */}
        <Card className="lg:col-span-2">
          <CardHeader className="flex flex-row items-center justify-between">
            <div>
              <CardTitle className="text-lg">Recent Notifications</CardTitle>
              <CardDescription>Stay updated on your appraisal activities</CardDescription>
            </div>
            <Link href="/dashboard/notifications">
              <Button variant="ghost" size="sm">
                View all <ArrowRight className="ml-1 h-4 w-4" />
              </Button>
            </Link>
          </CardHeader>
          <CardContent>
            {isLoading ? (
              <div className="space-y-3">
                {[1, 2].map((i) => (
                  <div key={i} className="h-12 animate-pulse rounded-lg bg-muted" />
                ))}
              </div>
            ) : notifications.length === 0 ? (
              <div className="flex flex-col items-center justify-center py-8 text-center">
                <CheckCircle2 className="mb-2 h-8 w-8 text-green-600" />
                <p className="text-sm text-muted-foreground">You&apos;re all caught up!</p>
              </div>
            ) : (
              <div className="space-y-2">
                {notifications.map((notif) => (
                  <div
                    key={notif.id}
                    className="flex items-start gap-3 rounded-lg border p-3"
                  >
                    <AlertCircle className="mt-0.5 h-5 w-5 text-blue-600" />
                    <div className="flex-1">
                      <p className="font-medium">{notif.title}</p>
                      <p className="text-sm text-muted-foreground">{notif.message}</p>
                      <p className="mt-1 text-xs text-muted-foreground">
                        {new Date(notif.createdAt).toLocaleString()}
                      </p>
                    </div>
                  </div>
                ))}
              </div>
            )}
          </CardContent>
        </Card>
      </div>
    </div>
  )
}
