"use client"

import { useEffect, useState, use } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/lib/auth-context"
import { apiClient } from "@/lib/api/client"
import type { Appraisal, Goal } from "@/lib/api/types"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Label } from "@/components/ui/label"
import { Textarea } from "@/components/ui/textarea"
import {
  Select,
  SelectContent,
  SelectItem,
  SelectTrigger,
  SelectValue,
} from "@/components/ui/select"
import { toast } from "sonner"
import {
  ArrowLeft,
  Loader2,
  Star,
  Target,
  CheckCircle2,
  User,
  Calendar,
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

export default function AppraisalDetailPage({ params }: { params: Promise<{ id: string }> }) {
  const { id } = use(params)
  const router = useRouter()
  const { user } = useAuth()
  const [appraisal, setAppraisal] = useState<Appraisal | null>(null)
  const [goals, setGoals] = useState<Goal[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isSaving, setIsSaving] = useState(false)

  // Self Assessment Form
  const [whatWentWell, setWhatWentWell] = useState("")
  const [whatToImprove, setWhatToImprove] = useState("")
  const [achievements, setAchievements] = useState("")
  const [selfRating, setSelfRating] = useState<string>("")

  // Manager Review Form
  const [managerStrengths, setManagerStrengths] = useState("")
  const [managerImprovements, setManagerImprovements] = useState("")
  const [managerComments, setManagerComments] = useState("")
  const [managerRating, setManagerRating] = useState<string>("")

  useEffect(() => {
    if (!user) return
    loadAppraisal()
  }, [id, user])

  const loadAppraisal = async () => {
    setIsLoading(true)
    try {
      if (!user) return
      const res = await apiClient.getAppraisalById(parseInt(id), user.id)
      if (res.success && res.data) {
        setAppraisal(res.data)
        // Populate forms with existing data
        setWhatWentWell(res.data.whatWentWell ?? "")
        setWhatToImprove(res.data.whatToImprove ?? "")
        setAchievements(res.data.achievements ?? "")
        setSelfRating(res.data.selfRating ? res.data.selfRating.toString() : "")
        setManagerStrengths(res.data.managerStrengths ?? "")
        setManagerImprovements(res.data.managerImprovements ?? "")
        setManagerComments(res.data.managerComments ?? "")
        setManagerRating(res.data.managerRating ? res.data.managerRating.toString() : "")

        // Load goals
        let resolvedGoals: Goal[] = []
        if (res.data.employeeId) {
          const employeeGoalsRes = await apiClient.getGoalsByEmployee(res.data.employeeId)
          if (employeeGoalsRes.success && employeeGoalsRes.data) {
            resolvedGoals = employeeGoalsRes.data.filter(
              (goal) => goal.appraisalId === res.data.id,
            )
          }
        }

        if (resolvedGoals.length === 0) {
          const goalsRes = await apiClient.getGoalsByAppraisal(parseInt(id))
          if (goalsRes.success && goalsRes.data) {
            resolvedGoals = goalsRes.data
          }
        }

        setGoals(resolvedGoals)

      }
    } catch {
      toast.error("Failed to load appraisal")
    }
    setIsLoading(false)
  }

  const handleSelfAssessment = async (mode: "draft" | "submit") => {
    if (!user || !appraisal) return
    if (!whatWentWell || !whatToImprove || !achievements || !selfRating) {
      toast.error("Please fill all fields")
      return
    }

    setIsSaving(true)
    try {
      const payload = {
        whatWentWell,
        whatToImprove,
        achievements,
        selfRating: parseInt(selfRating),
      }
      const res =
        mode === "draft"
          ? await apiClient.saveSelfAssessmentDraft(appraisal.id, user.id, payload)
          : await apiClient.submitSelfAssessment(appraisal.id, user.id, payload)
      if (res.success) {
        toast.success(
          mode === "draft"
            ? "Draft saved successfully"
            : "Self assessment submitted successfully",
        )
        loadAppraisal()
      } else {
        toast.error(res.message || "Failed to submit")
      }
    } catch {
      toast.error("Failed to submit self assessment")
    }
    setIsSaving(false)
  }

  const handleManagerReview = async (mode: "draft" | "submit") => {
    if (!user || !appraisal) return
    if (!managerStrengths || !managerImprovements || !managerRating) {
      toast.error("Please fill all fields")
      return
    }

    setIsSaving(true)
    try {
      const payload = {
        managerStrengths,
        managerImprovements,
        managerComments: managerComments || "",
        managerRating: parseInt(managerRating),
      }
      const res =
        mode === "draft"
          ? await apiClient.saveManagerReviewDraft(appraisal.id, user.id, payload)
          : await apiClient.submitManagerReview(appraisal.id, user.id, payload)
      if (res.success) {
        toast.success(
          mode === "draft"
            ? "Draft saved successfully"
            : "Manager review submitted successfully",
        )
        loadAppraisal()
      } else {
        toast.error(res.message || "Failed to submit")
      }
    } catch {
      toast.error("Failed to submit manager review")
    }
    setIsSaving(false)
  }

  const handleApprove = async () => {
    if (!appraisal) return
    setIsSaving(true)
    try {
      const res = await apiClient.approveAppraisal(appraisal.id)
      if (res.success) {
        toast.success("Appraisal approved")
        loadAppraisal()
      } else {
        toast.error(res.message || "Failed to approve")
      }
    } catch {
      toast.error("Failed to approve appraisal")
    }
    setIsSaving(false)
  }

  const handleAcknowledge = async () => {
    if (!user || !appraisal) return
    setIsSaving(true)
    try {
      const res = await apiClient.acknowledgeAppraisal(appraisal.id, user.id)
      if (res.success) {
        toast.success("Appraisal acknowledged")
        loadAppraisal()
      } else {
        toast.error(res.message || "Failed to acknowledge")
      }
    } catch {
      toast.error("Failed to acknowledge appraisal")
    }
    setIsSaving(false)
  }

  if (isLoading) {
    return (
      <div className="flex items-center justify-center py-12">
        <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
      </div>
    )
  }

  if (!appraisal || !user) {
    return (
      <div className="text-center py-12">
        <p>Appraisal not found</p>
        <Button variant="link" onClick={() => router.push("/dashboard/appraisals")}>
          Go back
        </Button>
      </div>
    )
  }

  const isEmployee = user.id === appraisal.employeeId
  const isManager = user.id === appraisal.managerId
  const isHR = user.role === "HR"

  const canEditSelfAssessment =
    isEmployee &&
    (appraisal.appraisalStatus === "PENDING" ||
      appraisal.appraisalStatus === "EMPLOYEE_DRAFT")
  const canEditManagerReview =
    isManager &&
    (appraisal.appraisalStatus === "SELF_SUBMITTED" ||
      appraisal.appraisalStatus === "MANAGER_DRAFT")
  const canApprove = isHR && appraisal.appraisalStatus === "MANAGER_REVIEWED"
  const canAcknowledge = isEmployee && appraisal.appraisalStatus === "APPROVED"

  return (
    <div className="space-y-6">
      <div className="flex items-center gap-4">
        <Button variant="ghost" size="icon" onClick={() => router.push("/dashboard/appraisals")}>
          <ArrowLeft className="h-5 w-5" />
        </Button>
        <div className="flex-1">
          <h1 className="text-3xl font-bold tracking-tight">{appraisal.cycleName}</h1>
          <p className="text-muted-foreground">Appraisal for {appraisal.employeeName}</p>
        </div>
        <Badge
          className={statusColors[appraisal.appraisalStatus] ?? "bg-gray-100 text-gray-800"}
          variant="secondary"
        >
          {(appraisal.appraisalStatus ?? "UNKNOWN").replace(/_/g, " ")}
        </Badge>
      </div>

      {/* Overview Card */}
      <Card>
        <CardHeader>
          <CardTitle>Overview</CardTitle>
        </CardHeader>
        <CardContent>
          <div className="grid gap-4 md:grid-cols-2 lg:grid-cols-4">
            <div className="flex items-center gap-3">
              <User className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">Employee</p>
                <p className="font-medium">{appraisal.employeeName}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <User className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">Manager</p>
                <p className="font-medium">{appraisal.managerName}</p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Calendar className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">Period</p>
                <p className="font-medium">
                  {new Date(appraisal.cycleStartDate).toLocaleDateString()} -{" "}
                  {new Date(appraisal.cycleEndDate).toLocaleDateString()}
                </p>
              </div>
            </div>
            <div className="flex items-center gap-3">
              <Star className="h-5 w-5 text-muted-foreground" />
              <div>
                <p className="text-sm text-muted-foreground">Ratings</p>
                <p className="font-medium">
                  Self: {appraisal.selfRating || "-"} | Manager: {appraisal.managerRating || "-"}
                </p>
              </div>
            </div>
          </div>
        </CardContent>
      </Card>

      <div className="grid gap-6 lg:grid-cols-2">
        {/* Self Assessment */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <User className="h-5 w-5" />
              Self Assessment
            </CardTitle>
            <CardDescription>
              {canEditSelfAssessment
                ? "Complete your self assessment"
                : "Employee self evaluation"}
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label>What went well?</Label>
              <Textarea
                placeholder="Describe your achievements and successes..."
                value={whatWentWell}
                onChange={(e) => setWhatWentWell(e.target.value)}
                disabled={!canEditSelfAssessment}
                rows={3}
              />
            </div>
            <div className="space-y-2">
              <Label>What could be improved?</Label>
              <Textarea
                placeholder="Areas for growth and development..."
                value={whatToImprove}
                onChange={(e) => setWhatToImprove(e.target.value)}
                disabled={!canEditSelfAssessment}
                rows={3}
              />
            </div>
            <div className="space-y-2">
              <Label>Key Achievements</Label>
              <Textarea
                placeholder="List your main accomplishments..."
                value={achievements}
                onChange={(e) => setAchievements(e.target.value)}
                disabled={!canEditSelfAssessment}
                rows={3}
              />
            </div>
            <div className="space-y-2">
              <Label>Self Rating (1-5)</Label>
              <Select value={selfRating} onValueChange={setSelfRating} disabled={!canEditSelfAssessment}>
                <SelectTrigger>
                  <SelectValue placeholder="Select rating" />
                </SelectTrigger>
                <SelectContent>
                  {[1, 2, 3, 4, 5].map((r) => (
                    <SelectItem key={r} value={r.toString()}>
                      {r} - {["Needs Improvement", "Below Expectations", "Meets Expectations", "Exceeds Expectations", "Outstanding"][r - 1]}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            {canEditSelfAssessment && (
              <div className="flex flex-col gap-2 sm:flex-row">
                <Button
                  variant="outline"
                  onClick={() => handleSelfAssessment("draft")}
                  disabled={isSaving}
                  className="w-full max-w-[220px] self-start"
                >
                  {isSaving && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  Save Draft
                </Button>
                <Button
                  onClick={() => handleSelfAssessment("submit")}
                  disabled={isSaving}
                  className="w-full max-w-[220px] self-start"
                >
                  {isSaving && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  Submit Self Assessment
                </Button>
              </div>
            )}
          </CardContent>
        </Card>

        {/* Manager Review */}
        <Card>
          <CardHeader>
            <CardTitle className="flex items-center gap-2">
              <Star className="h-5 w-5" />
              Manager Review
            </CardTitle>
            <CardDescription>
              {canEditManagerReview
                ? "Complete your review"
                : "Manager evaluation and review"}
            </CardDescription>
          </CardHeader>
          <CardContent className="space-y-4">
            <div className="space-y-2">
              <Label>Strengths</Label>
              <Textarea
                placeholder="Employee's key strengths..."
                value={managerStrengths}
                onChange={(e) => setManagerStrengths(e.target.value)}
                disabled={!canEditManagerReview}
                rows={3}
              />
            </div>
            <div className="space-y-2">
              <Label>Areas for Improvement</Label>
              <Textarea
                placeholder="Areas where the employee can grow..."
                value={managerImprovements}
                onChange={(e) => setManagerImprovements(e.target.value)}
                disabled={!canEditManagerReview}
                rows={3}
              />
            </div>
            <div className="space-y-2">
              <Label>Comments</Label>
              <Textarea
                placeholder="Overall comments and notes..."
                value={managerComments}
                onChange={(e) => setManagerComments(e.target.value)}
                disabled={!canEditManagerReview}
                rows={3}
              />
            </div>
            <div className="space-y-2">
              <Label>Manager Rating (1-5)</Label>
              <Select value={managerRating} onValueChange={setManagerRating} disabled={!canEditManagerReview}>
                <SelectTrigger>
                  <SelectValue placeholder="Select rating" />
                </SelectTrigger>
                <SelectContent>
                  {[1, 2, 3, 4, 5].map((r) => (
                    <SelectItem key={r} value={r.toString()}>
                      {r} - {["Needs Improvement", "Below Expectations", "Meets Expectations", "Exceeds Expectations", "Outstanding"][r - 1]}
                    </SelectItem>
                  ))}
                </SelectContent>
              </Select>
            </div>
            {canEditManagerReview && (
              <div className="flex flex-col gap-2 sm:flex-row">
                <Button
                  variant="outline"
                  onClick={() => handleManagerReview("draft")}
                  disabled={isSaving}
                  className="w-full max-w-[220px] self-start"
                >
                  {isSaving && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  Save Draft
                </Button>
                <Button
                  onClick={() => handleManagerReview("submit")}
                  disabled={isSaving}
                  className="w-full max-w-[220px] self-start"
                >
                  {isSaving && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  Submit Manager Review
                </Button>
              </div>
            )}
          </CardContent>
        </Card>
      </div>

      {/* Goals */}
      <Card>
        <CardHeader>
          <CardTitle className="flex items-center gap-2">
            <Target className="h-5 w-5" />
            Goals
          </CardTitle>
          <CardDescription>Performance objectives for this cycle</CardDescription>
        </CardHeader>
        <CardContent>
          {goals.length === 0 ? (
            <p className="text-center text-muted-foreground py-4">No goals assigned</p>
          ) : (
            <div className="space-y-4">
              {goals.map((goal) => (
                <div key={goal.id} className="rounded-lg border p-4">
                  <div className="flex items-start justify-between gap-2">
                    <div>
                      <p className="font-medium">{goal.title}</p>
                      {goal.description && (
                        <p className="text-sm text-muted-foreground">{goal.description}</p>
                      )}
                      <p className="text-sm text-muted-foreground mt-1">
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

      {/* Action Buttons */}
      {(canApprove || canAcknowledge) && (
        <Card>
          <CardContent className="pt-6">
            <div className="flex items-center justify-between">
              <div>
                <p className="font-medium">
                  {canApprove ? "Approve this appraisal?" : "Acknowledge this appraisal?"}
                </p>
                <p className="text-sm text-muted-foreground">
                  {canApprove
                    ? "Review the assessments and approve to finalize"
                    : "Confirm you have reviewed your appraisal results"}
                </p>
              </div>
              <Button onClick={canApprove ? handleApprove : handleAcknowledge} disabled={isSaving}>
                {isSaving && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                <CheckCircle2 className="mr-2 h-4 w-4" />
                {canApprove ? "Approve" : "Acknowledge"}
              </Button>
            </div>
          </CardContent>
        </Card>
      )}
    </div>
  )
}
