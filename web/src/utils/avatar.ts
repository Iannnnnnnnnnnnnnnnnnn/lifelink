export function getAvatarInitial(username?: string | null) {
  const normalized = username?.trim();
  if (!normalized) {
    return 'L';
  }
  return Array.from(normalized)[0].toUpperCase();
}
