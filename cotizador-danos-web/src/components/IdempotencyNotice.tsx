import styles from './IdempotencyNotice.module.css'

interface IdempotencyNoticeProps {
  message: string
  variant: 'info' | 'success' | 'warning'
}

export function IdempotencyNotice({ message, variant }: IdempotencyNoticeProps) {
  return <p className={`${styles.notice} ${styles[variant]}`}>{message}</p>
}