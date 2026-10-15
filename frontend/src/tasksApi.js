const apiBaseUrl = (import.meta.env.VITE_API_BASE_URL || 'http://127.0.0.1:8081').replace(/\/$/, '')

async function request(path, options) {
  const response = await fetch(`${apiBaseUrl}${path}`, {
    ...options,
    headers: {
      Accept: 'application/json',
      ...(options?.body ? { 'Content-Type': 'application/json' } : {}),
      ...options?.headers,
    },
  })
  const body = await response.text()
  let data
  if (body) {
    try {
      data = JSON.parse(body)
    } catch {
      throw new Error(`The server returned an unreadable response (${response.status}).`)
    }
  }

  if (!response.ok) {
    throw new Error(data?.detail || `Request failed (${response.status}).`)
  }
  if (!data) {
    throw new Error('The server returned an empty response.')
  }
  return data
}

export function getTaskDashboard() {
  return request('/api/tasks/dashboard').then((dashboard) => {
    if (!Array.isArray(dashboard?.items)) {
      throw new Error('The server returned an invalid task dashboard.')
    }
    return dashboard
  })
}

export function createTask(task) {
  return request('/api/tasks', {
    method: 'POST',
    body: JSON.stringify(task),
  })
}

export function updateTask(taskId, task) {
  return request(`/api/tasks/${encodeURIComponent(taskId)}`, {
    method: 'PUT',
    body: JSON.stringify(task),
  })
}

export function updateTaskStatus(taskId, status) {
  return request(`/api/tasks/${encodeURIComponent(taskId)}/status`, {
    method: 'PATCH',
    body: JSON.stringify({ status }),
  })
}
