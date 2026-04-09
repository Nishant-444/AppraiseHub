"use client"

import { useEffect, useState } from "react"
import { useRouter } from "next/navigation"
import { useAuth } from "@/lib/auth-context"
import { apiClient } from "@/lib/api/client"
import type { Notification } from "@/lib/api/types"
import { Button } from "@/components/ui/button"
import { Card, CardContent, CardDescription, CardHeader, CardTitle } from "@/components/ui/card"
import { Badge } from "@/components/ui/badge"
import { Separator } from "@/components/ui/separator"
import { toast } from "sonner"
import {
  Loader2,
  Bell,
  BellOff,
  CheckCheck,
  Circle,
  Clock,
  AlertCircle,
  Info,
} from "lucide-react"

export default function NotificationsPage() {
  const router = useRouter()
  const { user } = useAuth()
  const [notifications, setNotifications] = useState<Notification[]>([])
  const [isLoading, setIsLoading] = useState(true)
  const [isMarkingAll, setIsMarkingAll] = useState(false)
  const [openingId, setOpeningId] = useState<number | null>(null)
  const [showRead, setShowRead] = useState(true)

  useEffect(() => {
    if (!user) return
    loadNotifications()
  }, [user])

  const loadNotifications = async () => {
    if (!user) return
    setIsLoading(true)
    try {
      const res = await apiClient.getNotifications(user.id)
      if (res.success && res.data) {
        const normalized = res.data.map((notif: any) => ({
          ...notif,
          isRead: typeof notif.isRead === "boolean" ? notif.isRead : !!notif.read,
        }))
        setNotifications(normalized)
      }
    } catch {
      toast.error("Failed to load notifications")
    }
    setIsLoading(false)
  }

  const handleMarkRead = async (notificationId: number) => {
    try {
      if (!user) return
      const res = await apiClient.markNotificationRead(notificationId, user.id)
      if (res.success) {
        setNotifications((prev) =>
          prev.map((n) => (n.id === notificationId ? { ...n, isRead: true } : n))
        )
      }
    } catch {
      toast.error("Failed to mark as read")
    }
  }

  const handleMarkAllRead = async () => {
    if (!user) return
    setIsMarkingAll(true)
    try {
      const res = await apiClient.markAllNotificationsRead(user.id)
      if (res.success) {
        setNotifications((prev) => prev.map((n) => ({ ...n, isRead: true })))
        toast.success("All notifications marked as read")
      }
    } catch {
      toast.error("Failed to mark all as read")
    }
    setIsMarkingAll(false)
  }

  const unreadCount = notifications.filter((n) => !n.isRead).length
  const readCount = notifications.filter((n) => n.isRead).length
  const visibleNotifications = showRead
    ? notifications
    : notifications.filter((n) => !n.isRead)

  const getNotificationIcon = (title: string) => {
    if (title.toLowerCase().includes("reminder")) return Clock
    if (title.toLowerCase().includes("alert") || title.toLowerCase().includes("urgent"))
      return AlertCircle
    return Info
  }

  const formatTimeAgo = (dateString: string) => {
    const date = new Date(dateString)
    const now = new Date()
    const diffMs = now.getTime() - date.getTime()
    const diffMins = Math.floor(diffMs / 60000)
    const diffHours = Math.floor(diffMs / 3600000)
    const diffDays = Math.floor(diffMs / 86400000)

    if (diffMins < 60) return `${diffMins}m ago`
    if (diffHours < 24) return `${diffHours}h ago`
    if (diffDays < 7) return `${diffDays}d ago`
    return date.toLocaleDateString()
  }

  const getNotificationTarget = (notif: Notification) => {
    if (!user) return null
    const type = notif.type
    switch (type) {
      case "CYCLE_STARTED":
      case "APPRAISAL_DUE":
      case "SELF_ASSESSMENT_SUBMITTED":
      case "MANAGER_REVIEW_DONE":
      case "APPRAISAL_APPROVED":
        return "/dashboard/appraisals"
      default:
        return "/dashboard"
    }
  }

  const handleOpenNotification = async (notif: Notification) => {
    const target = getNotificationTarget(notif)
    if (!target) return
    setOpeningId(notif.id)
    try {
      if (user && !notif.isRead) {
        await apiClient.markNotificationRead(notif.id, user.id)
        setNotifications((prev) =>
          prev.map((n) => (n.id === notif.id ? { ...n, isRead: true } : n))
        )
      }
    } finally {
      setOpeningId(null)
      router.push(target)
    }
  }

  if (!user) return null

  return (
    <div className="space-y-6">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-3xl font-bold tracking-tight">Notifications</h1>
          <p className="text-muted-foreground">
            Stay updated on your appraisal activities
          </p>
        </div>
        <div className="flex items-center gap-2">
          <Button
            variant="outline"
            onClick={() => setShowRead((prev) => !prev)}
          >
            {showRead ? "Hide read" : "Show read"}
          </Button>
          {unreadCount > 0 && (
            <Button onClick={handleMarkAllRead} disabled={isMarkingAll} variant="outline">
              {isMarkingAll ? (
                <Loader2 className="mr-2 h-4 w-4 animate-spin" />
              ) : (
                <CheckCheck className="mr-2 h-4 w-4" />
              )}
              Mark all as read
            </Button>
          )}
        </div>
      </div>

      {/* Stats */}
      <div className="grid gap-4 md:grid-cols-3">
        <Card>
          <CardContent className="flex items-center gap-4 p-6">
            <div className="rounded-lg bg-blue-100 p-3 text-blue-600">
              <Bell className="h-5 w-5" />
            </div>
            <div>
              <p className="text-2xl font-bold">{notifications.length}</p>
              <p className="text-sm text-muted-foreground">Total Notifications</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-4 p-6">
            <div className="rounded-lg bg-yellow-100 p-3 text-yellow-600">
              <Circle className="h-5 w-5 fill-current" />
            </div>
            <div>
              <p className="text-2xl font-bold">{unreadCount}</p>
              <p className="text-sm text-muted-foreground">Unread</p>
            </div>
          </CardContent>
        </Card>
        <Card>
          <CardContent className="flex items-center gap-4 p-6">
            <div className="rounded-lg bg-green-100 p-3 text-green-600">
              <CheckCheck className="h-5 w-5" />
            </div>
            <div>
              <p className="text-2xl font-bold">{readCount}</p>
              <p className="text-sm text-muted-foreground">Read</p>
            </div>
          </CardContent>
        </Card>
      </div>

      <Card>
        <CardHeader>
          <CardTitle>All Notifications</CardTitle>
          <CardDescription>
            {unreadCount > 0
              ? `You have ${unreadCount} unread notification${unreadCount > 1 ? "s" : ""}`
              : "All caught up!"}
          </CardDescription>
        </CardHeader>
        <CardContent>
          {isLoading ? (
            <div className="flex items-center justify-center py-12">
              <Loader2 className="h-8 w-8 animate-spin text-muted-foreground" />
            </div>
          ) : visibleNotifications.length === 0 ? (
            <div className="flex flex-col items-center justify-center py-12 text-center">
              <BellOff className="mb-4 h-12 w-12 text-muted-foreground" />
              <h3 className="text-lg font-medium">No notifications</h3>
              <p className="text-sm text-muted-foreground">
                {notifications.length === 0
                  ? "You don't have any notifications yet"
                  : "No unread notifications"}
              </p>
            </div>
          ) : (
            <div className="space-y-1">
              {visibleNotifications.map((notif, index) => {
                const NotifIcon = getNotificationIcon(notif.title)
                return (
                  <div key={notif.id}>
                    <div
                      className={`flex items-start gap-4 rounded-lg p-4 transition-colors ${!notif.isRead ? "bg-muted/50" : "hover:bg-muted/30"
                        }`}
                    >
                      <div
                        className={`mt-0.5 rounded-lg p-2 ${!notif.isRead
                          ? "bg-blue-100 text-blue-600"
                          : "bg-gray-100 text-gray-600"
                          }`}
                      >
                        <NotifIcon className="h-4 w-4" />
                      </div>
                      <div className="flex-1 min-w-0">
                        <div className="flex items-start justify-between gap-2">
                          <div className="flex items-center gap-2">
                            <p className="font-medium">{notif.title}</p>
                            {!notif.isRead && (
                              <Badge variant="secondary" className="bg-blue-100 text-blue-800">
                                New
                              </Badge>
                            )}
                          </div>
                          <span className="text-xs text-muted-foreground whitespace-nowrap">
                            {formatTimeAgo(notif.createdAt)}
                          </span>
                        </div>
                        <p className="text-sm text-muted-foreground mt-1">{notif.message}</p>
                        <div className="mt-2 flex items-center gap-3">
                          {!notif.isRead && (
                            <Button
                              variant="ghost"
                              size="sm"
                              className="h-auto p-0 text-xs text-blue-600 hover:text-blue-800"
                              onClick={() => handleMarkRead(notif.id)}
                            >
                              Mark as read
                            </Button>
                          )}
                          <Button
                            variant="ghost"
                            size="sm"
                            className="h-auto p-0 text-xs text-muted-foreground hover:text-foreground"
                            onClick={() => handleOpenNotification(notif)}
                            disabled={openingId === notif.id}
                          >
                            {openingId === notif.id ? "Opening..." : "Open"}
                          </Button>
                        </div>
                      </div>
                    </div>
                    {index < visibleNotifications.length - 1 && <Separator />}
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
