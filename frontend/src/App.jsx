import { useEffect, useState } from 'react'

export default function App() {
  const [msg, setMsg] = useState('Đang tải...')

  useEffect(() => {
    fetch('/api/hello')
      .then((r) => r.json())
      .then((d) => setMsg(d.message))
      .catch(() => setMsg('Không gọi được API'))
  }, [])

  return (
    <main style={{ fontFamily: 'sans-serif', padding: 32 }}>
      <h1>EduNest: {msg}</h1>
    </main>
  )
}
