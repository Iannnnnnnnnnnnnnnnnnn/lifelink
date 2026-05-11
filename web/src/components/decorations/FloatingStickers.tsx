const stickers = ['✨', '💌', '🌙', '☁️', '🌷', '📝', '📅', '💕'];

export function FloatingStickers() {
  return (
    <div className="floating-stickers" aria-hidden="true">
      {stickers.map((sticker, index) => (
        <span key={`${sticker}-${index}`} className={`floating-sticker sticker-${index + 1}`}>
          {sticker}
        </span>
      ))}
    </div>
  );
}
