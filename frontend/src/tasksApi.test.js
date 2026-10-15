import { afterEach, describe, expect, it, vi } from 'vitest'
import { createTask, getTaskDashboard, updateTask, updateTaskStatus } from './tasksApi.js'

function response(data, status = 200) {
  return {
    ok: status >= 200 && status < 300,
    status,
    text: async () => typeof data === 'string' ? data : JSON.stringify(data),
  }
}

afterEach(() => {
  vi.unstubAllGlobals()
})

describe('task API client', () => {
  it('reads the dashboard response and validates its item list', async () => {
    const dashboard = { items: [], total: 0, limit: 7 }
    const fetchMock = vi.fn().mockResolvedValue(response(dashboard))
    vi.stubGlobal('fetch', fetchMock)

    await expect(getTaskDashboard()).resolves.toEqual(dashboard)
    expect(fetchMock.mock.calls[0][0]).toMatch(/\/api\/tasks\/dashboard$/)
    expect(fetchMock.mock.calls[0][1].headers.Accept).toBe('application/json')
  })

  it('rejects a successful response with an invalid dashboard shape', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(response({ items: null })))

    await expect(getTaskDashboard()).rejects.toThrow('The server returned an invalid task dashboard.')
  })

  it('sends task creation as JSON', async () => {
    const payload = { title: 'Prepare demo', notes: null }
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(response({ id: 'task-1', ...payload }, 201)))

    await expect(createTask(payload)).resolves.toMatchObject(payload)
    expect(fetch.mock.calls[0][1]).toMatchObject({
      method: 'POST',
      headers: { Accept: 'application/json', 'Content-Type': 'application/json' },
      body: JSON.stringify(payload),
    })
  })

  it('sends full task replacements with PUT', async () => {
    const task = {
      title: 'Prepare demo',
      notes: 'Updated notes',
      status: 'IN_PROGRESS',
    }
    const saved = { id: 'task/1', ...task }
    const fetchMock = vi.fn().mockResolvedValue(response(saved))
    vi.stubGlobal('fetch', fetchMock)

    await expect(updateTask('task/1', task)).resolves.toEqual(saved)
    expect(fetchMock).toHaveBeenCalledWith(
      expect.stringMatching(/\/api\/tasks\/task%2F1$/),
      expect.objectContaining({
        method: 'PUT',
        body: JSON.stringify(task),
      }),
    )
  })

  it('sends status-only changes with PATCH', async () => {
    const saved = { id: 'task-1', status: 'COMPLETED' }
    const fetchMock = vi.fn().mockResolvedValue(response(saved))
    vi.stubGlobal('fetch', fetchMock)

    await expect(updateTaskStatus('task-1', 'COMPLETED')).resolves.toEqual(saved)
    expect(fetchMock.mock.calls[0][0]).toMatch(/\/api\/tasks\/task-1\/status$/)
    expect(fetchMock.mock.calls[0][1]).toMatchObject({
      method: 'PATCH',
      body: JSON.stringify({ status: 'COMPLETED' }),
    })
  })

  it('uses problem detail when the API rejects a request', async () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(response({ detail: 'Title is required.' }, 400)))

    await expect(createTask({ title: '' })).rejects.toThrow('Title is required.')
  })

  it('reports malformed and empty successful responses explicitly', async () => {
    vi.stubGlobal('fetch', vi.fn()
      .mockResolvedValueOnce(response('not-json'))
      .mockResolvedValueOnce({ ok: true, status: 200, text: async () => '' }))

    await expect(getTaskDashboard()).rejects.toThrow('unreadable response')
    await expect(createTask({ title: 'Task' })).rejects.toThrow('empty response')
  })
})
