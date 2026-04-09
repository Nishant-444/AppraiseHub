"use client"

import { createContext, useContext, useEffect, useState, useCallback, type ReactNode } from "react"
import { apiClient } from "@/lib/api/client"
import type { User } from "@/lib/api/types"

interface AuthContextType {
  user: User | null
  isLoading: boolean
  isAuthenticated: boolean
  login: (email: string, password: string) => Promise<{ success: boolean; message: string }>
  logout: () => void
  refreshUser: () => Promise<void>
}

const AuthContext = createContext<AuthContextType | undefined>(undefined)

export function AuthProvider({ children }: { children: ReactNode }) {
  const [user, setUser] = useState<User | null>(null)
  const [isLoading, setIsLoading] = useState(true)

  const refreshUser = useCallback(async () => {
    const token = apiClient.getToken()
    if (!token) {
      setUser(null)
      setIsLoading(false)
      return
    }

    try {
      const response = await apiClient.getProfile()
      if (response.success && response.data) {
        const data = response.data as any
        const hydratedUser: User | null = data.user
          ? data.user
          : data.userId
            ? {
              id: data.userId,
              fullName: data.fullName ?? "",
              email: data.email ?? "",
              role: (data.role as User["role"]) ?? "EMPLOYEE",
              jobTitle: data.jobTitle ?? "",
              departmentName: data.departmentName,
              managerId: data.managerId ?? undefined,
              managerName: data.managerName ?? undefined,
              isActive: data.isActive ?? true,
              createdAt: data.createdAt ?? new Date().toISOString(),
            }
            : null

        if (hydratedUser) {
          setUser(hydratedUser)
        } else {
          apiClient.setToken(null)
          setUser(null)
        }
      } else {
        apiClient.setToken(null)
        setUser(null)
      }
    } catch {
      apiClient.setToken(null)
      setUser(null)
    } finally {
      setIsLoading(false)
    }
  }, [])

  useEffect(() => {
    refreshUser()
  }, [refreshUser])

  const login = async (email: string, password: string) => {
    try {
      const response = await apiClient.login({ email, password })
      if (response.success && response.data) {
        apiClient.setToken(response.data.token)
        const data = response.data
        const hydratedUser: User | null = data.user
          ? data.user
          : data.userId
            ? {
              id: data.userId,
              fullName: data.fullName ?? "",
              email: data.email ?? "",
              role: (data.role as User["role"]) ?? "EMPLOYEE",
              jobTitle: data.jobTitle ?? "",
              departmentName: data.departmentName,
              managerId: data.managerId ?? undefined,
              managerName: data.managerName ?? undefined,
              isActive: data.isActive ?? true,
              createdAt: data.createdAt ?? new Date().toISOString(),
            }
            : null

        if (hydratedUser) {
          setUser(hydratedUser)
        } else {
          await refreshUser()
        }
        return { success: true, message: "Login successful" }
      }
      return { success: false, message: response.message || "Login failed" }
    } catch {
      return { success: false, message: "Network error. Please try again." }
    }
  }

  const logout = () => {
    apiClient.setToken(null)
    setUser(null)
  }

  return (
    <AuthContext.Provider
      value={{
        user,
        isLoading,
        isAuthenticated: !!user,
        login,
        logout,
        refreshUser,
      }}
    >
      {children}
    </AuthContext.Provider>
  )
}

export function useAuth() {
  const context = useContext(AuthContext)
  if (context === undefined) {
    throw new Error("useAuth must be used within an AuthProvider")
  }
  return context
}
