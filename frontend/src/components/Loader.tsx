export function Loader({ text = "Loading..." }: { text?: string }) {
  return <p className="muted">{text}</p>;
}
