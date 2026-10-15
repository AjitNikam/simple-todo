import { useCallback, useEffect, useRef, useState } from 'react'
import { createTask, getTaskDashboard, updateTask, updateTaskStatus } from './tasksApi.js'
import './App.css'

const statuses = [
  ['TODO', 'TODO'],
  ['IN_PROGRESS', 'In progress'],
  ['COMPLETED', 'Completed'],
]

function TaskCard({ task, onUpdate, onUpdateStatus }) {
  const [editing, setEditing] = useState(false)
  const [title, setTitle] = useState(task.title)
  const [notes, setNotes] = useState(task.notes || '')
  const [editError, setEditError] = useState('')
  const [titleInvalid, setTitleInvalid] = useState(false)
  const [editPending, setEditPending] = useState(false)
  const [selectedStatus, setSelectedStatus] = useState(task.status)
  const [statusError, setStatusError] = useState('')
  const [statusPending, setStatusPending] = useState(false)

  async function saveDetails(event) {
    event.preventDefault()
    setEditError('')
    setTitleInvalid(false)
    if (!title.trim()) {
      setEditError('Title is required.')
      setTitleInvalid(true)
      return
    }
    setEditPending(true)
    try {
      await onUpdate(task.id, {
        title,
        notes: notes === '' ? null : notes,
        status: task.status,
      })
      setEditing(false)
    } catch (error) {
      setEditError(error.message)
    } finally {
      setEditPending(false)
    }
  }

  async function saveStatus(status = selectedStatus) {
    setStatusError('')
    setStatusPending(true)
    try {
      const updated = await onUpdateStatus(task.id, status)
      setSelectedStatus(updated.status)
    } catch (error) {
      setStatusError(error.message)
    } finally {
      setStatusPending(false)
    }
  }

  function beginEditing() {
    setTitle(task.title)
    setNotes(task.notes || '')
    setEditError('')
    setTitleInvalid(false)
    setEditing(true)
  }

  function cancelEditing() {
    setTitle(task.title)
    setNotes(task.notes || '')
    setEditError('')
    setTitleInvalid(false)
    setEditing(false)
  }

  const statusLabel = statuses.find(([value]) => value === task.status)?.[1] || task.status

  return (
    <li className="task-card">
      <div className="task-card-content">
        {editing ? (
          <form className="task-edit-form" onSubmit={saveDetails} noValidate>
            <label htmlFor={`task-title-${task.id}`}>Title</label>
            <input
              id={`task-title-${task.id}`}
              value={title}
              onChange={(event) => {
                setTitle(event.target.value)
                setEditError('')
                setTitleInvalid(false)
              }}
              aria-invalid={titleInvalid}
              aria-describedby={editError ? `task-edit-feedback-${task.id}` : undefined}
              disabled={editPending}
              required
            />
            <label htmlFor={`task-notes-${task.id}`}>
              Notes <span className="optional">(optional)</span>
            </label>
            <textarea
              id={`task-notes-${task.id}`}
              rows="3"
              value={notes}
              onChange={(event) => setNotes(event.target.value)}
              disabled={editPending}
            />
            <div className="form-actions">
              <button className="primary-button" disabled={editPending} type="submit">
                {editPending ? 'Saving…' : 'Save changes'}
              </button>
              <button
                className="secondary-button"
                disabled={editPending}
                onClick={cancelEditing}
                type="button"
              >
                Cancel
              </button>
            </div>
            {editError && (
              <p id={`task-edit-feedback-${task.id}`} className="feedback error" role="alert">
                {editError}
              </p>
            )}
          </form>
        ) : (
          <>
            <h4>{task.title}</h4>
            {task.notes && <p>{task.notes}</p>}
            <button
              className="secondary-button task-edit-button"
              disabled={statusPending}
              onClick={beginEditing}
              type="button"
            >
              Edit task
            </button>
          </>
        )}
      </div>
      <div className="task-card-controls">
        <span className={`status-label status-${task.status.toLowerCase()}`}>{statusLabel}</span>
        <label htmlFor={`task-status-${task.id}`}>Change status</label>
        <select
          id={`task-status-${task.id}`}
          value={selectedStatus}
          onChange={(event) => {
            setSelectedStatus(event.target.value)
            setStatusError('')
            void saveStatus(event.target.value)
          }}
          disabled={editing || editPending || statusPending}
        >
          {statuses.map(([value, label]) => <option key={value} value={value}>{label}</option>)}
        </select>
        {statusError && (
          <div className="task-status-error" role="alert">
            <p className="feedback error">Status could not be saved: {statusError}</p>
            <button
              className="secondary-button"
              disabled={statusPending}
              onClick={() => void saveStatus()}
              type="button"
            >
              Retry status change
            </button>
          </div>
        )}
      </div>
    </li>
  )
}

function App() {
  const [tasks, setTasks] = useState([])
  const [loading, setLoading] = useState(true)
  const [loadError, setLoadError] = useState('')
  const [title, setTitle] = useState('')
  const [notes, setNotes] = useState('')
  const [submitting, setSubmitting] = useState(false)
  const [formError, setFormError] = useState('')
  const [titleInvalid, setTitleInvalid] = useState(false)
  const [feedback, setFeedback] = useState('')
  const latestRequestId = useRef(0)

  const loadTasks = useCallback(async () => {
    const requestId = ++latestRequestId.current
    setLoading(true)
    setLoadError('')
    try {
      const dashboard = await getTaskDashboard()
      if (requestId !== latestRequestId.current) return
      setTasks(dashboard.items)
    } catch (error) {
      if (requestId === latestRequestId.current) setLoadError(error.message)
    } finally {
      if (requestId === latestRequestId.current) setLoading(false)
    }
  }, [])

  useEffect(() => {
    const requestId = ++latestRequestId.current
    getTaskDashboard()
      .then((dashboard) => {
        if (requestId === latestRequestId.current) {
          setTasks(dashboard.items)
          setLoadError('')
        }
      })
      .catch((error) => {
        if (requestId === latestRequestId.current) setLoadError(error.message)
      })
      .finally(() => {
        if (requestId === latestRequestId.current) setLoading(false)
      })
    return () => {
      if (requestId === latestRequestId.current) latestRequestId.current += 1
    }
  }, [])

  useEffect(() => {
    const refreshWhenFocused = () => void loadTasks()
    window.addEventListener('focus', refreshWhenFocused)
    return () => window.removeEventListener('focus', refreshWhenFocused)
  }, [loadTasks])

  async function handleSubmit(event) {
    event.preventDefault()
    setFormError('')
    setFeedback('')
    setTitleInvalid(false)
    if (!title.trim()) {
      setFormError('Title is required.')
      setTitleInvalid(true)
      return
    }

    setSubmitting(true)
    try {
      await createTask({ title, notes: notes === '' ? null : notes })
      setTitle('')
      setNotes('')
      setFeedback('Task created.')
      await loadTasks()
    } catch (error) {
      setFormError(error.message)
    } finally {
      setSubmitting(false)
    }
  }

  async function handleUpdateTask(taskId, task) {
    const updated = await updateTask(taskId, task)
    await loadTasks()
    return updated
  }

  async function handleUpdateStatus(taskId, status) {
    const updated = await updateTaskStatus(taskId, status)
    await loadTasks()
    return updated
  }

  return (
    <main className="page">
      <header className="page-header">
        <p className="eyebrow">Personal workspace</p>
        <h1>Simple ToDo</h1>
      </header>

      <section aria-labelledby="tasks-heading" className="section">
        <div className="section-heading">
          <div>
            <p className="eyebrow">Plan and track</p>
            <h2 id="tasks-heading">Tasks</h2>
          </div>
          <button className="secondary-button" onClick={() => void loadTasks()} type="button">
            Refresh tasks
          </button>
        </div>

        <form className="task-form" onSubmit={handleSubmit} noValidate>
          <h3>Add task</h3>
          <label htmlFor="task-title">Title</label>
          <input
            id="task-title"
            name="title"
            value={title}
            onChange={(event) => {
              setTitle(event.target.value)
              if (formError) setFormError('')
              setTitleInvalid(false)
            }}
            aria-invalid={titleInvalid}
            aria-describedby={titleInvalid ? 'task-form-feedback' : undefined}
            disabled={submitting}
            required
          />
          <label htmlFor="task-notes">Notes <span className="optional">(optional)</span></label>
          <textarea
            id="task-notes"
            name="notes"
            rows="3"
            value={notes}
            onChange={(event) => setNotes(event.target.value)}
            disabled={submitting}
          />
          <div className="form-actions">
            <button className="primary-button" disabled={submitting} type="submit">
              {submitting ? 'Adding…' : 'Add task'}
            </button>
            <p
              id="task-form-feedback"
              className={formError ? 'feedback error' : 'feedback success'}
              role={formError ? 'alert' : 'status'}
            >
              {formError || feedback}
            </p>
          </div>
        </form>

        {loading ? (
          <p className="state-message" role="status">Loading tasks…</p>
        ) : loadError ? (
          <div className="state-message error-panel" role="alert">
            <p>Couldn’t load tasks: {loadError}</p>
            <button className="secondary-button" onClick={() => void loadTasks()} type="button">
              Retry
            </button>
          </div>
        ) : tasks.length === 0 ? (
          <p className="state-message" role="status">No tasks yet. Add a task to get started.</p>
        ) : (
          <div className="task-groups">
            {statuses.map(([value, label]) => {
              const group = tasks.filter((task) => task.status === value)
              return (
                <section className="task-group" key={value} aria-labelledby={`status-${value}`}>
                  <h3 id={`status-${value}`}>{label} <span className="count">{group.length}</span></h3>
                  {group.length ? (
                    <ul className="task-list">
                      {group.map((task) => (
                        <TaskCard
                          key={task.id}
                          task={task}
                          onUpdate={handleUpdateTask}
                          onUpdateStatus={handleUpdateStatus}
                        />
                      ))}
                    </ul>
                  ) : (
                    <p className="group-empty">No tasks</p>
                  )}
                </section>
              )
            })}
          </div>
        )}
      </section>

      <section aria-labelledby="diary-heading" className="section diary-section">
        <div>
          <p className="eyebrow">Daily notes</p>
          <h2 id="diary-heading">Diary</h2>
        </div>
        <p className="state-message">Diary is not available yet.</p>
      </section>
    </main>
  )
}

export default App
