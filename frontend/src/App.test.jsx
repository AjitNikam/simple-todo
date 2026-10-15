// @vitest-environment jsdom
import { afterEach, describe, expect, it, vi } from 'vitest'
import { act, cleanup, render, screen, within } from '@testing-library/react'
import userEvent from '@testing-library/user-event'
import '@testing-library/jest-dom/vitest'
import App from './App.jsx'

function response(data, status = 200) {
  return {
    ok: status >= 200 && status < 300,
    status,
    text: async () => JSON.stringify(data),
  }
}

afterEach(() => {
  cleanup()
  vi.unstubAllGlobals()
})

describe('task dashboard', () => {
  it('distinguishes loading from a successfully loaded empty dashboard', async () => {
    let resolveDashboard
    vi.stubGlobal('fetch', vi.fn(() => new Promise((resolve) => { resolveDashboard = resolve })))
    render(<App />)

    expect(screen.getByText('Loading tasks…')).toBeInTheDocument()
    expect(screen.queryByText('No tasks yet. Add a task to get started.')).not.toBeInTheDocument()

    resolveDashboard(response({ items: [], total: 0, limit: 7 }))
    expect(await screen.findByText('No tasks yet. Add a task to get started.')).toBeInTheDocument()
  })

  it('shows a retrieval error and retries without treating failure as empty', async () => {
    const fetchMock = vi.fn()
      .mockRejectedValueOnce(new Error('API is unavailable'))
      .mockResolvedValueOnce(response({ items: [], total: 0, limit: 7 }))
    vi.stubGlobal('fetch', fetchMock)
    const user = userEvent.setup()
    render(<App />)

    expect(await screen.findByText(/Couldn’t load tasks: API is unavailable/)).toBeInTheDocument()
    expect(screen.queryByText('No tasks yet. Add a task to get started.')).not.toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Retry' }))

    expect(await screen.findByText('No tasks yet. Add a task to get started.')).toBeInTheDocument()
    expect(fetchMock).toHaveBeenCalledTimes(2)
  })

  it('refreshes on focus and ignores an older request that resolves afterward', async () => {
    let resolveInitialRequest
    const fresh = {
      id: 'fc5d9242-4a8f-4f34-a638-8e28fe159d90',
      title: 'Current server task',
      notes: null,
      status: 'TODO',
    }
    const stale = { ...fresh, id: '8d356c67-5737-4a47-a3a6-d66cbf11699a', title: 'Stale task' }
    const fetchMock = vi.fn()
      .mockImplementationOnce(() => new Promise((resolve) => { resolveInitialRequest = resolve }))
      .mockResolvedValueOnce(response({ items: [fresh], total: 1, limit: 7 }))
    vi.stubGlobal('fetch', fetchMock)
    render(<App />)

    await act(async () => {
      window.dispatchEvent(new Event('focus'))
    })
    expect(await screen.findByRole('heading', { name: 'Current server task' })).toBeInTheDocument()

    await act(async () => {
      resolveInitialRequest(response({ items: [stale], total: 1, limit: 7 }))
    })
    expect(screen.queryByRole('heading', { name: 'Stale task' })).not.toBeInTheDocument()
  })

  it('rejects a whitespace-only title before sending a create request', async () => {
    const fetchMock = vi.fn().mockResolvedValue(response({ items: [], total: 0, limit: 7 }))
    vi.stubGlobal('fetch', fetchMock)
    const user = userEvent.setup()
    render(<App />)
    await screen.findByText('No tasks yet. Add a task to get started.')

    await user.type(screen.getByLabelText('Title'), '   ')
    await user.click(screen.getByRole('button', { name: 'Add task' }))

    expect(screen.getByRole('alert')).toHaveTextContent('Title is required.')
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })

  it('retains entered values when create fails and prevents edits while submitting', async () => {
    let resolveCreate
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(response({ items: [], total: 0, limit: 7 }))
      .mockImplementationOnce(() => new Promise((resolve) => { resolveCreate = resolve }))
    vi.stubGlobal('fetch', fetchMock)
    const user = userEvent.setup()
    render(<App />)
    await screen.findByText('No tasks yet. Add a task to get started.')

    await user.type(screen.getByLabelText('Title'), 'Prepare demo')
    await user.type(screen.getByLabelText(/Notes/), 'Include the task view')
    await user.click(screen.getByLabelText('Title'))
    await user.keyboard('{Enter}')
    expect(screen.getByLabelText('Title')).toBeDisabled()
    expect(screen.getByLabelText(/Notes/)).toBeDisabled()
    await act(async () => {
      resolveCreate(response({ detail: 'The server could not save this task.' }, 500))
    })

    expect(await screen.findByRole('alert')).toHaveTextContent('The server could not save this task.')
    expect(screen.getByLabelText('Title')).toHaveValue('Prepare demo')
    expect(screen.getByLabelText(/Notes/)).toHaveValue('Include the task view')
    expect(screen.getByLabelText('Title')).toHaveAttribute('aria-invalid', 'false')
    expect(screen.queryByText('Task created.')).not.toBeInTheDocument()
  })

  it('creates a task, refetches server state, and renders it under TODO', async () => {
    const saved = {
      id: 'a48bd703-2086-4c31-b7e2-cdfc1844a67a',
      title: 'Prepare demo',
      notes: 'Include the task view',
      status: 'TODO',
      createdAt: '2026-10-13T10:00:00Z',
      updatedAt: '2026-10-13T10:00:00Z',
    }
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(response({ items: [], total: 0, limit: 7 }))
      .mockResolvedValueOnce(response(saved, 201))
      .mockResolvedValueOnce(response({ items: [saved], total: 1, limit: 7 }))
      .mockResolvedValueOnce(response({ items: [saved], total: 1, limit: 7 }))
    vi.stubGlobal('fetch', fetchMock)
    const user = userEvent.setup()
    const view = render(<App />)
    await screen.findByText('No tasks yet. Add a task to get started.')

    await user.type(screen.getByLabelText('Title'), 'Prepare demo')
    await user.type(screen.getByLabelText(/Notes/), 'Include the task view')
    await user.click(screen.getByRole('button', { name: 'Add task' }))

    expect(await screen.findByRole('heading', { name: 'Prepare demo' })).toBeInTheDocument()
    expect(screen.getByText('Include the task view')).toBeInTheDocument()
    expect(screen.getByText('Task created.')).toBeInTheDocument()
    expect(screen.getByText('TODO', { selector: '.status-label' })).toBeInTheDocument()
    expect(fetchMock).toHaveBeenCalledTimes(3)
    expect(fetchMock.mock.calls[1][1].method).toBe('POST')
    expect(fetchMock.mock.calls[1][1].body).toBe(JSON.stringify({
      title: 'Prepare demo',
      notes: 'Include the task view',
    }))

    view.unmount()
    render(<App />)
    expect(await screen.findByRole('heading', { name: 'Prepare demo' })).toBeInTheDocument()
    expect(fetchMock).toHaveBeenCalledTimes(4)
  })

  it('replaces title and notes, then refreshes the dashboard', async () => {
    const original = {
      id: 'task-1',
      title: 'Prepare demo',
      notes: 'Draft',
      status: 'TODO',
    }
    const updated = { ...original, title: 'Present demo', notes: 'Final slides' }
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(response({ items: [original], total: 1, limit: 7 }))
      .mockResolvedValueOnce(response(updated))
      .mockResolvedValueOnce(response({ items: [updated], total: 1, limit: 7 }))
    vi.stubGlobal('fetch', fetchMock)
    const user = userEvent.setup()
    render(<App />)
    await screen.findByRole('heading', { name: 'Prepare demo' })

    await user.click(screen.getByRole('button', { name: 'Edit task' }))
    const titleInput = screen.getAllByLabelText('Title', { selector: 'input' })[1]
    const notesInput = screen.getAllByLabelText(/Notes/)[1]
    await user.clear(titleInput)
    await user.type(titleInput, 'Present demo')
    await user.clear(notesInput)
    await user.type(notesInput, 'Final slides')
    await user.click(screen.getByRole('button', { name: 'Save changes' }))

    expect(await screen.findByRole('heading', { name: 'Present demo' })).toBeInTheDocument()
    expect(screen.getByText('Final slides')).toBeInTheDocument()
    expect(fetchMock.mock.calls[1][1]).toMatchObject({
      method: 'PUT',
      body: JSON.stringify({
        title: 'Present demo',
        notes: 'Final slides',
        status: 'TODO',
      }),
    })
  })

  it('keeps failed edit values in the editor so they can be retried', async () => {
    const task = { id: 'task-1', title: 'Prepare demo', notes: 'Draft', status: 'TODO' }
    const updated = { ...task, title: 'Present demo', notes: 'Final slides' }
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(response({ items: [task], total: 1, limit: 7 }))
      .mockResolvedValueOnce(response({ detail: 'Task is currently unavailable.' }, 409))
      .mockResolvedValueOnce(response(updated))
      .mockResolvedValueOnce(response({ items: [updated], total: 1, limit: 7 }))
    vi.stubGlobal('fetch', fetchMock)
    const user = userEvent.setup()
    render(<App />)
    await screen.findByRole('heading', { name: 'Prepare demo' })

    await user.click(screen.getByRole('button', { name: 'Edit task' }))
    const titleInput = screen.getAllByLabelText('Title', { selector: 'input' })[1]
    const notesInput = screen.getAllByLabelText(/Notes/)[1]
    await user.clear(titleInput)
    await user.type(titleInput, 'Present demo')
    await user.clear(notesInput)
    await user.type(notesInput, 'Final slides')
    await user.click(screen.getByRole('button', { name: 'Save changes' }))

    expect(await screen.findByRole('alert')).toHaveTextContent('Task is currently unavailable.')
    expect(titleInput).toHaveValue('Present demo')
    expect(notesInput).toHaveValue('Final slides')
    await user.click(screen.getByRole('button', { name: 'Save changes' }))

    expect(await screen.findByRole('heading', { name: 'Present demo' })).toBeInTheDocument()
    expect(fetchMock.mock.calls[2][1].method).toBe('PUT')
  })

  it('rejects a blank edit title without sending a mutation', async () => {
    const task = { id: 'task-1', title: 'Prepare demo', notes: null, status: 'TODO' }
    const fetchMock = vi.fn().mockResolvedValueOnce(response({ items: [task], total: 1, limit: 7 }))
    vi.stubGlobal('fetch', fetchMock)
    const user = userEvent.setup()
    render(<App />)
    await screen.findByRole('heading', { name: 'Prepare demo' })

    await user.click(screen.getByRole('button', { name: 'Edit task' }))
    const titleInput = screen.getAllByLabelText('Title', { selector: 'input' })[1]
    await user.clear(titleInput)
    await user.type(titleInput, '   ')
    await user.click(screen.getByRole('button', { name: 'Save changes' }))

    expect(screen.getByRole('alert')).toHaveTextContent('Title is required.')
    expect(titleInput).toHaveAttribute('aria-invalid', 'true')
    expect(fetchMock).toHaveBeenCalledTimes(1)
  })

  it('changes a task status through PATCH and shows the refreshed group', async () => {
    const task = { id: 'task-1', title: 'Prepare demo', notes: null, status: 'TODO' }
    const updated = { ...task, status: 'COMPLETED' }
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(response({ items: [task], total: 1, limit: 7 }))
      .mockResolvedValueOnce(response(updated))
      .mockResolvedValueOnce(response({ items: [updated], total: 1, limit: 7 }))
    vi.stubGlobal('fetch', fetchMock)
    const user = userEvent.setup()
    render(<App />)
    await screen.findByRole('heading', { name: 'Prepare demo' })

    await user.selectOptions(screen.getByLabelText('Change status'), 'COMPLETED')

    expect(await screen.findByText('Completed', { selector: '.status-label' })).toBeInTheDocument()
    const completedGroup = screen.getByRole('heading', { name: /Completed/ }).closest('.task-group')
    const todoGroup = screen.getByRole('heading', { name: /TODO/ }).closest('.task-group')
    expect(within(completedGroup).getByRole('heading', { name: 'Prepare demo' })).toBeInTheDocument()
    expect(within(todoGroup).queryByRole('heading', { name: 'Prepare demo' })).not.toBeInTheDocument()
    expect(fetchMock.mock.calls[1][1]).toMatchObject({
      method: 'PATCH',
      body: JSON.stringify({ status: 'COMPLETED' }),
    })
  })

  it('reopens a completed task as TODO', async () => {
    const task = { id: 'task-1', title: 'Prepare demo', notes: null, status: 'COMPLETED' }
    const updated = { ...task, status: 'TODO' }
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(response({ items: [task], total: 1, limit: 7 }))
      .mockResolvedValueOnce(response(updated))
      .mockResolvedValueOnce(response({ items: [updated], total: 1, limit: 7 }))
    vi.stubGlobal('fetch', fetchMock)
    const user = userEvent.setup()
    render(<App />)
    await screen.findByRole('heading', { name: 'Prepare demo' })

    await user.selectOptions(screen.getByLabelText('Change status'), 'TODO')

    expect(await screen.findByText('TODO', { selector: '.status-label' })).toBeInTheDocument()
    const todoGroup = screen.getByRole('heading', { name: /TODO/ }).closest('.task-group')
    const completedGroup = screen.getByRole('heading', { name: /Completed/ }).closest('.task-group')
    expect(within(todoGroup).getByRole('heading', { name: 'Prepare demo' })).toBeInTheDocument()
    expect(within(completedGroup).queryByRole('heading', { name: 'Prepare demo' })).not.toBeInTheDocument()
    expect(fetchMock.mock.calls[1][1]).toMatchObject({
      method: 'PATCH',
      body: JSON.stringify({ status: 'TODO' }),
    })
  })

  it('keeps an unsuccessful status choice available to retry without marking it saved', async () => {
    const task = { id: 'task-1', title: 'Prepare demo', notes: null, status: 'TODO' }
    const updated = { ...task, status: 'IN_PROGRESS' }
    const fetchMock = vi.fn()
      .mockResolvedValueOnce(response({ items: [task], total: 1, limit: 7 }))
      .mockResolvedValueOnce(response({ detail: 'Task is currently unavailable.' }, 409))
      .mockResolvedValueOnce(response(updated))
      .mockResolvedValueOnce(response({ items: [updated], total: 1, limit: 7 }))
    vi.stubGlobal('fetch', fetchMock)
    const user = userEvent.setup()
    render(<App />)
    await screen.findByRole('heading', { name: 'Prepare demo' })

    await user.selectOptions(screen.getByLabelText('Change status'), 'IN_PROGRESS')

    expect(await screen.findByRole('alert')).toHaveTextContent('Task is currently unavailable.')
    expect(screen.getByLabelText('Change status')).toHaveValue('IN_PROGRESS')
    expect(screen.getByText('TODO', { selector: '.status-label' })).toBeInTheDocument()
    await user.click(screen.getByRole('button', { name: 'Retry status change' }))

    expect(await screen.findByText('In progress', { selector: '.status-label' })).toBeInTheDocument()
    expect(fetchMock.mock.calls[2][1].method).toBe('PATCH')
  })

  it('shows the diary placeholder without presenting a saved empty entry', () => {
    vi.stubGlobal('fetch', vi.fn().mockResolvedValue(response({ items: [], total: 0, limit: 7 })))
    render(<App />)
    expect(screen.getByRole('heading', { name: 'Diary' })).toBeInTheDocument()
    expect(screen.getByText('Diary is not available yet.')).toBeInTheDocument()
  })
})
