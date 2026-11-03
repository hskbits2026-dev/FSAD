import { useState } from 'react'
import { api } from '../api/client'

export default function Feedback() {
  const [rating, setRating] = useState(0)
  const [hover, setHover] = useState(0)
  const [message, setMessage] = useState('')
  const [busy, setBusy] = useState(false)
  const [error, setError] = useState('')
  const [done, setDone] = useState(false)

  async function handleSubmit(e) {
    e.preventDefault()
    setError('')
    if (rating < 1) {
      setError('Please choose a rating from 1 to 5 stars.')
      return
    }
    setBusy(true)
    try {
      await api.post('/api/feedback', { message, rating })
      setDone(true)
      setMessage('')
      setRating(0)
    } catch (err) {
      setError(err.message)
    } finally {
      setBusy(false)
    }
  }

  return (
    <main className="wrap">
      <div className="auth-shell">
        <div className="auth-card">
          <h1>Tell us what you think</h1>
          <p className="muted">Your feedback helps us stock better things.</p>

          {done ? (
            <>
              <div className="alert alert-ok" style={{ marginTop: 20 }}>Thanks! Your feedback was received. 🌿</div>
              <button className="btn btn-ghost btn-block" style={{ marginTop: 16 }} onClick={() => setDone(false)}>
                Leave more feedback
              </button>
            </>
          ) : (
            <form onSubmit={handleSubmit}>
              <div className="field">
                <label>Rating</label>
                <div className="stars" onMouseLeave={() => setHover(0)}>
                  {[1, 2, 3, 4, 5].map((n) => (
                    <button type="button" key={n} className="star"
                      onClick={() => setRating(n)}
                      onMouseEnter={() => setHover(n)}
                      aria-label={`${n} star${n > 1 ? 's' : ''}`}>
                      {n <= (hover || rating) ? '★' : '☆'}
                    </button>
                  ))}
                </div>
              </div>

              <div className="field">
                <label htmlFor="message">Message</label>
                <textarea id="message" rows={5} value={message} maxLength={2000}
                  onChange={(e) => setMessage(e.target.value)}
                  placeholder="What did you like? What could be better?" required
                  style={{ width: '100%', border: '1px solid var(--line)', borderRadius: 'var(--radius-sm)', padding: '13px 15px', fontFamily: 'var(--font-body)', fontSize: 15, resize: 'vertical' }} />
              </div>

              {error && <div className="alert alert-error">{error}</div>}

              <button className="btn btn-terra btn-block" style={{ marginTop: 22 }} disabled={busy}>
                {busy ? 'Sending…' : 'Send feedback'}
              </button>
            </form>
          )}
        </div>
      </div>
    </main>
  )
}
