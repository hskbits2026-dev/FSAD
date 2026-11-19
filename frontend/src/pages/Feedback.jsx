import { api } from '../api/client'
import FeedbackForm from '../components/FeedbackForm'

export default function Feedback() {
  // General site feedback (not tied to a product). Reuses the shared FeedbackForm.
  function submit({ rating, message }) {
    return api.post('/api/feedback', { message, rating })
  }

  return (
    <main className="wrap">
      <div className="auth-shell">
        <div className="auth-card">
          <h1>Tell us what you think</h1>
          <p className="muted">Your feedback helps us stock better things.</p>
          <FeedbackForm onSubmit={submit} />
        </div>
      </div>
    </main>
  )
}
