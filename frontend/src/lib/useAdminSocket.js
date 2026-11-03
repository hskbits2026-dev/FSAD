// A small React hook that opens ONE live WebSocket to the backend and listens
// for admin notifications (new orders, low stock, new feedback).
//
// Background (plain words):
//  - A normal HTTP request is "ask once, get one answer". A WebSocket is a
//    phone line that stays open so the server can push us messages any time.
//  - We speak "STOMP" over that line — a tiny messaging format where the server
//    publishes to named channels called "topics" and we "subscribe" to them.
//  - AUTH QUIRK (see backend StompAuthChannelInterceptor): the backend reads our
//    login token from the STOMP CONNECT frame's `Authorization` header, NOT from
//    the browser's normal handshake. So we pass it via `connectHeaders`.

import { useEffect, useRef, useState } from 'react'
import { Client } from '@stomp/stompjs'
import { getToken } from '../api/client'

// Build ws://host/ws (or wss:// on https). The page lives on :5173 and Vite
// proxies /ws through to the backend on :8080 (see vite.config.js), so we just
// reuse the current host — no hard-coded port.
function brokerUrl() {
  const scheme = window.location.protocol === 'https:' ? 'wss' : 'ws'
  return `${scheme}://${window.location.host}/ws`
}

/**
 * @param {object} handlers { onOrder, onLowStock, onFeedback } — called with the
 *        parsed JSON payload each time the matching topic receives a message.
 * @returns {{ status: 'connecting'|'connected'|'error'|'closed' }}
 */
export function useAdminSocket(handlers) {
  const [status, setStatus] = useState('connecting')
  // Keep the latest handlers in a ref so re-renders don't tear down the socket.
  const handlersRef = useRef(handlers)
  handlersRef.current = handlers

  useEffect(() => {
    const token = getToken()
    if (!token) {
      setStatus('error')
      return
    }

    const client = new Client({
      brokerURL: brokerUrl(),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 4000, // auto-retry if the line drops
      onConnect: () => {
        setStatus('connected')
        const sub = (topic, fn) =>
          client.subscribe(topic, (msg) => {
            try {
              fn?.(JSON.parse(msg.body))
            } catch {
              /* ignore a malformed frame rather than crash the feed */
            }
          })
        sub('/topic/admin/orders', (d) => handlersRef.current.onOrder?.(d))
        sub('/topic/admin/low-stock', (d) => handlersRef.current.onLowStock?.(d))
        sub('/topic/admin/feedback', (d) => handlersRef.current.onFeedback?.(d))
      },
      onStompError: () => setStatus('error'),
      onWebSocketError: () => setStatus('error'),
      onWebSocketClose: () => setStatus((s) => (s === 'connected' ? 'closed' : s)),
    })

    client.activate()
    return () => {
      client.deactivate() // close the line when leaving the page
    }
  }, [])

  return { status }
}
