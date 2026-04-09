"use client"

import { useEffect, useState } from "react"
import { useAuth } from "@/lib/auth-context"
import { apiClient } from "@/lib/api/client"
import type { Appraisal, Department } from "@/lib/api/types"
import Link from "next/link"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Input } from "@/components/ui/input"
import { Label } from "@/components/ui/label"
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
import {
  Table,
  TableBody,
  TableCell,
  TableHead,
  TableHeader,
  TableRow,
} from "@/components/ui/table"
import { toast } from "sonner"
import { Plus, Search, Loader2, ClipboardCheck } from "lucide-react"

const statusColors: Record<string, string> = {
  PENDING: "bg-yellow-100 text-yellow-800",
  EMPLOYEE_DRAFT: "bg-amber-100 text-amber-800",
  SELF_SUBMITTED: "bg-blue-100 text-blue-800",
  MANAGER_DRAFT: "bg-indigo-100 text-indigo-800",
  MANAGER_REVIEWED: "bg-purple-100 text-purple-800",
  APPROVED: "bg-green-100 text-green-800",
  ACKNOWLEDGED: "bg-gray-100 text-gray-800",
}

export default function AppraisalsPage() {
  const { user } = useAuth()
  const [appraisals, setAppraisals] = useState<Appraisal[]>([])
  const [departments, setDepartments] = useState<Department[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [searchQuery, setSearchQuery] = useState("")
  const [statusFilter, setStatusFilter] = useState<string>("all")
  const [isCreateOpen, setIsCreateOpen] = useState(false)
  const [isCreating, setIsCreating] = useState(false)

  // Create cycle form state
  const [cycleName, setCycleName] = useState("")
  const [cycleStartDate, setCycleStartDate] = useState("")
  const [cycleEndDate, setCycleEndDate] = useState("")
  const [cycleDepId, setCycleDepId] = useState<string>("all")

  useEffect(() => {
    if (!user) return
    loadAppraisals()
    if (user.role === "HR") {
      loadDepartments()
    }
  }, [user])

  const loadAppraisals = async () => {
    if (!user) return
    setIsLoading(true)
    try {
      let res
      if (user.role === "HR") {
        res = await apiClient.getAllAppraisals()
      } else if (user.role === "MANAGER") {
        res = await apiClient.getAppraisalsByManager(user.id)
      } else {
        res = await apiClient.getAppraisalsByEmployee(user.id)
      }
      if (res.success && res.data) {
        setAppraisals(res.data)
      }
    } catch {
      toast.error("Failed to load appraisals")
    }
    setIsLoading(false)
  }

  const loadDepartments = async () => {
    const res = await apiClient.getDepartments()
    if (res.success && res.data) {
      setDepartments(res.data)
    }
  }

  const handleCreateCycle = async () => {
    if (!cycleName || !cycleStartDate || !cycleEndDate) {
      toast.error("Please fill all required fields")
      return
    }

    setIsCreating(true)
    try {
      const res = await apiClient.createBulkCycle({
        cycleName,
        cycleStartDate,
        cycleEndDate,
        departmentId: cycleDepId === "all" ? null : parseInt(cycleDepId),
      })
      if (res.success) {
        const created = res.data?.created ?? 0
        const skipped =
          (res.data?.skippedAlreadyExists ?? 0) +
          (res.data?.skippedNoManager ?? 0)
        toast.success(`Created ${created} appraisals${skipped ? ` (${skipped} skipped)` : ""}`)
        setIsCreateOpen(false)
        setCycleName("")
        setCycleStartDate("")
        setCycleEndDate("")
        setCycleDepId("all")
        loadAppraisals()
      } else {
        toast.error(res.message || "Failed to create cycle")
      }
    } catch {
      toast.error("Failed to create cycle")
    }
    setIsCreating(false)
  }

  const filteredAppraisals = appraisals.filter((a) => {
    const matchesSearch =
      a.employeeName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      a.cycleName.toLowerCase().includes(searchQuery.toLowerCase()) ||
      a.managerName.toLowerCase().includes(searchQuery.toLowerCase())
    const matchesStatus =
      statusFilter === "all" || a.appraisalStatus === statusFilter
    return matchesSearch && matchesStatus
  })

  if (!user) return null

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Appraisals</h1>
          <p className="text-muted-foreground">
            {user.role === "HR"
              ? "Manage all appraisal cycles and reviews"
              : user.role === "MANAGER"
                ? "Review your team members"
                : "View your performance reviews"}
          </p>
        </div>
        {user.role === "HR" && (
          <Dialog open={isCreateOpen} onOpenChange={setIsCreateOpen}>
            <DialogTrigger asChild>
              <Button>
                <Plus className="mr-2 h-4 w-4" />
                Create Cycle
              </Button>
            </DialogTrigger>
            <DialogContent>
              <DialogHeader>
                <DialogTitle>Create Appraisal Cycle</DialogTitle>
                <DialogDescription>
                  Create a new appraisal cycle for all employees or a specific department.
                </DialogDescription>
              </DialogHeader>
              <div className="space-y-4 py-4">
                <div className="space-y-2">
                  <Label htmlFor="cycleName">Cycle Name</Label>
                  <Input
                    id="cycleName"
                    placeholder="e.g., Annual Review 2025"
                    value={cycleName}
                    onChange={(e) => setCycleName(e.target.value)}
                  />
                </div>
                <div className="grid grid-cols-2 gap-4">
                  <div className="space-y-2">
                    <Label htmlFor="startDate">Start Date</Label>
                    <Input
                      id="startDate"
                      type="date"
                      value={cycleStartDate}
                      onChange={(e) => setCycleStartDate(e.target.value)}
                    />
                  </div>
                  <div className="space-y-2">
                    <Label htmlFor="endDate">End Date</Label>
                    <Input
                      id="endDate"
                      type="date"
                      value={cycleEndDate}
                      onChange={(e) => setCycleEndDate(e.target.value)}
                    />
                  </div>
                </div>
                <div className="space-y-2">
                  <Label htmlFor="department">Department (Optional)</Label>
                  <Select value={cycleDepId} onValueChange={setCycleDepId}>
                    <SelectTrigger>
                      <SelectValue placeholder="All Departments" />
                    </SelectTrigger>
                    <SelectContent>
                      <SelectItem value="all">All Departments</SelectItem>
                      {departments.map((d) => (
                        <SelectItem key={d.id} value={d.id.toString()}>
                          {d.name}
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
                <Button onClick={handleCreateCycle} disabled={isCreating}>
                  {isCreating && <Loader2 className="mr-2 h-4 w-4 animate-spin" />}
                  Create Cycle
                </Button>
              </DialogFooter>
            </DialogContent>
          </Dialog>
        )}
      </div>

      <Card>
        <CardHeader>
          <div className="flex flex-col gap-4 sm:flex-row sm:items-center sm:justify-between">
            <div>
              <CardTitle>All Appraisals</CardTitle>
              <CardDescription>{filteredAppraisals.length} appraisals found</CardDescription>
            </div>
            <div className="flex flex-col gap-2 sm:flex-row">
              <div className="relative">
                <Search className="absolute left-3 top-1/2 h-4 w-4 -translate-y-1/2 text-muted-foreground" />
                <Input
                  placeholder="Search..."
                  value={searchQuery}
                  onChange={(e) => setSearchQuery(e.target.value)}
                  className="pl-9"
                />
              </div>
              <Select value={statusFilter} onValueChange={setStatusFilter}>
                <SelectTrigger className="w-[180px]">
                  <SelectValue placeholder="Filter by status" />
                </SelectTrigger>
                <SelectContent>
                  <SelectItem value="all">All Statuses</SelectItem>
                  <SelectItem value="PENDING">Pending</SelectItem>
                  <SelectItem value="EMPLOYEE_DRAFT">Employee Draft</SelectItem>
                  <SelectItem value="SELF_SUBMITTED">Self Submitted</SelectItem>
                  <SelectItem value="MANAGER_DRAFT">Manager Draft</SelectItem>
                  <SelectItem value="MANAGER_REVIEWED">Manager Reviewed</SelectItem>
                  <SelectItem value="APPROVED">Approved</SelectItem>
                  <SelectItem value="ACKNOWLEDGED">Acknowledged</SelectItem>
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
          ) : filteredAppraisals.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <ClipboardCheck className="mb-4 h-12 w-12 text-muted-foreground" />
              <h3 className="text-lg font-medium">No appraisals found</h3>
              <p className="text-sm text-muted-foreground">
                {user.role === "HR"
                  ? "Create a new appraisal cycle to get started."
                  : "You have no appraisals assigned yet."}
              </p>
            </div>
          ) : (
            <Table>
              <TableHeader>
                <TableRow>
                  <TableHead>Employee</TableHead>
                  <TableHead>Cycle</TableHead>
                  <TableHead>Manager</TableHead>
                  <TableHead>Status</TableHead>
                  <TableHead>Rating</TableHead>
                  <TableHead className="text-right">Actions</TableHead>
                </TableRow>
              </TableHeader>
              <TableBody>
                {filteredAppraisals.map((appraisal) => (
                  <TableRow key={appraisal.id}>
                    <TableCell className="font-medium">{appraisal.employeeName}</TableCell>
                    <TableCell>{appraisal.cycleName}</TableCell>
                    <TableCell>{appraisal.managerName}</TableCell>
                    <TableCell>
                      <Badge
                        className={
                          statusColors[appraisal.appraisalStatus] ?? "bg-gray-100 text-gray-800"
                        }
                        variant="secondary"
                      >
                        {(appraisal.appraisalStatus ?? "UNKNOWN").replace(/_/g, " ")}
                      </Badge>
                    </TableCell>
                    <TableCell>
                      {appraisal.managerRating || appraisal.selfRating ? (
                        <div className="flex flex-col text-sm">
                          <span>
                            <span className="text-muted-foreground">Manager:</span>{" "}
                            {appraisal.managerRating ?? "-"}
                          </span>
                          <span>
                            <span className="text-muted-foreground">Employee:</span>{" "}
                            {appraisal.selfRating ?? "-"}
                          </span>
                        </div>
                      ) : (
                        "-"
                      )}
                    </TableCell>
                    <TableCell className="text-right">
                      <Link href={`/dashboard/appraisals/${appraisal.id}`}>
                        <Button variant="ghost" size="sm">
                          View
                        </Button>
                      </Link>
                    </TableCell>
                  </TableRow>
                ))}
              </TableBody>
            </Table>
          )}
        </CardContent>
      </Card>
    </div>
  )
}
