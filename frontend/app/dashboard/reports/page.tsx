"use client"

import { useEffect, useState } from "react"
import { useAuth } from "@/lib/auth-context"
import { apiClient } from "@/lib/api/client"
import type { CycleSummary, DepartmentBreakdown, RatingDistribution, TeamReport, Appraisal } from "@/lib/api/types"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Button } from "@/components/ui/button"
import { Label } from "@/components/ui/label"
import { Progress } from "@/components/ui/progress"
import { Badge } from "@/components/ui/badge"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { toast } from "sonner"
import Link from "next/link"
import { Loader2, BarChart3, TrendingUp, Star, AlertCircle } from "lucide-react"

const statusColors: Record<string, string> = {
  PENDING: "bg-yellow-100 text-yellow-800",
  EMPLOYEE_DRAFT: "bg-amber-100 text-amber-800",
  SELF_SUBMITTED: "bg-blue-100 text-blue-800",
  MANAGER_DRAFT: "bg-indigo-100 text-indigo-800",
  MANAGER_REVIEWED: "bg-purple-100 text-purple-800",
  APPROVED: "bg-green-100 text-green-800",
  ACKNOWLEDGED: "bg-gray-100 text-gray-800",
}

export default function ReportsPage() {
  const { user } = useAuth()
  const [cycleName, setCycleName] = useState("")
  const [availableCycles, setAvailableCycles] = useState<string[]>([])
  const [isLoading, setIsLoading] = useState(false)

  // HR Reports
  const [cycleSummary, setCycleSummary] = useState<CycleSummary | null>(null)
  const [departmentBreakdown, setDepartmentBreakdown] = useState<DepartmentBreakdown[]>([])
  const [ratingDistribution, setRatingDistribution] = useState<RatingDistribution | null>(null)
  const [prevCycleSummary, setPrevCycleSummary] = useState<CycleSummary | null>(null)
  const [prevCycleName, setPrevCycleName] = useState<string | null>(null)

  // Manager Reports
  const [teamReport, setTeamReport] = useState<TeamReport | null>(null)

  useEffect(() => {
    loadAvailableCycles()
  }, [user])

  useEffect(() => {
    if (cycleName) {
      loadReports()
    }
  }, [cycleName, availableCycles, user])

  const loadAvailableCycles = async () => {
    if (!user) return
    try {
      let res
      if (user.role === "HR") {
        res = await apiClient.getAllAppraisals()
      } else if (user.role === "MANAGER") {
        res = await apiClient.getAppraisalsByManager(user.id)
      } else {
        return
      }

      if (res.success && res.data) {
        const cycles = [...new Set(res.data.map((a: Appraisal) => a.cycleName))]
        setAvailableCycles(cycles)
        if (cycles.length > 0 && !cycleName) {
          setCycleName(cycles[0])
        }
      }
    } catch {
      console.error("Failed to load cycles")
    }
  }

  const loadReports = async () => {
    if (!user || !cycleName) return
    setIsLoading(true)
    try {
      if (user.role === "HR") {
        const currentIndex = availableCycles.indexOf(cycleName)
        const previousCycle =
          currentIndex >= 0 ? availableCycles[currentIndex + 1] ?? null : null
        setPrevCycleName(previousCycle)

        const [summaryRes, deptRes, ratingRes] = await Promise.all([
          apiClient.getCycleSummary(cycleName),
          apiClient.getDepartmentBreakdown(cycleName),
          apiClient.getRatingDistribution(cycleName),
        ])

        if (summaryRes.success && summaryRes.data) {
          setCycleSummary(summaryRes.data)
        }
        if (deptRes.success && deptRes.data) {
          setDepartmentBreakdown(deptRes.data)
        }
        if (ratingRes.success && ratingRes.data) {
          setRatingDistribution(ratingRes.data)
        }

        if (previousCycle) {
          const prevSummaryRes = await apiClient.getCycleSummary(previousCycle)
          if (prevSummaryRes.success && prevSummaryRes.data) {
            setPrevCycleSummary(prevSummaryRes.data)
          } else {
            setPrevCycleSummary(null)
          }
        } else {
          setPrevCycleSummary(null)
        }
      } else if (user.role === "MANAGER") {
        const teamRes = await apiClient.getTeamReport(user.id, cycleName)
        if (teamRes.success && teamRes.data) {
          setTeamReport(teamRes.data)
        }
      }
    } catch {
      toast.error("Failed to load reports")
    }
    setIsLoading(false)
  }

  if (!user || (user.role !== "HR" && user.role !== "MANAGER")) {
    return (
      <div className="text-center py-12">
        <p className="text-muted-foreground">You don&apos;t have access to reports</p>
      </div>
    )
  }

  const pendingReviews = cycleSummary
    ? cycleSummary.pending +
    cycleSummary.employeeDraft +
    cycleSummary.selfSubmitted +
    cycleSummary.managerDraft
    : 0

  const departmentStats = departmentBreakdown.map((dept) => {
    const completionRate = dept.totalEmployees
      ? (dept.completed / dept.totalEmployees) * 100
      : 0
    return {
      ...dept,
      completionRate,
    }
  })

  const rankedDepartments = [...departmentStats].sort(
    (a, b) => b.completionRate - a.completionRate,
  )

  const topDepartments = rankedDepartments.slice(0, 1)

  const atRiskTeams = departmentStats.filter((dept) => {
    const lowCompletion = dept.completionRate < 50
    const lowRating = (dept.averageRating ?? 0) > 0 && (dept.averageRating ?? 0) < 3.5
    const missingRating = dept.averageRating == null
    return lowCompletion || lowRating || missingRating
  })

  const ratingBuckets = ratingDistribution
    ? Object.entries(ratingDistribution.distribution)
      .map(([rating, count]) => ({
        rating: parseInt(rating, 10),
        count,
      }))
      .sort((a, b) => b.rating - a.rating)
    : []

  const completionDelta = cycleSummary && prevCycleSummary
    ? cycleSummary.completionPercentage - prevCycleSummary.completionPercentage
    : null

  const averageRatingDelta = cycleSummary && prevCycleSummary
    ? (cycleSummary.averageManagerRating ?? 0) - (prevCycleSummary.averageManagerRating ?? 0)
    : null

  const ratingTrendLabel =
    averageRatingDelta == null
      ? ""
      : `${averageRatingDelta >= 0 ? "Up" : "Down"} ${Math.abs(averageRatingDelta).toFixed(1)}`

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-3xl font-bold tracking-tight">Reports</h1>
        <p className="text-muted-foreground">
          {user.role === "HR"
            ? "View organization-wide performance analytics"
            : "View your team's performance analytics"}
        </p>
      </div>

      {/* Cycle Selector */}
      <Card>
        <CardContent className="pt-6">
          <div className="flex flex-wrap items-center gap-3">
            <Label htmlFor="cycle" className="text-sm font-medium">
              Select Appraisal Cycle
            </Label>
            <Select value={cycleName} onValueChange={setCycleName}>
              <SelectTrigger className="w-[220px]">
                <SelectValue placeholder="Select a cycle" />
              </SelectTrigger>
              <SelectContent>
                {availableCycles.map((cycle) => (
                  <SelectItem key={cycle} value={cycle}>
                    {cycle}
                  </SelectItem>
                ))}
              </SelectContent>
            </Select>
            {isLoading && <Loader2 className="h-5 w-5 animate-spin text-muted-foreground" />}
          </div>
        </CardContent>
      </Card>

      {!cycleName ? (
        <Card>
          <CardContent className="flex flex-col items-center justify-center py-12">
            <BarChart3 className="mb-4 h-12 w-12 text-muted-foreground" />
            <p className="text-muted-foreground">Select a cycle to view reports</p>
          </CardContent>
        </Card>
      ) : user.role === "HR" ? (
        <>
          {/* HR: Actionable Summary */}
          {cycleSummary && (
            <div className="grid gap-4 lg:grid-cols-6">
              <Card className="lg:col-span-3">
                <CardContent className="flex items-center gap-4 p-6">
                  <div className="rounded-lg bg-emerald-100 p-3 text-emerald-600">
                    <TrendingUp className="h-5 w-5" />
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Completion Rate</p>
                    <p className="text-3xl font-bold">
                      {Math.round(cycleSummary.completionPercentage)}%
                    </p>
                    <p className="text-xs text-muted-foreground">
                      {cycleSummary.acknowledged} acknowledged of {cycleSummary.totalAppraisals}
                    </p>
                  </div>
                </CardContent>
              </Card>
              <Card className="lg:col-span-1">
                <CardContent className="flex items-center gap-4 p-6">
                  <div className="rounded-lg bg-amber-100 p-3 text-amber-600">
                    <BarChart3 className="h-5 w-5" />
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Pending Reviews</p>
                    <p className="text-2xl font-bold">{pendingReviews}</p>
                  </div>
                </CardContent>
              </Card>
              <Card className="lg:col-span-1">
                <CardContent className="flex items-center gap-4 p-6">
                  <div className="rounded-lg bg-purple-100 p-3 text-purple-600">
                    <Star className="h-5 w-5" />
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">Avg Rating</p>
                    <p className="text-2xl font-bold">
                      {(cycleSummary.averageManagerRating ?? 0).toFixed(1)}
                    </p>
                    {ratingTrendLabel && (
                      <p className="text-xs text-muted-foreground">{ratingTrendLabel}</p>
                    )}
                  </div>
                </CardContent>
              </Card>
              <Card className="lg:col-span-1">
                <CardContent className="flex items-center gap-4 p-6">
                  <div className="rounded-lg bg-rose-100 p-3 text-rose-600">
                    <AlertCircle className="h-5 w-5" />
                  </div>
                  <div>
                    <p className="text-sm text-muted-foreground">At-Risk Teams</p>
                    <p className="text-2xl font-bold">{atRiskTeams.length}</p>
                  </div>
                </CardContent>
              </Card>
            </div>
          )}

          <div className="grid gap-6 lg:grid-cols-2">
            {/* Department Breakdown */}
            <Card>
              <CardHeader>
                <CardTitle>Department Performance</CardTitle>
                <CardDescription>Top performers and teams needing attention</CardDescription>
              </CardHeader>
              <CardContent>
                {departmentBreakdown.length === 0 ? (
                  <p className="text-center text-muted-foreground py-8">No data available</p>
                ) : (
                  <div className="space-y-5">
                    <div>
                      <p className="text-xs font-semibold uppercase text-muted-foreground">
                        Top performing
                      </p>
                      <div className="mt-2 space-y-3">
                        {topDepartments.map((dept) => {
                          const completionLabel = `${dept.completed}/${dept.totalEmployees}`
                          const completionPercent = Math.round(dept.completionRate)
                          const rating = dept.averageRating?.toFixed(1) ?? "No ratings yet"
                          return (
                            <div
                              key={dept.departmentName}
                              className="rounded-lg border px-4 py-3 transition-colors hover:bg-muted/30"
                            >
                              <div className="flex items-center justify-between">
                                <div>
                                  <p className="font-medium">{dept.departmentName}</p>
                                  <p className="text-xs text-muted-foreground">
                                    {completionLabel} completed
                                  </p>
                                </div>
                                <div className="text-right">
                                  <p className="text-sm font-semibold">{completionPercent}%</p>
                                  <p className="text-xs text-muted-foreground">Avg {rating}</p>
                                </div>
                              </div>
                              <div className="mt-2">
                                <Progress value={dept.completionRate} className="h-2" />
                              </div>
                            </div>
                          )
                        })}
                      </div>
                    </div>
                    <div>
                      <p className="text-xs font-semibold uppercase text-muted-foreground">
                        Needs attention
                      </p>
                      <div className="mt-2 space-y-3">
                        {atRiskTeams.length === 0 ? (
                          <p className="text-sm text-muted-foreground">No at-risk teams right now.</p>
                        ) : (
                          atRiskTeams.map((dept) => {
                            const completionLabel = `${dept.completed}/${dept.totalEmployees}`
                            const completionPercent = Math.round(dept.completionRate)
                            const rating = dept.averageRating?.toFixed(1) ?? "No ratings yet"
                            return (
                              <div
                                key={dept.departmentName}
                                className="rounded-lg border border-rose-100 px-4 py-3 transition-colors hover:bg-rose-50"
                              >
                                <div className="flex items-center justify-between">
                                  <div>
                                    <p className="font-medium">{dept.departmentName}</p>
                                    <p className="text-xs text-muted-foreground">
                                      {completionLabel} completed
                                    </p>
                                  </div>
                                  <div className="text-right">
                                    <p className="text-sm font-semibold">{completionPercent}%</p>
                                    <p className="text-xs text-muted-foreground">Avg {rating}</p>
                                  </div>
                                </div>
                                <div className="mt-2">
                                  <Progress value={dept.completionRate} className="h-2" />
                                </div>
                              </div>
                            )
                          })
                        )}
                      </div>
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>

            {/* Rating Distribution */}
            <Card>
              <CardHeader>
                <CardTitle>Rating Distribution</CardTitle>
                <CardDescription>Manager ratings breakdown</CardDescription>
              </CardHeader>
              <CardContent>
                {!ratingDistribution || ratingDistribution.totalRated === 0 ? (
                  <p className="text-center text-muted-foreground py-8">No data available</p>
                ) : (
                  <div className="space-y-4">
                    <div className="flex items-center justify-between rounded-lg border px-3 py-2">
                      <div>
                        <p className="text-sm text-muted-foreground">Total rated</p>
                        <p className="text-lg font-semibold">
                          {ratingDistribution.totalRated}
                        </p>
                      </div>
                      <div className="text-right">
                        <p className="text-sm text-muted-foreground">Average</p>
                        <p className="text-lg font-semibold">
                          {(ratingDistribution.averageRating ?? 0).toFixed(1)} / 5
                        </p>
                      </div>
                    </div>
                    <div className="space-y-3">
                      {ratingBuckets.map((item) => (
                        <div key={item.rating} className="space-y-2">
                          <div className="flex items-center justify-between text-sm">
                            <span className="font-medium">Rating {item.rating}</span>
                            <span className="text-muted-foreground">
                              {Math.round((item.count / ratingDistribution.totalRated) * 100)}%
                            </span>
                          </div>
                          <Progress
                            value={(item.count / ratingDistribution.totalRated) * 100}
                            className="h-2"
                          />
                        </div>
                      ))}
                    </div>
                  </div>
                )}
              </CardContent>
            </Card>
          </div>
        </>
      ) : (
        /* Manager: Team Report */
        teamReport && (
          <Card>
            <CardHeader>
              <CardTitle>Team Performance - {cycleName}</CardTitle>
              <CardDescription>Your direct reports&apos; appraisal status</CardDescription>
            </CardHeader>
            <CardContent>
              {teamReport.members.length === 0 ? (
                <p className="text-center text-muted-foreground py-8">No team members found</p>
              ) : (
                <div className="space-y-4">
                  {teamReport.members.map((member) => (
                    <div
                      key={member.employeeId}
                      className="flex items-center justify-between rounded-lg border p-4"
                    >
                      <div>
                        <p className="font-medium">{member.employeeName}</p>
                        <div className="flex items-center gap-4 mt-1 text-sm text-muted-foreground">
                          <span>Self: {member.selfRating || "-"}/5</span>
                          <span>Manager: {member.managerRating || "-"}/5</span>
                        </div>
                      </div>
                      <Badge
                        className={statusColors[member.status] ?? "bg-gray-100 text-gray-800"}
                        variant="secondary"
                      >
                        {(member.status ?? "UNKNOWN").replace(/_/g, " ")}
                      </Badge>
                    </div>
                  ))}
                </div>
              )}
            </CardContent>
          </Card>
        )
      )}
    </div>
  )
}
