interface Props {
  tamanho?: number
  /** Versão para fundo escuro (livro branco). */
  claro?: boolean
}

/** Livro aberto com "pixels" subindo: o acervo físico virando digital. */
export function Logo({ tamanho = 36, claro = false }: Props) {
  const livro = claro ? '#FFFFFF' : '#111827'
  const vinco = claro ? '#111827' : '#FFFFFF'
  const pixel = claro ? '#7B93F5' : '#2346D8'
  return (
    <svg width={tamanho} height={tamanho} viewBox="0 0 64 64" aria-hidden="true">
      <path d="M32 52 C24 46 14 45 6 48 L6 27 C14 24 24 25 32 31 Z" fill={livro} />
      <path d="M32 52 C40 46 50 45 58 48 L58 27 C50 24 40 25 32 31 Z" fill={livro} />
      <path d="M32 31 L32 52" stroke={vinco} strokeWidth="2" />
      <rect x="29" y="17" width="6" height="6" fill={pixel} />
      <rect x="37" y="10" width="5" height="5" fill={pixel} />
      <rect x="24" y="8" width="4" height="4" fill={pixel} />
      <rect x="32" y="3" width="4" height="4" fill={pixel} />
      <rect x="42" y="2" width="3" height="3" fill={pixel} />
    </svg>
  )
}
